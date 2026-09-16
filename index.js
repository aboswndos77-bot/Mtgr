const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();
const FieldValue = admin.firestore.FieldValue;

const requireAuth = (request) => {
  if (!request.auth || !request.auth.uid) throw new HttpsError('unauthenticated', 'يجب تسجيل الدخول أولاً.');
  return request.auth.uid;
};
const num = (v) => Number(v || 0);
const now = () => Date.now();

async function roleOf(uid) {
  const d = await db.doc(`users/${uid}`).get();
  if (!d.exists) throw new HttpsError('not-found', 'ملف المستخدم غير موجود.');
  return d.get('role');
}

exports.purchaseProduct = onCall(async (request) => {
  const uid = requireAuth(request);
  const productId = String(request.data?.productId || '').trim();
  const promoCode = String(request.data?.promoCode || '').trim();
  if (!productId) throw new HttpsError('invalid-argument', 'معرف المنتج مطلوب.');

  const consumerRef = db.doc(`users/${uid}`);
  const productRef = db.doc(`products/${productId}`);
  const walletRef = db.doc(`wallets/wallet_${uid}`);
  const [consumerSnap, productSnap, walletSnap] = await Promise.all([consumerRef.get(), productRef.get(), walletRef.get()]);
  if (!consumerSnap.exists) throw new HttpsError('not-found', 'المستخدم غير موجود.');
  if (consumerSnap.get('role') !== 'CONSUMER') throw new HttpsError('permission-denied', 'هذا الحساب ليس مستهلكاً.');
  if (consumerSnap.get('isFrozen')) throw new HttpsError('failed-precondition', 'الحساب مجمد ولا يمكنه الشراء.');
  if (!consumerSnap.get('isEmailVerified')) throw new HttpsError('failed-precondition', 'يجب تأكيد البريد الإلكتروني قبل الشراء.');
  if (!productSnap.exists) throw new HttpsError('not-found', 'المنتج غير موجود.');
  if (!walletSnap.exists) throw new HttpsError('not-found', 'محفظة المستهلك غير موجودة.');

  let promoterId = null;
  const code = promoCode || String(consumerSnap.get('referredByPromoCode') || '').trim();
  if (code) {
    const q = await db.collection('users').where('promoCode', '==', code).where('role', '==', 'PROMOTER').limit(1).get();
    if (!q.empty && q.docs[0].id !== uid && q.docs[0].id !== productSnap.get('merchantId')) promoterId = q.docs[0].id;
  }

  const price = num(productSnap.get('price'));
  const commission = promoterId ? num(productSnap.get('promoterCommission')) : 0;
  const platformFee = Math.floor(price * 3 / 100);
  const merchantNet = price - platformFee - commission;
  if (price <= 0 || commission < 0 || merchantNet < 0) throw new HttpsError('failed-precondition', 'بيانات السعر أو العمولات غير صالحة.');

  const orderId = `ord_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;
  const escrowTxId = `tx_escrow_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;
  const merchantId = productSnap.get('merchantId');
  const merchantSnap = await db.doc(`users/${merchantId}`).get();
  const merchantWalletRef = db.doc(`wallets/wallet_${merchantId}`);
  const platformWalletRef = db.doc('wallets/WALLET_PLATFORM');
  const orderRef = db.doc(`orders/${orderId}`);
  const txRef = db.doc(`wallet_transactions/${escrowTxId}`);

  await db.runTransaction(async (tx) => {
    const [freshProduct, freshWallet, freshMerchantWallet] = await Promise.all([
      tx.get(productRef), tx.get(walletRef), tx.get(merchantWalletRef)
    ]);
    if (!freshProduct.exists || freshProduct.get('status') !== 'PUBLISHED' || num(freshProduct.get('quantity')) <= 0)
      throw new HttpsError('failed-precondition', 'المنتج لم يعد متاحاً.');
    if (!freshWallet.exists || num(freshWallet.get('availableBalance')) < price)
      throw new HttpsError('failed-precondition', 'الرصيد المتاح غير كافٍ.');
    if (!freshMerchantWallet.exists) throw new HttpsError('failed-precondition', 'محفظة التاجر غير موجودة.');

    const qty = num(freshProduct.get('quantity')) - 1;
    tx.update(walletRef, { availableBalance: num(freshWallet.get('availableBalance')) - price, heldBalance: num(freshWallet.get('heldBalance')) + price, updatedAt: now() });
    tx.update(productRef, { quantity: qty, status: qty <= 0 ? 'SOLD' : 'PUBLISHED', updatedAt: now() });
    tx.set(orderRef, {
      orderId, consumerId: uid, consumerName: consumerSnap.get('fullName') || '',
      merchantId, merchantName: productSnap.get('merchantName') || merchantSnap.get('fullName') || '',
      merchantJeebAccount: merchantSnap.get('jeebAccountNumber') || '', productId,
      productName: productSnap.get('name') || '', productImage: productSnap.get('imageUrl') || '', quantity: 1,
      productPrice: price, promoterCommission: commission, platformFee, merchantNet, promoterId,
      status: 'RESERVED', escrowHoldTransactionId: escrowTxId, createdAt: now(), updatedAt: now()
    });
    tx.set(txRef, { transactionId: escrowTxId, type: 'ESCROW_HOLD', fromWalletId: walletRef.id, toWalletId: null, amount: price, currency: 'YER', orderId, userId: uid, status: 'COMPLETED', createdAt: now(), createdBy: uid, note: `حجز مبلغ الضمان للطلب ${orderId}` });
    tx.set(db.doc(`notifications/notif_${crypto.randomUUID()}`), { notificationId: `notif_${crypto.randomUUID()}`, userId: merchantId, title: 'لديك طلب جديد', message: `تم حجز مبلغ ${price} ريال في الضمان.`, isRead: false, type: 'ORDER', createdAt: now() });
  });
  return { orderId, message: 'تم إنشاء الطلب وحجز الضمان المالي بنجاح.' };
});

exports.releaseOrder = onCall(async (request) => {
  const uid = requireAuth(request);
  const orderId = String(request.data?.orderId || '').trim();
  const transferNumber = String(request.data?.transferNumber || '').trim();
  const receiptImage = String(request.data?.receiptImage || '');
  if (!orderId || !transferNumber) throw new HttpsError('invalid-argument', 'رقم عملية التحويل مطلوب.');

  const orderRef = db.doc(`orders/${orderId}`);
  const snap = await orderRef.get();
  if (!snap.exists) throw new HttpsError('not-found', 'الطلب غير موجود.');
  const order = snap.data();
  if (order.consumerId !== uid) throw new HttpsError('permission-denied', 'غير مصرح لك بتحرير هذا الطلب.');
  if (order.status !== 'DELIVERED_PENDING_RELEASE') throw new HttpsError('failed-precondition', 'الطلب ليس جاهزاً للتحرير.');

  const consumerWalletRef = db.doc(`wallets/wallet_${uid}`);
  const merchantWalletRef = db.doc(`wallets/wallet_${order.merchantId}`);
  const platformWalletRef = db.doc('wallets/WALLET_PLATFORM');
  const promoterWalletRef = order.promoterId ? db.doc(`wallets/wallet_${order.promoterId}`) : null;
  const settlementId = `tx_settle_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;
  const feeId = `tx_fee_${crypto.randomUUID().replaceAll('-', '').slice(0, 12)}`;

  await db.runTransaction(async (tx) => {
    const refs = [consumerWalletRef, merchantWalletRef, platformWalletRef].concat(promoterWalletRef ? [promoterWalletRef] : []);
    const snaps = await Promise.all(refs.map(r => tx.get(r)));
    const cw = snaps[0], mw = snaps[1], pw = snaps[2], promw = promoterWalletRef ? snaps[3] : null;
    if (!cw.exists || num(cw.get('heldBalance')) < num(order.productPrice)) throw new HttpsError('failed-precondition', 'الرصيد المحجوز غير كافٍ.');
    if (!mw.exists) throw new HttpsError('failed-precondition', 'محفظة التاجر غير موجودة.');
    if (!pw.exists) tx.set(platformWalletRef, {walletId:'WALLET_PLATFORM', userId:'SYSTEM_PLATFORM', availableBalance:0, heldBalance:0, listingBalance:0, currency:'YER', status:'ACTIVE', createdAt:now(), updatedAt:now()});
    if (order.promoterId && !promw?.exists) throw new HttpsError('failed-precondition', 'محفظة المروج غير موجودة.');

    tx.update(consumerWalletRef, { heldBalance: num(cw.get('heldBalance')) - num(order.productPrice), updatedAt: now() });
    tx.update(merchantWalletRef, { availableBalance: num(mw.get('availableBalance')) + num(order.merchantNet), updatedAt: now() });
    tx.update(platformWalletRef, { availableBalance: num(pw.exists ? pw.get('availableBalance') : 0) + num(order.platformFee), updatedAt: now() });
    if (promoterWalletRef) tx.update(promoterWalletRef, { availableBalance: num(promw.get('availableBalance')) + num(order.promoterCommission), updatedAt: now() });

    tx.set(db.doc(`wallet_transactions/${settlementId}`), { transactionId: settlementId, type: 'ESCROW_RELEASE', fromWalletId: consumerWalletRef.id, toWalletId: merchantWalletRef.id, amount: num(order.merchantNet), currency: 'YER', orderId, userId: order.merchantId, status: 'COMPLETED', createdAt: now(), createdBy: uid, note: 'تحرير صافي التاجر من الضمان' });
    tx.set(db.doc(`wallet_transactions/${feeId}`), { transactionId: feeId, type: 'PLATFORM_FEE', fromWalletId: consumerWalletRef.id, toWalletId: platformWalletRef.id, amount: num(order.platformFee), currency: 'YER', orderId, userId: 'SYSTEM_PLATFORM', status: 'COMPLETED', createdAt: now(), createdBy: uid, note: 'رسوم المنصة 3%' });
    if (promoterWalletRef) tx.set(db.doc(`wallet_transactions/tx_prom_${settlementId}`), { transactionId: `tx_prom_${settlementId}`, type: 'PROMOTER_COMMISSION', fromWalletId: platformWalletRef.id, toWalletId: promoterWalletRef.id, amount: num(order.promoterCommission), currency: 'YER', orderId, userId: order.promoterId, status: 'COMPLETED', createdAt: now(), createdBy: uid, note: 'عمولة المروج' });
    tx.update(orderRef, { status: 'COMPLETED', consumerTransferNumber: transferNumber, consumerReceiptImage: receiptImage, settlementTransactionId: settlementId, updatedAt: now() });
  });
  return { orderId, message: 'تم تحرير الضمان وتسوية الحصص بشكل ذري وآمن.' };
});

exports.resolveDispute = onCall(async (request) => {
  const uid = requireAuth(request);
  if (await roleOf(uid) !== 'ADMIN') throw new HttpsError('permission-denied', 'هذه العملية للإدارة فقط.');
  const disputeId = String(request.data?.disputeId || '').trim();
  const suppliedOrderId = String(request.data?.orderId || '').trim();
  const refund = !!request.data?.refundToConsumer;
  const note = String(request.data?.resolutionNote || '').trim();
  let orderId = suppliedOrderId;
  let disputeRef = null;
  if (disputeId) {
    disputeRef = db.doc(`disputes/${disputeId}`);
    const disputeSnap = await disputeRef.get();
    if (!disputeSnap.exists) throw new HttpsError('not-found','النزاع غير موجود.');
    orderId = String(disputeSnap.get('orderId') || '').trim();
  }
  if (!orderId) throw new HttpsError('invalid-argument','معرف الطلب أو النزاع مطلوب.');
  const orderRef = db.doc(`orders/${orderId}`);
  const orderSnap = await orderRef.get();
  if (!orderSnap.exists) throw new HttpsError('not-found', 'الطلب غير موجود.');
  const order = orderSnap.data();
  if (!['DISPUTED','DELIVERED_PENDING_RELEASE'].includes(order.status)) throw new HttpsError('failed-precondition', 'حالة الطلب لا تسمح بتسوية النزاع.');

  const consumerWalletRef = db.doc(`wallets/wallet_${order.consumerId}`);
  const merchantWalletRef = db.doc(`wallets/wallet_${order.merchantId}`);
  const platformWalletRef = db.doc('wallets/WALLET_PLATFORM');
  const promoterWalletRef = order.promoterId ? db.doc(`wallets/wallet_${order.promoterId}`) : null;
  await db.runTransaction(async tx => {
    const refs = [consumerWalletRef, merchantWalletRef, platformWalletRef].concat(promoterWalletRef ? [promoterWalletRef] : []);
    const ss = await Promise.all(refs.map(r => tx.get(r)));
    const cw=ss[0], mw=ss[1], pw=ss[2], promw=promoterWalletRef?ss[3]:null;
    if (!cw.exists || num(cw.get('heldBalance')) < num(order.productPrice)) throw new HttpsError('failed-precondition','الرصيد المحجوز غير كافٍ.');
    if (refund) {
      tx.update(consumerWalletRef,{heldBalance:num(cw.get('heldBalance'))-num(order.productPrice),availableBalance:num(cw.get('availableBalance'))+num(order.productPrice),updatedAt:now()});
      tx.set(db.doc(`wallet_transactions/tx_ref_${crypto.randomUUID()}`),{transactionId:`tx_ref_${crypto.randomUUID()}`,type:'REFUND',fromWalletId:null,toWalletId:consumerWalletRef.id,amount:num(order.productPrice),currency:'YER',orderId,userId:order.consumerId,status:'COMPLETED',createdAt:now(),createdBy:uid,note:'استرداد بناء على قرار الإدارة'});
      tx.update(orderRef,{status:'REFUNDED',updatedAt:now()});
    } else {
      tx.update(consumerWalletRef,{heldBalance:num(cw.get('heldBalance'))-num(order.productPrice),updatedAt:now()});
      tx.update(merchantWalletRef,{availableBalance:num(mw.get('availableBalance'))+num(order.merchantNet),updatedAt:now()});
      tx.update(platformWalletRef,{availableBalance:num(pw.get('availableBalance'))+num(order.platformFee),updatedAt:now()});
      if(order.promoterId && promw?.exists) tx.update(promoterWalletRef,{availableBalance:num(promw.get('availableBalance'))+num(order.promoterCommission),updatedAt:now()});
      tx.update(orderRef,{status:'COMPLETED',updatedAt:now()});
    }
    if (disputeRef) tx.update(disputeRef,{status:'RESOLVED',resolutionNote:note,resolvedAt:now(),resolvedBy:uid});
    tx.set(db.doc(`audit_logs/log_${crypto.randomUUID()}`),{logId:`log_${crypto.randomUUID()}`,actorId:uid,actorRole:'ADMIN',action:'RESOLVE_DISPUTE',targetId:orderId,oldValue:order.status,newValue:refund?'REFUNDED':'COMPLETED',reason:note,timestamp:now()});
  });
  return { orderId, message: refund ? 'تم رد المبلغ للمستهلك.' : 'تمت تسوية الطلب لصالح التاجر.' };
});



exports.createTopUpRequest = onCall(async (request) => {
  const uid = requireAuth(request);
  const amount = Math.floor(num(request.data?.amount));
  const transferNumber = String(request.data?.transferNumber || '').trim();
  const type = String(request.data?.type || 'WALLET_TOPUP');
  const receiptImage = String(request.data?.receiptImage || '');
  if (amount < 500) throw new HttpsError('invalid-argument', 'الحد الأدنى للشحن هو 500 ريال يمني.');
  if (!transferNumber) throw new HttpsError('invalid-argument', 'رقم عملية التحويل مطلوب.');
  if (!['WALLET_TOPUP','LISTING_FEE_TOPUP'].includes(type)) throw new HttpsError('invalid-argument','نوع الشحن غير صالح.');
  const userSnap = await db.doc(`users/${uid}`).get();
  if (!userSnap.exists) throw new HttpsError('not-found','المستخدم غير موجود.');
  if (userSnap.get('isFrozen')) throw new HttpsError('failed-precondition','الحساب مجمد.');
  const requestId = `topup_${crypto.randomUUID().replaceAll('-','').slice(0,12)}`;
  await db.doc(`top_up_requests/${requestId}`).set({
    requestId,userId:uid,userName:userSnap.get('fullName')||'',userRole:userSnap.get('role')||'CONSUMER',
    amount,transferNumber,receiptImage,type,status:'PENDING',submittedAt:now(),reviewedAt:null,reviewedBy:null,adminNotes:null
  });
  return {requestId,message:'تم إرسال طلب الشحن للإدارة.'};
});

exports.reviewTopUp = onCall(async (request) => {
  const adminUid = requireAuth(request);
  if (await roleOf(adminUid) !== 'ADMIN') throw new HttpsError('permission-denied','هذه العملية للإدارة فقط.');
  const requestId = String(request.data?.requestId || '').trim();
  const approved = !!request.data?.isApproved;
  const notes = String(request.data?.notes || '').trim();
  if (!requestId) throw new HttpsError('invalid-argument','معرف طلب الشحن مطلوب.');
  const reqRef = db.doc(`top_up_requests/${requestId}`);
  const reqSnap = await reqRef.get();
  if (!reqSnap.exists) throw new HttpsError('not-found','طلب الشحن غير موجود.');
  const req = reqSnap.data();
  if (req.status !== 'PENDING') throw new HttpsError('failed-precondition','تمت معالجة طلب الشحن مسبقاً.');
  const walletRef = db.doc(`wallets/wallet_${req.userId}`);
  const txId = `tx_topup_${crypto.randomUUID().replaceAll('-','').slice(0,12)}`;
  const txRef = db.doc(`wallet_transactions/${txId}`);
  const notifRef = db.doc(`notifications/notif_${crypto.randomUUID()}`);
  const auditRef = db.doc(`audit_logs/log_${crypto.randomUUID()}`);
  await db.runTransaction(async tx => {
    const freshReq = await tx.get(reqRef);
    if (!freshReq.exists || freshReq.get('status') !== 'PENDING') throw new HttpsError('aborted','تمت معالجة الطلب بالتزامن.');
    if (!approved) {
      tx.update(reqRef,{status:'REJECTED',reviewedAt:now(),reviewedBy:adminUid,adminNotes:notes || 'بيانات التحويل غير مطابقة'});
      tx.set(auditRef,{logId:auditRef.id,actorId:adminUid,actorRole:'ADMIN',action:'REJECT_TOP_UP',targetId:requestId,oldValue:'PENDING',newValue:'REJECTED',reason:notes,timestamp:now()});
      tx.set(notifRef,{notificationId:notifRef.id,userId:req.userId,title:'تم رفض طلب الشحن',message:notes || 'بيانات التحويل غير مطابقة',isRead:false,type:'WALLET',createdAt:now()});
      return;
    }
    const walletSnap = await tx.get(walletRef);
    if (!walletSnap.exists) throw new HttpsError('failed-precondition','محفظة المستخدم غير موجودة.');
    const w=walletSnap.data();
    const update = req.type === 'LISTING_FEE_TOPUP'
      ? {listingBalance:num(w.listingBalance)+num(req.amount),updatedAt:now()}
      : {availableBalance:num(w.availableBalance)+num(req.amount),updatedAt:now()};
    tx.update(walletRef,update);
    tx.update(reqRef,{status:'APPROVED',reviewedAt:now(),reviewedBy:adminUid,adminNotes:notes});
    tx.set(txRef,{transactionId:txId,type:req.type === 'LISTING_FEE_TOPUP' ? 'LISTING_FEE_TOPUP':'TOP_UP',fromWalletId:null,toWalletId:walletRef.id,amount:num(req.amount),currency:'YER',orderId:null,userId:req.userId,status:'COMPLETED',createdAt:now(),createdBy:adminUid,note:`اعتماد شحن عبر جيب: ${req.transferNumber}`});
    tx.set(auditRef,{logId:auditRef.id,actorId:adminUid,actorRole:'ADMIN',action:'APPROVE_TOP_UP',targetId:requestId,oldValue:'PENDING',newValue:`APPROVED (${req.amount} YER)`,reason:notes,timestamp:now()});
    tx.set(notifRef,{notificationId:notifRef.id,userId:req.userId,title:'تم اعتماد الشحن',message:`تم إيداع ${req.amount} ريال يمني في رصيدك.`,isRead:false,type:'WALLET',createdAt:now()});
  });
  return {requestId,message:approved?'تم اعتماد الشحن وإيداع الرصيد.':'تم رفض طلب الشحن.'};
});

exports.publishProduct = onCall(async (request) => {
  const uid = requireAuth(request);
  const name=String(request.data?.name||'').trim(), description=String(request.data?.description||'').trim();
  const price=Math.floor(num(request.data?.price)), promoterCommission=Math.floor(num(request.data?.promoterCommission));
  const category=String(request.data?.category||'').trim(), location=String(request.data?.location||'').trim(), imageUrl=String(request.data?.imageUrl||'');
  const quantity=Math.floor(num(request.data?.quantity));
  if (!name || price<=0 || quantity<=0) throw new HttpsError('invalid-argument','بيانات المنتج غير صالحة.');
  if (imageUrl && !(imageUrl.startsWith('https://firebasestorage.googleapis.com/') || imageUrl.startsWith('https://storage.googleapis.com/'))) throw new HttpsError('invalid-argument','رابط صورة المنتج يجب أن يكون من Firebase Storage.');
  if (promoterCommission < 500 || promoterCommission >= price) throw new HttpsError('failed-precondition','عمولة المروج يجب ألا تقل عن 500 ريال وأن تكون أقل من سعر المنتج.');
  const userSnap=await db.doc(`users/${uid}`).get();
  if (!userSnap.exists || userSnap.get('role')!=='MERCHANT') throw new HttpsError('permission-denied','هذه العملية للتاجر فقط.');
  if (userSnap.get('isFrozen')) throw new HttpsError('failed-precondition','الحساب مجمد.');
  const settingsSnap=await db.doc('platform_settings/GLOBAL_SETTINGS').get();
  const fee=Math.floor(num(settingsSnap.get('listingFeePerProduct')) || 100);
  const walletRef=db.doc(`wallets/wallet_${uid}`), platformRef=db.doc('wallets/WALLET_PLATFORM');
  const productId=`prod_${crypto.randomUUID().replaceAll('-','').slice(0,12)}`, txId=`tx_list_${crypto.randomUUID().replaceAll('-','').slice(0,12)}`;
  await db.runTransaction(async tx=>{
    const [w,pw]=await Promise.all([tx.get(walletRef),tx.get(platformRef)]);
    if(!w.exists || num(w.get('listingBalance'))<fee) throw new HttpsError('failed-precondition',`رصيد العرض غير كافٍ. يلزم ${fee} ريال.`);
    if(!pw.exists) tx.set(platformRef,{walletId:'WALLET_PLATFORM',userId:'SYSTEM_PLATFORM',availableBalance:0,heldBalance:0,listingBalance:0,currency:'YER',status:'ACTIVE',createdAt:now(),updatedAt:now()});
    tx.update(walletRef,{listingBalance:num(w.get('listingBalance'))-fee,updatedAt:now()});
    tx.update(platformRef,{availableBalance:num(pw.exists ? pw.get('availableBalance') : 0)+fee,updatedAt:now()});
    tx.set(db.doc(`products/${productId}`),{productId,merchantId:uid,merchantName:userSnap.get('fullName')||'',name,description,imageUrl,price,promoterCommission,category,quantity,location,status:'PUBLISHED',isFeatured:false,listingFee:fee,createdAt:now(),updatedAt:now()});
    tx.set(db.doc(`wallet_transactions/${txId}`),{transactionId:txId,type:'LISTING_FEE',fromWalletId:walletRef.id,toWalletId:platformRef.id,amount:fee,currency:'YER',orderId:null,userId:uid,status:'COMPLETED',createdAt:now(),createdBy:uid,note:`رسوم نشر المنتج ${productId}`});
  });
  return {productId,listingFee:fee,message:'تم نشر المنتج وخصم رسوم الإدراج من رصيد العرض.'};
});



exports.setAccountFreeze = onCall(async (request) => {
  const adminUid = requireAuth(request);
  if (await roleOf(adminUid) !== 'ADMIN') throw new HttpsError('permission-denied','هذه العملية للإدارة فقط.');
  const userId = String(request.data?.userId || '').trim();
  const freeze = !!request.data?.freeze;
  const reason = String(request.data?.reason || '').trim();
  if (!userId) throw new HttpsError('invalid-argument','معرف المستخدم مطلوب.');
  if (userId === adminUid) throw new HttpsError('failed-precondition','لا يمكن للإدارة تجميد حسابها.');
  const ref = db.doc(`users/${userId}`);
  const auditRef = db.doc(`audit_logs/log_${crypto.randomUUID()}`);
  const notifRef = db.doc(`notifications/notif_${crypto.randomUUID()}`);
  await db.runTransaction(async tx => {
    const snap = await tx.get(ref);
    if (!snap.exists) throw new HttpsError('not-found','المستخدم غير موجود.');
    if (snap.get('role') === 'ADMIN') throw new HttpsError('failed-precondition','لا يمكن تجميد حساب إداري.');
    const old = !!snap.get('isFrozen');
    tx.update(ref,{isFrozen:freeze,freezeReason:freeze ? (reason || 'قرار إداري') : null,updatedAt:now()});
    tx.set(auditRef,{logId:auditRef.id,actorId:adminUid,actorRole:'ADMIN',action:freeze?'FREEZE_USER':'UNFREEZE_USER',targetId:userId,oldValue:String(old),newValue:String(freeze),reason,timestamp:now()});
    tx.set(notifRef,{notificationId:notifRef.id,userId,title:freeze?'تم تجميد الحساب':'تم إلغاء تجميد الحساب',message:freeze?(reason || 'تم تجميد حسابك بواسطة الإدارة.'): 'تمت إعادة تفعيل حسابك.',isRead:false,type:'ACCOUNT',createdAt:now()});
  });
  return {userId,message:freeze?'تم تجميد الحساب.':'تم إلغاء تجميد الحساب.'};
});

exports.updatePlatformSettings = onCall(async (request) => {
  const adminUid = requireAuth(request);
  if (await roleOf(adminUid) !== 'ADMIN') throw new HttpsError('permission-denied','هذه العملية للإدارة فقط.');
  const data=request.data || {};
  const platformName=String(data.platformName||'').trim();
  const platformJeebAccount=String(data.platformJeebAccount||'').trim();
  const listingFeePerProduct=Math.floor(num(data.listingFeePerProduct));
  const minimumPromoterCommission=Math.floor(num(data.minimumPromoterCommission));
  const platformFeePercentage=Math.floor(num(data.platformFeePercentage));
  const supportPhone=String(data.supportPhone||'').trim();
  const disputePolicy=String(data.disputePolicy||'').trim();
  if(!platformName || listingFeePerProduct<0 || minimumPromoterCommission<0 || platformFeePercentage<0 || platformFeePercentage>100)
    throw new HttpsError('invalid-argument','إعدادات المنصة غير صالحة.');
  const ref=db.doc('platform_settings/GLOBAL_SETTINGS');
  const auditRef=db.doc(`audit_logs/log_${crypto.randomUUID()}`);
  await db.runTransaction(async tx=>{
    const before=await tx.get(ref);
    tx.set(ref,{id:'GLOBAL_SETTINGS',platformName,platformJeebAccount,listingFeePerProduct,minimumPromoterCommission,platformFeePercentage,supportPhone,disputePolicy,updatedAt:now(),updatedBy:adminUid},{merge:true});
    tx.set(auditRef,{logId:auditRef.id,actorId:adminUid,actorRole:'ADMIN',action:'UPDATE_PLATFORM_SETTINGS',targetId:'GLOBAL_SETTINGS',oldValue:before.exists?'updated':'missing',newValue:'updated',reason:'تعديل إعدادات المنصة',timestamp:now()});
  });
  return {message:'تم حفظ إعدادات المنصة.'};
});

exports.merchantAdvanceOrder = onCall(async (request) => {
  const uid=requireAuth(request);
  if(await roleOf(uid)!=='MERCHANT') throw new HttpsError('permission-denied','هذه العملية للتاجر فقط.');
  const orderId=String(request.data?.orderId||'').trim();
  const newStatus=String(request.data?.newStatus||'').trim();
  const allowed={RESERVED:'PREPARING',PREPARING:'OUT_FOR_DELIVERY'};
  if(!orderId || allowed[newStatus]===undefined) throw new HttpsError('invalid-argument','حالة الطلب غير صالحة.');
  const ref=db.doc(`orders/${orderId}`);
  await db.runTransaction(async tx=>{
    const snap=await tx.get(ref);
    if(!snap.exists) throw new HttpsError('not-found','الطلب غير موجود.');
    if(snap.get('merchantId')!==uid) throw new HttpsError('permission-denied','الطلب لا يتبع لهذا التاجر.');
    const current=snap.get('status');
    if(allowed[current]!==newStatus) throw new HttpsError('failed-precondition','لا يمكن الانتقال إلى هذه الحالة.');
    tx.update(ref,{status:newStatus,updatedAt:now()});
  });
  return {orderId,message:'تم تحديث حالة الطلب.'};
});

exports.submitDeliveryProof = onCall(async (request) => {
  const uid=requireAuth(request);
  const orderId=String(request.data?.orderId||'').trim();
  const proof=String(request.data?.proof||'').trim();
  const notes=String(request.data?.notes||'').trim();
  if(!orderId || !proof) throw new HttpsError('invalid-argument','إثبات التوصيل مطلوب.');
  const ref=db.doc(`orders/${orderId}`);
  const notifRef=db.doc(`notifications/notif_${crypto.randomUUID()}`);
  await db.runTransaction(async tx=>{
    const snap=await tx.get(ref);
    if(!snap.exists) throw new HttpsError('not-found','الطلب غير موجود.');
    const role=await roleOf(uid);
    if(role!=='MERCHANT' || snap.get('merchantId')!==uid) throw new HttpsError('permission-denied','هذه العملية للتاجر صاحب الطلب فقط.');
    if(snap.get('status')!=='OUT_FOR_DELIVERY') throw new HttpsError('failed-precondition','يجب أن يكون الطلب خارجاً للتوصيل.');
    tx.update(ref,{status:'DELIVERED_PENDING_RELEASE',deliveryProofImage:proof,deliveryTime:now(),deliveryNotes:notes,updatedAt:now()});
    tx.set(notifRef,{notificationId:notifRef.id,userId:snap.get('consumerId'),title:'تم رفع إثبات التوصيل',message:'قام التاجر برفع إثبات التوصيل. يمكنك مراجعة الطلب ثم تحرير الضمان.',isRead:false,type:'ORDER',createdAt:now()});
  });
  return {orderId,message:'تم رفع إثبات التوصيل.'};
});

exports.blockClientFinancialWrites = onDocumentCreated('wallet_transactions/{id}', async () => null);

// Fan-out Firestore notifications through FCM. Token documents are self-owned by the app user.
exports.dispatchNotification = onDocumentCreated('notifications/{notificationId}', async (event) => {
  const data = event.data?.data();
  if (!data?.userId) return null;
  const snap = await db.collection(`users/${data.userId}/fcm_tokens`).get();
  if (snap.empty) return null;
  const tokens = snap.docs.map(d => d.get('token')).filter(Boolean);
  if (!tokens.length) return null;
  const response = await admin.messaging().sendEachForMulticast({
    tokens,
    notification: { title: String(data.title || 'المتجر اليمني'), body: String(data.message || '') },
    data: { notificationId: String(data.notificationId || event.params.notificationId), type: String(data.type || 'GENERAL') }
  });
  const stale = [];
  response.responses.forEach((r, i) => {
    const code = r.error?.code || '';
    if (code.includes('registration-token-not-registered') || code.includes('invalid-registration-token')) stale.push(snap.docs[i].ref);
  });
  if (stale.length) await Promise.all(stale.map(ref => ref.delete()));
  return null;
});

# المتجر اليمني — Secure Financial V4

هذه النسخة تجعل العمليات المالية الحساسة خادمية:

- شحن محفظة المستهلك/رصيد إدراج التاجر: `createTopUpRequest` ثم اعتماد الإدارة عبر `reviewTopUp`.
- نشر المنتج: `publishProduct` ويتحقق الخادم من رصيد الإدراج ويخصم الرسوم وينشئ المنتج داخل Transaction.
- الشراء وEscrow والتسوية والنزاعات: وظائف V3 السابقة.
- قواعد Firestore تمنع العميل من كتابة المحافظ، المعاملات، الطلبات، المنتجات، طلبات الشحن، أو تعديل النزاعات.

## نشر Backend

```bash
cd functions
npm install
cd ..
firebase login
firebase use <YOUR_FIREBASE_PROJECT_ID>
firebase deploy --only functions,firestore:rules
```

يجب إضافة `google-services.json` إلى `app/app/` قبل بناء Android.

> ملاحظة: الدفع عبر جيب هنا يعتمد على إثبات التحويل ورقم العملية ومراجعة الإدارة. لا يوجد ادعاء بوجود API مباشر لجيب ما لم يتم توفير تكامل رسمي وبيانات الاعتماد الخاصة به.

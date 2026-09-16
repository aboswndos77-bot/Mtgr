package com.example.domain.service

import androidx.room.withTransaction
import com.example.data.db.YemeniStoreDatabase
import com.example.data.model.*
import java.util.UUID

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class FinancialEngine(
    private val database: YemeniStoreDatabase
) {
    private val userDao = database.userDao()
    private val walletDao = database.walletDao()
    private val productDao = database.productDao()
    private val orderDao = database.orderDao()
    private val txDao = database.transactionDao()
    private val topUpDao = database.topUpDao()
    private val disputeDao = database.disputeDao()
    private val notifDao = database.notificationDao()
    private val auditDao = database.auditLogDao()
    private val settingsDao = database.settingsDao()

    // 1. Backend Validation for Promoter Commission
    fun validatePromoterCommission(commission: Long): Result<Unit> {
        return if (commission < 500L) {
            Result.Error("عمولة المروّج يجب ألا تقل عن 500 ريال يمني.")
        } else {
            Result.Success(Unit)
        }
    }

    // 2. Listing Product with 100 YER Listing Fee check & deduction
    suspend fun createProductWithListingFee(
        merchantId: String,
        name: String,
        description: String,
        price: Long,
        promoterCommission: Long,
        category: String,
        quantity: Int,
        location: String,
        imageUrl: String = ""
    ): Result<ProductEntity> {
        val user = userDao.findUserById(merchantId)
            ?: return Result.Error("المستخدم غير موجود")

        if (user.role != UserRole.MERCHANT) {
            return Result.Error("الحساب ليس حساب تاجر مصرح له بإضافة منتجات")
        }
        if (user.isFrozen) {
            return Result.Error("الحساب مجمد بسبب: ${user.freezeReason ?: "مخالفة الشروط"}. لا يمكن إضافة منتجات.")
        }

        // Backend rule: promoter commission >= 500
        if (promoterCommission < 500L) {
            return Result.Error("عمولة المروّج يجب ألا تقل عن 500 ريال يمني.")
        }
        if (price <= 0L) {
            return Result.Error("سعر المنتج يجب أن يكون أكبر من صفر")
        }
        if (promoterCommission >= price) {
            return Result.Error("عمولة المروّج يجب أن تكون أقل من سعر المنتج الإجمالي")
        }

        val settings = settingsDao.findSettings() ?: PlatformSettingsEntity()
        val listingFee = settings.listingFeePerProduct // 100 YER

        return database.withTransaction {
            val wallet = walletDao.findWalletByUserId(merchantId)
                ?: return@withTransaction Result.Error("محفظة التاجر غير موجودة")

            if (wallet.listingBalance < listingFee) {
                return@withTransaction Result.Error(
                    "رصيد العرض غير كافٍ. يلزم $listingFee ريال يمني لعرض كل منتج. رصيدك الحالي: ${wallet.listingBalance} ريال. يرجى شحن رصيد العرض أولاً."
                )
            }

            // Deduct listing fee atomically
            val updatedWallet = wallet.copy(
                listingBalance = wallet.listingBalance - listingFee,
                updatedAt = System.currentTimeMillis()
            )
            walletDao.updateWallet(updatedWallet)

            val productId = "prod_${UUID.randomUUID().toString().take(8)}"
            val product = ProductEntity(
                productId = productId,
                merchantId = merchantId,
                merchantName = user.fullName,
                name = name.trim(),
                description = description.trim(),
                price = price,
                promoterCommission = promoterCommission,
                category = category,
                quantity = quantity,
                location = location,
                imageUrl = imageUrl,
                status = ProductStatus.PUBLISHED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            productDao.insertProduct(product)

            // Ledger record
            val tx = WalletTransactionEntity(
                transactionId = "tx_list_${UUID.randomUUID().toString().take(8)}",
                type = TransactionType.LISTING_FEE,
                fromWalletId = wallet.walletId,
                toWalletId = "WALLET_PLATFORM",
                amount = listingFee,
                userId = merchantId,
                status = "COMPLETED",
                createdBy = merchantId,
                note = "رسوم نشر منتج: ${product.name}"
            )
            txDao.insertTransaction(tx)

            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = merchantId,
                    title = "تم نشر منتجك بنجاح",
                    message = "تم خصم $listingFee ريال يمني كرسوم عرض ونشر منتج '${product.name}'."
                )
            )

            Result.Success(product)
        }
    }

    // 3. Request Wallet or Listing Fee Top Up via Jeeb
    suspend fun submitTopUpRequest(
        userId: String,
        amount: Long,
        transferNumber: String,
        type: TopUpType,
        receiptImage: String = "ic_app_logo"
    ): Result<TopUpRequestEntity> {
        val user = userDao.findUserById(userId)
            ?: return Result.Error("المستخدم غير موجود")

        if (amount < 500L) {
            return Result.Error("الحد الأدنى للشحن هو 500 ريال يمني")
        }
        if (transferNumber.isBlank()) {
            return Result.Error("يرجى إدخال رقم عملية التحويل في تطبيق جيب")
        }

        val request = TopUpRequestEntity(
            requestId = "topup_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            userName = user.fullName,
            userRole = user.role,
            amount = amount,
            transferNumber = transferNumber.trim(),
            receiptImage = receiptImage,
            type = type,
            status = RequestStatus.PENDING,
            submittedAt = System.currentTimeMillis()
        )
        topUpDao.insertTopUp(request)
        return Result.Success(request)
    }

    // 4. Admin Approves or Rejects Top Up
    suspend fun reviewTopUp(
        requestId: String,
        adminId: String,
        isApproved: Boolean,
        notes: String = ""
    ): Result<Unit> {
        val admin = userDao.findUserById(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return Result.Error("صلاحية غير مصرح بها. تتطلب مدير النظام.")
        }

        return database.withTransaction {
            val req = topUpDao.findTopUpById(requestId)
                ?: return@withTransaction Result.Error("طلب الشحن غير موجود")

            if (req.status != RequestStatus.PENDING) {
                return@withTransaction Result.Error("تمت معالجة هذا الطلب مسبقاً")
            }

            if (isApproved) {
                val wallet = walletDao.findWalletByUserId(req.userId)
                    ?: return@withTransaction Result.Error("محفظة المستخدم غير موجودة")

                val updatedWallet = when (req.type) {
                    TopUpType.WALLET_TOPUP -> wallet.copy(
                        availableBalance = wallet.availableBalance + req.amount,
                        updatedAt = System.currentTimeMillis()
                    )
                    TopUpType.LISTING_FEE_TOPUP -> wallet.copy(
                        listingBalance = wallet.listingBalance + req.amount,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                walletDao.updateWallet(updatedWallet)

                val tx = WalletTransactionEntity(
                    transactionId = "tx_topup_${UUID.randomUUID().toString().take(8)}",
                    type = if (req.type == TopUpType.WALLET_TOPUP) TransactionType.TOP_UP else TransactionType.LISTING_FEE_TOPUP,
                    fromWalletId = null,
                    toWalletId = wallet.walletId,
                    amount = req.amount,
                    userId = req.userId,
                    status = "COMPLETED",
                    createdBy = adminId,
                    note = "اعتماد شحن عبر جيب (رقم العملية: ${req.transferNumber})"
                )
                txDao.insertTransaction(tx)

                topUpDao.updateTopUp(
                    req.copy(
                        status = RequestStatus.APPROVED,
                        reviewedAt = System.currentTimeMillis(),
                        reviewedBy = adminId,
                        adminNotes = notes
                    )
                )

                auditDao.insertLog(
                    AuditLogEntity(
                        logId = "log_${UUID.randomUUID()}",
                        actorId = adminId,
                        actorRole = "ADMIN",
                        action = "APPROVE_TOP_UP",
                        targetId = req.requestId,
                        oldValue = "PENDING",
                        newValue = "APPROVED (${req.amount} YER)",
                        reason = notes.ifBlank { "اعتماد مطابق للإيصال ورقم العملية" }
                    )
                )

                notifDao.insertNotification(
                    NotificationEntity(
                        notificationId = "notif_${UUID.randomUUID()}",
                        userId = req.userId,
                        title = "تم قبول شحن محفظتك",
                        message = "تم اعتماد مبلغ ${req.amount} ريال يمني بنجاح وإيداعه في رصيدك."
                    )
                )
            } else {
                topUpDao.updateTopUp(
                    req.copy(
                        status = RequestStatus.REJECTED,
                        reviewedAt = System.currentTimeMillis(),
                        reviewedBy = adminId,
                        adminNotes = notes
                    )
                )

                auditDao.insertLog(
                    AuditLogEntity(
                        logId = "log_${UUID.randomUUID()}",
                        actorId = adminId,
                        actorRole = "ADMIN",
                        action = "REJECT_TOP_UP",
                        targetId = req.requestId,
                        oldValue = "PENDING",
                        newValue = "REJECTED",
                        reason = notes.ifBlank { "إيصال غير صالح أو رقم عملية غير مطابق" }
                    )
                )

                notifDao.insertNotification(
                    NotificationEntity(
                        notificationId = "notif_${UUID.randomUUID()}",
                        userId = req.userId,
                        title = "تم رفض إيصال الشحن",
                        message = "تم رفض طلب الشحن لمبلغ ${req.amount} ريال. السبب: ${notes.ifBlank { "بيانات غير مطابقة" }}"
                    )
                )
            }

            Result.Success(Unit)
        }
    }

    // 5. Purchase Product with Escrow Hold (Atomic)
    suspend fun purchaseProductWithEscrow(
        consumerId: String,
        productId: String,
        promoCodeInput: String? = null
    ): Result<OrderEntity> {
        val consumer = userDao.findUserById(consumerId)
            ?: return Result.Error("المستخدم غير موجود")

        if (consumer.isFrozen) {
            return Result.Error("حسابك مجمد بسبب: ${consumer.freezeReason ?: "مخالفة"}. لا يمكنك إتمام الشراء.")
        }
        if (!consumer.isEmailVerified) {
            return Result.Error("يرجى تأكيد بريدك الإلكتروني قبل إجراء عمليات الشراء.")
        }

        return database.withTransaction {
            val product = productDao.findProductById(productId)
                ?: return@withTransaction Result.Error("المنتج غير موجود")

            if (product.status != ProductStatus.PUBLISHED) {
                return@withTransaction Result.Error("هذا المنتج غير متاح للشراء حالياً")
            }
            if (product.quantity <= 0) {
                return@withTransaction Result.Error("نفدت كمية هذا المنتج")
            }
            if (product.merchantId == consumerId) {
                return@withTransaction Result.Error("لا يمكن للتاجر شراء منتجه الخاص")
            }

            val consumerWallet = walletDao.findWalletByUserId(consumerId)
                ?: return@withTransaction Result.Error("محفظة المستهلك غير موجودة")

            if (consumerWallet.availableBalance < product.price) {
                return@withTransaction Result.Error(
                    "رصيدك المتاح (${consumerWallet.availableBalance} ريال) غير كافٍ لشراء هذا المنتج (${product.price} ريال). يرجى شحن محفظتك أولاً لإتمام الطلب."
                )
            }

            val merchant = userDao.findUserById(product.merchantId)
            val merchantJeeb = merchant?.jeebAccountNumber?.ifBlank { "772223344" } ?: "772223344"

            // Look up promoter attribution (prevent self-referral)
            val codeToUse = promoCodeInput?.trim()?.ifBlank { null } ?: consumer.referredByPromoCode
            var promoterId: String? = null
            if (codeToUse != null) {
                val promoterUser = userDao.findUserByPromoCode(codeToUse)
                if (promoterUser != null && promoterUser.id != consumerId && promoterUser.id != product.merchantId) {
                    promoterId = promoterUser.id
                }
            }

            // Snapshots calculations
            val price = product.price
            val platformFee = (price * 3L) / 100L // 3%
            val promoterCommission = if (promoterId != null) product.promoterCommission else 0L
            val merchantNet = price - platformFee - promoterCommission

            // Atomic Escrow balance hold:
            // availableBalance -= price
            // heldBalance += price
            val updatedConsumerWallet = consumerWallet.copy(
                availableBalance = consumerWallet.availableBalance - price,
                heldBalance = consumerWallet.heldBalance + price,
                updatedAt = System.currentTimeMillis()
            )
            walletDao.updateWallet(updatedConsumerWallet)

            // Decrement product quantity
            val updatedQuantity = product.quantity - 1
            productDao.updateProduct(
                product.copy(
                    quantity = updatedQuantity,
                    status = if (updatedQuantity <= 0) ProductStatus.SOLD else ProductStatus.PUBLISHED,
                    updatedAt = System.currentTimeMillis()
                )
            )

            val orderId = "ord_${UUID.randomUUID().toString().take(8)}"
            val escrowTxId = "tx_escrow_${UUID.randomUUID().toString().take(8)}"

            val order = OrderEntity(
                orderId = orderId,
                consumerId = consumerId,
                consumerName = consumer.fullName,
                merchantId = product.merchantId,
                merchantName = product.merchantName,
                merchantJeebAccount = merchantJeeb,
                productId = productId,
                productName = product.name,
                productImage = product.imageUrl,
                quantity = 1,
                productPrice = price,
                promoterCommission = promoterCommission,
                platformFee = platformFee,
                merchantNet = merchantNet,
                promoterId = promoterId,
                status = OrderStatus.RESERVED,
                escrowHoldTransactionId = escrowTxId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            orderDao.insertOrder(order)

            // Ledger entry
            txDao.insertTransaction(
                WalletTransactionEntity(
                    transactionId = escrowTxId,
                    type = TransactionType.ESCROW_HOLD,
                    fromWalletId = consumerWallet.walletId,
                    toWalletId = null,
                    amount = price,
                    orderId = orderId,
                    userId = consumerId,
                    status = "COMPLETED",
                    createdBy = consumerId,
                    note = "حجز مبلغ الضمان في Escrow للطلب: $orderId (${product.name})"
                )
            )

            // Notifications
            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = product.merchantId,
                    title = "لديك طلب جديد!",
                    message = "تم حجز مبلغ الطلب ($price ريال) بنجاح. يرجى تجهيز وشحن الطلب: ${product.name}."
                )
            )
            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = consumerId,
                    title = "تم تأكيد طلبك والضمان المالي محفوظ",
                    message = "تم حجز مبلغ $price ريال في الضمان المالي Escrow. التوصيل مجاني بالكامل."
                )
            )

            Result.Success(order)
        }
    }

    // 6. Merchant updates order status to PREPARING / OUT_FOR_DELIVERY
    suspend fun updateOrderStatusByMerchant(
        orderId: String,
        merchantId: String,
        newStatus: OrderStatus
    ): Result<OrderEntity> {
        return database.withTransaction {
            val order = orderDao.findOrderById(orderId)
                ?: return@withTransaction Result.Error("الطلب غير موجود")

            if (order.merchantId != merchantId) {
                return@withTransaction Result.Error("ليس لديك صلاحية لتعديل هذا الطلب")
            }

            val updated = order.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            orderDao.updateOrder(updated)

            if (newStatus == OrderStatus.OUT_FOR_DELIVERY) {
                notifDao.insertNotification(
                    NotificationEntity(
                        notificationId = "notif_${UUID.randomUUID()}",
                        userId = order.consumerId,
                        title = "طلبك في الطريق للتوصيل",
                        message = "التاجر أرسل طلبك '${order.productName}' مع المندوب (التوصيل مجاني)."
                    )
                )
            }
            Result.Success(updated)
        }
    }

    // 7. Delivery Proof Upload by Merchant/Courier
    suspend fun submitDeliveryProof(
        orderId: String,
        merchantId: String,
        proofImage: String = "ic_app_logo",
        notes: String = ""
    ): Result<OrderEntity> {
        return database.withTransaction {
            val order = orderDao.findOrderById(orderId)
                ?: return@withTransaction Result.Error("الطلب غير موجود")

            if (order.merchantId != merchantId) {
                return@withTransaction Result.Error("غير مصرح")
            }
            if (order.status != OrderStatus.OUT_FOR_DELIVERY && order.status != OrderStatus.PREPARING) {
                return@withTransaction Result.Error("لا يمكن رفع إثبات التوصيل إلا عندما يكون الطلب قيد التوصيل")
            }

            val updated = order.copy(
                status = OrderStatus.DELIVERED_PENDING_RELEASE,
                deliveryProofImage = proofImage,
                deliveryTime = System.currentTimeMillis(),
                deliveryNotes = notes.ifBlank { "تم التسليم بنجاح مع إثبات الاستلام" },
                updatedAt = System.currentTimeMillis()
            )
            orderDao.updateOrder(updated)

            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = order.consumerId,
                    title = "تم تسليم الطلب - يرجى تحرير الطلب",
                    message = "قام المندوب بتسليم طلبك. يرجى تأكيد الاستلام، تحويل المبلغ عبر جيب، وإرفاق الإشعار لتحرير الطلب."
                )
            )

            Result.Success(updated)
        }
    }

    // 8. Order Release & Atomic Settlement
    suspend fun releaseOrderSettlement(
        orderId: String,
        consumerId: String,
        transferNumber: String,
        receiptImage: String = "ic_app_logo"
    ): Result<OrderEntity> {
        if (transferNumber.isBlank()) {
            return Result.Error("يرجى إدخال رقم عملية التحويل عبر جيب")
        }

        return database.withTransaction {
            val order = orderDao.findOrderById(orderId)
                ?: return@withTransaction Result.Error("الطلب غير موجود")

            if (order.consumerId != consumerId) {
                return@withTransaction Result.Error("غير مصرح لك بتحرير هذا الطلب")
            }
            if (order.status != OrderStatus.DELIVERED_PENDING_RELEASE) {
                return@withTransaction Result.Error("الطلب ليس في حالة بانتظار التحرير")
            }

            val consumerWallet = walletDao.findWalletByUserId(consumerId)
                ?: return@withTransaction Result.Error("محفظة المستهلك غير موجودة")
            val merchantWallet = walletDao.findWalletByUserId(order.merchantId)
                ?: return@withTransaction Result.Error("محفظة التاجر غير موجودة")
            val platformWallet = walletDao.findWalletById("WALLET_PLATFORM")
                ?: return@withTransaction Result.Error("محفظة المنصة غير موجودة")

            if (consumerWallet.heldBalance < order.productPrice) {
                return@withTransaction Result.Error("الرصيد المحجوز في الضمان غير كافٍ لإتمام التسوية")
            }

            // 1. Release Consumer Held
            walletDao.updateWallet(
                consumerWallet.copy(
                    heldBalance = consumerWallet.heldBalance - order.productPrice,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // 2. Merchant Net Payout
            walletDao.updateWallet(
                merchantWallet.copy(
                    availableBalance = merchantWallet.availableBalance + order.merchantNet,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // 3. Platform Fee (3%)
            walletDao.updateWallet(
                platformWallet.copy(
                    availableBalance = platformWallet.availableBalance + order.platformFee,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // 4. Promoter Commission (if exists)
            if (order.promoterId != null && order.promoterCommission > 0L) {
                val promoterWallet = walletDao.findWalletByUserId(order.promoterId)
                if (promoterWallet != null) {
                    walletDao.updateWallet(
                        promoterWallet.copy(
                            availableBalance = promoterWallet.availableBalance + order.promoterCommission,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    txDao.insertTransaction(
                        WalletTransactionEntity(
                            transactionId = "tx_prom_${UUID.randomUUID().toString().take(8)}",
                            type = TransactionType.PROMOTER_COMMISSION,
                            fromWalletId = "WALLET_PLATFORM",
                            toWalletId = promoterWallet.walletId,
                            amount = order.promoterCommission,
                            orderId = orderId,
                            userId = order.promoterId,
                            status = "COMPLETED",
                            createdBy = consumerId,
                            note = "عمولة مروّج مستحقة للطلب: $orderId"
                        )
                    )
                    notifDao.insertNotification(
                        NotificationEntity(
                            notificationId = "notif_${UUID.randomUUID()}",
                            userId = order.promoterId,
                            title = "تمت إضافة عمولة إلى محفظتك!",
                            message = "تمت إضافة عمولة بقيمة ${order.promoterCommission} ريال يمني من مبيعات كود الترويج الخاص بك."
                        )
                    )
                }
            }

            val settlementTxId = "tx_settle_${UUID.randomUUID().toString().take(8)}"

            // Ledger records
            txDao.insertTransaction(
                WalletTransactionEntity(
                    transactionId = settlementTxId,
                    type = TransactionType.ESCROW_RELEASE,
                    fromWalletId = consumerWallet.walletId,
                    toWalletId = merchantWallet.walletId,
                    amount = order.merchantNet,
                    orderId = orderId,
                    userId = order.merchantId,
                    status = "COMPLETED",
                    createdBy = consumerId,
                    note = "تحرير صافي مبيعات للتاجر (${order.merchantNet} ريال)"
                )
            )
            txDao.insertTransaction(
                WalletTransactionEntity(
                    transactionId = "tx_fee_${UUID.randomUUID().toString().take(8)}",
                    type = TransactionType.PLATFORM_FEE,
                    fromWalletId = consumerWallet.walletId,
                    toWalletId = platformWallet.walletId,
                    amount = order.platformFee,
                    orderId = orderId,
                    userId = "SYSTEM_PLATFORM",
                    status = "COMPLETED",
                    createdBy = consumerId,
                    note = "رسوم المنصة 3% (${order.platformFee} ريال)"
                )
            )

            val completedOrder = order.copy(
                status = OrderStatus.COMPLETED,
                consumerTransferNumber = transferNumber.trim(),
                consumerReceiptImage = receiptImage,
                settlementTransactionId = settlementTxId,
                updatedAt = System.currentTimeMillis()
            )
            orderDao.updateOrder(completedOrder)

            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = order.merchantId,
                    title = "تم تحرير وتسوية الطلب بنجاح",
                    message = "تم إيداع صافي أرباحك (${order.merchantNet} ريال) في محفظتك المتاحة للطلب $orderId."
                )
            )
            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = consumerId,
                    title = "اكتمل الطلب بنجاح",
                    message = "شكراً لتعاملك مع المتجر اليمني. تم تحرير الضمان المالي وإغلاق الطلب."
                )
            )

            Result.Success(completedOrder)
        }
    }

    // 9. Dispute Management
    suspend fun openDispute(
        orderId: String,
        userId: String,
        reason: String,
        evidence: String = ""
    ): Result<DisputeEntity> {
        val user = userDao.findUserById(userId)
            ?: return Result.Error("المستخدم غير موجود")

        return database.withTransaction {
            val order = orderDao.findOrderById(orderId)
                ?: return@withTransaction Result.Error("الطلب غير موجود")

            if (order.consumerId != userId && order.merchantId != userId) {
                return@withTransaction Result.Error("غير مصرح لك بفتح نزاع لهذا الطلب")
            }

            val dispute = DisputeEntity(
                disputeId = "disp_${UUID.randomUUID().toString().take(8)}",
                orderId = orderId,
                openedByUserId = userId,
                openedByName = user.fullName,
                openedByRole = user.role,
                reason = reason.trim(),
                evidence = evidence,
                status = DisputeStatus.OPEN,
                createdAt = System.currentTimeMillis()
            )
            disputeDao.insertDispute(dispute)
            orderDao.updateOrder(order.copy(status = OrderStatus.DISPUTED, updatedAt = System.currentTimeMillis()))

            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = if (userId == order.consumerId) order.merchantId else order.consumerId,
                    title = "تم فتح نزاع على الطلب",
                    message = "قام الطرف الآخر بفتح نزاع للطلب $orderId. الإدارة ستقوم بالتحقق والمراجعة."
                )
            )

            Result.Success(dispute)
        }
    }

    // 10. Admin Resolves Dispute
    suspend fun resolveDispute(
        disputeId: String,
        adminId: String,
        refundToConsumer: Boolean,
        resolutionNote: String
    ): Result<Unit> {
        val admin = userDao.findUserById(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return Result.Error("صلاحية غير مصرح بها")
        }

        return database.withTransaction {
            val dispute = disputeDao.findDisputeById(disputeId)
                ?: return@withTransaction Result.Error("النزاع غير موجود")

            val order = orderDao.findOrderById(dispute.orderId)
                ?: return@withTransaction Result.Error("الطلب غير موجود")

            if (refundToConsumer) {
                // Refund consumer: return held balance to available balance
                val consumerWallet = walletDao.findWalletByUserId(order.consumerId)
                    ?: return@withTransaction Result.Error("محفظة المستهلك غير موجودة")

                walletDao.updateWallet(
                    consumerWallet.copy(
                        heldBalance = consumerWallet.heldBalance - order.productPrice,
                        availableBalance = consumerWallet.availableBalance + order.productPrice,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                txDao.insertTransaction(
                    WalletTransactionEntity(
                        transactionId = "tx_ref_${UUID.randomUUID().toString().take(8)}",
                        type = TransactionType.REFUND,
                        fromWalletId = null,
                        toWalletId = consumerWallet.walletId,
                        amount = order.productPrice,
                        orderId = order.orderId,
                        userId = order.consumerId,
                        status = "COMPLETED",
                        createdBy = adminId,
                        note = "استرداد كامل المبلغ للمستهلك بناء على قرار فض النزاع"
                    )
                )

                orderDao.updateOrder(order.copy(status = OrderStatus.REFUNDED, updatedAt = System.currentTimeMillis()))
            } else {
                // Settle in favor of merchant
                releaseOrderSettlement(
                    orderId = order.orderId,
                    consumerId = order.consumerId,
                    transferNumber = "ADMIN_SETTLED_${System.currentTimeMillis()}"
                )
            }

            disputeDao.updateDispute(
                dispute.copy(
                    status = DisputeStatus.RESOLVED,
                    resolutionNote = resolutionNote,
                    resolvedAt = System.currentTimeMillis(),
                    resolvedBy = adminId
                )
            )

            auditDao.insertLog(
                AuditLogEntity(
                    logId = "log_${UUID.randomUUID()}",
                    actorId = adminId,
                    actorRole = "ADMIN",
                    action = "RESOLVE_DISPUTE",
                    targetId = disputeId,
                    oldValue = "OPEN",
                    newValue = if (refundToConsumer) "REFUNDED_TO_CONSUMER" else "SETTLED_TO_MERCHANT",
                    reason = resolutionNote
                )
            )

            Result.Success(Unit)
        }
    }

    // 11. Admin Freeze / Unfreeze Account
    suspend fun setAccountFreeze(
        userId: String,
        adminId: String,
        freeze: Boolean,
        reason: String = ""
    ): Result<Unit> {
        val admin = userDao.findUserById(adminId)
        if (admin?.role != UserRole.ADMIN) {
            return Result.Error("صلاحية غير مصرح بها")
        }

        return database.withTransaction {
            val user = userDao.findUserById(userId)
                ?: return@withTransaction Result.Error("المستخدم غير موجود")

            val updated = user.copy(
                isFrozen = freeze,
                freezeReason = if (freeze) reason.ifBlank { "مخالفة معايير الأمان وشروط المنصة" } else null
            )
            userDao.updateUser(updated)

            auditDao.insertLog(
                AuditLogEntity(
                    logId = "log_${UUID.randomUUID()}",
                    actorId = adminId,
                    actorRole = "ADMIN",
                    action = if (freeze) "FREEZE_ACCOUNT" else "UNFREEZE_ACCOUNT",
                    targetId = userId,
                    oldValue = user.isFrozen.toString(),
                    newValue = freeze.toString(),
                    reason = reason
                )
            )

            notifDao.insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    userId = userId,
                    title = if (freeze) "تم تجميد حسابك مؤقتاً" else "تمت إعادة تفعيل حسابك",
                    message = if (freeze) "تم تجميد حسابك بسبب: $reason. يمكنك التواصل مع الإدارة لطلب المراجعة."
                    else "تمت إعادة تنشيط حسابك بنجاح وبإمكانك استئناف عملياتك."
                )
            )

            Result.Success(Unit)
        }
    }
}

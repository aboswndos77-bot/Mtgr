package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus {
    RESERVED,                   // محجوز وضمان مالي مقفل
    PREPARING,                  // قيد التجهيز من التاجر
    OUT_FOR_DELIVERY,           // خارج للتوصيل (توصيل مجاني)
    DELIVERED_PENDING_RELEASE,  // تم التسليم وبانتظار تحرير المستهلك وإرفاق إشعار جيب
    RELEASED,                   // تم تحرير الأموال وتوزيع الحصص
    COMPLETED,                  // مكتمل
    DISPUTED,                   // نزاع مفتوح
    CANCELLED,                  // ملغي
    REFUNDED                    // مسترد
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderId: String,
    val consumerId: String,
    val consumerName: String,
    val merchantId: String,
    val merchantName: String,
    val merchantJeebAccount: String = "",
    val productId: String,
    val productName: String,
    val productImage: String = "",
    val quantity: Int = 1,
    // Snapshots at purchase time (Immutable financial contract)
    val productPrice: Long,             // سعر المنتج الإجمالي
    val promoterCommission: Long,       // عمولة المروّج (>= 500 ريال)
    val platformFee: Long,              // رسوم المنصة (3%)
    val merchantNet: Long,              // صافي التاجر = السعر - الرسوم - العمولة
    val promoterId: String? = null,     // المروّج المستحق للعمولة إن وجد
    val status: OrderStatus = OrderStatus.RESERVED,
    // Delivery Proof (uploaded by merchant/courier)
    val deliveryProofImage: String? = null,
    val deliveryTime: Long? = null,
    val deliveryNotes: String? = null,
    // Consumer Settlement / Jeeb Transfer (uploaded by consumer before release)
    val consumerTransferNumber: String? = null,
    val consumerReceiptImage: String? = null,
    val escrowHoldTransactionId: String? = null,
    val settlementTransactionId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

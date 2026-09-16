package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    val walletId: String,
    val userId: String,
    val availableBalance: Long = 0L,  // بالريال اليمني YER
    val heldBalance: Long = 0L,       // المبالغ المحجوزة في الضمان Escrow
    val listingBalance: Long = 0L,    // رصيد نشر المنتجات للتاجر (100 ريال لكل منتج)
    val currency: String = "YER",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    TOP_UP,              // شحن محفظة عبر جيب
    LISTING_FEE_TOPUP,   // شحن رصيد العرض للتاجر
    LISTING_FEE,         // خصم رسوم نشر منتج (100 ريال)
    ESCROW_HOLD,         // حجز قيمة الطلب عند الشراء
    ESCROW_RELEASE,      // تحرير الضمان عند استلام الطلب
    PROMOTER_COMMISSION, // عمولة المروّج (لا تقل عن 500 ريال)
    PLATFORM_FEE,        // عمولة المنصة (3%)
    MERCHANT_PAYOUT,     // صافي أرباح التاجر بعد الخصومات
    REFUND,              // استرداد أموال للمستهلك (إلغاء أو نزاع)
    ADJUSTMENT           // تسوية إدارية
}

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey
    val transactionId: String,
    val type: TransactionType,
    val fromWalletId: String?,
    val toWalletId: String?,
    val amount: Long,          // مبلغ المعاملة بالريال اليمني
    val currency: String = "YER",
    val orderId: String? = null,
    val userId: String,
    val status: String = "COMPLETED", // COMPLETED, PENDING, REVERSED
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String,     // userId or ADMIN
    val note: String = "",
    val metadata: String = ""
)

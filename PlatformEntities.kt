package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TopUpType {
    WALLET_TOPUP,        // شحن محفظة المستهلك للشراء
    LISTING_FEE_TOPUP    // شحن رصيد العرض للتاجر (100 ريال لكل منتج)
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "top_up_requests")
data class TopUpRequestEntity(
    @PrimaryKey
    val requestId: String,
    val userId: String,
    val userName: String,
    val userRole: UserRole,
    val amount: Long,                    // المبلغ بالريال اليمني YER
    val transferNumber: String,          // رقم العملية في تطبيق جيب
    val receiptImage: String = "",       // صورة إشعار التحويل
    val type: TopUpType,
    val status: RequestStatus = RequestStatus.PENDING,
    val submittedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null,      // Admin ID who approved/rejected
    val adminNotes: String? = null
)

enum class DisputeStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    REJECTED
}

@Entity(tableName = "disputes")
data class DisputeEntity(
    @PrimaryKey
    val disputeId: String,
    val orderId: String,
    val openedByUserId: String,
    val openedByName: String,
    val openedByRole: UserRole,
    val reason: String,
    val evidence: String = "",
    val status: DisputeStatus = DisputeStatus.OPEN,
    val resolutionNote: String? = null,
    val resolvedAt: Long? = null,
    val resolvedBy: String? = null,      // Admin ID
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val notificationId: String,
    val userId: String,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val type: String = "GENERAL",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey
    val logId: String,
    val actorId: String,
    val actorRole: String,
    val action: String,
    val targetId: String,
    val oldValue: String = "",
    val newValue: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "platform_settings")
data class PlatformSettingsEntity(
    @PrimaryKey
    val id: String = "GLOBAL_SETTINGS",
    val platformName: String = "المتجر اليمني",
    val platformJeebAccount: String = "",
    val listingFeePerProduct: Long = 100L,        // 100 YER default
    val minimumPromoterCommission: Long = 500L,   // 500 YER default
    val platformFeePercentage: Int = 3,           // 3% default
    val supportPhone: String = "777000111",
    val disputePolicy: String = "تتم مراجعة كافة النزاعات بالتدقيق في إثباتات التوصيل وإشعارات تطبيق جيب لضمان حقوق كافة الأطراف."
)

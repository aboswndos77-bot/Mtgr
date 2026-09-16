package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CONSUMER,  // مستهلك
    MERCHANT,  // تاجر
    PROMOTER,  // مروّج
    ADMIN      // إدارة
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String = "",
    val role: UserRole,
    val isEmailVerified: Boolean = true,
    val isFrozen: Boolean = false,
    val freezeReason: String? = null,
    val promoCode: String? = null,           // Generated for promoters (e.g. RYD-12345)
    val referredByPromoCode: String? = null, // If referred by a promoter
    val jeebAccountNumber: String = "",      // Jeeb payment account for merchants/consumers
    val createdAt: Long = System.currentTimeMillis()
)

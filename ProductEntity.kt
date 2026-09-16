package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProductStatus {
    DRAFT,
    PENDING,
    PUBLISHED,
    SOLD,
    HIDDEN,
    SUSPENDED
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val productId: String,
    val merchantId: String,
    val merchantName: String,
    val name: String,
    val description: String,
    val imageUrl: String = "",
    val price: Long,                    // بالريال اليمني YER
    val promoterCommission: Long,       // شرط إلزامي: >= 500 YER
    val category: String,               // إلكترونيات، عسل يمني، بن وقشر، عطور وبخور، ملابس وجنابي، إلخ
    val quantity: Int = 1,
    val location: String = "صنعاء",     // المدينة: صنعاء، عدن، تعز، الحديدة، حضرموت، إب، ذمار
    val status: ProductStatus = ProductStatus.PUBLISHED,
    val isFeatured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

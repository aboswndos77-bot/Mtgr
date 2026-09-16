package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun findUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE promoCode = :code LIMIT 1")
    suspend fun findUserByPromoCode(code: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE referredByPromoCode = :code")
    fun countReferredUsers(code: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets WHERE userId = :userId")
    fun getWalletByUserId(userId: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallets WHERE userId = :userId")
    suspend fun findWalletByUserId(userId: String): WalletEntity?

    @Query("SELECT * FROM wallets WHERE walletId = :walletId")
    suspend fun findWalletById(walletId: String): WalletEntity?

    @Query("SELECT * FROM wallets")
    fun getAllWallets(): Flow<List<WalletEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletEntity)

    @Update
    suspend fun updateWallet(wallet: WalletEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE status = 'PUBLISHED' ORDER BY createdAt DESC")
    fun getPublishedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE merchantId = :merchantId ORDER BY createdAt DESC")
    fun getProductsByMerchant(merchantId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId")
    fun getProductById(productId: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE productId = :productId")
    suspend fun findProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE consumerId = :consumerId ORDER BY createdAt DESC")
    fun getOrdersByConsumer(consumerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE merchantId = :merchantId ORDER BY createdAt DESC")
    fun getOrdersByMerchant(merchantId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE promoterId = :promoterId ORDER BY createdAt DESC")
    fun getOrdersByPromoter(promoterId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun findOrderById(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)
}

@Dao
interface TopUpRequestDao {
    @Query("SELECT * FROM top_up_requests ORDER BY submittedAt DESC")
    fun getAllTopUps(): Flow<List<TopUpRequestEntity>>

    @Query("SELECT * FROM top_up_requests WHERE userId = :userId ORDER BY submittedAt DESC")
    fun getTopUpsByUserId(userId: String): Flow<List<TopUpRequestEntity>>

    @Query("SELECT * FROM top_up_requests WHERE requestId = :requestId")
    suspend fun findTopUpById(requestId: String): TopUpRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopUp(request: TopUpRequestEntity)

    @Update
    suspend fun updateTopUp(request: TopUpRequestEntity)
}

@Dao
interface DisputeDao {
    @Query("SELECT * FROM disputes ORDER BY createdAt DESC")
    fun getAllDisputes(): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE orderId = :orderId")
    fun getDisputesByOrderId(orderId: String): Flow<List<DisputeEntity>>

    @Query("SELECT * FROM disputes WHERE disputeId = :disputeId")
    suspend fun findDisputeById(disputeId: String): DisputeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispute(dispute: DisputeEntity)

    @Update
    suspend fun updateDispute(dispute: DisputeEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsByUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface PlatformSettingsDao {
    @Query("SELECT * FROM platform_settings WHERE id = 'GLOBAL_SETTINGS'")
    fun getSettings(): Flow<PlatformSettingsEntity?>

    @Query("SELECT * FROM platform_settings WHERE id = 'GLOBAL_SETTINGS'")
    suspend fun findSettings(): PlatformSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: PlatformSettingsEntity)

    @Update
    suspend fun updateSettings(settings: PlatformSettingsEntity)
}

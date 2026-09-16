package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        WalletEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        WalletTransactionEntity::class,
        TopUpRequestEntity::class,
        DisputeEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        PlatformSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YemeniStoreDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun walletDao(): WalletDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun transactionDao(): WalletTransactionDao
    abstract fun topUpDao(): TopUpRequestDao
    abstract fun disputeDao(): DisputeDao
    abstract fun notificationDao(): NotificationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun settingsDao(): PlatformSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: YemeniStoreDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): YemeniStoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YemeniStoreDatabase::class.java,
                    "yemeni_store_db"
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: YemeniStoreDatabase) {
    val userDao = database.userDao()
    val walletDao = database.walletDao()
    val productDao = database.productDao()
    val settingsDao = database.settingsDao()
    val transactionDao = database.transactionDao()

    // 1. Platform Global Settings
    settingsDao.insertSettings(
        PlatformSettingsEntity(
            id = "GLOBAL_SETTINGS",
            platformName = "المتجر اليمني",
            platformJeebAccount = "حساب محفظة جيب الرسمية: 777123456 - المتجر اليمني",
            listingFeePerProduct = 100L,
            minimumPromoterCommission = 500L,
            platformFeePercentage = 3,
            supportPhone = "777000111"
        )
    )

    // Platform system wallet
    walletDao.insertWallet(
        WalletEntity(
            walletId = "WALLET_PLATFORM",
            userId = "SYSTEM_PLATFORM",
            availableBalance = 0L,
            heldBalance = 0L,
            currency = "YER"
        )
    )

    // 2. Pre-seeded Users as specified in prompt
    // Consumer
    val consumerUser = UserEntity(
        id = "user_consumer_1",
        email = "consumer@test.example",
        fullName = "صالح اليماني (مستهلك)",
        phone = "771112233",
        role = UserRole.CONSUMER,
        isEmailVerified = true,
        jeebAccountNumber = "771112233"
    )
    userDao.insertUser(consumerUser)
    walletDao.insertWallet(
        WalletEntity(
            walletId = "wallet_consumer_1",
            userId = consumerUser.id,
            availableBalance = 50000L, // Initial test balance 50,000 YER
            heldBalance = 0L
        )
    )

    // Merchant
    val merchantUser = UserEntity(
        id = "user_merchant_1",
        email = "merchant@test.example",
        fullName = "مطهر الشيباني (تاجر)",
        phone = "772223344",
        role = UserRole.MERCHANT,
        isEmailVerified = true,
        jeebAccountNumber = "772223344"
    )
    userDao.insertUser(merchantUser)
    walletDao.insertWallet(
        WalletEntity(
            walletId = "wallet_merchant_1",
            userId = merchantUser.id,
            availableBalance = 0L,
            heldBalance = 0L,
            listingBalance = 500L // 500 YER listing balance (allows listing 5 products)
        )
    )

    // Promoter
    val promoterUser = UserEntity(
        id = "user_promoter_1",
        email = "promoter@test.example",
        fullName = "عمر باعباد (مروّج)",
        phone = "773334455",
        role = UserRole.PROMOTER,
        isEmailVerified = true,
        promoCode = "RYD-78241",
        jeebAccountNumber = "773334455"
    )
    userDao.insertUser(promoterUser)
    walletDao.insertWallet(
        WalletEntity(
            walletId = "wallet_promoter_1",
            userId = promoterUser.id,
            availableBalance = 0L,
            heldBalance = 0L
        )
    )

    // Admin
    val adminUser = UserEntity(
        id = "user_admin_1",
        email = "admin@test.example",
        fullName = "الإدارة المركزية - المتجر اليمني",
        phone = "777123456",
        role = UserRole.ADMIN,
        isEmailVerified = true
    )
    userDao.insertUser(adminUser)
    walletDao.insertWallet(
        WalletEntity(
            walletId = "wallet_admin_1",
            userId = adminUser.id,
            availableBalance = 0L,
            heldBalance = 0L
        )
    )

    // 3. Authentic Initial Products with >= 500 YER promoter commission
    val p1 = ProductEntity(
        productId = "prod_honey_101",
        merchantId = merchantUser.id,
        merchantName = merchantUser.fullName,
        name = "عسل سدر دوعني ملكي فاخر (1 كجم)",
        description = "عسل سدر يمني حر طبيعي 100% مستخرج من مناحل وادي دوعن بحضرموت، معبأ بأعلى معايير الجودة.",
        price = 10000L,                // 10,000 YER
        promoterCommission = 500L,      // 500 YER commission
        category = "عسل يمني",
        quantity = 15,
        location = "حضرموت",
        status = ProductStatus.PUBLISHED,
        isFeatured = true
    )
    productDao.insertProduct(p1)

    val p2 = ProductEntity(
        productId = "prod_coffee_102",
        merchantId = merchantUser.id,
        merchantName = merchantUser.fullName,
        name = "بن مطري يمني فاخر - تحميص متوسط (500 جم)",
        description = "بن يمني أنديكا أصيل من مدرجات بني مطر العالية بنكهة الشوكولاتة والتوابل الفاخرة.",
        price = 8500L,
        promoterCommission = 600L,
        category = "بن وقشر",
        quantity = 25,
        location = "صنعاء",
        status = ProductStatus.PUBLISHED,
        isFeatured = true
    )
    productDao.insertProduct(p2)

    val p3 = ProductEntity(
        productId = "prod_bakhoor_103",
        merchantId = merchantUser.id,
        merchantName = merchantUser.fullName,
        name = "بخور عدني ملكي فاخر برائحة العود الأصلي",
        description = "طبخة عدنية منزلية تقليدية معتقة بدهن العود الملكي والمسك والظفرة الأصيلة.",
        price = 4500L,
        promoterCommission = 500L,
        category = "عطور وبخور",
        quantity = 30,
        location = "عدن",
        status = ProductStatus.PUBLISHED,
        isFeatured = false
    )
    productDao.insertProduct(p3)

    val p4 = ProductEntity(
        productId = "prod_jambiya_104",
        merchantId = merchantUser.id,
        merchantName = merchantUser.fullName,
        name = "جنبية صيفاني تراثية مع نصلة فولاذية دمشقية ومحزم مطرز",
        description = "صناعة يدوية صنعانية أصيلة، رأس فاخر مجلي بحرفية عالية مع عسيب مطرز بالخيوط الذهبية.",
        price = 35000L,
        promoterCommission = 1500L,
        category = "تراثيات وجنابي",
        quantity = 5,
        location = "صنعاء",
        status = ProductStatus.PUBLISHED,
        isFeatured = true
    )
    productDao.insertProduct(p4)

    // Initial Listing fee transaction record for the merchant's 500 YER balance
    transactionDao.insertTransaction(
        WalletTransactionEntity(
            transactionId = "tx_seed_init",
            type = TransactionType.LISTING_FEE_TOPUP,
            fromWalletId = null,
            toWalletId = "wallet_merchant_1",
            amount = 500L,
            userId = merchantUser.id,
            status = "COMPLETED",
            createdBy = "ADMIN",
            note = "شحن رصيد عرض تجريبي مبدئي"
        )
    )
}

package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.YemeniStoreDatabase
import com.example.data.model.*
import com.example.data.repository.StoreRepository
import com.example.domain.service.FinancialEngine
import com.example.domain.service.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val database = YemeniStoreDatabase.getDatabase(application, viewModelScope)
    val financialEngine = FinancialEngine(database)
    val repository = StoreRepository(database, financialEngine)
    private val cloudRepository = com.example.data.repository.CloudStoreRepository()
    private val cloudWriter = com.example.data.repository.FirestoreWriteRepository()
    private val authRepository = com.example.data.repository.FirebaseAuthRepository()
    private val secureFunctions = com.example.data.repository.FirebaseFunctionsRepository()
    private val storageRepository = com.example.data.repository.FirebaseStorageRepository()

    // Current logged-in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Navigation & Tab state
    private val _currentScreen = MutableStateFlow("HOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Search & Filter state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("الكل")
    val selectedCity = MutableStateFlow("الكل")

    // UI feedback (Success/Error messages)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Data streams: Firestore is the cross-device source of truth.
    val allUsers: StateFlow<List<UserEntity>> = cloudRepository.users()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allProducts: StateFlow<List<ProductEntity>> = cloudRepository.products()
        .map { list -> list.filter { it.status == ProductStatus.PUBLISHED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAdminProducts: StateFlow<List<ProductEntity>> = cloudRepository.products()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val platformSettings: StateFlow<PlatformSettingsEntity?> = cloudRepository.settings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allTopUps: StateFlow<List<TopUpRequestEntity>> = cloudRepository.topUps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDisputes: StateFlow<List<DisputeEntity>> = cloudRepository.disputes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allOrders: StateFlow<List<OrderEntity>> = cloudRepository.orders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTransactions: StateFlow<List<WalletTransactionEntity>> = cloudRepository.transactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAuditLogs: StateFlow<List<AuditLogEntity>> = cloudRepository.logs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active User Specific Streams
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userWallet: StateFlow<WalletEntity?> = _currentUser.flatMapLatest { user ->
        if (user != null) cloudRepository.wallets().map { list -> list.firstOrNull { it.userId == user.id } } else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) {
            when (user.role) {
                UserRole.CONSUMER -> cloudRepository.orders().map { list -> list.filter { it.consumerId == user.id } }
                UserRole.MERCHANT -> cloudRepository.orders().map { list -> list.filter { it.merchantId == user.id } }
                UserRole.PROMOTER -> cloudRepository.orders().map { list -> list.filter { it.promoterId == user.id } }
                UserRole.ADMIN -> cloudRepository.orders()
            }
        } else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userTransactions: StateFlow<List<WalletTransactionEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) cloudRepository.transactions().map { list -> list.filter { it.userId == user.id } } else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) cloudRepository.notifications(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val merchantProducts: StateFlow<List<ProductEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && user.role == UserRole.MERCHANT) {
            cloudRepository.products().map { list -> list.filter { it.merchantId == user.id } }
        } else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { runCatching { cloudWriter.ensurePlatformData() } }
        // Firebase Authentication restores the session automatically.
        // The explicit login screen loads the cloud profile and establishes the app session.
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
        _currentScreen.value = "HOME"
        showMessage("تم التبديل إلى حساب: ${user.fullName} (${getRoleArabic(user.role)})")
    }

    fun getRoleArabic(role: UserRole): String = when (role) {
        UserRole.CONSUMER -> "مستهلك"
        UserRole.MERCHANT -> "تاجر"
        UserRole.PROMOTER -> "مروّج"
        UserRole.ADMIN -> "إدارة المنصة"
    }

    // Real Firebase registration
    fun register(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        role: UserRole,
        promoCode: String?,
        jeebAccount: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val res = authRepository.register(email, password, fullName, phone, role, promoCode, jeebAccount)) {
                is Result.Success -> {
                    repository.userDao.insertUser(res.data)
                    repository.walletDao.insertWallet(WalletEntity(walletId = "wallet_${res.data.id}", userId = res.data.id))
                    cloudWriter.saveUser(res.data)
                    cloudWriter.saveWallet(WalletEntity(walletId = "wallet_${res.data.id}", userId = res.data.id))
                    showMessage("تم إنشاء الحساب. تحقق من بريدك الإلكتروني ثم سجل الدخول")
                }
                is Result.Error -> showMessage(res.message)
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val res = authRepository.login(email, password)) {
                is Result.Success -> {
                    repository.userDao.insertUser(res.data)
                    cloudWriter.saveUser(res.data)
                    if (repository.walletDao.findWalletByUserId(res.data.id) == null) {
                        repository.walletDao.insertWallet(WalletEntity(walletId = "wallet_${res.data.id}", userId = res.data.id))
                    }
                    _currentUser.value = res.data
                    _currentScreen.value = "HOME"
                    showMessage("مرحباً ${res.data.fullName} — ${getRoleArabic(res.data.role)}")
                }
                is Result.Error -> showMessage(res.message)
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _currentScreen.value = "HOME"
    }

    // Purchase product
    fun purchaseProduct(productId: String, promoCode: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                secureFunctions.purchaseProduct(productId, promoCode)
            }.onSuccess {
                showMessage("تم الشراء وحجز المبلغ في الضمان المالي Escrow على الخادم بنجاح! التوصيل مجاني.")
                _currentScreen.value = "ORDERS"
            }.onFailure { e ->
                showMessage(e.message ?: "تعذر إتمام عملية الشراء الآمنة")
            }
            _isLoading.value = false
        }
    }

    // Merchant adds product: server validates balance, deducts listing fee and creates the product.
    fun addProduct(
        name: String, description: String, price: Long, promoterCommission: Long,
        category: String, quantity: Int, location: String, imageUri: Uri? = null
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val imageUrl = imageUri?.let { storageRepository.uploadProductImage(it) } ?: ""
                secureFunctions.publishProduct(name, description, price, promoterCommission, category, quantity, location, imageUrl)
            }.onSuccess {
                showMessage("تم نشر المنتج وخصم رسوم الإدراج من رصيد العرض على الخادم.")
                _currentScreen.value = "MERCHANT_PRODUCTS"
            }.onFailure { e -> showMessage(e.message ?: "تعذر نشر المنتج") }
            _isLoading.value = false
        }
    }

    // Top up wallet: request is created by the server, not by a client-side write.
    fun submitTopUp(amount: Long, transferNumber: String, type: TopUpType, receiptUri: Uri? = null) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val receiptUrl = receiptUri?.let { storageRepository.uploadTopUpReceipt(it) } ?: ""
                secureFunctions.createTopUpRequest(amount, transferNumber, type.name, receiptUrl)
            }.onSuccess {
                showMessage("تم إرسال طلب الشحن للإدارة بنجاح. سيتم إيداع الرصيد بعد اعتماد التحويل.")
                _currentScreen.value = "WALLET"
            }.onFailure { e -> showMessage(e.message ?: "تعذر إرسال طلب الشحن") }
            _isLoading.value = false
        }
    }

    // Admin approves/rejects top up on the server; wallet changes are never client-side.
    fun reviewTopUp(requestId: String, isApproved: Boolean, notes: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { secureFunctions.reviewTopUp(requestId, isApproved, notes) }
                .onSuccess { showMessage(if (isApproved) "تم اعتماد الشحن وإيداع الرصيد بنجاح." else "تم رفض طلب الشحن.") }
                .onFailure { e -> showMessage(e.message ?: "تعذر معالجة طلب الشحن") }
            _isLoading.value = false
        }
    }

    // Merchant advances order (e.g. to PREPARING or OUT_FOR_DELIVERY)
    fun merchantAdvanceOrder(orderId: String, newStatus: OrderStatus) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { secureFunctions.merchantAdvanceOrder(orderId, newStatus.name) }
                .onSuccess { showMessage("تم تحديث حالة الطلب إلى: ${newStatus.name}") }
                .onFailure { e -> showMessage(e.message ?: "تعذر تحديث حالة الطلب") }
            _isLoading.value = false
        }
    }

    // Merchant submits delivery proof
    fun submitDeliveryProof(orderId: String, notes: String, proofUri: Uri? = null) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val proofUrl = proofUri?.let { storageRepository.uploadDeliveryProof(it) }
                    ?: throw IllegalArgumentException("يجب اختيار صورة إثبات التسليم")
                secureFunctions.submitDeliveryProof(orderId, proofUrl, notes)
            }
                .onSuccess { showMessage("تم رفع إثبات التوصيل بنجاح وبانتظار تحرير المستهلك وإرفاق إشعار جيب.") }
                .onFailure { e -> showMessage(e.message ?: "تعذر رفع إثبات التوصيل") }
            _isLoading.value = false
        }
    }

    // Consumer releases order settlement
    fun releaseOrder(orderId: String, transferNumber: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                secureFunctions.releaseOrder(orderId, transferNumber)
            }.onSuccess {
                showMessage("تم تحرير الضمان وتسوية الحصص (التاجر + المروّج + رسوم المنصة) ذرياً على الخادم بنجاح!")
            }.onFailure { e ->
                showMessage(e.message ?: "تعذر تحرير الضمان")
            }
            _isLoading.value = false
        }
    }

    // Open dispute
    fun openDispute(orderId: String, reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            when (val res = financialEngine.openDispute(orderId, user.id, reason)) {
                is Result.Success -> {
                    showMessage("تم فتح النزاع بنجاح. سيقوم فريق الإدارة بمراجعته وتدقيق الإيصالات.")
                    _currentScreen.value = "DISPUTES"
                }
                is Result.Error -> {
                    showMessage(res.message)
                }
            }
            _isLoading.value = false
        }
    }

    // Admin resolves dispute
    fun resolveDispute(disputeId: String, refundConsumer: Boolean, resolutionNote: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            when (val res = runCatching { secureFunctions.resolveDispute(disputeId, refundConsumer, resolutionNote) }.fold({ Result.Success(it) }, { Result.Error(it.message ?: "تعذر إنهاء النزاع") })) {
                is Result.Success -> {
                    showMessage("تم إنهاء النزاع بنجاح: " + if (refundConsumer) "تم استرداد المبلغ للمستهلك" else "تمت التسوية لصالح التاجر")
                }
                is Result.Error -> {
                    showMessage(res.message)
                }
            }
            _isLoading.value = false
        }
    }

    // Admin freeze/unfreeze account
    fun setAccountFreeze(userId: String, freeze: Boolean, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { secureFunctions.setAccountFreeze(userId, freeze, reason) }
                .onSuccess { showMessage(if (freeze) "تم تجميد الحساب بنجاح." else "تمت إعادة تفعيل الحساب.") }
                .onFailure { e -> showMessage(e.message ?: "تعذر تغيير حالة الحساب") }
            _isLoading.value = false
        }
    }

    // Update settings
    fun updatePlatformSettings(settings: PlatformSettingsEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { secureFunctions.updatePlatformSettings(settings) }
                .onSuccess { showMessage("تم حفظ إعدادات المنصة بنجاح.") }
                .onFailure { e -> showMessage(e.message ?: "تعذر حفظ الإعدادات") }
            _isLoading.value = false
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.BinanceLogoIcon
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAuthModal by remember { mutableStateOf(false) }
    var showNotifsModal by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val unreadNotifsCount = notifications.count { !it.isRead }

    Scaffold(
        containerColor = BinanceBgDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = BinanceHeaderDark,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderDark)
            ) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BinanceLogoIcon(size = 32)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المتجر اليمني",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BinanceTextWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = BinanceYellow.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "P2P",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BinanceYellow,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "ضمان مالي Escrow 100% • جيب Jeeb",
                                    fontSize = 10.sp,
                                    color = BinanceTextGray
                                )
                            }
                        }
                    },
                    actions = {
                        // Notifications icon with Binance yellow badge
                        IconButton(onClick = { showNotifsModal = true }) {
                            BadgedBox(badge = {
                                if (unreadNotifsCount > 0) {
                                    Badge(
                                        containerColor = BinanceYellow,
                                        contentColor = Color.Black
                                    ) { Text("$unreadNotifsCount", fontWeight = FontWeight.Bold) }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "الإشعارات",
                                    tint = BinanceTextWhite
                                )
                            }
                        }

                        // Active User Switcher Pill (Binance VIP Style)
                        currentUser?.let { user ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BinanceInputDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderHighlight),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { showAuthModal = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(BinanceYellow),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.fullName.take(1),
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${user.fullName.take(8)} (${viewModel.getRoleArabic(user.role)})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BinanceTextWhite
                                    )
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = BinanceTextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } ?: run {
                            Button(
                                onClick = { showAuthModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("تسجيل الدخول", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = BinanceTextWhite
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                color = BinanceHeaderDark,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderDark)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    val role = currentUser?.role ?: UserRole.CONSUMER

                    NavigationBarItem(
                        selected = currentScreen == "HOME",
                        onClick = { viewModel.navigateTo("HOME") },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "الأسواق") },
                        label = { Text("الأسواق", fontSize = 11.sp, fontWeight = if (currentScreen == "HOME") FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = BinanceYellow,
                            indicatorColor = BinanceYellow,
                            unselectedIconColor = BinanceTextGray,
                            unselectedTextColor = BinanceTextGray
                        )
                    )

                    if (role == UserRole.CONSUMER) {
                        NavigationBarItem(
                            selected = currentScreen == "ORDERS",
                            onClick = { viewModel.navigateTo("ORDERS") },
                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "الطلبات") },
                            label = { Text("الطلبات", fontSize = 11.sp, fontWeight = if (currentScreen == "ORDERS") FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinanceYellow,
                                indicatorColor = BinanceYellow,
                                unselectedIconColor = BinanceTextGray,
                                unselectedTextColor = BinanceTextGray
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "WALLET",
                            onClick = { viewModel.navigateTo("WALLET") },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "الأصول") },
                            label = { Text("المحفظة", fontSize = 11.sp, fontWeight = if (currentScreen == "WALLET") FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinanceYellow,
                                indicatorColor = BinanceYellow,
                                unselectedIconColor = BinanceTextGray,
                                unselectedTextColor = BinanceTextGray
                            )
                        )
                    }

                    if (role == UserRole.MERCHANT) {
                        NavigationBarItem(
                            selected = currentScreen == "MERCHANT_DASHBOARD",
                            onClick = { viewModel.navigateTo("MERCHANT_DASHBOARD") },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "لوحة التاجر") },
                            label = { Text("لوحة التاجر", fontSize = 11.sp, fontWeight = if (currentScreen == "MERCHANT_DASHBOARD") FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinanceYellow,
                                indicatorColor = BinanceYellow,
                                unselectedIconColor = BinanceTextGray,
                                unselectedTextColor = BinanceTextGray
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "WALLET",
                            onClick = { viewModel.navigateTo("WALLET") },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "المحفظة") },
                            label = { Text("المحفظة والعرض", fontSize = 11.sp, fontWeight = if (currentScreen == "WALLET") FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinanceYellow,
                                indicatorColor = BinanceYellow,
                                unselectedIconColor = BinanceTextGray,
                                unselectedTextColor = BinanceTextGray
                            )
                        )
                    }

                    if (role == UserRole.PROMOTER) {
                        NavigationBarItem(
                            selected = currentScreen == "PROMOTER_DASHBOARD",
                            onClick = { viewModel.navigateTo("PROMOTER_DASHBOARD") },
                            icon = { Icon(Icons.Default.Campaign, contentDescription = "الترويج") },
                            label = { Text("كود الترويج", fontSize = 11.sp, fontWeight = if (currentScreen == "PROMOTER_DASHBOARD") FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinanceYellow,
                                indicatorColor = BinanceYellow,
                                unselectedIconColor = BinanceTextGray,
                                unselectedTextColor = BinanceTextGray
                            )
                        )
                        NavigationBarItem(
                            selected = currentScreen == "WALLET",
                            onClick = { viewModel.navigateTo("WALLET") },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "العمولات") },
                            label = { Text("العمولات", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinanceYellow,
                                indicatorColor = BinanceYellow,
                                unselectedIconColor = BinanceTextGray,
                                unselectedTextColor = BinanceTextGray
                            )
                        )
                    }

                    if (role == UserRole.ADMIN) {
                        NavigationBarItem(
                            selected = currentScreen == "ADMIN_DASHBOARD",
                            onClick = { viewModel.navigateTo("ADMIN_DASHBOARD") },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "الإدارة") },
                            label = { Text("لوحة الإدارة", fontSize = 11.sp, fontWeight = if (currentScreen == "ADMIN_DASHBOARD") FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BinanceYellow,
                                indicatorColor = BinanceYellow,
                                unselectedIconColor = BinanceTextGray,
                                unselectedTextColor = BinanceTextGray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BinanceBgDark)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "HOME" -> ConsumerCatalogScreen(viewModel = viewModel)
                "ORDERS" -> ConsumerOrdersScreen(viewModel = viewModel)
                "WALLET" -> ConsumerWalletScreen(viewModel = viewModel)
                "MERCHANT_DASHBOARD", "MERCHANT_PRODUCTS" -> MerchantDashboardScreen(viewModel = viewModel)
                "PROMOTER_DASHBOARD" -> PromoterDashboardScreen(viewModel = viewModel)
                "ADMIN_DASHBOARD" -> AdminDashboardScreen(viewModel = viewModel)
                else -> ConsumerCatalogScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Switcher / Auth
    if (showAuthModal) {
        WelcomeAndAuthModal(
            viewModel = viewModel,
            onDismiss = { showAuthModal = false }
        )
    }

    // Notifications Dialog
    if (showNotifsModal) {
        AlertDialog(
            onDismissRequest = { showNotifsModal = false },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = { showNotifsModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إشعارات الحساب والعمليات", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BinanceTextWhite)
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Text("لا توجد إشعارات جديدة حالياً", color = BinanceTextGray)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications.size) { index ->
                            val n = notifications[index]
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = BinanceInputDark),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BinanceYellow)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(n.message, fontSize = 12.sp, color = BinanceTextWhite)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

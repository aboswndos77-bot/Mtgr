package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedAdminTab by remember { mutableStateOf(0) }
    val adminTabs = listOf("الرئيسية", "طلبات جيب", "النزاعات", "المستخدمون", "الإعدادات", "سجل التدقيق")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BinanceBgDark)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedAdminTab,
            edgePadding = 12.dp,
            containerColor = BinanceCardDark,
            contentColor = BinanceTextWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                    color = BinanceYellow,
                    height = 2.5.dp
                )
            },
            divider = { HorizontalDivider(color = BinanceBorderDark) }
        ) {
            adminTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedAdminTab == index,
                    onClick = { selectedAdminTab = index },
                    text = {
                        Text(
                            title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedAdminTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedAdminTab == index) BinanceYellow else BinanceTextGray
                        )
                    }
                )
            }
        }

        when (selectedAdminTab) {
            0 -> AdminOverviewTab(viewModel)
            1 -> AdminTopUpsTab(viewModel)
            2 -> AdminDisputesTab(viewModel)
            3 -> AdminUsersTab(viewModel)
            4 -> AdminSettingsTab(viewModel)
            5 -> AdminAuditLogsTab(viewModel)
        }
    }
}

@Composable
fun AdminOverviewTab(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val products by viewModel.allAdminProducts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()

    val platformFees = transactions.filter { it.type == TransactionType.PLATFORM_FEE }.sumOf { it.amount }
    val escrowHeld = orders.filter { it.status == OrderStatus.RESERVED || it.status == OrderStatus.PREPARING || it.status == OrderStatus.OUT_FOR_DELIVERY || it.status == OrderStatus.DELIVERED_PENDING_RELEASE }.sumOf { it.productPrice }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BinanceLogoIcon(size = 22)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مركز إدارة المنصة والسيولة المالية",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextWhite
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BinanceInputDark,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight)
                ) {
                    Text(
                        text = "Admin Hub",
                        color = BinanceYellow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "إيرادات المنصة (3%)",
                    value = formatYer(platformFees),
                    icon = Icons.Default.AccountBalance,
                    iconColor = BinanceYellow,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "أموال الضمان (Escrow)",
                    value = formatYer(escrowHeld),
                    icon = Icons.Default.Lock,
                    iconColor = BinanceGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "إجمالي المستخدمين",
                    value = "${users.size} عضو",
                    icon = Icons.Default.People,
                    iconColor = BinanceTextWhite,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "إجمالي الطلبات",
                    value = "${orders.size} طلب",
                    icon = Icons.Default.ShoppingBag,
                    iconColor = BinanceYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("توزيع الحسابات والأدوار في المنصة:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BinanceYellow)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• المستهلكون: ${users.count { it.role == UserRole.CONSUMER }}", fontSize = 12.sp, color = BinanceTextWhite)
                    Text("• التجار: ${users.count { it.role == UserRole.MERCHANT }}", fontSize = 12.sp, color = BinanceTextWhite)
                    Text("• المروّجون: ${users.count { it.role == UserRole.PROMOTER }}", fontSize = 12.sp, color = BinanceTextWhite)
                    Text("• الإدارة: ${users.count { it.role == UserRole.ADMIN }}", fontSize = 12.sp, color = BinanceTextWhite)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• إجمالي المنتجات المعروضة: ${products.size} منتج", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BinanceGreen)
                }
            }
        }
        item {
            Text(
                text = "معرض المنتجات المنشورة",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextWhite
            )
        }
        items(products, key = { it.productId }) { product ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    ProductImage(
                        imageUrl = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.size(82.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, color = BinanceTextWhite, maxLines = 2)
                        Text("${product.merchantName} • ${product.category}", fontSize = 11.sp, color = BinanceTextGray)
                        Text(formatYer(product.price), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BinanceYellow)
                    }
                }
            }
        }

    }
}

// TopUps Review Tab
@Composable
fun AdminTopUpsTab(viewModel: MainViewModel) {
    val topUps by viewModel.allTopUps.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تدقيق إشعارات تحويلات جيب وإيداع الأرصدة:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }

        if (topUps.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BinanceCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد طلبات إيداع بانتظار التدقيق حالياً", color = BinanceTextGray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(topUps) { req ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (req.type == TopUpType.WALLET_TOPUP) "إيداع رصيد محفظة P2P" else "شحن رصيد إدراج تاجر (100 ر.ي)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BinanceTextWhite
                            )
                            TopUpStatusBadge(req.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("صاحب الطلب: ${req.userName} (${req.userRole.name})", fontSize = 12.sp, color = BinanceTextGray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المبلغ المطلوب:", fontSize = 12.sp, color = BinanceTextGray)
                            Text(formatYer(req.amount), fontWeight = FontWeight.Bold, color = BinanceYellow, fontSize = 15.sp)
                        }
                        Text("رقم العملية في تطبيق جيب: ${req.transferNumber}", fontSize = 12.sp, color = BinanceTextWhite, fontWeight = FontWeight.SemiBold)

                        if (req.status == RequestStatus.PENDING) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.reviewTopUp(req.requestId, isApproved = true, notes = "تمت المطابقة مع كشف حساب جيب")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اعتماد وإيداع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.reviewTopUp(req.requestId, isApproved = false, notes = "رقم عملية غير صحيح أو لم يصل")
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceRed),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("رفض الطلب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Disputes Tab
@Composable
fun AdminDisputesTab(viewModel: MainViewModel) {
    val disputes by viewModel.allDisputes.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "غرفة التحكيم والنزاعات المالية:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }

        if (disputes.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BinanceCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد أي نزاعات مفتوحة حالياً (كافة المعاملات تسير بسلاسة)", color = BinanceTextGray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(disputes) { disp ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نزاع على طلب #${disp.orderId}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BinanceTextWhite)
                            Surface(
                                color = if (disp.status == DisputeStatus.OPEN) BinanceRedBg else BinanceGreenBg,
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, if (disp.status == DisputeStatus.OPEN) BinanceRed else BinanceGreen)
                            ) {
                                Text(
                                    disp.status.name,
                                    color = if (disp.status == DisputeStatus.OPEN) BinanceRed else BinanceGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("مقدّم النزاع: ${disp.openedByName} (${disp.openedByRole.name})", fontSize = 12.sp, color = BinanceTextGray)
                        Text("السبب: ${disp.reason}", fontSize = 12.sp, color = BinanceTextWhite)

                        if (disp.status == DisputeStatus.OPEN) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.resolveDispute(disp.disputeId, refundConsumer = true, resolutionNote = "استرداد كامل المبلغ للمستهلك لعدم ثبوت التسليم")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("استرداد للمستهلك", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        viewModel.resolveDispute(disp.disputeId, refundConsumer = false, resolutionNote = "تم التحقق من صحة إشعار التسليم وجيب، التسوية للتاجر")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("تسوية للتاجر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Users Tab
@Composable
fun AdminUsersTab(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    var freezeUserTarget by remember { mutableStateOf<UserEntity?>(null) }
    var freezeReason by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إدارة المستخدمين والتحكم بالحسابات:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }

        items(users) { user ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BinanceTextWhite)
                        Text("${user.email} • ${user.role.name}", fontSize = 11.sp, color = BinanceTextGray)
                        if (user.isFrozen) {
                            Text("❌ الحساب مجمّد: ${user.freezeReason}", fontSize = 11.sp, color = BinanceRed, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (user.role != UserRole.ADMIN) {
                        if (user.isFrozen) {
                            OutlinedButton(
                                onClick = { viewModel.setAccountFreeze(user.id, freeze = false, reason = "إلغاء التجميد من الإدارة") },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceGreen)
                            ) {
                                Text("إلغاء التجميد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { freezeUserTarget = user },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceRed),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("تجميد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    freezeUserTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { freezeUserTarget = null },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setAccountFreeze(user.id, freeze = true, reason = freezeReason.ifBlank { "مخالفة معايير الاستخدام" })
                        freezeUserTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceRed, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("تأكيد التجميد", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { freezeUserTarget = null }) {
                    Text("إلغاء", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = BinanceRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تجميد حساب: ${user.fullName}", fontWeight = FontWeight.Bold, color = BinanceTextWhite, fontSize = 15.sp)
                }
            },
            text = {
                Column {
                    Text("سيتم منع المستخدم من الشراء أو النشر فوراً مع إمكانية مراجعة الإدارة.", fontSize = 12.sp, color = BinanceTextGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = freezeReason,
                        onValueChange = { freezeReason = it },
                        label = { Text("سبب التجميد", color = BinanceTextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BinanceInputDark,
                            unfocusedContainerColor = BinanceInputDark,
                            focusedBorderColor = BinanceRed,
                            unfocusedBorderColor = BinanceBorderDark,
                            focusedTextColor = BinanceTextWhite,
                            unfocusedTextColor = BinanceTextWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

// Platform Settings Tab
@Composable
fun AdminSettingsTab(viewModel: MainViewModel) {
    val settingsState by viewModel.platformSettings.collectAsState()
    val settings = settingsState ?: PlatformSettingsEntity()

    var platformName by remember(settings) { mutableStateOf(settings.platformName) }
    var jeebAccount by remember(settings) { mutableStateOf(settings.platformJeebAccount) }
    var listingFeeStr by remember(settings) { mutableStateOf(settings.listingFeePerProduct.toString()) }
    var minCommissionStr by remember(settings) { mutableStateOf(settings.minimumPromoterCommission.toString()) }
    var feePercentStr by remember(settings) { mutableStateOf(settings.platformFeePercentage.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("إعدادات المنصة والسياسات المالية:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BinanceTextWhite)
        }

        OutlinedTextField(
            value = platformName,
            onValueChange = { platformName = it },
            label = { Text("اسم المنصة", color = BinanceTextGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceInputDark,
                unfocusedContainerColor = BinanceInputDark,
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = BinanceBorderDark,
                focusedTextColor = BinanceTextWhite,
                unfocusedTextColor = BinanceTextWhite
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = jeebAccount,
            onValueChange = { jeebAccount = it },
            label = { Text("بيانات حساب محفظة جيب الرسمية لاستقبال الشحن", color = BinanceTextGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceInputDark,
                unfocusedContainerColor = BinanceInputDark,
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = BinanceBorderDark,
                focusedTextColor = BinanceTextWhite,
                unfocusedTextColor = BinanceTextWhite
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = listingFeeStr,
            onValueChange = { listingFeeStr = it },
            label = { Text("رسوم إدراج المنتج للتاجر (بالريال اليمني - افتراضي 100)", color = BinanceTextGray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceInputDark,
                unfocusedContainerColor = BinanceInputDark,
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = BinanceBorderDark,
                focusedTextColor = BinanceTextWhite,
                unfocusedTextColor = BinanceTextWhite
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = minCommissionStr,
            onValueChange = { minCommissionStr = it },
            label = { Text("الحد الأدنى لعمولة المروّج (بالريال اليمني - افتراضي 500)", color = BinanceTextGray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceInputDark,
                unfocusedContainerColor = BinanceInputDark,
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = BinanceBorderDark,
                focusedTextColor = BinanceTextWhite,
                unfocusedTextColor = BinanceTextWhite
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = feePercentStr,
            onValueChange = { feePercentStr = it },
            label = { Text("نسبة عمولة المنصة % (افتراضي 3%)", color = BinanceTextGray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BinanceInputDark,
                unfocusedContainerColor = BinanceInputDark,
                focusedBorderColor = BinanceYellow,
                unfocusedBorderColor = BinanceBorderDark,
                focusedTextColor = BinanceTextWhite,
                unfocusedTextColor = BinanceTextWhite
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val updated = settings.copy(
                    platformName = platformName.trim(),
                    platformJeebAccount = jeebAccount.trim(),
                    listingFeePerProduct = listingFeeStr.toLongOrNull() ?: 100L,
                    minimumPromoterCommission = minCommissionStr.toLongOrNull() ?: 500L,
                    platformFeePercentage = feePercentStr.toIntOrNull() ?: 3
                )
                viewModel.updatePlatformSettings(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text("حفظ التغييرات", fontWeight = FontWeight.Bold)
        }
    }
}

// Audit Logs Tab
@Composable
fun AdminAuditLogsTab(viewModel: MainViewModel) {
    val logs by viewModel.allAuditLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("سجل التدقيق الأمني والعمليات المالية (Audit Trail):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BinanceTextWhite)
            }
        }

        if (logs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BinanceCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد سجلات تدقيق مسجلة بعد", color = BinanceTextGray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BinanceYellow)
                            Text(log.actorRole, fontSize = 11.sp, color = BinanceTextGray)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("الهدف: ${log.targetId}", fontSize = 11.sp, color = BinanceTextWhite)
                        Text("السبب / البيان: ${log.reason}", fontSize = 11.sp, color = BinanceTextGray)
                    }
                }
            }
        }
    }
}

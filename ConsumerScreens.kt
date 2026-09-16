package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerCatalogScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }
    var showPurchaseDialog by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf("الكل", "عسل يمني", "بن وقشر", "عطور وبخور", "تراثيات وجنابي")
    val cities = listOf("الكل", "صنعاء", "عدن", "حضرموت", "تعز", "إب")

    val filteredProducts = products.filter { prod ->
        val matchesSearch = prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.description.contains(searchQuery, ignoreCase = true) ||
                prod.merchantName.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCat == "الكل" || prod.category == selectedCat
        val matchesCity = selectedCity == "الكل" || prod.location == selectedCity
        matchesSearch && matchesCat && matchesCity
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BinanceBgDark),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Binance Search Box
        item {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("ابحث في الأسواق، المنتجات، التجار...", color = BinanceTextGray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BinanceYellow) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = BinanceTextGray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BinanceInputDark,
                        unfocusedContainerColor = BinanceInputDark,
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = BinanceBorderDark,
                        focusedTextColor = BinanceTextWhite,
                        unfocusedTextColor = BinanceTextWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // 2. Binance Top Ticker Bar
        item {
            Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                BinanceMarketTickerBar()
            }
        }

        // 3. Binance Quick Actions
        item {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                BinanceQuickActionsGrid(
                    onDepositClick = { viewModel.navigateTo("WALLET") },
                    onOrdersClick = { viewModel.navigateTo("ORDERS") },
                    onPromoClick = { viewModel.navigateTo("PROMOTER_DASHBOARD") },
                    onAuditClick = { viewModel.navigateTo("WALLET") }
                )
            }
        }

        // 4. Market Tabs (Categories)
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("أسواق المنتجات والسلع", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BinanceTextWhite)
                    Text("${filteredProducts.size} منتج نشط", fontSize = 11.sp, color = BinanceTextGray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCat == cat
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) BinanceYellow else BinanceInputDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BinanceYellow else BinanceBorderDark
                            ),
                            modifier = Modifier.clickable { viewModel.selectedCategory.value = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else BinanceTextGray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. City Selector Pills
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(cities) { city ->
                    val isSelected = selectedCity == city
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) BinanceCardDark else Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isSelected) BinanceYellow.copy(alpha = 0.6f) else BinanceBorderDark
                        ),
                        modifier = Modifier.clickable { viewModel.selectedCity.value = city }
                    ) {
                        Text(
                            text = "📍 $city",
                            fontSize = 11.sp,
                            color = if (isSelected) BinanceYellow else BinanceTextGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 6. Products Listing
        if (filteredProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = BinanceTextGray.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد منتجات مطابقة لخيارات البحث",
                            fontSize = 14.sp,
                            color = BinanceTextGray
                        )
                    }
                }
            }
        } else {
            items(filteredProducts, key = { it.productId }) { prod ->
                Box(modifier = Modifier.padding(horizontal = 14.dp)) {
                    ProductCardItem(
                        product = prod,
                        onClick = { selectedProductForDetail = prod },
                        onBuyClick = { showPurchaseDialog = prod }
                    )
                }
            }
        }
    }

    // Product detail modal
    selectedProductForDetail?.let { prod ->
        AlertDialog(
            onDismissRequest = { selectedProductForDetail = null },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        val toBuy = selectedProductForDetail
                        selectedProductForDetail = null
                        showPurchaseDialog = toBuy
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("متابعة الشراء الفوري", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForDetail = null }) {
                    Text("إغلاق", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BinanceLogoIcon(size = 24)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = prod.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = BinanceTextWhite
                    )
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ProductImage(
                        imageUrl = prod.imageUrl,
                        contentDescription = prod.name,
                        modifier = Modifier.fillMaxWidth().height(190.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FreeDeliveryBadge()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = prod.description,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = BinanceTextWhite
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BinanceBorderDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("السعر:", color = BinanceTextGray, fontSize = 13.sp)
                        Text(
                            formatYer(prod.price),
                            fontWeight = FontWeight.Bold,
                            color = BinanceYellow,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("التاجر:", color = BinanceTextGray, fontSize = 13.sp)
                        Text(prod.merchantName, fontWeight = FontWeight.SemiBold, color = BinanceTextWhite)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المدينة:", color = BinanceTextGray, fontSize = 13.sp)
                        Text("📍 ${prod.location}", color = BinanceTextWhite)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عمولة المروّج المكفولة:", color = BinanceTextGray, fontSize = 13.sp)
                        Text("${formatYer(prod.promoterCommission)}", color = BinanceGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // Purchase checkout dialog with Escrow notice
    showPurchaseDialog?.let { prod ->
        PurchaseCheckoutModal(
            product = prod,
            viewModel = viewModel,
            onDismiss = { showPurchaseDialog = null }
        )
    }
}

@Composable
fun ProductCardItem(
    product: ProductEntity,
    onClick: () -> Unit,
    onBuyClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            ProductImage(
                imageUrl = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextWhite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${product.merchantName} • 📍 ${product.location}",
                        fontSize = 11.sp,
                        color = BinanceTextGray
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BinanceInputDark,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight)
                ) {
                    Text(
                        text = product.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BinanceYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.description,
                fontSize = 12.sp,
                color = BinanceTextGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FreeDeliveryBadge()
                if (product.promoterCommission > 0L) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BinanceGreenBg,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceGreen.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "+${formatYer(product.promoterCommission)} عمولة",
                            color = BinanceGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BinanceBorderDark)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "السعر المقدر",
                        fontSize = 10.sp,
                        color = BinanceTextGray
                    )
                    Text(
                        text = formatYer(product.price),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BinanceYellow
                    )
                }

                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("شراء فوري P2P", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}


@Composable
fun PurchaseCheckoutModal(
    product: ProductEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val wallet by viewModel.userWallet.collectAsState()
    val available = wallet?.availableBalance ?: 0L
    val isBalanceSufficient = available >= product.price
    var promoCodeInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BinanceCardDark,
        confirmButton = {
            if (isBalanceSufficient) {
                Button(
                    onClick = {
                        viewModel.purchaseProduct(product.productId, promoCodeInput.ifBlank { null })
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("تأكيد الشراء وحجز الضمان P2P", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        onDismiss()
                        viewModel.navigateTo("WALLET")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("إيداع رصيد عبر جيب أولاً", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = BinanceTextGray)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BinanceLogoIcon(size = 22)
                Spacer(modifier = Modifier.width(8.dp))
                Text("طلب فوري وحجز الضمان Escrow", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BinanceTextWhite)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "المنتج: ${product.name}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = BinanceTextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("القيمة الإجمالية:", color = BinanceTextGray, fontSize = 13.sp)
                    Text(
                        formatYer(product.price),
                        fontWeight = FontWeight.ExtraBold,
                        color = BinanceYellow,
                        fontSize = 16.sp
                    )
                }
                FreeDeliveryBadge(modifier = Modifier.padding(vertical = 6.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // Wallet balance check
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isBalanceSufficient) BinanceGreenBg else BinanceRedBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBalanceSufficient) BinanceGreen.copy(alpha = 0.3f) else BinanceRed.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("رصيدك المتاح في المحفظة:", fontSize = 12.sp, color = BinanceTextWhite)
                            Text(
                                formatYer(available),
                                fontWeight = FontWeight.Bold,
                                color = if (isBalanceSufficient) BinanceGreen else BinanceRed
                            )
                        }
                        if (!isBalanceSufficient) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ رصيدك غير كافٍ. يرجى إيداع المبلغ المطلوب عبر تطبيق جيب للمتابعة.",
                                fontSize = 11.sp,
                                color = BinanceRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional promo code
                OutlinedTextField(
                    value = promoCodeInput,
                    onValueChange = { promoCodeInput = it },
                    label = { Text("كود خصم / مروّج إن وجد (مثل RYD-78241)", color = BinanceTextGray, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BinanceInputDark,
                        unfocusedContainerColor = BinanceInputDark,
                        focusedBorderColor = BinanceYellow,
                        unfocusedBorderColor = BinanceBorderDark,
                        focusedTextColor = BinanceTextWhite,
                        unfocusedTextColor = BinanceTextWhite
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Escrow explanation notice
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BinanceInputDark,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = BinanceYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حماية الضمان المالي 100%: يتم حجز المبلغ (${formatYer(product.price)}) في محفظة Escrow آمنة ولن تُحرر للتاجر إلا بعد استلامك للمنتج وتأكيدك بنفسك.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = BinanceTextGray
                        )
                    }
                }
            }
        }
    )
}

// Consumer Wallet Screen
@Composable
fun ConsumerWalletScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.userWallet.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val allTopUps by viewModel.allTopUps.collectAsState()
    val user = viewModel.currentUser.collectAsState().value
    val settings by viewModel.platformSettings.collectAsState()

    var showTopUpModal by remember { mutableStateOf(false) }
    var topUpReceiptUri by remember { mutableStateOf<Uri?>(null) }
    val topUpReceiptPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { topUpReceiptUri = it }
    var hideBalance by remember { mutableStateOf(false) }

    val myTopUps = allTopUps.filter { it.userId == user?.id }
    val available = wallet?.availableBalance ?: 0L
    val held = wallet?.heldBalance ?: 0L
    val totalEstimated = available + held

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BinanceBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Binance Balance Overview Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "إجمالي الرصيد المقدر",
                                color = BinanceTextGray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { hideBalance = !hideBalance },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (hideBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = BinanceTextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BinanceInputDark,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight)
                        ) {
                            Text(
                                text = "YER • ر.ي",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BinanceYellow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hideBalance) "••••••••" else formatYer(totalEstimated),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BinanceTextWhite
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BinanceBorderDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "الرصيد الفوري (متاح للشراء)",
                                color = BinanceTextGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hideBalance) "••••" else formatYer(available),
                                color = BinanceGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "المحجوز بالضمان (Escrow)",
                                color = BinanceTextGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hideBalance) "••••" else formatYer(held),
                                color = BinanceYellow,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Binance Quick Wallet Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showTopUpModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إيداع جيب", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo("ORDERS") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BinanceTextWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderHighlight),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.SyncAlt, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الطلبات والضمان", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Official Platform Jeeb account info
        item {
            val jeebAcct = settings?.platformJeebAccount ?: "لم يتم ضبط حساب جيب للمنصة بعد"
            JeebAccountCard(accountNumber = jeebAcct)
        }

        // Top up requests section
        if (myTopUps.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "طلبات الإيداع عبر جيب",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextWhite
                    )
                    Text("${myTopUps.size} عملية", fontSize = 11.sp, color = BinanceTextGray)
                }
            }
            items(myTopUps) { req ->
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
                        Column {
                            Text(
                                text = "إيداع: ${formatYer(req.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BinanceTextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "رقم التحويل في جيب: ${req.transferNumber}",
                                fontSize = 11.sp,
                                color = BinanceTextGray
                            )
                        }
                        TopUpStatusBadge(req.status)
                    }
                }
            }
        }

        // Transactions history
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل العمليات المالية (Ledger)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
                Text("${transactions.size} حركة", fontSize = 11.sp, color = BinanceTextGray)
            }
        }

        if (transactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BinanceCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد حركات مالية مسجلة بعد",
                            fontSize = 13.sp,
                            color = BinanceTextGray
                        )
                    }
                }
            }
        } else {
            items(transactions) { tx ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = when (tx.type) {
                                    TransactionType.TOP_UP, TransactionType.REFUND, TransactionType.PROMOTER_COMMISSION, TransactionType.MERCHANT_PAYOUT -> BinanceGreenBg
                                    TransactionType.ESCROW_HOLD -> BinanceCardDark
                                    else -> BinanceRedBg
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (tx.type) {
                                            TransactionType.TOP_UP -> Icons.Default.ArrowDownward
                                            TransactionType.ESCROW_HOLD -> Icons.Default.Lock
                                            TransactionType.REFUND -> Icons.Default.Undo
                                            TransactionType.MERCHANT_PAYOUT -> Icons.Default.Check
                                            else -> Icons.Default.ArrowUpward
                                        },
                                        contentDescription = null,
                                        tint = when (tx.type) {
                                            TransactionType.TOP_UP, TransactionType.REFUND, TransactionType.PROMOTER_COMMISSION, TransactionType.MERCHANT_PAYOUT -> BinanceGreen
                                            TransactionType.ESCROW_HOLD -> BinanceYellow
                                            else -> BinanceRed
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tx.note.ifBlank { tx.type.name },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = BinanceTextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "TX-${tx.transactionId.take(12)}",
                                    fontSize = 11.sp,
                                    color = BinanceTextGray
                                )
                            }
                        }
                        Text(
                            text = when (tx.type) {
                                TransactionType.TOP_UP, TransactionType.REFUND, TransactionType.PROMOTER_COMMISSION, TransactionType.MERCHANT_PAYOUT -> "+${formatYer(tx.amount)}"
                                TransactionType.ESCROW_HOLD -> "🔒 ${formatYer(tx.amount)}"
                                else -> "-${formatYer(tx.amount)}"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when (tx.type) {
                                TransactionType.TOP_UP, TransactionType.REFUND, TransactionType.PROMOTER_COMMISSION, TransactionType.MERCHANT_PAYOUT -> BinanceGreen
                                TransactionType.ESCROW_HOLD -> BinanceYellow
                                else -> BinanceRed
                            }
                        )
                    }
                }
            }
        }
    }

    // Top up dialog
    if (showTopUpModal) {
        var topUpAmountStr by remember { mutableStateOf("10000") }
        var transferNo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTopUpModal = false },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        val amt = topUpAmountStr.toLongOrNull() ?: 0L
                        if (amt < 500L) {
                            viewModel.showMessage("الحد الأدنى للإيداع 500 ريال")
                            return@Button
                        }
                        if (transferNo.isBlank()) {
                            viewModel.showMessage("يرجى إدخال رقم العملية في تطبيق جيب")
                            return@Button
                        }
                        if (topUpReceiptUri == null) {
                            viewModel.showMessage("يرجى إرفاق صورة إيصال التحويل")
                            return@Button
                        }
                        viewModel.submitTopUp(amt, transferNo, TopUpType.WALLET_TOPUP, topUpReceiptUri)
                        showTopUpModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("إرسال طلب الإيداع للتدقيق", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpModal = false }) {
                    Text("إلغاء", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BinanceLogoIcon(size = 22)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إيداع رصيد عبر تطبيق جيب", fontWeight = FontWeight.Bold, color = BinanceTextWhite, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "1. قم بالتحويل من حسابك في تطبيق جيب إلى حساب المنصة الموضح أدناه:",
                        fontSize = 12.sp,
                        color = BinanceTextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JeebAccountCard(
                        accountNumber = settings?.platformJeebAccount ?: "لم يتم ضبط حساب جيب للمنصة بعد"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "2. أدخل تفاصيل التحويل لإيداع الرصيد بعد تدقيق الإدارة:",
                        fontSize = 12.sp,
                        color = BinanceTextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = topUpAmountStr,
                        onValueChange = { topUpAmountStr = it },
                        label = { Text("مبلغ الإيداع (بالريال اليمني)", color = BinanceTextGray) },
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
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = transferNo,
                        onValueChange = { transferNo = it },
                        label = { Text("رقم عملية التحويل من جيب", color = BinanceTextGray) },
                        placeholder = { Text("مثلاً: 98712345", color = BinanceTextGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BinanceInputDark,
                            unfocusedContainerColor = BinanceInputDark,
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = BinanceBorderDark,
                            focusedTextColor = BinanceTextWhite,
                            unfocusedTextColor = BinanceTextWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(onClick = { topUpReceiptPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (topUpReceiptUri == null) "إرفاق إيصال جيب *" else "تم إرفاق الإيصال ✓")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BinanceInputDark,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سيتم تدقيق الإشعار وتأكيد الرصيد في محفظتك تلقائياً وبسرعة فائقة.",
                                fontSize = 11.sp,
                                color = BinanceTextGray
                            )
                        }
                    }
                }
            }
        )
    }
}


// Consumer Orders Screen
@Composable
fun ConsumerOrdersScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.userOrders.collectAsState()
    var releaseOrderTarget by remember { mutableStateOf<OrderEntity?>(null) }
    var disputeOrderTarget by remember { mutableStateOf<OrderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BinanceBgDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BinanceLogoIcon(size = 22)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "طلبات الضمان المالي P2P Escrow",
                    fontSize = 16.sp,
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
                    text = "${orders.size} طلب",
                    fontSize = 11.sp,
                    color = BinanceYellow,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = BinanceTextGray.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ليس لديك أي طلبات نشطة حالياً",
                        fontSize = 14.sp,
                        color = BinanceTextGray
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.navigateTo("HOME") },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصفح الأسواق والمنتجات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(orders, key = { it.orderId }) { order ->
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
                                    text = "طلب P2P #${order.orderId}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BinanceTextGray
                                )
                                OrderStatusBadge(order.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = order.productName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "التاجر: ${order.merchantName}",
                                    fontSize = 12.sp,
                                    color = BinanceTextGray
                                )
                                Text(
                                    text = formatYer(order.productPrice),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BinanceYellow
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FreeDeliveryBadge()

                            // Delivery proof note if available
                            if (order.deliveryNotes != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BinanceInputDark,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = BinanceGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "إشعار التوصيل: ${order.deliveryNotes}",
                                            fontSize = 11.sp,
                                            color = BinanceTextWhite
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BinanceBorderDark)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (order.status == OrderStatus.DELIVERED_PENDING_RELEASE) {
                                    Button(
                                        onClick = { releaseOrderTarget = order },
                                        colors = ButtonDefaults.buttonColors(containerColor = BinanceGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تحرير الطلب والتسوية P2P", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                } else {
                                    Text(
                                        text = if (order.status == OrderStatus.COMPLETED) "تم إغلاق وتسوية الطلب بنجاح" else "الضمان المالي محفوظ في Escrow",
                                        fontSize = 11.sp,
                                        color = BinanceTextGray
                                    )
                                }

                                if (order.status != OrderStatus.COMPLETED && order.status != OrderStatus.DISPUTED && order.status != OrderStatus.REFUNDED) {
                                    TextButton(
                                        onClick = { disputeOrderTarget = order },
                                        colors = ButtonDefaults.textButtonColors(contentColor = BinanceRed)
                                    ) {
                                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = BinanceRed, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فتح نزاع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Release settlement modal
    releaseOrderTarget?.let { order ->
        var transferNo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { releaseOrderTarget = null },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        if (transferNo.isBlank()) {
                            viewModel.showMessage("يرجى إدخال رقم العملية في تطبيق جيب")
                            return@Button
                        }
                        viewModel.releaseOrder(order.orderId, transferNo)
                        releaseOrderTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("تأكيد التحرير وتسوية الحصص P2P", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { releaseOrderTarget = null }) {
                    Text("إلغاء", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BinanceLogoIcon(size = 22)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تحرير الضمان المالي وتسوية الحصص", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BinanceTextWhite)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "قام التاجر بتسليم طلبك بنجاح. يرجى تحويل مبلغ الطلب عبر تطبيق جيب إلى حساب التاجر الموضح أدناه ثم إدخال رقم العملية لتحرير الضمان:",
                        fontSize = 12.sp,
                        color = BinanceTextGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    JeebAccountCard(
                        accountNumber = order.merchantJeebAccount.ifBlank { "772223344 (التاجر: ${order.merchantName})" },
                        title = "حساب محفظة جيب للتاجر (${order.merchantName})"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("قيمة الطلب:", color = BinanceTextGray, fontSize = 13.sp)
                        Text(
                            formatYer(order.productPrice),
                            fontWeight = FontWeight.Bold,
                            color = BinanceYellow,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = transferNo,
                        onValueChange = { transferNo = it },
                        label = { Text("رقم عملية التحويل من جيب", color = BinanceTextGray) },
                        placeholder = { Text("مثلاً: 88771122", color = BinanceTextGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BinanceInputDark,
                            unfocusedContainerColor = BinanceInputDark,
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = BinanceBorderDark,
                            focusedTextColor = BinanceTextWhite,
                            unfocusedTextColor = BinanceTextWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BinanceInputDark,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("توزيع الحصص الآلي عند التحرير:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BinanceYellow)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• صافي التاجر: ${formatYer(order.merchantNet)}", fontSize = 11.sp, color = BinanceTextWhite)
                            Text("• رسوم المنصة (3%): ${formatYer(order.platformFee)}", fontSize = 11.sp, color = BinanceTextWhite)
                            if (order.promoterCommission > 0L) {
                                Text("• عمولة المروّج: ${formatYer(order.promoterCommission)}", fontSize = 11.sp, color = BinanceGreen)
                            }
                        }
                    }
                }
            }
        )
    }

    // Dispute dialog
    disputeOrderTarget?.let { order ->
        var disputeReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { disputeOrderTarget = null },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        if (disputeReason.isBlank()) {
                            viewModel.showMessage("يرجى كتابة سبب النزاع بالتفصيل")
                            return@Button
                        }
                        viewModel.openDispute(order.orderId, disputeReason)
                        disputeOrderTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceRed, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("رفع النزاع للإدارة للتحكيم", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { disputeOrderTarget = null }) {
                    Text("إلغاء", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = BinanceRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فتح نزاع وتحكيم على الطلب #${order.orderId}", fontWeight = FontWeight.Bold, color = BinanceTextWhite, fontSize = 15.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "سيتم تجميد تسوية الطلب ومراجعة كافة إثباتات التوصيل والتحويلات بواسطة إدارة المنصة المستقلة.",
                        fontSize = 12.sp,
                        color = BinanceTextGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = disputeReason,
                        onValueChange = { disputeReason = it },
                        label = { Text("سبب النزاع والمشكلة بالتفصيل", color = BinanceTextGray) },
                        placeholder = { Text("مثال: لم أستلم المنتج كاملاً، المنتج غير مطابق للمواصفات...", color = BinanceTextGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BinanceInputDark,
                            unfocusedContainerColor = BinanceInputDark,
                            focusedBorderColor = BinanceRed,
                            unfocusedBorderColor = BinanceBorderDark,
                            focusedTextColor = BinanceTextWhite,
                            unfocusedTextColor = BinanceTextWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        )
    }
}


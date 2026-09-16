package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
fun MerchantDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.userWallet.collectAsState()
    val merchantProducts by viewModel.merchantProducts.collectAsState()
    val orders by viewModel.userOrders.collectAsState()
    val settings by viewModel.platformSettings.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var showTopUpListingDialog by remember { mutableStateOf(false) }
    var deliveryProofTargetOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var productImageUri by remember { mutableStateOf<Uri?>(null) }
    var listingReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var deliveryProofUri by remember { mutableStateOf<Uri?>(null) }

    val productImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { productImageUri = it }
    val listingReceiptPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { listingReceiptUri = it }
    val deliveryProofPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { deliveryProofUri = it }

    val listingBal = wallet?.listingBalance ?: 0L

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BinanceBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Merchant Financials Card (Binance Wallet Style)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BinanceLogoIcon(size = 20)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حساب التاجر والسيولة (Merchant Hub)",
                                color = BinanceTextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BinanceInputDark,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight)
                        ) {
                            Text(
                                text = "P2P Merchant",
                                color = BinanceYellow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "رصيد العرض والإدراج",
                        color = BinanceTextGray,
                        fontSize = 11.sp
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatYer(listingBal),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BinanceYellow
                        )
                    }
                    Text(
                        text = "رسوم إدراج المنتج: 100 ر.ي (تخصم تلقائياً من رصيد العرض)",
                        color = BinanceTextGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BinanceBorderDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("أرباح المبيعات المحررة:", color = BinanceTextGray, fontSize = 11.sp)
                            Text(
                                text = formatYer(wallet?.availableBalance ?: 0L),
                                color = BinanceGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Button(
                            onClick = { showTopUpListingDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("شحن رصيد العرض عبر جيب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Action Bar: Add product & Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "منتجات المتجر المعروضة (${merchantProducts.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextWhite
                    )
                }
                Button(
                    onClick = { showAddProductDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إدراج منتج (100 ر.ي)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Orders Needing Merchant Action
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "طلبات بانتظار الشحن والتسليم:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }

        val activeOrders = orders.filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.REFUNDED }
        if (activeOrders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BinanceCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لا توجد طلبات شحن معلقة حالياً",
                            fontSize = 12.sp,
                            color = BinanceTextGray
                        )
                    }
                }
            }
        } else {
            items(activeOrders) { order ->
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
                            Text("طلب #${order.orderId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BinanceTextGray)
                            OrderStatusBadge(order.status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(order.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BinanceTextWhite)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "العميل: ${order.consumerName}",
                                fontSize = 12.sp,
                                color = BinanceTextGray
                            )
                            Text(
                                text = "صافي أرباحك: ${formatYer(order.merchantNet)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BinanceGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.status == OrderStatus.RESERVED) {
                                Button(
                                    onClick = { viewModel.merchantAdvanceOrder(order.orderId, OrderStatus.PREPARING) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("بدء التجهيز", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (order.status == OrderStatus.PREPARING) {
                                Button(
                                    onClick = { viewModel.merchantAdvanceOrder(order.orderId, OrderStatus.OUT_FOR_DELIVERY) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تسليم للمندوب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (order.status == OrderStatus.OUT_FOR_DELIVERY || order.status == OrderStatus.PREPARING) {
                                Button(
                                    onClick = { deliveryProofTargetOrder = order },
                                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("رفع إثبات التسليم", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // List of merchant's products
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "قائمة الأصول والمنتجات المعروضة:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }

        if (merchantProducts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BinanceCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لم تقم بإدراج أي منتجات بعد. اضغط على 'إدراج منتج' للبدء.",
                            fontSize = 12.sp,
                            color = BinanceTextGray
                        )
                    }
                }
            }
        } else {
            items(merchantProducts) { prod ->
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
                            Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BinanceTextWhite)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "السعر: ${formatYer(prod.price)} • عمولة المروّج: ${formatYer(prod.promoterCommission)}",
                                fontSize = 12.sp,
                                color = BinanceYellow
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "المخزون: ${prod.quantity} قطعة • 📍 ${prod.location}",
                                fontSize = 11.sp,
                                color = BinanceTextGray
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BinanceGreenBg,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceGreen)
                        ) {
                            Text(
                                text = prod.status.name,
                                fontSize = 10.sp,
                                color = BinanceGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Product Modal (Binance Asset Listing Style)
    if (showAddProductDialog) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var priceStr by remember { mutableStateOf("12000") }
        var commissionStr by remember { mutableStateOf("500") }
        var category by remember { mutableStateOf("عسل سدر دوعني") }
        var quantityStr by remember { mutableStateOf("10") }
        var location by remember { mutableStateOf("صنعاء") }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceStr.toLongOrNull() ?: 0L
                        val comm = commissionStr.toLongOrNull() ?: 0L
                        val qty = quantityStr.toIntOrNull() ?: 1

                        if (name.isBlank() || desc.isBlank()) {
                            viewModel.showMessage("يرجى ملء اسم ووصف المنتج")
                            return@Button
                        }
                        if (comm < 500L) {
                            viewModel.showMessage("عمولة المروّج يجب ألا تقل عن 500 ريال يمني.")
                            return@Button
                        }
                        if (listingBal < 100L) {
                            viewModel.showMessage("رصيد العرض غير كافٍ. يلزم 100 ريال يمني لنشر المنتج.")
                            return@Button
                        }

                        if (productImageUri == null) {
                            viewModel.showMessage("يرجى اختيار صورة حقيقية للمنتج")
                            return@Button
                        }
                        viewModel.addProduct(
                            name = name,
                            description = desc,
                            price = price,
                            promoterCommission = comm,
                            category = category,
                            quantity = qty,
                            location = location,
                            imageUri = productImageUri
                        )
                        showAddProductDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("إدراج المنتج فوراً (خصم 100 ر.ي)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("إلغاء", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BinanceLogoIcon(size = 22)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إدراج منتج جديد في المتجر", fontWeight = FontWeight.Bold, color = BinanceTextWhite, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedButton(onClick = { deliveryProofPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (deliveryProofUri == null) "اختيار صورة إثبات التسليم *" else "تم اختيار إثبات التسليم ✓")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Listing fee reminder banner
                    Surface(
                        color = BinanceInputDark,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceBorderHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "رسوم الإدراج: 100 ر.ي تخصم من رصيد العرض (المتاح: ${formatYer(listingBal)})",
                                fontSize = 11.sp,
                                color = BinanceYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المنتج / السلعة", color = BinanceTextGray) },
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
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("الوصف والمميزات", color = BinanceTextGray) },
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
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("السعر بالريال اليمني (YER)", color = BinanceTextGray) },
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

                    // Mandatory promoter commission rule >= 500
                    OutlinedTextField(
                        value = commissionStr,
                        onValueChange = { commissionStr = it },
                        label = { Text("عمولة المروّج (الحد الأدنى 500 ر.ي)", color = BinanceTextGray) },
                        supportingText = {
                            Text(
                                text = "شرط إلزامي: عمولة المروّج يجب ألا تقل عن 500 ريال يمني.",
                                color = if ((commissionStr.toLongOrNull() ?: 0L) < 500L) BinanceRed else BinanceGreen,
                                fontSize = 11.sp
                            )
                        },
                        isError = (commissionStr.toLongOrNull() ?: 0L) < 500L,
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
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("القسم (عسل يمني، بن وقشر، عطور، هدايا)", color = BinanceTextGray) },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("الكمية", color = BinanceTextGray) },
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
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("المدينة", color = BinanceTextGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BinanceInputDark,
                                unfocusedContainerColor = BinanceInputDark,
                                focusedBorderColor = BinanceYellow,
                                unfocusedBorderColor = BinanceBorderDark,
                                focusedTextColor = BinanceTextWhite,
                                unfocusedTextColor = BinanceTextWhite
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { productImagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (productImageUri == null) "اختيار صورة المنتج *" else "تم اختيار صورة المنتج ✓")
                    }
                    if (productImageUri != null) {
                        Text("سيتم رفع الصورة إلى Firebase Storage عند النشر.", color = BinanceGreen, fontSize = 11.sp)
                    }
                }
            }
        )
    }

    // Top up listing balance via Jeeb modal
    if (showTopUpListingDialog) {
        var topUpAmtStr by remember { mutableStateOf("1000") }
        var transferNo by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showTopUpListingDialog = false },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        val amt = topUpAmtStr.toLongOrNull() ?: 0L
                        if (amt < 500L) {
                            viewModel.showMessage("الحد الأدنى للشحن 500 ريال (يكفي لعرض 5 منتجات)")
                            return@Button
                        }
                        if (transferNo.isBlank()) {
                            viewModel.showMessage("يرجى إدخال رقم العملية في تطبيق جيب")
                            return@Button
                        }
                        if (listingReceiptUri == null) {
                            viewModel.showMessage("يرجى إرفاق صورة إيصال التحويل")
                            return@Button
                        }
                        viewModel.submitTopUp(amt, transferNo, TopUpType.LISTING_FEE_TOPUP, listingReceiptUri)
                        showTopUpListingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("إرسال إشعار الشحن للتدقيق", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTopUpListingDialog = false }) {
                    Text("إلغاء", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BinanceLogoIcon(size = 22)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("شحن رصيد العرض عبر جيب", fontWeight = FontWeight.Bold, color = BinanceTextWhite, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "التاجر يدفع 100 ريال يمني لكل قطعة يتم عرضها في المتجر. قم بالتحويل إلى حساب المنصة:",
                        fontSize = 12.sp,
                        color = BinanceTextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JeebAccountCard(
                        accountNumber = settings?.platformJeebAccount ?: "لم يتم ضبط حساب جيب للمنصة بعد"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = topUpAmtStr,
                        onValueChange = { topUpAmtStr = it },
                        label = { Text("مبلغ الشحن (مثلاً 1000 ر.ي = 10 منتجات)", color = BinanceTextGray) },
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
                        label = { Text("رقم عملية التحويل في تطبيق جيب", color = BinanceTextGray) },
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
                    OutlinedButton(onClick = { listingReceiptPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (listingReceiptUri == null) "إرفاق إيصال جيب *" else "تم إرفاق الإيصال ✓")
                    }
                }
            }
        )
    }

    // Delivery proof upload modal
    deliveryProofTargetOrder?.let { order ->
        var deliveryNotes by remember { mutableStateOf("تم تسليم المنتج للعميل يداً بيد بحالة ممتازة ومطابق للمواصفات") }

        AlertDialog(
            onDismissRequest = { deliveryProofTargetOrder = null },
            containerColor = BinanceCardDark,
            confirmButton = {
                Button(
                    onClick = {
                        if (deliveryProofUri == null) {
                            viewModel.showMessage("يرجى اختيار صورة إثبات التسليم")
                            return@Button
                        }
                        viewModel.submitDeliveryProof(order.orderId, deliveryNotes, deliveryProofUri)
                        deliveryProofTargetOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BinanceGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("تأكيد ورفع إثبات التسليم", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deliveryProofTargetOrder = null }) {
                    Text("إلغاء", color = BinanceTextGray)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = BinanceGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إثبات تسليم الطلب #${order.orderId}", fontWeight = FontWeight.Bold, color = BinanceTextWhite, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    OutlinedButton(onClick = { deliveryProofPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (deliveryProofUri == null) "اختيار صورة إثبات التسليم *" else "تم اختيار إثبات التسليم ✓")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "عند رفع إثبات التسليم، سيتم تحويل حالة الطلب إلى 'تم التسليم - بانتظار التحرير' وإشعار العميل لتحرير الضمان والتسوية عبر جيب.",
                        fontSize = 12.sp,
                        color = BinanceTextGray
                    )
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
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = BinanceGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تم التقاط وتوثيق صورة إثبات الاستلام والتسليم.", fontSize = 11.sp, color = BinanceTextWhite)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = deliveryNotes,
                        onValueChange = { deliveryNotes = it },
                        label = { Text("ملاحظات المندوب والتسليم", color = BinanceTextGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BinanceInputDark,
                            unfocusedContainerColor = BinanceInputDark,
                            focusedBorderColor = BinanceGreen,
                            unfocusedBorderColor = BinanceBorderDark,
                            focusedTextColor = BinanceTextWhite,
                            unfocusedTextColor = BinanceTextWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        )
    }
}


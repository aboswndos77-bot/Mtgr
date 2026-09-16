package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PromoterDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val wallet by viewModel.userWallet.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val orders by viewModel.userOrders.collectAsState()
    val context = LocalContext.current

    val promoCode = user?.promoCode ?: "RYD-78241"
    val commissionTxs = transactions.filter { it.type == TransactionType.PROMOTER_COMMISSION }
    val totalEarned = commissionTxs.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BinanceBgDark)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Promoter Code Binance Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderHighlight),
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
                                text = "برنامج الإحالة والعمولات",
                                color = BinanceYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Surface(
                            color = BinanceGreenBg,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceGreen)
                        ) {
                            Text(
                                text = "عمولة مضمونة ≥ 500 ر.ي",
                                color = BinanceGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "كود الإحالة الحصري الخاص بك (Referral Code):",
                        color = BinanceTextGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = BinanceInputDark,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = promoCode,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BinanceYellow,
                                letterSpacing = 2.sp
                            )
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("PromoCode", promoCode))
                                    viewModel.showMessage("تم نسخ كود الإحالة: $promoCode")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "امنح هذا الكود للمشترين ليحصلوا على توصيل مجاني فوري وتكسب أنت عمولة ترويج فورية ومباشرة لمحفظتك عند تسليم الطلب.",
                        color = BinanceTextGray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Stats cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "إجمالي العمولات المستلمة",
                    value = formatYer(totalEarned),
                    icon = Icons.Default.MonetizationOn,
                    iconColor = BinanceGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الرصيد المتاح للسحب",
                    value = formatYer(wallet?.availableBalance ?: 0L),
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = BinanceYellow,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Products with highest commissions
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "منتجات مقترحة للترويج بعائد عمولة مرتفع:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }

        items(products) { prod ->
            // الصور الحقيقية للمنتجات القادمة من Firebase Storage

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                ProductImage(
                    imageUrl = prod.imageUrl,
                    contentDescription = prod.name,
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BinanceTextWhite)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("سعر البيع: ${formatYer(prod.price)}", fontSize = 12.sp, color = BinanceTextGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = BinanceGreenBg,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, BinanceGreen)
                        ) {
                            Text(
                                text = "عمولتك: +${formatYer(prod.promoterCommission)}",
                                color = BinanceGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val shareText = "اشتري '${prod.name}' بتوصيل مجاني عبر المتجر اليمني باستخدام كود الترويج الخاص بي: $promoCode"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ShareProduct", shareText))
                            viewModel.showMessage("تم نسخ رابط الترويج مع كودك إلى الحافظة بنجاح!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مشاركة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Commission ledger
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "سجل العمولات والأرباح المحققة:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }

        if (commissionTxs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BinanceCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "لم يتم تسجيل أي عمولات بعد. شارك كودك مع المشترين لتبدأ الأرباح!",
                            fontSize = 12.sp,
                            color = BinanceTextGray
                        )
                    }
                }
            }
        } else {
            items(commissionTxs) { tx ->
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
                            Text(tx.note, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = BinanceTextWhite)
                            Text("معاملة #${tx.transactionId}", fontSize = 10.sp, color = BinanceTextGray)
                        }
                        Text(
                            text = "+${formatYer(tx.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BinanceGreen
                        )
                    }
                }
            }
        }
    }
}

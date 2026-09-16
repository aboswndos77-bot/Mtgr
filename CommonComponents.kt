package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.RequestStatus
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

fun formatYer(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "${formatter.format(amount)} ر.ي"
}

@Composable
fun BinanceLogoIcon(modifier: Modifier = Modifier, size: Int = 34) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BinanceYellow),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CurrencyBitcoin,
            contentDescription = "Binance Style Logo",
            tint = Color.Black,
            modifier = Modifier.size((size * 0.65).dp)
        )
    }
}

@Composable
fun FreeDeliveryBadge(modifier: Modifier = Modifier) {
    Surface(
        color = BinanceGreenBg,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BinanceGreen.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "توصيل مجاني فوري",
                tint = BinanceGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "توصيل مجاني 0 ر.ي",
                color = BinanceGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    val (label, bg, fg, border) = when (status) {
        OrderStatus.RESERVED -> Quad("حجز Escrow P2P", Color(0xFF2E2713), BinanceYellow, BinanceYellow.copy(alpha = 0.5f))
        OrderStatus.PREPARING -> Quad("قيد التجهيز", Color(0xFF1E293B), Color(0xFF38BDF8), Color(0xFF38BDF8).copy(alpha = 0.4f))
        OrderStatus.OUT_FOR_DELIVERY -> Quad("خرج للتوصيل", Color(0xFF132D24), BinanceGreen, BinanceGreen.copy(alpha = 0.4f))
        OrderStatus.DELIVERED_PENDING_RELEASE -> Quad("بانتظار تأكيد التحرير", Color(0xFF2E2713), BinanceYellowLight, BinanceYellowLight.copy(alpha = 0.5f))
        OrderStatus.RELEASED -> Quad("تم التحرير والتسوية", Color(0xFF132D24), BinanceGreen, BinanceGreen.copy(alpha = 0.4f))
        OrderStatus.COMPLETED -> Quad("مكتمل ومغلق", Color(0xFF132D24), BinanceGreen, BinanceGreen.copy(alpha = 0.4f))
        OrderStatus.DISPUTED -> Quad("نزاع P2P مفتوح", Color(0xFF32181C), BinanceRed, BinanceRed.copy(alpha = 0.5f))
        OrderStatus.CANCELLED -> Quad("ملغي", Color(0xFF22262E), BinanceTextGray, BinanceBorderDark)
        OrderStatus.REFUNDED -> Quad("مسترد للمحفظة", Color(0xFF311C28), Color(0xFFF472B6), Color(0xFFF472B6).copy(alpha = 0.4f))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun TopUpStatusBadge(status: RequestStatus) {
    val (label, bg, fg) = when (status) {
        RequestStatus.PENDING -> Triple("قيد التدقيق (جيب)", Color(0xFF2E2713), BinanceYellow)
        RequestStatus.APPROVED -> Triple("ناجح ومودع ✓", Color(0xFF132D24), BinanceGreen)
        RequestStatus.REJECTED -> Triple("مرفوض ✕", Color(0xFF32181C), BinanceRed)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, fg.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun JeebAccountCard(
    accountNumber: String,
    title: String = "حساب الإيداع الرسمي عبر محفظة جيب (P2P)",
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderHighlight),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JeebBrandBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = BinanceTextGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = accountNumber,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = BinanceYellow
                )
            }
            Surface(
                color = BinanceInputDark,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "جيب Jeeb",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JeebBrandAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BinanceCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = BinanceTextGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
            }
        }
    }
}

// Binance Style Top Market Ticker Banner
@Composable
fun BinanceMarketTickerBar(modifier: Modifier = Modifier) {
    Surface(
        color = BinanceCardDark,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BinanceBorderDark),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ticker 1: Escrow P2P
            Column(horizontalAlignment = Alignment.Start) {
                Text("Escrow / P2P", fontSize = 11.sp, color = BinanceTextGray, fontWeight = FontWeight.SemiBold)
                Text("ضمان 100%", fontSize = 12.sp, color = BinanceGreen, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(BinanceBorderDark))

            // Ticker 2: Free Delivery
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("الشحن والتوصيل", fontSize = 11.sp, color = BinanceTextGray, fontWeight = FontWeight.SemiBold)
                Text("مجاني بالكامل", fontSize = 12.sp, color = BinanceYellow, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(BinanceBorderDark))

            // Ticker 3: Promoter Commission
            Column(horizontalAlignment = Alignment.End) {
                Text("عمولة المروّج", fontSize = 11.sp, color = BinanceTextGray, fontWeight = FontWeight.SemiBold)
                Text("≥ 500 ر.ي", fontSize = 12.sp, color = BinanceGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Binance Quick Actions Bar (Deposit, Orders, Escrow, Promo)
@Composable
fun BinanceQuickActionsGrid(
    onDepositClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onPromoClick: () -> Unit,
    onAuditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BinanceActionItem(
            icon = Icons.Default.AddCard,
            title = "إيداع جيب",
            tint = BinanceYellow,
            onClick = onDepositClick
        )
        BinanceActionItem(
            icon = Icons.Default.Shield,
            title = "الضمان P2P",
            tint = BinanceGreen,
            onClick = onOrdersClick
        )
        BinanceActionItem(
            icon = Icons.Default.Campaign,
            title = "كود الترويج",
            tint = Color(0xFF60A5FA),
            onClick = onPromoClick
        )
        BinanceActionItem(
            icon = Icons.Default.ReceiptLong,
            title = "سجل الأصول",
            tint = Color(0xFFA78BFA),
            onClick = onAuditClick
        )
    }
}

@Composable
private fun BinanceActionItem(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BinanceInputDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = title, fontSize = 11.sp, color = BinanceTextWhite, fontWeight = FontWeight.Medium)
    }
}



@Composable
fun ProductImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    cornerRadius: Int = 10
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    if (imageUrl.isBlank()) {
        Box(
            modifier = modifier.clip(shape).background(BinanceInputDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Image, contentDescription = contentDescription, tint = BinanceTextGray, modifier = Modifier.size(34.dp))
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
            onError = { }
        )
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.BinanceLogoIcon
import com.example.ui.theme.*

@Composable
fun WelcomeAndAuthModal(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.CONSUMER) }
    var promoCode by remember { mutableStateOf("") }
    var jeebAccount by remember { mutableStateOf("") }


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = null,
        containerColor = BinanceCardDark,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Emblem
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BinanceBgDark),
                    contentAlignment = Alignment.Center
                ) {
                    BinanceLogoIcon(size = 36)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "المتجر اليمني • Binance Style",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextWhite
                )
                Text(
                    text = "منصة التجارة المالية والضمان المحمي Escrow مع محفظة جيب الإلكترونية",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = BinanceTextGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(14.dp))
                // Toggle login / register
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BinanceInputDark)
                        .padding(3.dp)
                ) {
                    Button(
                        onClick = { isRegister = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isRegister) BinanceYellow else Color.Transparent,
                            contentColor = if (!isRegister) Color.Black else BinanceTextGray
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("تسجيل الدخول", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isRegister = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegister) BinanceYellow else Color.Transparent,
                            contentColor = if (isRegister) Color.Black else BinanceTextGray
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("إنشاء حساب جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isRegister) {
                    // Registration form
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل", color = BinanceTextGray) },
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
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني", color = BinanceTextGray) },
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
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف (اليمن)", color = BinanceTextGray) },
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
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور", color = BinanceTextGray) },
                        visualTransformation = PasswordVisualTransformation(),
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
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("تأكيد كلمة المرور", color = BinanceTextGray) },
                        visualTransformation = PasswordVisualTransformation(),
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

                    Text(
                        text = "اختر نوع الحساب:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextWhite,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = role == UserRole.CONSUMER,
                            onClick = { role = UserRole.CONSUMER },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BinanceInputDark,
                                labelColor = BinanceTextWhite,
                                selectedContainerColor = BinanceYellow,
                                selectedLabelColor = Color.Black
                            ),
                            label = { Text("مستهلك", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        FilterChip(
                            selected = role == UserRole.MERCHANT,
                            onClick = { role = UserRole.MERCHANT },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BinanceInputDark,
                                labelColor = BinanceTextWhite,
                                selectedContainerColor = BinanceYellow,
                                selectedLabelColor = Color.Black
                            ),
                            label = { Text("تاجر", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        FilterChip(
                            selected = role == UserRole.PROMOTER,
                            onClick = { role = UserRole.PROMOTER },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BinanceInputDark,
                                labelColor = BinanceTextWhite,
                                selectedContainerColor = BinanceYellow,
                                selectedLabelColor = Color.Black
                            ),
                            label = { Text("مروّج", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (role == UserRole.CONSUMER) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = promoCode,
                            onValueChange = { promoCode = it },
                            label = { Text("كود المروّج (اختياري، مثلاً RYD-78241)", color = BinanceTextGray) },
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
                    }

                    if (role == UserRole.MERCHANT) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = jeebAccount,
                            onValueChange = { jeebAccount = it },
                            label = { Text("رقم حساب محفظة جيب لاستلام الأرباح", color = BinanceTextGray) },
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (fullName.isBlank() || email.isBlank()) {
                                viewModel.showMessage("يرجى ملء جميع الحقول الإلزامية")
                                return@Button
                            }
                            if (password != confirmPassword) {
                                viewModel.showMessage("كلمتا المرور غير متطابقتين")
                                return@Button
                            }
                            viewModel.register(
                                email = email,
                                password = password,
                                fullName = fullName,
                                phone = phone,
                                role = role,
                                promoCode = promoCode,
                                jeebAccount = jeebAccount
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("إنشاء الحساب والمتابعة", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Login form
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني", color = BinanceTextGray) },
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
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور", color = BinanceTextGray) },
                        visualTransformation = PasswordVisualTransformation(),
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                viewModel.showMessage("أدخل البريد الإلكتروني وكلمة المرور")
                            } else {
                                viewModel.login(email, password)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("تسجيل الدخول", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDismiss) {
                    Text("إغلاق", color = BinanceTextGray)
                }
            }
        }
    )
}

package com.pro.qlkho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pro.qlkho.ui.theme.*

data class DemoAccount(
    val title: String,
    val username: String,
    val pass: String,
    val name: String,
    val role: String,
    val warehouse: String?,
    val badgeColor: Color,
    val textColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (username: String, pass: String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var usernameInput by remember { mutableStateOf("admin") }
    var passwordInput by remember { mutableStateOf("admin123") }
    var showPassword by remember { mutableStateOf(false) }

    val demoAccounts = listOf(
        DemoAccount(
            title = "ADMIN",
            username = "admin",
            pass = "admin123",
            name = "Quản trị viên",
            role = "Quản trị viên toàn hệ thống",
            warehouse = "Toàn quyền",
            badgeColor = Rose50,
            textColor = Rose600
        ),
        DemoAccount(
            title = "MANAGER 1",
            username = "qlkho1",
            pass = "123456",
            name = "Nguyễn Minh Anh",
            role = "Quản lý kho",
            warehouse = "KH001 (Kho Tổng Miền Nam)",
            badgeColor = PrimaryBlueLight,
            textColor = PrimaryBlueHover
        ),
        DemoAccount(
            title = "MANAGER 2",
            username = "taixe1",
            pass = "123456",
            name = "Trần Văn Nam",
            role = "Nhân viên vận chuyển",
            warehouse = "XE-01 (Xe 29H-123.45)",
            badgeColor = Emerald50,
            textColor = Emerald700
        ),
        DemoAccount(
            title = "MANAGER 3",
            username = "qlkho2",
            pass = "123456",
            name = "Lê Văn Bình",
            role = "Quản lý kho",
            warehouse = "KH002 (Kho Bình Dương)",
            badgeColor = Amber50,
            textColor = Amber700
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Slate900, Slate800)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo & Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "QL Kho Pro",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Hệ thống quản lý kho & vận chuyển",
                fontSize = 14.sp,
                color = Slate400,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Login Card
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Đăng nhập hệ thống",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Rose50,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Rose100)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Rose600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = errorMessage,
                                    fontSize = 12.sp,
                                    color = Rose700,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Username Input
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Tên đăng nhập") },
                        placeholder = { Text("admin, qlkho1, taixe1...") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = Slate500)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Slate500)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Hiện/ẩn mật khẩu",
                                    tint = Slate500
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onLogin(usernameInput, passwordInput)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    Button(
                        onClick = {
                            onLogin(usernameInput, passwordInput)
                        },
                        enabled = !isLoading && usernameInput.isNotBlank() && passwordInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Đăng nhập",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Accounts Section
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TÀI KHOẢN DEMO CÓ SẴN (CLICK ĐỂ TỰ ĐỘNG ĐIỀN):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400
                )

                demoAccounts.forEach { acc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                usernameInput = acc.username
                                passwordInput = acc.pass
                                onLogin(acc.username, acc.pass)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(acc.badgeColor)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = acc.title,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = acc.textColor
                                        )
                                    }
                                    Text(
                                        text = acc.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Username: ${acc.username} • Pass: ${acc.pass}",
                                    fontSize = 11.sp,
                                    color = Slate300
                                )
                                Text(
                                    text = "Vị trí: ${acc.warehouse ?: "Không giới hạn"}",
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = "Chọn tài khoản",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

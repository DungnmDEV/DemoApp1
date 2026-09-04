package com.pro.qlkho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pro.qlkho.data.model.User
import com.pro.qlkho.data.model.UserRole
import com.pro.qlkho.data.model.Warehouse
import com.pro.qlkho.ui.components.RoleBadge
import com.pro.qlkho.ui.components.StatusBadge
import com.pro.qlkho.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersRolesScreen(
    currentUser: User?,
    users: List<User>,
    warehouses: List<Warehouse>,
    onSaveUser: (User, Boolean) -> Unit,
    onDeleteUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentUser?.role != UserRole.ADMIN) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Rose100)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Rose600,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Quyền truy cập bị từ chối",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Bạn không có quyền truy cập chức năng này. Chỉ Quản trị viên (ADMIN) mới có quyền quản lý người dùng và phân quyền.",
                        fontSize = 13.sp,
                        color = Slate600,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        return
    }

    var isEditMode by remember { mutableStateOf(false) }
    var userId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.MANAGER) }
    var selectedWarehouseId by remember { mutableStateOf<String?>(warehouses.firstOrNull()?.id) }
    var warehouseDropdownExpanded by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    val warehouseMap = warehouses.associateBy { it.id }

    val filteredUsers = users.filter { u ->
        u.username.contains(searchQuery, ignoreCase = true) ||
                u.name.contains(searchQuery, ignoreCase = true) ||
                (u.warehouseId?.contains(searchQuery, ignoreCase = true) == true)
    }

    fun resetForm() {
        isEditMode = false
        userId = ""
        username = ""
        password = ""
        fullName = ""
        selectedRole = UserRole.MANAGER
        selectedWarehouseId = warehouses.firstOrNull()?.id
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Xác nhận xóa tài khoản?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa tài khoản '${userToDelete?.name}' (${userToDelete?.username}) không?") },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.id?.let { onDeleteUser(it) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEditMode) "Chỉnh sửa tài khoản người dùng" else "Thêm tài khoản & phân quyền mới",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        if (isEditMode) {
                            TextButton(onClick = { resetForm() }) {
                                Text("Hủy sửa", color = Rose600, fontSize = 12.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { if (!isEditMode) username = it },
                            label = { Text("Tên đăng nhập") },
                            placeholder = { Text("qlkho1, taixe3...") },
                            enabled = !isEditMode,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            placeholder = { Text("123456...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Họ và tên") },
                        placeholder = { Text("Nguyễn Văn Cường...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Role Selection (Moved to its own row for full width to prevent vertical wrapping)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Vai trò người dùng:", fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = selectedRole == UserRole.MANAGER,
                                onClick = { selectedRole = UserRole.MANAGER },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Quản lý kho/xe", fontSize = 12.sp, maxLines = 1) 
                                    }
                                }
                            )
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = selectedRole == UserRole.ADMIN,
                                onClick = { selectedRole = UserRole.ADMIN },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Quản trị viên", fontSize = 12.sp, maxLines = 1) 
                                    }
                                }
                            )
                        }
                    }

                    // Warehouse selection (Mandatory for MANAGER)
                    if (selectedRole == UserRole.MANAGER) {
                        Column {
                            Text(
                                text = "Kho hoặc Xe được phân công quản lý (Bắt buộc):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            ExposedDropdownMenuBox(
                                expanded = warehouseDropdownExpanded,
                                onExpandedChange = { warehouseDropdownExpanded = it }
                            ) {
                                val selectedWh = warehouseMap[selectedWarehouseId]
                                OutlinedTextField(
                                    value = if (selectedWh != null) "${selectedWh.id} - ${selectedWh.name}" else "Chọn kho/xe...",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = warehouseDropdownExpanded,
                                    onDismissRequest = { warehouseDropdownExpanded = false }
                                ) {
                                    warehouses.forEach { wh ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text("${wh.id} - ${wh.name}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                    Text("Loại: ${wh.type.displayName} • ${wh.location}", fontSize = 10.sp, color = Slate500)
                                                }
                                            },
                                            onClick = {
                                                selectedWarehouseId = wh.id
                                                warehouseDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()) {
                                onSaveUser(
                                    User(
                                        id = if (isEditMode) userId else "",
                                        username = username.trim(),
                                        password = password.trim(),
                                        name = fullName.trim(),
                                        role = selectedRole,
                                        warehouseId = if (selectedRole == UserRole.MANAGER) selectedWarehouseId else null
                                    ),
                                    isEditMode
                                )
                                resetForm()
                            }
                        },
                        enabled = username.isNotBlank() && password.isNotBlank() && fullName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "Cập nhật người dùng" else "Thêm người dùng & cấp quyền", 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }
        }

        // Section header & Search
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DANH SÁCH TÀI KHOẢN HỆ THỐNG (${filteredUsers.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm theo tên đăng nhập, họ tên, kho quản lý...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = Slate400)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Users List
        items(filteredUsers, key = { it.username }) { user ->
            val assignedWh = user.warehouseId?.let { warehouseMap[it] }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = user.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            RoleBadge(user.role)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Username: ${user.username} • Mật khẩu: ••••••",
                            fontSize = 11.sp,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        if (user.role == UserRole.ADMIN) {
                            StatusBadge(
                                text = "Toàn quyền truy cập tất cả kho & xe",
                                bgColor = Slate100,
                                textColor = Slate700
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Outlined.Warehouse, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Quản lý: ${assignedWh?.id ?: user.warehouseId} - ${assignedWh?.name ?: ""}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                isEditMode = true
                                userId = user.id
                                username = user.username
                                password = user.password
                                fullName = user.name
                                selectedRole = user.role
                                selectedWarehouseId = user.warehouseId ?: warehouses.firstOrNull()?.id
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        }

                        if (user.username != "admin") {
                            IconButton(
                                onClick = { userToDelete = user }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Rose600, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.Driver
import com.example.data.model.InventoryItem
import com.example.data.model.Product
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversVehiclesScreen(
    drivers: List<Driver>,
    inventory: List<InventoryItem>,
    products: List<Product>,
    onSaveDriver: (Driver, Boolean) -> Unit,
    onDeleteDriver: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var driverId by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverPlate by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var driverToDelete by remember { mutableStateOf<Driver?>(null) }

    val productsMap = products.associateBy { it.sku }

    val filteredDrivers = drivers.filter { d ->
        d.id.contains(searchQuery, ignoreCase = true) ||
                d.name.contains(searchQuery, ignoreCase = true) ||
                d.plate.contains(searchQuery, ignoreCase = true) ||
                d.phone.contains(searchQuery, ignoreCase = true)
    }

    fun resetForm() {
        isEditMode = false
        driverId = ""
        driverName = ""
        driverPlate = ""
        driverPhone = ""
    }

    if (driverToDelete != null) {
        AlertDialog(
            onDismissRequest = { driverToDelete = null },
            title = { Text("Xác nhận xóa xe/nhân viên?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa nhân viên/xe '${driverToDelete?.name}' (${driverToDelete?.id}) không?") },
            confirmButton = {
                Button(
                    onClick = {
                        driverToDelete?.id?.let { onDeleteDriver(it) }
                        driverToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { driverToDelete = null }) {
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
        // Form Card (Add/Edit Driver & Vehicle)
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
                        Column {
                            Text(
                                text = if (isEditMode) "Chỉnh sửa thông tin Xe & Nhân viên" else "Thêm Nhân viên & Xe vận chuyển mới",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "💡 Hệ thống sẽ tự động khởi tạo Kho di động tương ứng để quản lý tồn kho",
                                fontSize = 11.sp,
                                color = Emerald700
                            )
                        }
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
                            value = driverId,
                            onValueChange = { if (!isEditMode) driverId = it },
                            label = { Text("Mã kho xe / Nhân viên") },
                            placeholder = { Text("XE-01, XE-02, XE-03...") },
                            enabled = !isEditMode,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Tên nhân viên lái xe") },
                            placeholder = { Text("Trần Văn Nam, Nguyễn Văn Cường...") },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = driverPlate,
                            onValueChange = { driverPlate = it },
                            label = { Text("Biển số xe") },
                            placeholder = { Text("29H-123.45, 30H-555.66...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = driverPhone,
                            onValueChange = { driverPhone = it },
                            label = { Text("Số điện thoại") },
                            placeholder = { Text("0901234567...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (driverId.isNotBlank() && driverName.isNotBlank() && driverPlate.isNotBlank()) {
                                onSaveDriver(
                                    Driver(
                                        id = driverId.trim().uppercase(),
                                        name = driverName.trim(),
                                        plate = driverPlate.trim(),
                                        phone = driverPhone.trim()
                                    ),
                                    isEditMode
                                )
                                resetForm()
                            }
                        },
                        enabled = driverId.isNotBlank() && driverName.isNotBlank() && driverPlate.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditMode) "Cập nhật nhân viên & xe" else "Lưu nhân viên & tạo kho xe", fontWeight = FontWeight.Bold)
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
                    text = "DANH SÁCH NHÂN VIÊN & XE VẬN CHUYỂN (${filteredDrivers.size})",
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
                placeholder = { Text("Tìm theo mã xe, tên tài xế, biển số, SĐT...") },
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

        // Drivers List
        if (filteredDrivers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không tìm thấy nhân viên/xe nào.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredDrivers, key = { it.id }) { driver ->
                val driverStock = inventory.filter { it.warehouseId == driver.id && it.qty > 0 }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = driver.id,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                StatusBadge(
                                    text = "VẬN CHUYỂN",
                                    bgColor = Emerald50,
                                    textColor = Emerald700,
                                    borderColor = Emerald100
                                )
                                StatusBadge(
                                    text = driver.plate,
                                    bgColor = Slate100,
                                    textColor = Slate800,
                                    borderColor = Slate300
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        isEditMode = true
                                        driverId = driver.id
                                        driverName = driver.name
                                        driverPlate = driver.plate
                                        driverPhone = driver.phone
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { driverToDelete = driver }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Rose600, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
                                Text(text = driver.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            }
                            if (driver.phone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                    Text(text = driver.phone, fontSize = 12.sp, color = Slate600)
                                }
                            }
                        }

                        // Stock preview on this vehicle
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Slate50,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = Slate500, modifier = Modifier.size(14.dp))
                                    Text("Tồn kho trên xe:", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Medium)
                                }

                                if (driverStock.isEmpty()) {
                                    Text("Trống (0 SP)", fontSize = 11.sp, color = Slate400)
                                } else {
                                    val summaryText = driverStock.joinToString(", ") { inv ->
                                        val p = productsMap[inv.sku]
                                        "${inv.sku}: ${inv.qty} ${p?.unit ?: ""}"
                                    }
                                    Text(
                                        text = summaryText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

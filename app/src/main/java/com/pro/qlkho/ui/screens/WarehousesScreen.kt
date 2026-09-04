package com.pro.qlkho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import com.pro.qlkho.data.model.InventoryItem
import com.pro.qlkho.data.model.Warehouse
import com.pro.qlkho.data.model.WarehouseType
import com.pro.qlkho.ui.components.WarehouseTypeBadge
import com.pro.qlkho.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehousesScreen(
    warehouses: List<Warehouse>,
    inventory: List<InventoryItem>,
    onSaveWarehouse: (Warehouse, Boolean) -> Unit,
    onDeleteWarehouse: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var warehouseId by remember { mutableStateOf("") }
    var warehouseName by remember { mutableStateOf("") }
    var warehouseType by remember { mutableStateOf(WarehouseType.KHO_TONG) }
    var warehouseLocation by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var warehouseToDelete by remember { mutableStateOf<Warehouse?>(null) }

    val filteredWarehouses = warehouses.filter { w ->
        w.id.contains(searchQuery, ignoreCase = true) ||
                w.name.contains(searchQuery, ignoreCase = true) ||
                w.location.contains(searchQuery, ignoreCase = true) ||
                w.type.displayName.contains(searchQuery, ignoreCase = true)
    }

    fun resetForm() {
        isEditMode = false
        warehouseId = ""
        warehouseName = ""
        warehouseType = WarehouseType.KHO_TONG
        warehouseLocation = ""
    }

    if (warehouseToDelete != null) {
        val stockCount = inventory.filter { it.warehouseId == warehouseToDelete!!.id }.sumOf { it.qty }
        AlertDialog(
            onDismissRequest = { warehouseToDelete = null },
            title = { Text("Xác nhận xóa kho?", fontWeight = FontWeight.Bold) },
            text = {
                if (stockCount > 0) {
                    Text("Không thể xóa kho '${warehouseToDelete?.name}' (${warehouseToDelete?.id}) vì còn $stockCount sản phẩm tồn kho!", color = Rose600)
                } else {
                    Text("Bạn có chắc muốn xóa kho '${warehouseToDelete?.name}' (${warehouseToDelete?.id}) không?")
                }
            },
            confirmButton = {
                if (stockCount == 0) {
                    Button(
                        onClick = {
                            warehouseToDelete?.id?.let { onDeleteWarehouse(it) }
                            warehouseToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                    ) {
                        Text("Xóa")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { warehouseToDelete = null }) {
                    Text(if (stockCount > 0) "Đã hiểu" else "Hủy")
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
        // Form Card (Add/Edit Warehouse)
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
                            text = if (isEditMode) "Chỉnh sửa thông tin Kho & Bãi" else "Thêm Kho / Điểm tập kết mới",
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

                    OutlinedTextField(
                        value = warehouseId,
                        onValueChange = { if (!isEditMode) warehouseId = it },
                        label = { Text("Mã kho / Bãi", maxLines = 1, modifier = Modifier.basicMarquee()) },
                        placeholder = { Text("KH001, KH002, BAI-01...") },
                        enabled = !isEditMode,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = warehouseName,
                        onValueChange = { warehouseName = it },
                        label = { Text("Tên kho / Vị trí", maxLines = 1, modifier = Modifier.basicMarquee()) },
                        placeholder = { Text("Kho Tổng Miền Nam...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = warehouseLocation,
                        onValueChange = { warehouseLocation = it },
                        label = { Text("Địa chỉ / Vị trí địa lý", maxLines = 1, modifier = Modifier.basicMarquee()) },
                        placeholder = { Text("TP. Hồ Chí Minh, Bình Dương...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Type Selection (Moved to its own row for full width)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Phân loại kho:", fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        // We have 4 types: KHO_TONG, KHO_CHI_NHANH, BAI_XE, VAN_CHUYEN
                        // Split into 2 rows if needed or just use a horizontal scrollable row? 
                        // Let's use a Column with FlowRow-like behavior or just a Row with weights.
                        // Given 4 items, 2x2 grid or single scrollable row is best. 
                        // For simplicity and alignment, let's use a Row with scroll if needed, or 2 rows.
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(WarehouseType.KHO_TONG, WarehouseType.KHO_CHI_NHANH).forEach { t ->
                                    FilterChip(
                                        modifier = Modifier.weight(1f),
                                        selected = warehouseType == t,
                                        onClick = { warehouseType = t },
                                        label = { 
                                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                Text(t.displayName, fontSize = 11.sp, maxLines = 1) 
                                            }
                                        }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(WarehouseType.BAI_XE, WarehouseType.VAN_CHUYEN).forEach { t ->
                                    FilterChip(
                                        modifier = Modifier.weight(1f),
                                        selected = warehouseType == t,
                                        onClick = { warehouseType = t },
                                        label = { 
                                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                Text(t.displayName, fontSize = 11.sp, maxLines = 1) 
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (warehouseId.isNotBlank() && warehouseName.isNotBlank()) {
                                onSaveWarehouse(
                                    Warehouse(
                                        id = warehouseId.trim().uppercase(),
                                        name = warehouseName.trim(),
                                        type = warehouseType,
                                        location = warehouseLocation.trim().ifBlank { "Di động" }
                                    ),
                                    isEditMode
                                )
                                resetForm()
                            }
                        },
                        enabled = warehouseId.isNotBlank() && warehouseName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "Cập nhật thông tin kho" else "Lưu thông tin kho mới", 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }
        }

        // Section header & Search (Combined to reduce gaps)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DANH SÁCH KHO & BÃI (${filteredWarehouses.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).basicMarquee()
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm theo mã kho, tên kho, loại kho, địa chỉ...") },
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
        }

        // Warehouse cards
        items(filteredWarehouses, key = { it.id }) { wh ->
            val whStockItems = inventory.filter { it.warehouseId == wh.id }
            val totalWhQty = whStockItems.sumOf { it.qty }
            val distinctSkus = whStockItems.count { it.qty > 0 }

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
                                text = wh.id,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            WarehouseTypeBadge(wh.type)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    isEditMode = true
                                    warehouseId = wh.id
                                    warehouseName = wh.name
                                    warehouseType = wh.type
                                    warehouseLocation = wh.location
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = { warehouseToDelete = wh }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Rose600, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = wh.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                        Text(
                            text = wh.location, 
                            fontSize = 11.sp, 
                            color = Slate600,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Slate50,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tổng tồn kho:", fontSize = 11.sp, color = Slate600)
                            Text(
                                text = "$totalWhQty SP ($distinctSkus mã SKU)",
                                fontSize = 12.sp,
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

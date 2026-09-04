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
import com.pro.qlkho.data.model.Product
import com.pro.qlkho.data.model.Warehouse
import com.pro.qlkho.ui.components.StatusBadge
import com.pro.qlkho.ui.components.WarehouseTypeBadge
import com.pro.qlkho.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    warehouses: List<Warehouse>,
    products: List<Product>,
    inventory: List<InventoryItem>,
    modifier: Modifier = Modifier
) {
    var selectedWarehouseId by remember { mutableStateOf<String?>("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var warehouseDropdownExpanded by remember { mutableStateOf(false) }

    val warehouseMap = warehouses.associateBy { it.id }
    val productMap = products.associateBy { it.sku }

    val filteredInventory = inventory.filter { item ->
        val matchWh = selectedWarehouseId == "ALL" || item.warehouseId == selectedWarehouseId
        val p = productMap[item.sku]
        val wh = warehouseMap[item.warehouseId]
        val matchSearch = searchQuery.isBlank() ||
                item.sku.contains(searchQuery, ignoreCase = true) ||
                (p?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                item.warehouseId.contains(searchQuery, ignoreCase = true) ||
                (wh?.name?.contains(searchQuery, ignoreCase = true) == true)
        matchWh && matchSearch
    }

    val totalItemsCount = filteredInventory.sumOf { it.qty }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary & Filter Bar
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Báo cáo tồn kho theo thời gian thực",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = "Tổng: $totalItemsCount SP (${filteredInventory.size} bản ghi)",
                                fontSize = 12.sp,
                                color = Slate500,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = PrimaryBlueLight,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Inventory, contentDescription = null, tint = PrimaryBlueHover, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "$totalItemsCount SP",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueHover
                                )
                            }
                        }
                    }

                    Divider(color = Slate100)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Warehouse selector dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = warehouseDropdownExpanded,
                                onExpandedChange = { warehouseDropdownExpanded = it }
                            ) {
                                val currentWh = warehouseMap[selectedWarehouseId]
                                OutlinedTextField(
                                    value = if (selectedWarehouseId == "ALL") "Tất cả kho & xe" else "${currentWh?.id} - ${currentWh?.name}",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text("Lọc theo vị trí kho / xe") },
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
                                    DropdownMenuItem(
                                        text = { Text("Tất cả kho & xe", fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            selectedWarehouseId = "ALL"
                                            warehouseDropdownExpanded = false
                                        }
                                    )
                                    warehouses.forEach { wh ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text("${wh.id} - ${wh.name}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                    Text("Loại: ${wh.type.displayName}", fontSize = 10.sp, color = Slate500)
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

                        // Search box
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Tìm theo SKU, tên SP...") },
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
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Inventory table / list
        if (filteredInventory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không có tồn kho nào phù hợp với bộ lọc.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredInventory, key = { "${it.warehouseId}_${it.sku}" }) { inv ->
                val p = productMap[inv.sku]
                val wh = warehouseMap[inv.warehouseId]

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
                                    text = inv.sku,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = p?.name ?: "Sản phẩm",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate900,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Outlined.Warehouse, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = "${wh?.id ?: inv.warehouseId} - ${wh?.name ?: ""}",
                                        fontSize = 12.sp,
                                        color = Slate600,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee()
                                    )
                                }
                                if (wh != null) {
                                    WarehouseTypeBadge(wh.type)
                                }
                            }
                        }

                        // Right: Quantity & Availability Status
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${inv.qty} ${p?.unit ?: ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (inv.qty > 0) PrimaryBlue else Rose600
                            )

                            when {
                                inv.qty == 0 -> StatusBadge(
                                    text = "Hết hàng",
                                    bgColor = Rose50,
                                    textColor = Rose700,
                                    borderColor = Rose100
                                )
                                inv.qty < 50 -> StatusBadge(
                                    text = "Sắp hết",
                                    bgColor = Amber50,
                                    textColor = Amber700,
                                    borderColor = Amber100
                                )
                                else -> StatusBadge(
                                    text = "Còn hàng",
                                    bgColor = Emerald50,
                                    textColor = Emerald700,
                                    borderColor = Emerald100
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

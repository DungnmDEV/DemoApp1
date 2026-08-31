package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.ModalState
import com.example.ui.components.InputOrderStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputOrdersScreen(
    currentUser: User?,
    partners: List<Partner>,
    warehouses: List<Warehouse>,
    products: List<Product>,
    inputOrders: List<InputOrder>,
    onCreateOrder: (supplierId: String, warehouseId: String, items: List<OrderItem>) -> Unit,
    onConfirmOrder: (orderId: String) -> Unit,
    onCancelOrder: (orderId: String) -> Unit,
    onOpenModal: (ModalState) -> Unit,
    canManageWarehouse: (String?) -> Boolean,
    modifier: Modifier = Modifier
) {
    val suppliers = partners.filter { it.type == PartnerType.SUPPLIER }
    val warehouseMap = warehouses.associateBy { it.id }
    val partnerMap = partners.associateBy { it.id }
    val productMap = products.associateBy { it.sku }

    // Form state
    var selectedSupplierId by remember { mutableStateOf(suppliers.firstOrNull()?.id ?: "") }
    var selectedWarehouseId by remember {
        mutableStateOf(
            if (currentUser?.role == UserRole.MANAGER && currentUser.warehouseId != null) {
                currentUser.warehouseId
            } else {
                warehouses.firstOrNull()?.id ?: ""
            }
        )
    }

    var orderItems by remember {
        mutableStateOf(
            listOf(OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 100))
        )
    }

    var supplierDropdownExpanded by remember { mutableStateOf(false) }
    var warehouseDropdownExpanded by remember { mutableStateOf(false) }

    // Filter state
    var filterStatus by remember { mutableStateOf<InputOrderStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var orderToCancel by remember { mutableStateOf<String?>(null) }
    var orderToConfirm by remember { mutableStateOf<String?>(null) }

    val filteredOrders = inputOrders.filter { order ->
        val matchStatus = filterStatus == null || order.status == filterStatus
        val suppName = partnerMap[order.supplierId]?.name ?: ""
        val whName = warehouseMap[order.warehouseId]?.name ?: ""
        val matchSearch = searchQuery.isBlank() ||
                order.id.contains(searchQuery, ignoreCase = true) ||
                order.supplierId.contains(searchQuery, ignoreCase = true) ||
                suppName.contains(searchQuery, ignoreCase = true) ||
                order.warehouseId.contains(searchQuery, ignoreCase = true) ||
                whName.contains(searchQuery, ignoreCase = true)
        matchStatus && matchSearch
    }

    // Confirmation dialog for Receiving items
    if (orderToConfirm != null) {
        AlertDialog(
            onDismissRequest = { orderToConfirm = null },
            title = { Text("Xác nhận nhập hàng vào kho?", fontWeight = FontWeight.Bold) },
            text = { Text("Hệ thống sẽ cộng số lượng sản phẩm trong đơn $orderToConfirm vào tồn kho thực tế của kho nhận.") },
            confirmButton = {
                Button(
                    onClick = {
                        orderToConfirm?.let { onConfirmOrder(it) }
                        orderToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Xác nhận nhận hàng")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToConfirm = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Confirmation dialog for Cancelling order
    if (orderToCancel != null) {
        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            title = { Text("Xác nhận hủy đơn nhập?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn hủy đơn nhập $orderToCancel không?") },
            confirmButton = {
                Button(
                    onClick = {
                        orderToCancel?.let { onCancelOrder(it) }
                        orderToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Hủy đơn")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { orderToCancel = null }) {
                    Text("Đóng")
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
        // Create Order Card Form
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
                    Text(
                        text = "Tạo đơn hàng đầu vào (Inbound Order)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Supplier dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = supplierDropdownExpanded,
                                onExpandedChange = { supplierDropdownExpanded = it }
                            ) {
                                val currentSupp = partnerMap[selectedSupplierId]
                                OutlinedTextField(
                                    value = if (currentSupp != null) "${currentSupp.id} - ${currentSupp.name}" else "Chọn nhà cung cấp...",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text("Nhà cung cấp") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = supplierDropdownExpanded,
                                    onDismissRequest = { supplierDropdownExpanded = false }
                                ) {
                                    suppliers.forEach { supp ->
                                        DropdownMenuItem(
                                            text = { Text("${supp.id} - ${supp.name}", fontSize = 12.sp) },
                                            onClick = {
                                                selectedSupplierId = supp.id
                                                supplierDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Destination Warehouse dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            val isWarehouseFixed = currentUser?.role == UserRole.MANAGER && currentUser.warehouseId != null
                            ExposedDropdownMenuBox(
                                expanded = warehouseDropdownExpanded && !isWarehouseFixed,
                                onExpandedChange = { if (!isWarehouseFixed) warehouseDropdownExpanded = it }
                            ) {
                                val currentWh = warehouseMap[selectedWarehouseId]
                                OutlinedTextField(
                                    value = if (currentWh != null) "${currentWh.id} - ${currentWh.name}" else "Chọn kho nhập...",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    enabled = !isWarehouseFixed,
                                    label = { Text("Kho đích nhập hàng") },
                                    trailingIcon = { if (!isWarehouseFixed) ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                if (!isWarehouseFixed) {
                                    ExposedDropdownMenu(
                                        expanded = warehouseDropdownExpanded,
                                        onDismissRequest = { warehouseDropdownExpanded = false }
                                    ) {
                                        warehouses.forEach { wh ->
                                            DropdownMenuItem(
                                                text = { Text("${wh.id} - ${wh.name} (${wh.type.displayName})", fontSize = 12.sp) },
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
                    }

                    // Product Items rows
                    Text(
                        text = "Danh sách sản phẩm nhập:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )

                    orderItems.forEachIndexed { index, item ->
                        var itemSkuDropdown by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(2f)) {
                                ExposedDropdownMenuBox(
                                    expanded = itemSkuDropdown,
                                    onExpandedChange = { itemSkuDropdown = it }
                                ) {
                                    val curProd = productMap[item.sku]
                                    OutlinedTextField(
                                        value = "${item.sku} - ${curProd?.name ?: ""}",
                                        onValueChange = {},
                                        readOnly = true,
                                        singleLine = true,
                                        label = { Text("Sản phẩm #${index + 1}") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemSkuDropdown) },
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = itemSkuDropdown,
                                        onDismissRequest = { itemSkuDropdown = false }
                                    ) {
                                        products.forEach { prod ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "${prod.sku} - ${prod.name} (${prod.unit})",
                                                        fontSize = 12.sp,
                                                        maxLines = 1,
                                                        modifier = Modifier.basicMarquee()
                                                    )
                                                },
                                                onClick = {
                                                    val newList = orderItems.toMutableList()
                                                    newList[index] = item.copy(sku = prod.sku)
                                                    orderItems = newList
                                                    itemSkuDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = if (item.qty > 0) item.qty.toString() else "",
                                onValueChange = { str ->
                                    val q = str.toIntOrNull() ?: 0
                                    val newList = orderItems.toMutableList()
                                    newList[index] = item.copy(qty = q)
                                    orderItems = newList
                                },
                                label = { Text("Số lượng") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            if (orderItems.size > 1) {
                                IconButton(
                                    onClick = {
                                        val newList = orderItems.toMutableList()
                                        newList.removeAt(index)
                                        orderItems = newList
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Xóa dòng", tint = Rose600)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                orderItems = orderItems + OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 50)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Thêm sản phẩm khác", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (selectedSupplierId.isNotBlank() && selectedWarehouseId.isNotBlank() && orderItems.any { it.qty > 0 }) {
                                    val validItems = orderItems.filter { it.qty > 0 }
                                    onCreateOrder(selectedSupplierId, selectedWarehouseId, validItems)
                                    orderItems = listOf(OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 100))
                                }
                            },
                            enabled = selectedSupplierId.isNotBlank() && selectedWarehouseId.isNotBlank() && orderItems.any { it.qty > 0 },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tạo đơn nhập", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section header & Filters + Search bar (Combined to reduce gap)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DANH SÁCH ĐƠN NHẬP HÀNG (${filteredOrders.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = filterStatus == null,
                            onClick = { filterStatus = null },
                            label = { Text("Tất cả", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterStatus == InputOrderStatus.PENDING,
                            onClick = { filterStatus = InputOrderStatus.PENDING },
                            label = { Text("Chờ nhận", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterStatus == InputOrderStatus.RECEIVED,
                            onClick = { filterStatus = InputOrderStatus.RECEIVED },
                            label = { Text("Đã nhận", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterStatus == InputOrderStatus.CANCELLED,
                            onClick = { filterStatus = InputOrderStatus.CANCELLED },
                            label = { Text("Đã hủy", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm theo mã đơn, nhà cung cấp, kho nhận...") },
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

        // Removed the separate search bar item to eliminate the large gap caused by spacedBy(16.dp)

        // Orders List
        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không có đơn nhập nào phù hợp.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                val supp = partnerMap[order.supplierId]
                val wh = warehouseMap[order.warehouseId]
                val canManageThis = canManageWarehouse(order.warehouseId)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Top bar
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
                                    text = order.id,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                InputOrderStatusBadge(order.status)
                            }

                            Text(
                                text = order.timestamp,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Routing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Từ: ${supp?.name ?: order.supplierId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).basicMarquee()
                            )
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Đến: ${wh?.id ?: order.warehouseId} - ${wh?.name ?: ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).basicMarquee()
                            )
                        }

                        // Product summary
                        Spacer(modifier = Modifier.height(6.dp))
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
                                val itemsSummary = order.items.joinToString(", ") { item ->
                                    val p = productMap[item.sku]
                                    "${item.sku}: ${item.qty} ${p?.unit ?: ""}"
                                }
                                Text(
                                    text = "Sản phẩm: $itemsSummary",
                                    fontSize = 11.sp,
                                    color = Slate700,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                        }

                        // Bottom Actions
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { onOpenModal(ModalState.InputOrderDetail(order)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Xem chi tiết & lịch sử", fontSize = 12.sp)
                            }

                            if (order.status == InputOrderStatus.PENDING) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { orderToCancel = order.id },
                                        enabled = canManageThis,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Rose100),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Hủy đơn", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { orderToConfirm = order.id },
                                        enabled = canManageThis,
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Xác nhận nhận hàng", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

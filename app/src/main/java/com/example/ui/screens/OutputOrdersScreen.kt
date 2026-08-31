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
import com.example.ui.components.OutputOrderStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputOrdersScreen(
    currentUser: User?,
    partners: List<Partner>,
    warehouses: List<Warehouse>,
    products: List<Product>,
    outputOrders: List<OutputOrder>,
    getInventory: (warehouseId: String, sku: String) -> Int,
    onCreateOrder: (warehouseId: String, customerId: String, items: List<OrderItem>) -> Unit,
    onConfirmOrder: (orderId: String) -> Unit,
    onCancelOrder: (orderId: String) -> Unit,
    onOpenModal: (ModalState) -> Unit,
    canManageWarehouse: (String?) -> Boolean,
    modifier: Modifier = Modifier
) {
    val customers = partners.filter { it.type == PartnerType.CUSTOMER }
    val warehouseMap = warehouses.associateBy { it.id }
    val partnerMap = partners.associateBy { it.id }
    val productMap = products.associateBy { it.sku }

    // Form state
    var selectedWarehouseId by remember {
        mutableStateOf(
            if (currentUser?.role == UserRole.MANAGER && currentUser.warehouseId != null) {
                currentUser.warehouseId
            } else {
                warehouses.firstOrNull()?.id ?: ""
            }
        )
    }
    var selectedCustomerId by remember { mutableStateOf(customers.firstOrNull()?.id ?: "") }

    var orderItems by remember {
        mutableStateOf(
            listOf(OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 50))
        )
    }

    var warehouseDropdownExpanded by remember { mutableStateOf(false) }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    // Filter state
    var filterStatus by remember { mutableStateOf<OutputOrderStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var orderToCancel by remember { mutableStateOf<String?>(null) }
    var orderToConfirm by remember { mutableStateOf<String?>(null) }

    val filteredOrders = outputOrders.filter { order ->
        val matchStatus = filterStatus == null || order.status == filterStatus
        val custName = partnerMap[order.customerId]?.name ?: ""
        val whName = warehouseMap[order.warehouseId]?.name ?: ""
        val matchSearch = searchQuery.isBlank() ||
                order.id.contains(searchQuery, ignoreCase = true) ||
                order.customerId.contains(searchQuery, ignoreCase = true) ||
                custName.contains(searchQuery, ignoreCase = true) ||
                order.warehouseId.contains(searchQuery, ignoreCase = true) ||
                whName.contains(searchQuery, ignoreCase = true)
        matchStatus && matchSearch
    }

    // Confirmation dialog for Delivering items
    if (orderToConfirm != null) {
        AlertDialog(
            onDismissRequest = { orderToConfirm = null },
            title = { Text("Xác nhận xuất hàng khỏi kho?", fontWeight = FontWeight.Bold) },
            text = { Text("Hệ thống sẽ trừ số lượng sản phẩm trong đơn $orderToConfirm khỏi tồn kho thực tế của kho xuất.") },
            confirmButton = {
                Button(
                    onClick = {
                        orderToConfirm?.let { onConfirmOrder(it) }
                        orderToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Xác nhận xuất hàng")
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
            title = { Text("Xác nhận hủy đơn xuất?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn hủy đơn xuất $orderToCancel không?") },
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
                        text = "Tạo đơn hàng đầu ra (Outbound Order)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Source Warehouse dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            val isWarehouseFixed = currentUser?.role == UserRole.MANAGER && currentUser.warehouseId != null
                            ExposedDropdownMenuBox(
                                expanded = warehouseDropdownExpanded && !isWarehouseFixed,
                                onExpandedChange = { if (!isWarehouseFixed) warehouseDropdownExpanded = it }
                            ) {
                                val currentWh = warehouseMap[selectedWarehouseId]
                                OutlinedTextField(
                                    value = if (currentWh != null) "${currentWh.id} - ${currentWh.name}" else "Chọn kho xuất...",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    enabled = !isWarehouseFixed,
                                    label = { Text("Kho xuất hàng") },
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

                        // Customer dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = customerDropdownExpanded,
                                onExpandedChange = { customerDropdownExpanded = it }
                            ) {
                                val currentCust = partnerMap[selectedCustomerId]
                                OutlinedTextField(
                                    value = if (currentCust != null) "${currentCust.id} - ${currentCust.name}" else "Chọn khách hàng...",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text("Khách hàng nhận hàng") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = customerDropdownExpanded,
                                    onDismissRequest = { customerDropdownExpanded = false }
                                ) {
                                    customers.forEach { cust ->
                                        DropdownMenuItem(
                                            text = { Text("${cust.id} - ${cust.name}", fontSize = 12.sp) },
                                            onClick = {
                                                selectedCustomerId = cust.id
                                                customerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Product Items rows with Stock Validation Indicator
                    Text(
                        text = "Danh sách sản phẩm xuất:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )

                    var hasInsufficientStock = false

                    orderItems.forEachIndexed { index, item ->
                        var itemSkuDropdown by remember { mutableStateOf(false) }
                        val curStock = getInventory(selectedWarehouseId, item.sku)
                        val isOverStock = item.qty > curStock
                        if (isOverStock) hasInsufficientStock = true

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
                                        supportingText = {
                                            Text(
                                                text = "Hiện có tại kho: $curStock ${curProd?.unit ?: ""}",
                                                color = if (curStock > 0) Emerald700 else Rose600,
                                                fontSize = 10.sp
                                            )
                                        },
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
                                            val stock = getInventory(selectedWarehouseId, prod.sku)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "${prod.sku} - ${prod.name}",
                                                            fontSize = 12.sp,
                                                            maxLines = 1,
                                                            modifier = Modifier.basicMarquee()
                                                        )
                                                        Text("Tồn: $stock", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                                    }
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
                                isError = isOverStock,
                                supportingText = {
                                    if (isOverStock) {
                                        Text("Vượt tồn kho!", color = Rose600, fontSize = 10.sp)
                                    }
                                },
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
                                orderItems = orderItems + OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 20)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Thêm sản phẩm khác", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (selectedCustomerId.isNotBlank() && selectedWarehouseId.isNotBlank() && orderItems.any { it.qty > 0 } && !hasInsufficientStock) {
                                    val validItems = orderItems.filter { it.qty > 0 }
                                    onCreateOrder(selectedWarehouseId, selectedCustomerId, validItems)
                                    orderItems = listOf(OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 50))
                                }
                            },
                            enabled = selectedCustomerId.isNotBlank() && selectedWarehouseId.isNotBlank() && orderItems.any { it.qty > 0 } && !hasInsufficientStock,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tạo đơn xuất", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section header & Filters + Search Bar (Combined to reduce gap)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DANH SÁCH ĐƠN XUẤT HÀNG (${filteredOrders.size})",
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
                            selected = filterStatus == OutputOrderStatus.PENDING,
                            onClick = { filterStatus = OutputOrderStatus.PENDING },
                            label = { Text("Chờ xuất", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterStatus == OutputOrderStatus.DELIVERED,
                            onClick = { filterStatus = OutputOrderStatus.DELIVERED },
                            label = { Text("Đã xuất", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterStatus == OutputOrderStatus.CANCELLED,
                            onClick = { filterStatus = OutputOrderStatus.CANCELLED },
                            label = { Text("Đã hủy", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm theo mã đơn, khách hàng, kho xuất...") },
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

        // Removed the separate search bar item to eliminate the large gap

        // Orders List
        if (filteredOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không có đơn xuất nào phù hợp.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                val cust = partnerMap[order.customerId]
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
                                OutputOrderStatusBadge(order.status)
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
                                text = "Từ: ${wh?.id ?: order.warehouseId} - ${wh?.name ?: ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Đến: ${cust?.name ?: order.customerId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
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
                                onClick = { onOpenModal(ModalState.OutputOrderDetail(order)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Xem chi tiết & lịch sử", fontSize = 12.sp)
                            }

                            if (order.status == OutputOrderStatus.PENDING) {
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
                                        Text("Xác nhận xuất hàng", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

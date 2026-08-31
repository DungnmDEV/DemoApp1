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
import com.example.ui.components.TransferStatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    currentUser: User?,
    warehouses: List<Warehouse>,
    products: List<Product>,
    transfers: List<TransferOrder>,
    getInventory: (warehouseId: String, sku: String) -> Int,
    onCreateTransfer: (fromId: String, toId: String, items: List<OrderItem>) -> Unit,
    onConfirmTransfer: (transferId: String) -> Unit,
    onCancelTransfer: (transferId: String) -> Unit,
    onOpenModal: (ModalState) -> Unit,
    canManageWarehouse: (String?) -> Boolean,
    modifier: Modifier = Modifier
) {
    val warehouseMap = warehouses.associateBy { it.id }
    val productMap = products.associateBy { it.sku }

    // Form state
    var selectedFromId by remember {
        mutableStateOf(
            if (currentUser?.role == UserRole.MANAGER && currentUser.warehouseId != null) {
                currentUser.warehouseId
            } else {
                warehouses.firstOrNull()?.id ?: ""
            }
        )
    }
    var selectedToId by remember {
        mutableStateOf(
            warehouses.firstOrNull { it.id != selectedFromId }?.id ?: ""
        )
    }

    var transferItems by remember {
        mutableStateOf(
            listOf(OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 100))
        )
    }

    var fromDropdownExpanded by remember { mutableStateOf(false) }
    var toDropdownExpanded by remember { mutableStateOf(false) }

    // Filter state
    var filterStatus by remember { mutableStateOf<TransferStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var transferToConfirm by remember { mutableStateOf<TransferOrder?>(null) }
    var transferToCancel by remember { mutableStateOf<TransferOrder?>(null) }

    val filteredTransfers = transfers.filter { trf ->
        val matchStatus = filterStatus == null || trf.status == filterStatus
        val fromName = warehouseMap[trf.fromId]?.name ?: ""
        val toName = warehouseMap[trf.toId]?.name ?: ""
        val matchSearch = searchQuery.isBlank() ||
                trf.id.contains(searchQuery, ignoreCase = true) ||
                trf.fromId.contains(searchQuery, ignoreCase = true) ||
                fromName.contains(searchQuery, ignoreCase = true) ||
                trf.toId.contains(searchQuery, ignoreCase = true) ||
                toName.contains(searchQuery, ignoreCase = true)
        matchStatus && matchSearch
    }

    // Confirmation dialog for Receiving items
    if (transferToConfirm != null) {
        val trf = transferToConfirm!!
        AlertDialog(
            onDismissRequest = { transferToConfirm = null },
            title = { Text("Xác nhận đã nhận hàng luân chuyển?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Hàng sẽ được cộng vào kho đích (${trf.toId} - ${warehouseMap[trf.toId]?.name}). Trạng thái phiếu sẽ chuyển sang HOÀN THÀNH.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmTransfer(trf.id)
                        transferToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Xác nhận nhận hàng")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { transferToConfirm = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Confirmation dialog for Cancelling Transfer
    if (transferToCancel != null) {
        val trf = transferToCancel!!
        AlertDialog(
            onDismissRequest = { transferToCancel = null },
            title = { Text("Xác nhận hủy phiếu luân chuyển?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Hệ thống sẽ hoàn trả lại toàn bộ số lượng hàng đang vận chuyển vào tồn kho nguồn (${trf.fromId}).")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelTransfer(trf.id)
                        transferToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Hủy luân chuyển")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { transferToCancel = null }) {
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
        // Form Card
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
                            text = "Tạo phiếu luân chuyển nội bộ (Internal Transfer)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    // Rule Callout Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Indigo100.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Indigo100)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = Indigo600, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Quy tắc nghiệp vụ: Ngay khi tạo phiếu, kho nguồn sẽ bị trừ hàng ngay lập tức. Kho đích CHƯA được cộng. Trạng thái coi là 'Đang vận chuyển' cho đến khi kho đích bấm xác nhận nhận.",
                                fontSize = 11.sp,
                                color = Indigo600,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // From Warehouse dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            val isWarehouseFixed = currentUser?.role == UserRole.MANAGER && currentUser.warehouseId != null
                            ExposedDropdownMenuBox(
                                expanded = fromDropdownExpanded && !isWarehouseFixed,
                                onExpandedChange = { if (!isWarehouseFixed) fromDropdownExpanded = it }
                            ) {
                                val currentWh = warehouseMap[selectedFromId]
                                OutlinedTextField(
                                    value = if (currentWh != null) "${currentWh.id} - ${currentWh.name}" else "Chọn kho nguồn...",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    enabled = !isWarehouseFixed,
                                    label = { Text("Kho xuất (Nguồn)") },
                                    trailingIcon = { if (!isWarehouseFixed) ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                if (!isWarehouseFixed) {
                                    ExposedDropdownMenu(
                                        expanded = fromDropdownExpanded,
                                        onDismissRequest = { fromDropdownExpanded = false }
                                    ) {
                                        warehouses.forEach { wh ->
                                            DropdownMenuItem(
                                                text = { Text("${wh.id} - ${wh.name} (${wh.type.displayName})", fontSize = 12.sp) },
                                                onClick = {
                                                    selectedFromId = wh.id
                                                    if (selectedToId == wh.id) {
                                                        selectedToId = warehouses.firstOrNull { it.id != wh.id }?.id ?: ""
                                                    }
                                                    fromDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // To Warehouse dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = toDropdownExpanded,
                                onExpandedChange = { toDropdownExpanded = it }
                            ) {
                                val currentWh = warehouseMap[selectedToId]
                                OutlinedTextField(
                                    value = if (currentWh != null) "${currentWh.id} - ${currentWh.name}" else "Chọn kho đích...",
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text("Kho nhận (Đích)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = toDropdownExpanded,
                                    onDismissRequest = { toDropdownExpanded = false }
                                ) {
                                    warehouses.filter { it.id != selectedFromId }.forEach { wh ->
                                        DropdownMenuItem(
                                            text = { Text("${wh.id} - ${wh.name} (${wh.type.displayName})", fontSize = 12.sp) },
                                            onClick = {
                                                selectedToId = wh.id
                                                toDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Product Items rows with Stock Validation Indicator
                    Text(
                        text = "Danh sách sản phẩm luân chuyển:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )

                    var hasInsufficientStock = false

                    transferItems.forEachIndexed { index, item ->
                        var itemSkuDropdown by remember { mutableStateOf(false) }
                        val curStock = getInventory(selectedFromId, item.sku)
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
                                                text = "Hiện có tại kho nguồn: $curStock ${curProd?.unit ?: ""}",
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
                                            val stock = getInventory(selectedFromId, prod.sku)
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
                                                    val newList = transferItems.toMutableList()
                                                    newList[index] = item.copy(sku = prod.sku)
                                                    transferItems = newList
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
                                    val newList = transferItems.toMutableList()
                                    newList[index] = item.copy(qty = q)
                                    transferItems = newList
                                },
                                label = { Text("Số lượng") },
                                isError = isOverStock,
                                supportingText = {
                                    if (isOverStock) {
                                        Text("Vượt tồn kho nguồn!", color = Rose600, fontSize = 10.sp)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            if (transferItems.size > 1) {
                                IconButton(
                                    onClick = {
                                        val newList = transferItems.toMutableList()
                                        newList.removeAt(index)
                                        transferItems = newList
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
                                transferItems = transferItems + OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 20)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Thêm sản phẩm khác", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (selectedFromId.isNotBlank() && selectedToId.isNotBlank() && selectedFromId != selectedToId && transferItems.any { it.qty > 0 } && !hasInsufficientStock) {
                                    val validItems = transferItems.filter { it.qty > 0 }
                                    onCreateTransfer(selectedFromId, selectedToId, validItems)
                                    transferItems = listOf(OrderItem(sku = products.firstOrNull()?.sku ?: "SKU-88", qty = 100))
                                }
                            },
                            enabled = selectedFromId.isNotBlank() && selectedToId.isNotBlank() && selectedFromId != selectedToId && transferItems.any { it.qty > 0 } && !hasInsufficientStock,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tạo phiếu chuyển & Trừ tồn nguồn", fontWeight = FontWeight.Bold)
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
                        text = "DANH SÁCH PHIẾU LUÂN CHUYỂN (${filteredTransfers.size})",
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
                            selected = filterStatus == TransferStatus.PENDING,
                            onClick = { filterStatus = TransferStatus.PENDING },
                            label = { Text("Đang chuyển", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterStatus == TransferStatus.COMPLETED,
                            onClick = { filterStatus = TransferStatus.COMPLETED },
                            label = { Text("Hoàn thành", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterStatus == TransferStatus.CANCELLED,
                            onClick = { filterStatus = TransferStatus.CANCELLED },
                            label = { Text("Đã hủy", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm theo mã phiếu, kho nguồn, kho đích...") },
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

        // Removed separate search bar item

        // Transfers List
        if (filteredTransfers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không có phiếu luân chuyển nào phù hợp.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredTransfers, key = { it.id }) { trf ->
                val fromWh = warehouseMap[trf.fromId]
                val toWh = warehouseMap[trf.toId]
                val canManageFrom = canManageWarehouse(trf.fromId)
                val canManageTo = canManageWarehouse(trf.toId)

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
                                    text = trf.id,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                TransferStatusBadge(trf.status)
                            }

                            Text(
                                text = trf.timestamp,
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
                                text = "Nguồn: ${fromWh?.id ?: trf.fromId} - ${fromWh?.name ?: ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Rose600,
                                maxLines = 1,
                                modifier = Modifier.weight(1f).basicMarquee()
                            )
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Đích: ${toWh?.id ?: trf.toId} - ${toWh?.name ?: ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald700,
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
                                val itemsSummary = trf.items.joinToString(", ") { item ->
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
                                onClick = { onOpenModal(ModalState.TransferOrderDetail(trf)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Xem chi tiết & lịch sử", fontSize = 12.sp)
                            }

                            if (trf.status == TransferStatus.PENDING) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { transferToCancel = trf },
                                        enabled = canManageFrom,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Rose100),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Hủy chuyển", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { transferToConfirm = trf },
                                        enabled = canManageTo,
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

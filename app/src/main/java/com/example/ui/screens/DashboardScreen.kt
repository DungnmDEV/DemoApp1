package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.ModalState
import com.example.ui.WmsScreen
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    partners: List<Partner>,
    drivers: List<Driver>,
    warehouses: List<Warehouse>,
    products: List<Product>,
    inventory: List<InventoryItem>,
    inputOrders: List<InputOrder>,
    outputOrders: List<OutputOrder>,
    transfers: List<TransferOrder>,
    onNavigate: (WmsScreen) -> Unit,
    onOpenModal: (ModalState) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalStock = inventory.sumOf { it.qty }
    val totalOrders = inputOrders.size + outputOrders.size
    val customerCount = partners.count { it.type == PartnerType.CUSTOMER }
    val supplierCount = partners.count { it.type == PartnerType.SUPPLIER }

    val productsMap = products.associateBy { it.sku }
    val warehouseMap = warehouses.associateBy { it.id }
    val partnerMap = partners.associateBy { it.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 4 Sleek KPI Cards Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 1: Đối tác
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(WmsScreen.PARTNERS) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ĐỐI TÁC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate500,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${partners.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "+2 mới",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Emerald600
                                )
                            }
                            Text(
                                text = "$customerCount khách • $supplierCount NCC",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }

                    // KPI 2: Nhân viên / Xe
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(WmsScreen.DRIVERS_VEHICLES) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "NHÂN VIÊN/XE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate500,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${drivers.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Emerald500)
                                    )
                                    Text(
                                        text = "${drivers.size}/${drivers.size}",
                                        fontSize = 10.sp,
                                        color = Slate500,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Text(
                                text = "Đang hoạt động",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI 3: Đơn hàng
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(WmsScreen.INPUT_ORDERS) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "ĐƠN HÀNG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate500,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$totalOrders",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "Tháng này",
                                        fontSize = 10.sp,
                                        color = Slate400
                                    )
                                }
                            }
                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Slate100)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(PrimaryBlue)
                                )
                            }
                        }
                    }

                    // KPI 4: Tồn kho
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(WmsScreen.INVENTORY) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "TỒN KHO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate500,
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = totalStock.toString(),
                                        fontSize = if (totalStock > 9999) 20.sp else 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "SKU",
                                        fontSize = 10.sp,
                                        color = Slate400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${warehouses.size} kho & bãi xe",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigate(WmsScreen.INPUT_ORDERS) },
                    color = Emerald50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald100)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Emerald700, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nhập kho", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald700)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigate(WmsScreen.OUTPUT_ORDERS) },
                    color = Amber50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber100)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = Amber700, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xuất kho", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber700)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigate(WmsScreen.TRANSFERS) },
                    color = PrimaryBlueLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Luân chuyển", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                }
            }
        }

        // Sleek Recent Activity Card (Hoạt động gần đây)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate50.copy(alpha = 0.6f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hoạt động gần đây",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate800
                        )
                        Text(
                            text = "Xem tất cả",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigate(WmsScreen.INPUT_ORDERS) }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Divider(color = Slate100)

                    // Recent Activity Items List
                    val recentItems = buildList {
                        inputOrders.take(2).forEach {
                            add(ActivityItemData(
                                id = it.id,
                                prefix = "IN",
                                title = "Nhập hàng - ${warehouseMap[it.warehouseId]?.name ?: it.warehouseId}",
                                badgeBg = Emerald100,
                                badgeText = Emerald700,
                                statusBadge = { InputOrderStatusBadge(it.status) },
                                onClick = { onOpenModal(ModalState.InputOrderDetail(it)) }
                            ))
                        }
                        outputOrders.take(2).forEach {
                            add(ActivityItemData(
                                id = it.id,
                                prefix = "OUT",
                                title = "Xuất hàng - ${partnerMap[it.customerId]?.name ?: it.customerId}",
                                badgeBg = Amber100,
                                badgeText = Amber700,
                                statusBadge = { OutputOrderStatusBadge(it.status) },
                                onClick = { onOpenModal(ModalState.OutputOrderDetail(it)) }
                            ))
                        }
                        transfers.take(2).forEach {
                            val from = warehouseMap[it.fromId]?.name ?: it.fromId
                            val to = warehouseMap[it.toId]?.name ?: it.toId
                            add(ActivityItemData(
                                id = it.id,
                                prefix = "TRF",
                                title = "$from → $to",
                                badgeBg = Indigo100,
                                badgeText = Indigo600,
                                statusBadge = { TransferStatusBadge(it.status) },
                                onClick = { onOpenModal(ModalState.TransferOrderDetail(it)) }
                            ))
                        }
                    }

                    recentItems.forEachIndexed { index, act ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { act.onClick() }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Rounded square badge (IN, OUT, TRF)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(act.badgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = act.prefix,
                                        color = act.badgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = act.id,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800
                                    )
                                    Text(
                                        text = act.title,
                                        fontSize = 10.sp,
                                        color = Slate500,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            act.statusBadge()
                        }

                        if (index < recentItems.size - 1) {
                            Divider(color = Slate100, modifier = Modifier.padding(horizontal = 14.dp))
                        }
                    }
                }
            }
        }

        // Vehicle Inventory Snapshot Card
        item {
            val mobileWarehouses = warehouses.filter { it.type == WarehouseType.VAN_CHUYEN }
            if (mobileWarehouses.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HÀNG HÓA TRÊN XE VẬN CHUYỂN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate600
                        )
                        Text(
                            text = "Chi tiết",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.clickable { onNavigate(WmsScreen.INVENTORY) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        mobileWarehouses.forEach { wh ->
                            val whInv = inventory.filter { it.warehouseId == wh.id && it.qty > 0 }
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onNavigate(WmsScreen.INVENTORY) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = wh.id,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    }
                                    Text(
                                        text = wh.name,
                                        fontSize = 10.sp,
                                        color = Slate500,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                                    )

                                    Divider(color = Slate100)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (whInv.isEmpty()) {
                                        Text("Xe trống (0 SP)", fontSize = 10.sp, color = Slate400)
                                    } else {
                                        whInv.take(2).forEach { inv ->
                                            val p = productsMap[inv.sku]
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 1.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = inv.sku,
                                                    fontSize = 10.sp,
                                                    color = Slate700
                                                )
                                                Text(
                                                    text = "${inv.qty} ${p?.unit ?: ""}",
                                                    fontSize = 10.sp,
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
        }
    }
}

private data class ActivityItemData(
    val id: String,
    val prefix: String,
    val title: String,
    val badgeBg: Color,
    val badgeText: Color,
    val statusBadge: @Composable () -> Unit,
    val onClick: () -> Unit
)

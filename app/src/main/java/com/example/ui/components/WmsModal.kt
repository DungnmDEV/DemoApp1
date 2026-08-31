package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun OrderDetailModal(
    orderId: String,
    orderType: String,
    timestamp: String,
    creator: String,
    sourceLocation: String,
    destinationLocation: String,
    partnerName: String?,
    items: List<OrderItem>,
    statusBadge: @Composable () -> Unit,
    timeline: List<TimelineEvent>,
    productsMap: Map<String, Product>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = orderId,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            statusBadge()
                        }
                        Text(
                            text = "$orderType • $timestamp",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Slate500
                        )
                    }
                }

                Divider(color = Slate200, modifier = Modifier.padding(vertical = 12.dp))

                // Order Information Summary Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(10.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (partnerName != null) {
                        DetailRow(label = "Đối tác:", value = partnerName)
                    }
                    DetailRow(label = "Kho xuất / Nguồn:", value = sourceLocation)
                    DetailRow(label = "Kho nhận / Đích:", value = destinationLocation)
                    DetailRow(label = "Người tạo:", value = creator)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Items List
                Text(
                    text = "DANH SÁCH SẢN PHẨM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sản phẩm / SKU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)
                        Text("Số lượng", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)
                    }

                    items.forEachIndexed { idx, item ->
                        val p = productsMap[item.sku]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = p?.name ?: item.sku,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate900
                                )
                                Text(
                                    text = item.sku,
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }
                            Text(
                                text = "${item.qty} ${p?.unit ?: ""}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        if (idx < items.size - 1) {
                            Divider(color = Slate100)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visual Timeline History
                Text(
                    text = "LỊCH SỬ XỬ LÝ & TIẾN ĐỘ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    timeline.forEachIndexed { index, event ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Timeline dot & line
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (index == timeline.size - 1) Emerald500 else Slate400)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = event.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "${event.timestamp} • ${event.user}",
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    text = event.description,
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Đóng", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 12.sp, color = Slate800, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Warning, contentDescription = null, tint = Rose600)
                Text("Khôi phục dữ liệu demo?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Text(
                "Bạn có chắc muốn khôi phục dữ liệu demo ban đầu? Toàn bộ danh sách đơn hàng, luân chuyển và tồn kho sẽ được đặt lại như trạng thái ban đầu.",
                fontSize = 13.sp,
                color = Slate600
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Khôi phục", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Hủy")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

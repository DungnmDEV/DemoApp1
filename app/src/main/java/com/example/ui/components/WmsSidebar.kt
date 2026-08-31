package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.WmsScreen
import com.example.ui.theme.*

data class NavMenuItem(
    val screen: WmsScreen,
    val icon: ImageVector,
    val requiresAdmin: Boolean = false,
    val badgeCount: Int? = null
)

@Composable
fun WmsSidebarContent(
    currentScreen: WmsScreen,
    currentUser: User?,
    onSelectScreen: (WmsScreen) -> Unit,
    pendingInboundCount: Int = 0,
    pendingOutboundCount: Int = 0,
    pendingTransferCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val menuItems = listOf(
        NavMenuItem(WmsScreen.DASHBOARD, Icons.Outlined.Dashboard),
        NavMenuItem(WmsScreen.PARTNERS, Icons.Outlined.Business),
        NavMenuItem(WmsScreen.DRIVERS_VEHICLES, Icons.Outlined.LocalShipping),
        NavMenuItem(WmsScreen.USERS_ROLES, Icons.Outlined.AdminPanelSettings, requiresAdmin = true),
        NavMenuItem(WmsScreen.INPUT_ORDERS, Icons.Outlined.MoveToInbox, badgeCount = if (pendingInboundCount > 0) pendingInboundCount else null),
        NavMenuItem(WmsScreen.OUTPUT_ORDERS, Icons.Outlined.Outbox, badgeCount = if (pendingOutboundCount > 0) pendingOutboundCount else null),
        NavMenuItem(WmsScreen.TRANSFERS, Icons.Outlined.SyncAlt, badgeCount = if (pendingTransferCount > 0) pendingTransferCount else null),
        NavMenuItem(WmsScreen.PRODUCTS, Icons.Outlined.Category),
        NavMenuItem(WmsScreen.WAREHOUSES, Icons.Outlined.Warehouse),
        NavMenuItem(WmsScreen.INVENTORY, Icons.Outlined.Inventory2)
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Slate900)
            .padding(vertical = 16.dp)
    ) {
        // App Header Brand
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warehouse,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = "QL Kho Pro",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Hệ thống quản lý kho & vận chuyển",
                    color = Slate400,
                    fontSize = 10.sp
                )
            }
        }

        Divider(
            color = Slate800,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Menu Section Label
        Text(
            text = "MENU HỆ THỐNG",
            color = Slate500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Menu Items List
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            menuItems.forEach { item ->
                val isSelected = currentScreen == item.screen
                val isBlockedForUser = item.requiresAdmin && currentUser?.role != UserRole.ADMIN

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isSelected -> PrimaryBlue
                                else -> Color.Transparent
                            }
                        )
                        .clickable {
                            onSelectScreen(item.screen)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.screen.title,
                            tint = when {
                                isSelected -> Color.White
                                isBlockedForUser -> Slate600
                                else -> Slate300
                            },
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = item.screen.title,
                            color = when {
                                isSelected -> Color.White
                                isBlockedForUser -> Slate600
                                else -> Slate200
                            },
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }

                    // Badge counter or lock icon
                    if (isBlockedForUser) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Khóa",
                            tint = Slate600,
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (item.badgeCount != null && item.badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Amber500)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${item.badgeCount}",
                                color = if (isSelected) Color.White else Slate950,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom Footer
        Divider(
            color = Slate800,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "QL Kho Pro v1.0",
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Emerald500.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Demo Mode",
                    color = Emerald500,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    text: String,
    bgColor: Color,
    textColor: Color,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bgColor, CircleShape)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape) else Modifier)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            maxLines = 1
        )
    }
}

@Composable
fun InputOrderStatusBadge(status: InputOrderStatus) {
    when (status) {
        InputOrderStatus.PENDING -> StatusBadge(
            text = status.displayName,
            bgColor = Amber100,
            textColor = Amber700,
            borderColor = Amber100
        )
        InputOrderStatus.RECEIVED -> StatusBadge(
            text = status.displayName,
            bgColor = Emerald100,
            textColor = Emerald700,
            borderColor = Emerald100
        )
        InputOrderStatus.CANCELLED -> StatusBadge(
            text = status.displayName,
            bgColor = Rose100,
            textColor = Rose700,
            borderColor = Rose100
        )
    }
}

@Composable
fun OutputOrderStatusBadge(status: OutputOrderStatus) {
    when (status) {
        OutputOrderStatus.PENDING -> StatusBadge(
            text = status.displayName,
            bgColor = Amber100,
            textColor = Amber700,
            borderColor = Amber100
        )
        OutputOrderStatus.DELIVERED -> StatusBadge(
            text = status.displayName,
            bgColor = Emerald100,
            textColor = Emerald700,
            borderColor = Emerald100
        )
        OutputOrderStatus.CANCELLED -> StatusBadge(
            text = status.displayName,
            bgColor = Slate200,
            textColor = Slate600,
            borderColor = Slate300
        )
    }
}

@Composable
fun TransferStatusBadge(status: TransferStatus) {
    when (status) {
        TransferStatus.PENDING -> StatusBadge(
            text = status.displayName,
            bgColor = Indigo100,
            textColor = Indigo600,
            borderColor = Indigo100
        )
        TransferStatus.COMPLETED -> StatusBadge(
            text = status.displayName,
            bgColor = Emerald100,
            textColor = Emerald700,
            borderColor = Emerald100
        )
        TransferStatus.CANCELLED -> StatusBadge(
            text = status.displayName,
            bgColor = Rose100,
            textColor = Rose700,
            borderColor = Rose100
        )
    }
}

@Composable
fun RoleBadge(role: UserRole) {
    when (role) {
        UserRole.ADMIN -> StatusBadge(
            text = role.displayName,
            bgColor = Rose100,
            textColor = Rose700,
            borderColor = Rose100
        )
        UserRole.MANAGER -> StatusBadge(
            text = role.displayName,
            bgColor = PrimaryBlueLight,
            textColor = PrimaryBlueHover,
            borderColor = PrimaryBlueBorder
        )
    }
}

@Composable
fun WarehouseTypeBadge(type: WarehouseType) {
    val (bg, text, border) = when (type) {
        WarehouseType.KHO_TONG -> Triple(Indigo100, Indigo600, Indigo100)
        WarehouseType.KHO_CHI_NHANH -> Triple(PrimaryBlueLight, PrimaryBlueHover, PrimaryBlueBorder)
        WarehouseType.BAI_XE -> Triple(Amber100, Amber700, Amber100)
        WarehouseType.VAN_CHUYEN -> Triple(Emerald100, Emerald700, Emerald100)
    }
    StatusBadge(
        text = type.displayName,
        bgColor = bg,
        textColor = text,
        borderColor = border
    )
}

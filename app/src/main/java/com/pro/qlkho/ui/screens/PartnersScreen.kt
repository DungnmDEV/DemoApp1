package com.pro.qlkho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.pro.qlkho.data.model.Partner
import com.pro.qlkho.data.model.PartnerType
import com.pro.qlkho.ui.components.StatusBadge
import com.pro.qlkho.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersScreen(
    partners: List<Partner>,
    onSavePartner: (Partner, Boolean) -> Unit,
    onDeletePartner: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var partnerId by remember { mutableStateOf("") }
    var partnerName by remember { mutableStateOf("") }
    var partnerType by remember { mutableStateOf(PartnerType.SUPPLIER) }
    var partnerPhone by remember { mutableStateOf("") }
    var partnerAddress by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf<PartnerType?>(null) }
    var partnerToDelete by remember { mutableStateOf<Partner?>(null) }

    val filteredPartners = partners.filter { p ->
        (filterType == null || p.type == filterType) &&
                (p.id.contains(searchQuery, ignoreCase = true) ||
                        p.name.contains(searchQuery, ignoreCase = true) ||
                        p.phone.contains(searchQuery, ignoreCase = true) ||
                        p.address.contains(searchQuery, ignoreCase = true))
    }

    fun resetForm() {
        isEditMode = false
        partnerId = ""
        partnerName = ""
        partnerType = PartnerType.SUPPLIER
        partnerPhone = ""
        partnerAddress = ""
    }

    if (partnerToDelete != null) {
        AlertDialog(
            onDismissRequest = { partnerToDelete = null },
            title = { Text("Xác nhận xóa đối tác?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa đối tác '${partnerToDelete?.name}' (${partnerToDelete?.id}) không?") },
            confirmButton = {
                Button(
                    onClick = {
                        partnerToDelete?.id?.let { onDeletePartner(it) }
                        partnerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { partnerToDelete = null }) {
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
        // Top Form Card (Add/Edit Partner)
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
                            text = if (isEditMode) "Chỉnh sửa thông tin đối tác" else "Thêm đối tác mới",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = partnerId,
                            onValueChange = { if (!isEditMode) partnerId = it },
                            label = { Text("Mã đối tác") },
                            placeholder = { Text("DT001, KH001...") },
                            enabled = !isEditMode,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("Tên đơn vị / công ty") },
                            placeholder = { Text("Tên công ty...") },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = partnerPhone,
                        onValueChange = { partnerPhone = it },
                        label = { Text("Số điện thoại") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Type Selection (Moved to its own row for full width to prevent vertical wrapping)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Phân loại đối tác:", fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = partnerType == PartnerType.SUPPLIER,
                                onClick = { partnerType = PartnerType.SUPPLIER },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Nhà cung cấp", fontSize = 12.sp, maxLines = 1) 
                                    }
                                }
                            )
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = partnerType == PartnerType.CUSTOMER,
                                onClick = { partnerType = PartnerType.CUSTOMER },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Khách hàng", fontSize = 12.sp, maxLines = 1) 
                                    }
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = partnerAddress,
                        onValueChange = { partnerAddress = it },
                        label = { Text("Địa chỉ") },
                        placeholder = { Text("Số nhà, đường, quận, tỉnh/TP...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (partnerId.isNotBlank() && partnerName.isNotBlank()) {
                                onSavePartner(
                                    Partner(
                                        id = partnerId.trim(),
                                        name = partnerName.trim(),
                                        type = partnerType,
                                        phone = partnerPhone.trim(),
                                        address = partnerAddress.trim()
                                    ),
                                    isEditMode
                                )
                                resetForm()
                            }
                        },
                        enabled = partnerId.isNotBlank() && partnerName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditMode) "Cập nhật đối tác" else "Lưu thông tin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Table & Search section (Combined to prevent vertical wrapping of chips)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DANH SÁCH ĐỐI TÁC (${filteredPartners.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).basicMarquee()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = filterType == null,
                            onClick = { filterType = null },
                            label = { Text("Tất cả", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterType == PartnerType.SUPPLIER,
                            onClick = { filterType = PartnerType.SUPPLIER },
                            label = { Text("Nhà cung cấp", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = filterType == PartnerType.CUSTOMER,
                            onClick = { filterType = PartnerType.CUSTOMER },
                            label = { Text("Khách hàng", fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm theo mã, tên đơn vị, SĐT hoặc địa chỉ...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Xóa tìm kiếm", tint = Slate400)
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

        // List of Partners
        if (filteredPartners.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không tìm thấy đối tác nào phù hợp.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredPartners, key = { it.id }) { partner ->
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
                                    text = partner.id,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                StatusBadge(
                                    text = partner.type.displayName,
                                    bgColor = if (partner.type == PartnerType.SUPPLIER) Emerald50 else Amber50,
                                    textColor = if (partner.type == PartnerType.SUPPLIER) Emerald700 else Amber700,
                                    borderColor = if (partner.type == PartnerType.SUPPLIER) Emerald100 else Amber100
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = partner.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (partner.phone.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically, 
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Outlined.Phone, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                        Text(text = partner.phone, fontSize = 11.sp, color = Slate600, maxLines = 1, modifier = Modifier.basicMarquee())
                                    }
                                }
                                if (partner.address.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.weight(1.5f),
                                        verticalAlignment = Alignment.CenterVertically, 
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                                        Text(text = partner.address, fontSize = 11.sp, color = Slate600, maxLines = 1, modifier = Modifier.basicMarquee())
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    isEditMode = true
                                    partnerId = partner.id
                                    partnerName = partner.name
                                    partnerType = partner.type
                                    partnerPhone = partner.phone
                                    partnerAddress = partner.address
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = { partnerToDelete = partner }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Rose600, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

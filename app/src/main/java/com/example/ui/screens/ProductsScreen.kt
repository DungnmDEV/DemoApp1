package com.example.ui.screens

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
import com.example.data.model.Product
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    products: List<Product>,
    getTotalInventory: (String) -> Int,
    onSaveProduct: (Product, Boolean) -> Unit,
    onDeleteProduct: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var sku by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var productUnit by remember { mutableStateOf("Thùng") }

    var searchQuery by remember { mutableStateOf("") }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = products.filter { p ->
        p.sku.contains(searchQuery, ignoreCase = true) ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.unit.contains(searchQuery, ignoreCase = true)
    }

    fun resetForm() {
        isEditMode = false
        sku = ""
        productName = ""
        productUnit = "Thùng"
    }

    if (productToDelete != null) {
        val totalQty = getTotalInventory(productToDelete!!.sku)
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Xác nhận xóa sản phẩm?", fontWeight = FontWeight.Bold) },
            text = {
                if (totalQty > 0) {
                    Text("Không thể xóa sản phẩm '${productToDelete?.name}' (${productToDelete?.sku}) vì vẫn còn $totalQty tồn kho trong hệ thống!", color = Rose600)
                } else {
                    Text("Bạn có chắc muốn xóa sản phẩm '${productToDelete?.name}' (${productToDelete?.sku}) không?")
                }
            },
            confirmButton = {
                if (totalQty == 0) {
                    Button(
                        onClick = {
                            productToDelete?.sku?.let { onDeleteProduct(it) }
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                    ) {
                        Text("Xóa")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text(if (totalQty > 0) "Đã hiểu" else "Hủy")
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
        // Form Card (Add/Edit Product)
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
                            text = if (isEditMode) "Chỉnh sửa thông tin sản phẩm" else "Thêm sản phẩm mới vào danh mục",
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
                            value = sku,
                            onValueChange = { if (!isEditMode) sku = it },
                            label = { Text("Mã SKU", maxLines = 1, modifier = Modifier.basicMarquee()) },
                            placeholder = { Text("SKU-88, SKU-99...") },
                            enabled = !isEditMode,
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = productUnit,
                            onValueChange = { productUnit = it },
                            label = { Text("Đơn vị tính", maxLines = 1, modifier = Modifier.basicMarquee()) },
                            placeholder = { Text("Thùng, Cái, Bộ...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Tên sản phẩm / Quy cách", maxLines = 1, modifier = Modifier.basicMarquee()) },
                        placeholder = { Text("Thùng Carton X...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (sku.isNotBlank() && productName.isNotBlank()) {
                                onSaveProduct(
                                    Product(
                                        sku = sku.trim().uppercase(),
                                        name = productName.trim(),
                                        unit = productUnit.trim().ifBlank { "Cái" }
                                    ),
                                    isEditMode
                                )
                                resetForm()
                            }
                        },
                        enabled = sku.isNotBlank() && productName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "Cập nhật sản phẩm" else "Lưu sản phẩm vào danh mục", 
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }
        }

        // Search & List section (Combined to reduce gaps)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DANH MỤC SẢN PHẨM (${filteredProducts.size})",
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
                    placeholder = { Text("Tìm theo SKU, tên sản phẩm, đơn vị tính...") },
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

        // Products List
        if (filteredProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Không tìm thấy sản phẩm nào.", color = Slate500, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredProducts, key = { it.sku }) { product ->
                val totalQty = getTotalInventory(product.sku)
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
                                    text = product.sku,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                                StatusBadge(
                                    text = product.unit,
                                    bgColor = Slate100,
                                    textColor = Slate800,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Tổng tồn hệ thống:",
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    maxLines = 1
                                )
                                Text(
                                    text = "$totalQty ${product.unit}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalQty > 0) Emerald700 else Amber700,
                                    maxLines = 1,
                                    modifier = Modifier.basicMarquee()
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    isEditMode = true
                                    sku = product.sku
                                    productName = product.name
                                    productUnit = product.unit
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = { productToDelete = product }
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

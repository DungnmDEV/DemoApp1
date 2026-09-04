package com.pro.qlkho.data.model

enum class UserRole(val displayName: String) {
    ADMIN("Quản trị viên"),
    MANAGER("Quản lý kho")
}

data class User(
    val id: String,
    val username: String,
    val password: String,
    val name: String,
    val role: UserRole,
    val warehouseId: String? = null
)

enum class PartnerType(val displayName: String) {
    SUPPLIER("Nhà cung cấp"),
    CUSTOMER("Khách hàng")
}

data class Partner(
    val id: String,
    val name: String,
    val type: PartnerType,
    val phone: String,
    val address: String
)

data class Driver(
    val id: String,
    val name: String,
    val plate: String,
    val phone: String
)

enum class WarehouseType(val displayName: String) {
    KHO_TONG("KHO TỔNG"),
    KHO_CHI_NHANH("KHO CHI NHÁNH"),
    BAI_XE("BÃI XE"),
    VAN_CHUYEN("VẬN CHUYỂN")
}

data class Warehouse(
    val id: String,
    val name: String,
    val type: WarehouseType,
    val location: String
)

data class Product(
    val sku: String,
    val name: String,
    val unit: String
)

data class InventoryItem(
    val warehouseId: String,
    val sku: String,
    val qty: Int
)

data class OrderItem(
    val sku: String,
    val qty: Int
)

data class TimelineEvent(
    val timestamp: String,
    val title: String,
    val description: String,
    val user: String
)

enum class InputOrderStatus(val displayName: String) {
    PENDING("Chờ xử lý"),
    RECEIVED("Đã nhận"),
    CANCELLED("Đã hủy")
}

data class InputOrder(
    val id: String,
    val timestamp: String,
    val supplierId: String,
    val warehouseId: String,
    val items: List<OrderItem>,
    val status: InputOrderStatus = InputOrderStatus.PENDING,
    val createdBy: String,
    val history: List<TimelineEvent> = emptyList()
)

enum class OutputOrderStatus(val displayName: String) {
    PENDING("Chờ xử lý"),
    DELIVERED("Đã xuất"),
    CANCELLED("Đã hủy")
}

data class OutputOrder(
    val id: String,
    val timestamp: String,
    val warehouseId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val status: OutputOrderStatus = OutputOrderStatus.PENDING,
    val createdBy: String,
    val history: List<TimelineEvent> = emptyList()
)

enum class TransferStatus(val displayName: String) {
    PENDING("Đang vận chuyển"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy")
}

data class TransferOrder(
    val id: String,
    val timestamp: String,
    val fromId: String,
    val toId: String,
    val items: List<OrderItem>,
    val status: TransferStatus = TransferStatus.PENDING,
    val createdBy: String,
    val confirmedBy: String? = null,
    val history: List<TimelineEvent> = emptyList()
)

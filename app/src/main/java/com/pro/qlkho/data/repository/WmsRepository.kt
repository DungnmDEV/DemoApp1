package com.pro.qlkho.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pro.qlkho.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WmsRepository private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wms_prefs", Context.MODE_PRIVATE)

    // State flows for real-time reactivity
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _partners = MutableStateFlow<List<Partner>>(emptyList())
    val partners: StateFlow<List<Partner>> = _partners.asStateFlow()

    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _warehouses = MutableStateFlow<List<Warehouse>>(emptyList())
    val warehouses: StateFlow<List<Warehouse>> = _warehouses.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _inputOrders = MutableStateFlow<List<InputOrder>>(emptyList())
    val inputOrders: StateFlow<List<InputOrder>> = _inputOrders.asStateFlow()

    private val _outputOrders = MutableStateFlow<List<OutputOrder>>(emptyList())
    val outputOrders: StateFlow<List<OutputOrder>> = _outputOrders.asStateFlow()

    private val _transfers = MutableStateFlow<List<TransferOrder>>(emptyList())
    val transfers: StateFlow<List<TransferOrder>> = _transfers.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadData()
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentTimeOnly(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // Default Seed Data
    private fun getDefaultUsers(): List<User> = listOf(
        User("1", "admin", "admin123", "Quản trị viên", UserRole.ADMIN, null),
        User("2", "qlkho1", "123456", "Nguyễn Minh Anh", UserRole.MANAGER, "KH001"),
        User("3", "taixe1", "123456", "Trần Văn Nam", UserRole.MANAGER, "XE-01"),
        User("4", "qlkho2", "123456", "Lê Văn Bình", UserRole.MANAGER, "KH002")
    )

    private fun getDefaultPartners(): List<Partner> = listOf(
        Partner("DT001", "Công ty Hóa Chất Việt", PartnerType.SUPPLIER, "0903112233", "KCN Tân Bình, TP.HCM"),
        Partner("DT002", "Công ty Điện Máy ABC", PartnerType.SUPPLIER, "0908889900", "KCN Sóng Thần, Bình Dương"),
        Partner("KH001", "Đại lý Điện Máy Minh Long", PartnerType.CUSTOMER, "0912345678", "Q.10, TP.HCM"),
        Partner("KH002", "Cửa hàng Gia Dụng 24H", PartnerType.CUSTOMER, "0987654321", "TP. Thủ Đức, TP.HCM")
    )

    private fun getDefaultDrivers(): List<Driver> = listOf(
        Driver("XE-01", "Trần Văn Nam", "29H-123.45", "0901234567"),
        Driver("XE-02", "Phạm Văn Hùng", "60C-888.99", "0909876543")
    )

    private fun getDefaultWarehouses(): List<Warehouse> = listOf(
        Warehouse("KH001", "Kho Tổng Miền Nam", WarehouseType.KHO_TONG, "TP. Hồ Chí Minh"),
        Warehouse("KH002", "Kho Bình Dương", WarehouseType.KHO_CHI_NHANH, "Bình Dương"),
        Warehouse("XE-01", "Xe 29H-123.45 (Trần Văn Nam)", WarehouseType.VAN_CHUYEN, "Di động"),
        Warehouse("XE-02", "Xe 60C-888.99 (Phạm Văn Hùng)", WarehouseType.VAN_CHUYEN, "Di động")
    )

    private fun getDefaultProducts(): List<Product> = listOf(
        Product("SKU-88", "Thùng Carton X", "Thùng"),
        Product("SKU-99", "Thiết bị điện tử Y", "Cái"),
        Product("SKU-100", "Linh kiện Z", "Bộ"),
        Product("SKU-101", "Bao bì đóng gói", "Cuộn")
    )

    private fun getDefaultInventory(): List<InventoryItem> = listOf(
        InventoryItem("KH001", "SKU-88", 1000),
        InventoryItem("KH001", "SKU-99", 500),
        InventoryItem("KH001", "SKU-100", 300),
        InventoryItem("KH002", "SKU-88", 500),
        InventoryItem("KH002", "SKU-99", 200),
        InventoryItem("XE-01", "SKU-88", 100),
        InventoryItem("XE-01", "SKU-99", 50),
        InventoryItem("XE-02", "SKU-88", 80),
        InventoryItem("XE-02", "SKU-100", 30)
    )

    private fun getDefaultInputOrders(): List<InputOrder> = listOf(
        InputOrder(
            id = "IN-1024",
            timestamp = "2026-08-30 09:30",
            supplierId = "DT001",
            warehouseId = "KH001",
            items = listOf(OrderItem("SKU-88", 200)),
            status = InputOrderStatus.RECEIVED,
            createdBy = "admin",
            history = listOf(
                TimelineEvent("09:30", "Tạo đơn nhập hàng", "Đơn nhập từ Công ty Hóa Chất Việt", "admin"),
                TimelineEvent("09:45", "Xác nhận nhận hàng", "Kho Tổng Miền Nam đã nhận đủ 200 thùng", "qlkho1")
            )
        )
    )

    private fun getDefaultOutputOrders(): List<OutputOrder> = listOf(
        OutputOrder(
            id = "OUT-1023",
            timestamp = "2026-08-30 14:15",
            warehouseId = "KH001",
            customerId = "KH001",
            items = listOf(OrderItem("SKU-99", 50)),
            status = OutputOrderStatus.PENDING,
            createdBy = "qlkho1",
            history = listOf(
                TimelineEvent("14:15", "Tạo đơn xuất hàng", "Đơn xuất cho Đại lý Điện Máy Minh Long", "qlkho1")
            )
        )
    )

    private fun getDefaultTransfers(): List<TransferOrder> = listOf(
        TransferOrder(
            id = "TRF-1022",
            timestamp = "2026-08-31 08:00",
            fromId = "KH001",
            toId = "XE-01",
            items = listOf(OrderItem("SKU-88", 100)),
            status = TransferStatus.PENDING,
            createdBy = "qlkho1",
            history = listOf(
                TimelineEvent("08:00", "Lập phiếu chuyển hàng", "Kho Tổng Miền Nam chuyển 100 thùng tới Xe 29H-123.45", "qlkho1"),
                TimelineEvent("08:05", "Đang vận chuyển", "Đã trừ 100 thùng tại Kho Tổng Miền Nam", "Hệ thống")
            )
        )
    )

    private fun loadData() {
        // Load or initialize default
        val hasData = prefs.getBoolean("has_initialized", false)
        if (!hasData) {
            resetData()
            return
        }

        try {
            _users.value = loadUsersJson()
            _partners.value = loadPartnersJson()
            _drivers.value = loadDriversJson()
            _warehouses.value = loadWarehousesJson()
            _products.value = loadProductsJson()
            _inventory.value = loadInventoryJson()
            _inputOrders.value = loadInputOrdersJson()
            _outputOrders.value = loadOutputOrdersJson()
            _transfers.value = loadTransfersJson()

            // Always require fresh login on app start
            prefs.edit().remove("current_username").apply()
            _currentUser.value = null
        } catch (e: Exception) {
            resetData()
        }
    }

    fun resetData() {
        _users.value = getDefaultUsers()
        _partners.value = getDefaultPartners()
        _drivers.value = getDefaultDrivers()
        _warehouses.value = getDefaultWarehouses()
        _products.value = getDefaultProducts()
        _inventory.value = getDefaultInventory()
        _inputOrders.value = getDefaultInputOrders()
        _outputOrders.value = getDefaultOutputOrders()
        _transfers.value = getDefaultTransfers()
        _currentUser.value = null // Start with no user (require login)

        saveAll()
        prefs.edit().putBoolean("has_initialized", true).apply()
    }

    private fun saveAll() {
        saveUsersJson(_users.value)
        savePartnersJson(_partners.value)
        saveDriversJson(_drivers.value)
        saveWarehousesJson(_warehouses.value)
        saveProductsJson(_products.value)
        saveInventoryJson(_inventory.value)
        saveInputOrdersJson(_inputOrders.value)
        saveOutputOrdersJson(_outputOrders.value)
        saveTransfersJson(_transfers.value)
        prefs.edit().putString("current_username", _currentUser.value?.username).apply()
    }

    // Auth
    fun login(username: String, pass: String): Result<User> {
        val user = _users.value.find { it.username.equals(username.trim(), ignoreCase = true) && it.password == pass }
        return if (user != null) {
            _currentUser.value = user
            prefs.edit().putString("current_username", user.username).apply()
            Result.success(user)
        } else {
            Result.failure(Exception("Sai tên đăng nhập hoặc mật khẩu."))
        }
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().remove("current_username").apply()
    }

    // Inventory Helpers
    fun getInventory(warehouseId: String, sku: String): Int {
        return _inventory.value.find { it.warehouseId == warehouseId && it.sku == sku }?.qty ?: 0
    }

    fun getTotalInventoryForProduct(sku: String): Int {
        return _inventory.value.filter { it.sku == sku }.sumOf { it.qty }
    }

    fun getInventoryForWarehouse(warehouseId: String): List<InventoryItem> {
        return _inventory.value.filter { it.warehouseId == warehouseId }
    }

    private fun updateStock(warehouseId: String, sku: String, delta: Int) {
        val currentList = _inventory.value.toMutableList()
        val index = currentList.indexOfFirst { it.warehouseId == warehouseId && it.sku == sku }
        if (index != -1) {
            val updatedQty = (currentList[index].qty + delta).coerceAtLeast(0)
            currentList[index] = currentList[index].copy(qty = updatedQty)
        } else if (delta > 0) {
            currentList.add(InventoryItem(warehouseId, sku, delta))
        }
        _inventory.value = currentList
        saveInventoryJson(currentList)
    }

    // Partner CRUD
    fun addPartner(partner: Partner): Result<Unit> {
        if (_partners.value.any { it.id.equals(partner.id.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("Mã đối tác '${partner.id}' đã tồn tại!"))
        }
        val updated = _partners.value + partner.copy(id = partner.id.trim(), name = partner.name.trim())
        _partners.value = updated
        savePartnersJson(updated)
        return Result.success(Unit)
    }

    fun updatePartner(partner: Partner): Result<Unit> {
        val updated = _partners.value.map { if (it.id == partner.id) partner else it }
        _partners.value = updated
        savePartnersJson(updated)
        return Result.success(Unit)
    }

    fun deletePartner(id: String): Result<Unit> {
        val updated = _partners.value.filterNot { it.id == id }
        _partners.value = updated
        savePartnersJson(updated)
        return Result.success(Unit)
    }

    // Driver & Vehicle CRUD (Auto creates Mobile Warehouse)
    fun addDriver(driver: Driver): Result<Unit> {
        val driverId = driver.id.trim()
        if (_drivers.value.any { it.id.equals(driverId, ignoreCase = true) }) {
            return Result.failure(Exception("Mã xe/nhân viên '$driverId' đã tồn tại!"))
        }
        val updatedDrivers = _drivers.value + driver.copy(id = driverId, name = driver.name.trim(), plate = driver.plate.trim())
        _drivers.value = updatedDrivers
        saveDriversJson(updatedDrivers)

        // Automatically create mobile warehouse
        val mobileWarehouse = Warehouse(
            id = driverId,
            name = "Xe ${driver.plate.trim()} (${driver.name.trim()})",
            type = WarehouseType.VAN_CHUYEN,
            location = "Di động"
        )
        if (!_warehouses.value.any { it.id.equals(driverId, ignoreCase = true) }) {
            val updatedWarehouses = _warehouses.value + mobileWarehouse
            _warehouses.value = updatedWarehouses
            saveWarehousesJson(updatedWarehouses)
        }
        return Result.success(Unit)
    }

    fun updateDriver(driver: Driver): Result<Unit> {
        val updatedDrivers = _drivers.value.map { if (it.id == driver.id) driver else it }
        _drivers.value = updatedDrivers
        saveDriversJson(updatedDrivers)

        // Also update corresponding mobile warehouse name
        val updatedWarehouses = _warehouses.value.map {
            if (it.id == driver.id) {
                it.copy(name = "Xe ${driver.plate.trim()} (${driver.name.trim()})")
            } else it
        }
        _warehouses.value = updatedWarehouses
        saveWarehousesJson(updatedWarehouses)
        return Result.success(Unit)
    }

    fun deleteDriver(id: String): Result<Unit> {
        val updatedDrivers = _drivers.value.filterNot { it.id == id }
        _drivers.value = updatedDrivers
        saveDriversJson(updatedDrivers)
        return Result.success(Unit)
    }

    // User CRUD
    fun addUser(user: User): Result<Unit> {
        val username = user.username.trim()
        if (_users.value.any { it.username.equals(username, ignoreCase = true) }) {
            return Result.failure(Exception("Tên đăng nhập '$username' đã tồn tại!"))
        }
        if (user.role == UserRole.MANAGER && user.warehouseId.isNullOrBlank()) {
            return Result.failure(Exception("Quản lý kho bắt buộc phải chọn Kho/Xe quản lý!"))
        }
        val newUser = user.copy(
            id = (System.currentTimeMillis() % 100000).toString(),
            username = username,
            name = user.name.trim()
        )
        val updated = _users.value + newUser
        _users.value = updated
        saveUsersJson(updated)
        return Result.success(Unit)
    }

    fun updateUser(user: User): Result<Unit> {
        val updated = _users.value.map { if (it.id == user.id) user else it }
        _users.value = updated
        saveUsersJson(updated)
        return Result.success(Unit)
    }

    fun deleteUser(id: String): Result<Unit> {
        if (id == "1" || _users.value.find { it.id == id }?.username == "admin") {
            return Result.failure(Exception("Không thể xóa tài khoản Quản trị viên mặc định!"))
        }
        val updated = _users.value.filterNot { it.id == id }
        _users.value = updated
        saveUsersJson(updated)
        return Result.success(Unit)
    }

    // Product CRUD
    fun addProduct(product: Product): Result<Unit> {
        val sku = product.sku.trim().uppercase()
        if (_products.value.any { it.sku.equals(sku, ignoreCase = true) }) {
            return Result.failure(Exception("Mã SKU '$sku' đã tồn tại trong hệ thống!"))
        }
        val updated = _products.value + product.copy(sku = sku, name = product.name.trim(), unit = product.unit.trim())
        _products.value = updated
        saveProductsJson(updated)
        return Result.success(Unit)
    }

    fun updateProduct(product: Product): Result<Unit> {
        val updated = _products.value.map { if (it.sku == product.sku) product else it }
        _products.value = updated
        saveProductsJson(updated)
        return Result.success(Unit)
    }

    fun deleteProduct(sku: String): Result<Unit> {
        val totalQty = getTotalInventoryForProduct(sku)
        if (totalQty > 0) {
            return Result.failure(Exception("Không thể xóa sản phẩm '$sku' vì vẫn còn $totalQty tồn kho trong hệ thống!"))
        }
        val updated = _products.value.filterNot { it.sku == sku }
        _products.value = updated
        saveProductsJson(updated)
        return Result.success(Unit)
    }

    // Warehouse CRUD
    fun addWarehouse(warehouse: Warehouse): Result<Unit> {
        val id = warehouse.id.trim().uppercase()
        if (_warehouses.value.any { it.id.equals(id, ignoreCase = true) }) {
            return Result.failure(Exception("Mã kho '$id' đã tồn tại!"))
        }
        val updated = _warehouses.value + warehouse.copy(id = id, name = warehouse.name.trim())
        _warehouses.value = updated
        saveWarehousesJson(updated)
        return Result.success(Unit)
    }

    fun updateWarehouse(warehouse: Warehouse): Result<Unit> {
        val updated = _warehouses.value.map { if (it.id == warehouse.id) warehouse else it }
        _warehouses.value = updated
        saveWarehousesJson(updated)
        return Result.success(Unit)
    }

    fun deleteWarehouse(id: String): Result<Unit> {
        val stockCount = _inventory.value.filter { it.warehouseId == id }.sumOf { it.qty }
        if (stockCount > 0) {
            return Result.failure(Exception("Kho '$id' còn $stockCount sản phẩm tồn kho, không thể xóa!"))
        }
        val updated = _warehouses.value.filterNot { it.id == id }
        _warehouses.value = updated
        saveWarehousesJson(updated)
        return Result.success(Unit)
    }

    // --- Inbound Orders (Đơn hàng đầu vào) ---
    fun createInputOrder(supplierId: String, warehouseId: String, items: List<OrderItem>, createdBy: String): Result<InputOrder> {
        if (items.isEmpty() || items.all { it.qty <= 0 }) {
            return Result.failure(Exception("Vui lòng chọn ít nhất 1 sản phẩm với số lượng > 0"))
        }
        val nextNum = 1000 + _inputOrders.value.size + 1
        val orderId = "IN-$nextNum"
        val timestamp = getCurrentTimestamp()
        val supplier = _partners.value.find { it.id == supplierId }?.name ?: supplierId
        val warehouse = _warehouses.value.find { it.id == warehouseId }?.name ?: warehouseId

        val newOrder = InputOrder(
            id = orderId,
            timestamp = timestamp,
            supplierId = supplierId,
            warehouseId = warehouseId,
            items = items.filter { it.qty > 0 },
            status = InputOrderStatus.PENDING,
            createdBy = createdBy,
            history = listOf(
                TimelineEvent(getCurrentTimeOnly(), "Tạo đơn nhập hàng", "Lập đơn từ $supplier vào $warehouse", createdBy)
            )
        )
        val updated = listOf(newOrder) + _inputOrders.value
        _inputOrders.value = updated
        saveInputOrdersJson(updated)
        return Result.success(newOrder)
    }

    fun confirmInputOrder(orderId: String, confirmedBy: String): Result<Unit> {
        val order = _inputOrders.value.find { it.id == orderId }
            ?: return Result.failure(Exception("Không tìm thấy đơn hàng $orderId"))

        if (order.status != InputOrderStatus.PENDING) {
            return Result.failure(Exception("Đơn hàng này đã được xử lý trước đó!"))
        }

        // Increase inventory of destination warehouse
        order.items.forEach { item ->
            updateStock(order.warehouseId, item.sku, item.qty)
        }

        val warehouse = _warehouses.value.find { it.id == order.warehouseId }?.name ?: order.warehouseId
        val newEvent = TimelineEvent(getCurrentTimeOnly(), "Xác nhận nhận hàng", "Đã nhận hàng vào $warehouse. Tồn kho đã tăng.", confirmedBy)

        val updated = _inputOrders.value.map {
            if (it.id == orderId) {
                it.copy(status = InputOrderStatus.RECEIVED, history = it.history + newEvent)
            } else it
        }
        _inputOrders.value = updated
        saveInputOrdersJson(updated)
        return Result.success(Unit)
    }

    fun cancelInputOrder(orderId: String, cancelledBy: String): Result<Unit> {
        val order = _inputOrders.value.find { it.id == orderId }
            ?: return Result.failure(Exception("Không tìm thấy đơn hàng $orderId"))

        if (order.status != InputOrderStatus.PENDING) {
            return Result.failure(Exception("Chỉ có thể hủy đơn hàng đang ở trạng thái Chờ xử lý!"))
        }

        val newEvent = TimelineEvent(getCurrentTimeOnly(), "Hủy đơn nhập hàng", "Đơn hàng đã bị hủy.", cancelledBy)
        val updated = _inputOrders.value.map {
            if (it.id == orderId) {
                it.copy(status = InputOrderStatus.CANCELLED, history = it.history + newEvent)
            } else it
        }
        _inputOrders.value = updated
        saveInputOrdersJson(updated)
        return Result.success(Unit)
    }

    // --- Outbound Orders (Đơn hàng đầu ra) ---
    fun createOutputOrder(warehouseId: String, customerId: String, items: List<OrderItem>, createdBy: String): Result<OutputOrder> {
        if (items.isEmpty() || items.all { it.qty <= 0 }) {
            return Result.failure(Exception("Vui lòng chọn ít nhất 1 sản phẩm với số lượng > 0"))
        }

        // Check stock availability
        for (item in items.filter { it.qty > 0 }) {
            val available = getInventory(warehouseId, item.sku)
            if (item.qty > available) {
                val pName = _products.value.find { it.sku == item.sku }?.name ?: item.sku
                return Result.failure(Exception("Không đủ tồn kho cho $pName ($item.sku). Hiện còn: $available, yêu cầu xuất: ${item.qty}"))
            }
        }

        // DEDUCT stock immediately from warehouse
        val validItems = items.filter { it.qty > 0 }
        validItems.forEach { item ->
            updateStock(warehouseId, item.sku, -item.qty)
        }

        val nextNum = 1000 + _outputOrders.value.size + 1
        val orderId = "OUT-$nextNum"
        val timestamp = getCurrentTimestamp()
        val customer = _partners.value.find { it.id == customerId }?.name ?: customerId
        val warehouse = _warehouses.value.find { it.id == warehouseId }?.name ?: warehouseId

        val newOrder = OutputOrder(
            id = orderId,
            timestamp = timestamp,
            warehouseId = warehouseId,
            customerId = customerId,
            items = validItems,
            status = OutputOrderStatus.PENDING,
            createdBy = createdBy,
            history = listOf(
                TimelineEvent(getCurrentTimeOnly(), "Tạo đơn xuất kho", "Xuất từ $warehouse cho $customer", createdBy),
                TimelineEvent(getCurrentTimeOnly(), "Đã trừ tồn kho", "Hệ thống đã trừ hàng tại $warehouse ngay khi lập đơn.", "Hệ thống")
            )
        )
        val updated = listOf(newOrder) + _outputOrders.value
        _outputOrders.value = updated
        saveOutputOrdersJson(updated)
        return Result.success(newOrder)
    }

    fun confirmOutputOrder(orderId: String, confirmedBy: String): Result<Unit> {
        val order = _outputOrders.value.find { it.id == orderId }
            ?: return Result.failure(Exception("Không tìm thấy đơn hàng $orderId"))

        if (order.status != OutputOrderStatus.PENDING) {
            return Result.failure(Exception("Đơn hàng này đã được xử lý trước đó!"))
        }

        // Double check stock
        for (item in order.items) {
            val available = getInventory(order.warehouseId, item.sku)
            if (item.qty > available) {
                return Result.failure(Exception("Không đủ tồn kho để xuất! (Hiện có $available, yêu cầu ${item.qty})"))
            }
        }

        val warehouse = _warehouses.value.find { it.id == order.warehouseId }?.name ?: order.warehouseId
        val newEvent = TimelineEvent(getCurrentTimeOnly(), "Xác nhận xuất hàng", "Đã xác nhận xuất hàng thành công từ $warehouse.", confirmedBy)

        val updated = _outputOrders.value.map {
            if (it.id == orderId) {
                it.copy(status = OutputOrderStatus.DELIVERED, history = it.history + newEvent)
            } else it
        }
        _outputOrders.value = updated
        saveOutputOrdersJson(updated)
        return Result.success(Unit)
    }

    fun cancelOutputOrder(orderId: String, cancelledBy: String): Result<Unit> {
        val order = _outputOrders.value.find { it.id == orderId }
            ?: return Result.failure(Exception("Không tìm thấy đơn hàng $orderId"))

        if (order.status != OutputOrderStatus.PENDING) {
            return Result.failure(Exception("Chỉ có thể hủy đơn hàng đang Chờ xử lý!"))
        }

        // RESTORE stock back to warehouse
        order.items.forEach { item ->
            updateStock(order.warehouseId, item.sku, item.qty)
        }

        val warehouse = _warehouses.value.find { it.id == order.warehouseId }?.name ?: order.warehouseId
        val newEvent = TimelineEvent(getCurrentTimeOnly(), "Hủy đơn xuất kho", "Đơn hàng đã bị hủy. Đã hoàn lại tồn kho cho $warehouse.", cancelledBy)
        val updated = _outputOrders.value.map {
            if (it.id == orderId) {
                it.copy(status = OutputOrderStatus.CANCELLED, history = it.history + newEvent)
            } else it
        }
        _outputOrders.value = updated
        saveOutputOrdersJson(updated)
        return Result.success(Unit)
    }

    // --- Internal Transfers (Luân chuyển nội bộ - CRITICAL WORKFLOW) ---
    fun createTransfer(fromId: String, toId: String, items: List<OrderItem>, createdBy: String): Result<TransferOrder> {
        if (fromId == toId) {
            return Result.failure(Exception("Kho nguồn và kho đích không được trùng nhau!"))
        }
        if (items.isEmpty() || items.all { it.qty <= 0 }) {
            return Result.failure(Exception("Vui lòng chọn ít nhất 1 sản phẩm với số lượng > 0"))
        }

        // Check stock availability in source warehouse
        for (item in items.filter { it.qty > 0 }) {
            val available = getInventory(fromId, item.sku)
            if (item.qty > available) {
                val pName = _products.value.find { it.sku == item.sku }?.name ?: item.sku
                return Result.failure(Exception("Kho nguồn không đủ tồn kho cho $pName ($item.sku). Hiện còn: $available, yêu cầu chuyển: ${item.qty}"))
            }
        }

        // DEDUCT stock immediately from source warehouse
        val validItems = items.filter { it.qty > 0 }
        validItems.forEach { item ->
            updateStock(fromId, item.sku, -item.qty)
        }

        val nextNum = 1000 + _transfers.value.size + 1
        val transferId = "TRF-$nextNum"
        val timestamp = getCurrentTimestamp()
        val fromName = _warehouses.value.find { it.id == fromId }?.name ?: fromId
        val toName = _warehouses.value.find { it.id == toId }?.name ?: toId

        val newTransfer = TransferOrder(
            id = transferId,
            timestamp = timestamp,
            fromId = fromId,
            toId = toId,
            items = validItems,
            status = TransferStatus.PENDING,
            createdBy = createdBy,
            history = listOf(
                TimelineEvent(getCurrentTimeOnly(), "Lập phiếu chuyển kho", "Chuyển từ $fromName sang $toName", createdBy),
                TimelineEvent(getCurrentTimeOnly(), "Đang vận chuyển", "Đã trừ hàng tại $fromName. Hàng đang trên đường tới $toName.", "Hệ thống")
            )
        )
        val updated = listOf(newTransfer) + _transfers.value
        _transfers.value = updated
        saveTransfersJson(updated)
        return Result.success(newTransfer)
    }

    fun confirmTransfer(transferId: String, confirmedBy: String): Result<Unit> {
        val transfer = _transfers.value.find { it.id == transferId }
            ?: return Result.failure(Exception("Không tìm thấy phiếu luân chuyển $transferId"))

        if (transfer.status != TransferStatus.PENDING) {
            return Result.failure(Exception("Phiếu chuyển này đã được xử lý trước đó!"))
        }

        // ADD stock to destination warehouse
        transfer.items.forEach { item ->
            updateStock(transfer.toId, item.sku, item.qty)
        }

        val toName = _warehouses.value.find { it.id == transfer.toId }?.name ?: transfer.toId
        val newEvent = TimelineEvent(getCurrentTimeOnly(), "Đã nhận hàng thành công", "Kho đích $toName đã nhận hàng và cập nhật tồn kho.", confirmedBy)

        val updated = _transfers.value.map {
            if (it.id == transferId) {
                it.copy(
                    status = TransferStatus.COMPLETED,
                    confirmedBy = confirmedBy,
                    history = it.history + newEvent
                )
            } else it
        }
        _transfers.value = updated
        saveTransfersJson(updated)
        return Result.success(Unit)
    }

    fun cancelTransfer(transferId: String, cancelledBy: String): Result<Unit> {
        val transfer = _transfers.value.find { it.id == transferId }
            ?: return Result.failure(Exception("Không tìm thấy phiếu luân chuyển $transferId"))

        if (transfer.status != TransferStatus.PENDING) {
            return Result.failure(Exception("Không thể hủy phiếu chuyển đã hoàn thành hoặc đã hủy!"))
        }

        // RESTORE stock back to source warehouse
        transfer.items.forEach { item ->
            updateStock(transfer.fromId, item.sku, item.qty)
        }

        val fromName = _warehouses.value.find { it.id == transfer.fromId }?.name ?: transfer.fromId
        val newEvent = TimelineEvent(getCurrentTimeOnly(), "Đã hủy phiếu chuyển", "Hoàn trả hàng về lại kho nguồn $fromName.", cancelledBy)

        val updated = _transfers.value.map {
            if (it.id == transferId) {
                it.copy(
                    status = TransferStatus.CANCELLED,
                    history = it.history + newEvent
                )
            } else it
        }
        _transfers.value = updated
        saveTransfersJson(updated)
        return Result.success(Unit)
    }

    // JSON Serializers and Deserializers
    private fun loadUsersJson(): List<User> {
        val str = prefs.getString("users_json", null) ?: return getDefaultUsers()
        val arr = JSONArray(str)
        val list = mutableListOf<User>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                User(
                    id = obj.getString("id"),
                    username = obj.getString("username"),
                    password = obj.getString("password"),
                    name = obj.getString("name"),
                    role = UserRole.valueOf(obj.getString("role")),
                    warehouseId = if (obj.has("warehouseId") && !obj.isNull("warehouseId")) obj.getString("warehouseId") else null
                )
            )
        }
        return list
    }

    private fun saveUsersJson(list: List<User>) {
        val arr = JSONArray()
        list.forEach { u ->
            val obj = JSONObject()
            obj.put("id", u.id)
            obj.put("username", u.username)
            obj.put("password", u.password)
            obj.put("name", u.name)
            obj.put("role", u.role.name)
            obj.put("warehouseId", u.warehouseId)
            arr.put(obj)
        }
        prefs.edit().putString("users_json", arr.toString()).apply()
    }

    private fun loadPartnersJson(): List<Partner> {
        val str = prefs.getString("partners_json", null) ?: return getDefaultPartners()
        val arr = JSONArray(str)
        val list = mutableListOf<Partner>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Partner(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = PartnerType.valueOf(obj.getString("type")),
                    phone = obj.getString("phone"),
                    address = obj.getString("address")
                )
            )
        }
        return list
    }

    private fun savePartnersJson(list: List<Partner>) {
        val arr = JSONArray()
        list.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("type", p.type.name)
            obj.put("phone", p.phone)
            obj.put("address", p.address)
            arr.put(obj)
        }
        prefs.edit().putString("partners_json", arr.toString()).apply()
    }

    private fun loadDriversJson(): List<Driver> {
        val str = prefs.getString("drivers_json", null) ?: return getDefaultDrivers()
        val arr = JSONArray(str)
        val list = mutableListOf<Driver>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Driver(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    plate = obj.getString("plate"),
                    phone = obj.getString("phone")
                )
            )
        }
        return list
    }

    private fun saveDriversJson(list: List<Driver>) {
        val arr = JSONArray()
        list.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("name", d.name)
            obj.put("plate", d.plate)
            obj.put("phone", d.phone)
            arr.put(obj)
        }
        prefs.edit().putString("drivers_json", arr.toString()).apply()
    }

    private fun loadWarehousesJson(): List<Warehouse> {
        val str = prefs.getString("warehouses_json", null) ?: return getDefaultWarehouses()
        val arr = JSONArray(str)
        val list = mutableListOf<Warehouse>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Warehouse(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    type = WarehouseType.valueOf(obj.getString("type")),
                    location = obj.getString("location")
                )
            )
        }
        return list
    }

    private fun saveWarehousesJson(list: List<Warehouse>) {
        val arr = JSONArray()
        list.forEach { w ->
            val obj = JSONObject()
            obj.put("id", w.id)
            obj.put("name", w.name)
            obj.put("type", w.type.name)
            obj.put("location", w.location)
            arr.put(obj)
        }
        prefs.edit().putString("warehouses_json", arr.toString()).apply()
    }

    private fun loadProductsJson(): List<Product> {
        val str = prefs.getString("products_json", null) ?: return getDefaultProducts()
        val arr = JSONArray(str)
        val list = mutableListOf<Product>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                Product(
                    sku = obj.getString("sku"),
                    name = obj.getString("name"),
                    unit = obj.getString("unit")
                )
            )
        }
        return list
    }

    private fun saveProductsJson(list: List<Product>) {
        val arr = JSONArray()
        list.forEach { p ->
            val obj = JSONObject()
            obj.put("sku", p.sku)
            obj.put("name", p.name)
            obj.put("unit", p.unit)
            arr.put(obj)
        }
        prefs.edit().putString("products_json", arr.toString()).apply()
    }

    private fun loadInventoryJson(): List<InventoryItem> {
        val str = prefs.getString("inventory_json", null) ?: return getDefaultInventory()
        val arr = JSONArray(str)
        val list = mutableListOf<InventoryItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                InventoryItem(
                    warehouseId = obj.getString("warehouseId"),
                    sku = obj.getString("sku"),
                    qty = obj.getInt("qty")
                )
            )
        }
        return list
    }

    private fun saveInventoryJson(list: List<InventoryItem>) {
        val arr = JSONArray()
        list.forEach { inv ->
            val obj = JSONObject()
            obj.put("warehouseId", inv.warehouseId)
            obj.put("sku", inv.sku)
            obj.put("qty", inv.qty)
            arr.put(obj)
        }
        prefs.edit().putString("inventory_json", arr.toString()).apply()
    }

    private fun loadInputOrdersJson(): List<InputOrder> {
        val str = prefs.getString("input_orders_json", null) ?: return getDefaultInputOrders()
        val arr = JSONArray(str)
        val list = mutableListOf<InputOrder>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val itemsArr = obj.getJSONArray("items")
            val items = mutableListOf<OrderItem>()
            for (j in 0 until itemsArr.length()) {
                val itmObj = itemsArr.getJSONObject(j)
                items.add(OrderItem(itmObj.getString("sku"), itmObj.getInt("qty")))
            }

            val histArr = obj.optJSONArray("history") ?: JSONArray()
            val history = mutableListOf<TimelineEvent>()
            for (k in 0 until histArr.length()) {
                val hObj = histArr.getJSONObject(k)
                history.add(TimelineEvent(hObj.getString("timestamp"), hObj.getString("title"), hObj.getString("description"), hObj.getString("user")))
            }

            list.add(
                InputOrder(
                    id = obj.getString("id"),
                    timestamp = obj.getString("timestamp"),
                    supplierId = obj.getString("supplierId"),
                    warehouseId = obj.getString("warehouseId"),
                    items = items,
                    status = InputOrderStatus.valueOf(obj.getString("status")),
                    createdBy = obj.getString("createdBy"),
                    history = history
                )
            )
        }
        return list
    }

    private fun saveInputOrdersJson(list: List<InputOrder>) {
        val arr = JSONArray()
        list.forEach { ord ->
            val obj = JSONObject()
            obj.put("id", ord.id)
            obj.put("timestamp", ord.timestamp)
            obj.put("supplierId", ord.supplierId)
            obj.put("warehouseId", ord.warehouseId)
            obj.put("status", ord.status.name)
            obj.put("createdBy", ord.createdBy)

            val itemsArr = JSONArray()
            ord.items.forEach { itm ->
                val itmObj = JSONObject()
                itmObj.put("sku", itm.sku)
                itmObj.put("qty", itm.qty)
                itemsArr.put(itmObj)
            }
            obj.put("items", itemsArr)

            val histArr = JSONArray()
            ord.history.forEach { h ->
                val hObj = JSONObject()
                hObj.put("timestamp", h.timestamp)
                hObj.put("title", h.title)
                hObj.put("description", h.description)
                hObj.put("user", h.user)
                histArr.put(hObj)
            }
            obj.put("history", histArr)
            arr.put(obj)
        }
        prefs.edit().putString("input_orders_json", arr.toString()).apply()
    }

    private fun loadOutputOrdersJson(): List<OutputOrder> {
        val str = prefs.getString("output_orders_json", null) ?: return getDefaultOutputOrders()
        val arr = JSONArray(str)
        val list = mutableListOf<OutputOrder>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val itemsArr = obj.getJSONArray("items")
            val items = mutableListOf<OrderItem>()
            for (j in 0 until itemsArr.length()) {
                val itmObj = itemsArr.getJSONObject(j)
                items.add(OrderItem(itmObj.getString("sku"), itmObj.getInt("qty")))
            }

            val histArr = obj.optJSONArray("history") ?: JSONArray()
            val history = mutableListOf<TimelineEvent>()
            for (k in 0 until histArr.length()) {
                val hObj = histArr.getJSONObject(k)
                history.add(TimelineEvent(hObj.getString("timestamp"), hObj.getString("title"), hObj.getString("description"), hObj.getString("user")))
            }

            list.add(
                OutputOrder(
                    id = obj.getString("id"),
                    timestamp = obj.getString("timestamp"),
                    warehouseId = obj.getString("warehouseId"),
                    customerId = obj.getString("customerId"),
                    items = items,
                    status = OutputOrderStatus.valueOf(obj.getString("status")),
                    createdBy = obj.getString("createdBy"),
                    history = history
                )
            )
        }
        return list
    }

    private fun saveOutputOrdersJson(list: List<OutputOrder>) {
        val arr = JSONArray()
        list.forEach { ord ->
            val obj = JSONObject()
            obj.put("id", ord.id)
            obj.put("timestamp", ord.timestamp)
            obj.put("warehouseId", ord.warehouseId)
            obj.put("customerId", ord.customerId)
            obj.put("status", ord.status.name)
            obj.put("createdBy", ord.createdBy)

            val itemsArr = JSONArray()
            ord.items.forEach { itm ->
                val itmObj = JSONObject()
                itmObj.put("sku", itm.sku)
                itmObj.put("qty", itm.qty)
                itemsArr.put(itmObj)
            }
            obj.put("items", itemsArr)

            val histArr = JSONArray()
            ord.history.forEach { h ->
                val hObj = JSONObject()
                hObj.put("timestamp", h.timestamp)
                hObj.put("title", h.title)
                hObj.put("description", h.description)
                hObj.put("user", h.user)
                histArr.put(hObj)
            }
            obj.put("history", histArr)
            arr.put(obj)
        }
        prefs.edit().putString("output_orders_json", arr.toString()).apply()
    }

    private fun loadTransfersJson(): List<TransferOrder> {
        val str = prefs.getString("transfers_json", null) ?: return getDefaultTransfers()
        val arr = JSONArray(str)
        val list = mutableListOf<TransferOrder>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val itemsArr = obj.getJSONArray("items")
            val items = mutableListOf<OrderItem>()
            for (j in 0 until itemsArr.length()) {
                val itmObj = itemsArr.getJSONObject(j)
                items.add(OrderItem(itmObj.getString("sku"), itmObj.getInt("qty")))
            }

            val histArr = obj.optJSONArray("history") ?: JSONArray()
            val history = mutableListOf<TimelineEvent>()
            for (k in 0 until histArr.length()) {
                val hObj = histArr.getJSONObject(k)
                history.add(TimelineEvent(hObj.getString("timestamp"), hObj.getString("title"), hObj.getString("description"), hObj.getString("user")))
            }

            list.add(
                TransferOrder(
                    id = obj.getString("id"),
                    timestamp = obj.getString("timestamp"),
                    fromId = obj.getString("fromId"),
                    toId = obj.getString("toId"),
                    items = items,
                    status = TransferStatus.valueOf(obj.getString("status")),
                    createdBy = obj.getString("createdBy"),
                    confirmedBy = if (obj.has("confirmedBy") && !obj.isNull("confirmedBy")) obj.getString("confirmedBy") else null,
                    history = history
                )
            )
        }
        return list
    }

    private fun saveTransfersJson(list: List<TransferOrder>) {
        val arr = JSONArray()
        list.forEach { trf ->
            val obj = JSONObject()
            obj.put("id", trf.id)
            obj.put("timestamp", trf.timestamp)
            obj.put("fromId", trf.fromId)
            obj.put("toId", trf.toId)
            obj.put("status", trf.status.name)
            obj.put("createdBy", trf.createdBy)
            obj.put("confirmedBy", trf.confirmedBy)

            val itemsArr = JSONArray()
            trf.items.forEach { itm ->
                val itmObj = JSONObject()
                itmObj.put("sku", itm.sku)
                itmObj.put("qty", itm.qty)
                itemsArr.put(itmObj)
            }
            obj.put("items", itemsArr)

            val histArr = JSONArray()
            trf.history.forEach { h ->
                val hObj = JSONObject()
                hObj.put("timestamp", h.timestamp)
                hObj.put("title", h.title)
                hObj.put("description", h.description)
                hObj.put("user", h.user)
                histArr.put(hObj)
            }
            obj.put("history", histArr)
            arr.put(obj)
        }
        prefs.edit().putString("transfers_json", arr.toString()).apply()
    }

    companion object {
        @Volatile
        private var instance: WmsRepository? = null

        fun getInstance(context: Context): WmsRepository {
            return instance ?: synchronized(this) {
                instance ?: WmsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

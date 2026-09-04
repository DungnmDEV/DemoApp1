package com.pro.qlkho.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pro.qlkho.data.model.*
import com.pro.qlkho.data.repository.WmsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class WmsScreen(val title: String, val badge: String? = null) {
    DASHBOARD("Tổng quan hệ thống"),
    PARTNERS("Đối tác & Khách hàng"),
    DRIVERS_VEHICLES("Quản lý Nhân viên & Xe"),
    USERS_ROLES("Người dùng & Phân quyền"),
    INPUT_ORDERS("Đơn hàng đầu vào"),
    OUTPUT_ORDERS("Đơn hàng đầu ra"),
    TRANSFERS("Luân chuyển nội bộ"),
    PRODUCTS("Quản lý sản phẩm"),
    WAREHOUSES("Danh sách Kho & Bãi"),
    INVENTORY("Tồn kho chi tiết")
}

sealed class ModalState {
    object None : ModalState()
    data class InputOrderDetail(val order: InputOrder) : ModalState()
    data class OutputOrderDetail(val order: OutputOrder) : ModalState()
    data class TransferOrderDetail(val transfer: TransferOrder) : ModalState()
    object ResetConfirm : ModalState()
}

class WmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WmsRepository.getInstance(application)

    val currentUser = repository.currentUser
    val users = repository.users
    val partners = repository.partners
    val drivers = repository.drivers
    val warehouses = repository.warehouses
    val products = repository.products
    val inventory = repository.inventory
    val inputOrders = repository.inputOrders
    val outputOrders = repository.outputOrders
    val transfers = repository.transfers

    private val _currentScreen = MutableStateFlow(WmsScreen.DASHBOARD)
    val currentScreen: StateFlow<WmsScreen> = _currentScreen.asStateFlow()

    private val _modalState = MutableStateFlow<ModalState>(ModalState.None)
    val modalState: StateFlow<ModalState> = _modalState.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun navigateTo(screen: WmsScreen) {
        val user = currentUser.value
        if (screen == WmsScreen.USERS_ROLES && user?.role != UserRole.ADMIN) {
            emitToast("Bạn không có quyền truy cập chức năng này.")
            return
        }
        _currentScreen.value = screen
    }

    fun openModal(state: ModalState) {
        _modalState.value = state
    }

    fun closeModal() {
        _modalState.value = ModalState.None
    }

    fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // Auth actions
    fun login(username: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(400) // Realistic enterprise responsiveness
            val result = repository.login(username, pass)
            _isLoading.value = false
            result.onSuccess {
                _currentScreen.value = WmsScreen.DASHBOARD
                emitToast("✓ Đăng nhập thành công với vai trò ${it.role.displayName}")
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Sai tên đăng nhập hoặc mật khẩu.")
            }
        }
    }

    fun loginWithSso(token: String, username: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(200)
            val result = repository.loginWithSso(token, username)
            _isLoading.value = false
            result.onSuccess {
                _currentScreen.value = WmsScreen.DASHBOARD
                emitToast("✓ SSO: Đã liên kết tài khoản ${it.username} từ Synergy")
            }.onFailure {
                emitToast("Lỗi liên kết: ${it.message}")
            }
        }
    }

    fun logout() {
        repository.logout()
        _currentScreen.value = WmsScreen.DASHBOARD
        emitToast("Đã đăng xuất.")
    }

    fun resetDemoData() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(500)
            repository.resetData()
            _isLoading.value = false
            closeModal()
            _currentScreen.value = WmsScreen.DASHBOARD
            emitToast("✓ Đã khôi phục toàn bộ dữ liệu demo ban đầu!")
        }
    }

    // Check permissions
    fun canManageWarehouse(targetWarehouseId: String?): Boolean {
        val user = currentUser.value ?: return false
        if (user.role == UserRole.ADMIN) return true
        if (targetWarehouseId == null) return false
        return user.warehouseId == targetWarehouseId
    }

    // --- Partners CRUD ---
    fun savePartner(partner: Partner, isEdit: Boolean) {
        viewModelScope.launch {
            val res = if (isEdit) repository.updatePartner(partner) else repository.addPartner(partner)
            res.onSuccess {
                emitToast(if (isEdit) "✓ Đã cập nhật đối tác ${partner.name}" else "✓ Đã thêm đối tác ${partner.name}")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun deletePartner(id: String) {
        viewModelScope.launch {
            repository.deletePartner(id)
            emitToast("✓ Đã xóa đối tác $id")
        }
    }

    // --- Drivers CRUD ---
    fun saveDriver(driver: Driver, isEdit: Boolean) {
        viewModelScope.launch {
            val res = if (isEdit) repository.updateDriver(driver) else repository.addDriver(driver)
            res.onSuccess {
                emitToast(if (isEdit) "✓ Đã cập nhật xe ${driver.plate}" else "✓ Đã thêm xe ${driver.plate} & tạo kho di động")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun deleteDriver(id: String) {
        viewModelScope.launch {
            repository.deleteDriver(id)
            emitToast("✓ Đã xóa nhân viên/xe $id")
        }
    }

    // --- Users CRUD ---
    fun saveUser(user: User, isEdit: Boolean) {
        viewModelScope.launch {
            val res = if (isEdit) repository.updateUser(user) else repository.addUser(user)
            res.onSuccess {
                emitToast(if (isEdit) "✓ Đã cập nhật người dùng ${user.username}" else "✓ Đã thêm người dùng ${user.username}")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            val res = repository.deleteUser(id)
            res.onSuccess {
                emitToast("✓ Đã xóa người dùng")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    // --- Products CRUD ---
    fun saveProduct(product: Product, isEdit: Boolean) {
        viewModelScope.launch {
            val res = if (isEdit) repository.updateProduct(product) else repository.addProduct(product)
            res.onSuccess {
                emitToast(if (isEdit) "✓ Đã cập nhật sản phẩm ${product.sku}" else "✓ Đã thêm sản phẩm ${product.sku}")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun deleteProduct(sku: String) {
        viewModelScope.launch {
            val res = repository.deleteProduct(sku)
            res.onSuccess {
                emitToast("✓ Đã xóa sản phẩm $sku")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    // --- Warehouses CRUD ---
    fun saveWarehouse(warehouse: Warehouse, isEdit: Boolean) {
        viewModelScope.launch {
            val res = if (isEdit) repository.updateWarehouse(warehouse) else repository.addWarehouse(warehouse)
            res.onSuccess {
                emitToast(if (isEdit) "✓ Đã cập nhật kho ${warehouse.id}" else "✓ Đã thêm kho ${warehouse.id}")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun deleteWarehouse(id: String) {
        viewModelScope.launch {
            val res = repository.deleteWarehouse(id)
            res.onSuccess {
                emitToast("✓ Đã xóa kho $id")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    // --- Inbound Orders ---
    fun createInputOrder(supplierId: String, warehouseId: String, items: List<OrderItem>) {
        val user = currentUser.value ?: return
        if (!canManageWarehouse(warehouseId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            val res = repository.createInputOrder(supplierId, warehouseId, items, user.username)
            _isLoading.value = false
            res.onSuccess {
                emitToast("✓ Đã tạo đơn nhập ${it.id}")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun confirmInputOrder(orderId: String) {
        val user = currentUser.value ?: return
        val order = inputOrders.value.find { it.id == orderId } ?: return
        if (!canManageWarehouse(order.warehouseId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            val res = repository.confirmInputOrder(orderId, user.username)
            _isLoading.value = false
            res.onSuccess {
                emitToast("✓ Đã xác nhận nhận hàng. Tồn kho đã được cập nhật.")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun cancelInputOrder(orderId: String) {
        val user = currentUser.value ?: return
        val order = inputOrders.value.find { it.id == orderId } ?: return
        if (!canManageWarehouse(order.warehouseId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            val res = repository.cancelInputOrder(orderId, user.username)
            res.onSuccess {
                emitToast("✓ Đã hủy đơn nhập $orderId")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    // --- Outbound Orders ---
    fun createOutputOrder(warehouseId: String, customerId: String, items: List<OrderItem>) {
        val user = currentUser.value ?: return
        if (!canManageWarehouse(warehouseId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            val res = repository.createOutputOrder(warehouseId, customerId, items, user.username)
            _isLoading.value = false
            res.onSuccess {
                emitToast("✓ Đã tạo đơn xuất ${it.id}")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun confirmOutputOrder(orderId: String) {
        val user = currentUser.value ?: return
        val order = outputOrders.value.find { it.id == orderId } ?: return
        if (!canManageWarehouse(order.warehouseId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            val res = repository.confirmOutputOrder(orderId, user.username)
            _isLoading.value = false
            res.onSuccess {
                emitToast("✓ Đã xuất hàng thành công. Tồn kho đã được trừ.")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun cancelOutputOrder(orderId: String) {
        val user = currentUser.value ?: return
        val order = outputOrders.value.find { it.id == orderId } ?: return
        if (!canManageWarehouse(order.warehouseId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            val res = repository.cancelOutputOrder(orderId, user.username)
            res.onSuccess {
                emitToast("✓ Đã hủy đơn xuất $orderId")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    // --- Internal Transfers ---
    fun createTransfer(fromId: String, toId: String, items: List<OrderItem>) {
        val user = currentUser.value ?: return
        if (!canManageWarehouse(fromId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            val res = repository.createTransfer(fromId, toId, items, user.username)
            _isLoading.value = false
            res.onSuccess {
                emitToast("✓ Đã tạo phiếu chuyển ${it.id}. Hàng đang vận chuyển.")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun confirmTransfer(transferId: String) {
        val user = currentUser.value ?: return
        val transfer = transfers.value.find { it.id == transferId } ?: return
        // Must manage destination warehouse (or be admin)
        if (!canManageWarehouse(transfer.toId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này. Chỉ kho đích mới có thể xác nhận nhận hàng.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            val res = repository.confirmTransfer(transferId, user.username)
            _isLoading.value = false
            res.onSuccess {
                emitToast("✓ Đã nhận hàng thành công. Hàng đã được cộng vào tồn kho ${transfer.toId}")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    fun cancelTransfer(transferId: String) {
        val user = currentUser.value ?: return
        val transfer = transfers.value.find { it.id == transferId } ?: return
        // Must manage source warehouse (or be admin)
        if (!canManageWarehouse(transfer.fromId)) {
            emitToast("Bạn không có quyền thao tác tại vị trí này.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            val res = repository.cancelTransfer(transferId, user.username)
            _isLoading.value = false
            res.onSuccess {
                emitToast("✓ Đã hủy phiếu chuyển ${transfer.id}. Đã hoàn lại tồn kho nguồn.")
            }.onFailure {
                emitToast("Lỗi: ${it.message}")
            }
        }
    }

    // Helper queries
    fun getInventory(warehouseId: String, sku: String): Int = repository.getInventory(warehouseId, sku)
    fun getTotalInventory(sku: String): Int = repository.getTotalInventoryForProduct(sku)
}

package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class BottomNavDestination(
    val screen: WmsScreen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun WmsApp(
    viewModel: WmsViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val modalState by viewModel.modalState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val users by viewModel.users.collectAsStateWithLifecycle()
    val partners by viewModel.partners.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val warehouses by viewModel.warehouses.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val inputOrders by viewModel.inputOrders.collectAsStateWithLifecycle()
    val outputOrders by viewModel.outputOrders.collectAsStateWithLifecycle()
    val transfers by viewModel.transfers.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var loginError by remember { mutableStateOf<String?>(null) }

    val productsMap = remember(products) { products.associateBy { it.sku } }
    val warehouseMap = remember(warehouses) { warehouses.associateBy { it.id } }
    val partnerMap = remember(partners) { partners.associateBy { it.id } }

    val pendingInboundCount = remember(inputOrders) { inputOrders.count { it.status == InputOrderStatus.PENDING } }
    val pendingOutboundCount = remember(outputOrders) { outputOrders.count { it.status == OutputOrderStatus.PENDING } }
    val pendingTransferCount = remember(transfers) { transfers.count { it.status == TransferStatus.PENDING } }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    if (currentUser == null) {
        LoginScreen(
            onLogin = { username, pass ->
                loginError = null
                viewModel.login(
                    username = username,
                    pass = pass,
                    onSuccess = { loginError = null },
                    onError = { err -> loginError = err }
                )
            },
            isLoading = isLoading,
            errorMessage = loginError
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Slate900,
                modifier = Modifier.width(280.dp)
            ) {
                WmsSidebarContent(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    onSelectScreen = { screen ->
                        viewModel.navigateTo(screen)
                        scope.launch { drawerState.close() }
                    },
                    pendingInboundCount = pendingInboundCount,
                    pendingOutboundCount = pendingOutboundCount,
                    pendingTransferCount = pendingTransferCount
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                WmsHeader(
                    title = currentScreen.title,
                    currentUser = currentUser,
                    onOpenDrawer = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    },
                    onResetDataClick = {
                        viewModel.openModal(ModalState.ResetConfirm)
                    },
                    onLogoutClick = {
                        viewModel.logout()
                    }
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp,
                        modifier = Modifier.wrapContentHeight()
                    ) {
                        val bottomNavItems = listOf(
                            BottomNavDestination(WmsScreen.DASHBOARD, "Tổng quan", Icons.Outlined.Dashboard),
                            BottomNavDestination(WmsScreen.INPUT_ORDERS, "Nhập kho", Icons.Outlined.MoveToInbox),
                            BottomNavDestination(WmsScreen.OUTPUT_ORDERS, "Xuất kho", Icons.Outlined.Outbox),
                            BottomNavDestination(WmsScreen.TRANSFERS, "Luân chuyển", Icons.Outlined.SyncAlt),
                            BottomNavDestination(WmsScreen.INVENTORY, "Tồn kho", Icons.Outlined.Inventory2)
                        )

                        bottomNavItems.forEach { item ->
                            val isSelected = currentScreen == item.screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) PrimaryBlue else Slate400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryBlue else Slate500
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueLight,
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate500
                                )
                            )
                        }
                    }
                }
            },
            containerColor = Slate50
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    WmsScreen.DASHBOARD -> DashboardScreen(
                        partners = partners,
                        drivers = drivers,
                        warehouses = warehouses,
                        products = products,
                        inventory = inventory,
                        inputOrders = inputOrders,
                        outputOrders = outputOrders,
                        transfers = transfers,
                        onNavigate = { viewModel.navigateTo(it) },
                        onOpenModal = { viewModel.openModal(it) }
                    )

                    WmsScreen.PARTNERS -> PartnersScreen(
                        partners = partners,
                        onSavePartner = { partner, isEdit -> viewModel.savePartner(partner, isEdit) },
                        onDeletePartner = { viewModel.deletePartner(it) }
                    )

                    WmsScreen.DRIVERS_VEHICLES -> DriversVehiclesScreen(
                        drivers = drivers,
                        inventory = inventory,
                        products = products,
                        onSaveDriver = { driver, isEdit -> viewModel.saveDriver(driver, isEdit) },
                        onDeleteDriver = { viewModel.deleteDriver(it) }
                    )

                    WmsScreen.USERS_ROLES -> UsersRolesScreen(
                        currentUser = currentUser,
                        users = users,
                        warehouses = warehouses,
                        onSaveUser = { user, isEdit -> viewModel.saveUser(user, isEdit) },
                        onDeleteUser = { viewModel.deleteUser(it) }
                    )

                    WmsScreen.INPUT_ORDERS -> InputOrdersScreen(
                        currentUser = currentUser,
                        partners = partners,
                        warehouses = warehouses,
                        products = products,
                        inputOrders = inputOrders,
                        onCreateOrder = { supp, wh, items -> viewModel.createInputOrder(supp, wh, items) },
                        onConfirmOrder = { viewModel.confirmInputOrder(it) },
                        onCancelOrder = { viewModel.cancelInputOrder(it) },
                        onOpenModal = { viewModel.openModal(it) },
                        canManageWarehouse = { viewModel.canManageWarehouse(it) }
                    )

                    WmsScreen.OUTPUT_ORDERS -> OutputOrdersScreen(
                        currentUser = currentUser,
                        partners = partners,
                        warehouses = warehouses,
                        products = products,
                        outputOrders = outputOrders,
                        getInventory = { wh, sku -> viewModel.getInventory(wh, sku) },
                        onCreateOrder = { wh, cust, items -> viewModel.createOutputOrder(wh, cust, items) },
                        onConfirmOrder = { viewModel.confirmOutputOrder(it) },
                        onCancelOrder = { viewModel.cancelOutputOrder(it) },
                        onOpenModal = { viewModel.openModal(it) },
                        canManageWarehouse = { viewModel.canManageWarehouse(it) }
                    )

                    WmsScreen.TRANSFERS -> TransfersScreen(
                        currentUser = currentUser,
                        warehouses = warehouses,
                        products = products,
                        transfers = transfers,
                        getInventory = { wh, sku -> viewModel.getInventory(wh, sku) },
                        onCreateTransfer = { from, to, items -> viewModel.createTransfer(from, to, items) },
                        onConfirmTransfer = { viewModel.confirmTransfer(it) },
                        onCancelTransfer = { viewModel.cancelTransfer(it) },
                        onOpenModal = { viewModel.openModal(it) },
                        canManageWarehouse = { viewModel.canManageWarehouse(it) }
                    )

                    WmsScreen.PRODUCTS -> ProductsScreen(
                        products = products,
                        getTotalInventory = { viewModel.getTotalInventory(it) },
                        onSaveProduct = { prod, isEdit -> viewModel.saveProduct(prod, isEdit) },
                        onDeleteProduct = { viewModel.deleteProduct(it) }
                    )

                    WmsScreen.WAREHOUSES -> WarehousesScreen(
                        warehouses = warehouses,
                        inventory = inventory,
                        onSaveWarehouse = { wh, isEdit -> viewModel.saveWarehouse(wh, isEdit) },
                        onDeleteWarehouse = { viewModel.deleteWarehouse(it) }
                    )

                    WmsScreen.INVENTORY -> InventoryScreen(
                        warehouses = warehouses,
                        products = products,
                        inventory = inventory
                    )
                }

                // Loading overlay if needed
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            }
        }
    }

    // Modal dialogs
    when (val modal = modalState) {
        is ModalState.InputOrderDetail -> {
            val o = modal.order
            val supp = partnerMap[o.supplierId]
            val wh = warehouseMap[o.warehouseId]
            OrderDetailModal(
                orderId = o.id,
                orderType = "Đơn nhập hàng (Inbound)",
                timestamp = o.timestamp,
                creator = o.createdBy,
                sourceLocation = "${o.supplierId} - ${supp?.name ?: ""}",
                destinationLocation = "${o.warehouseId} - ${wh?.name ?: ""}",
                partnerName = supp?.name,
                items = o.items,
                statusBadge = { InputOrderStatusBadge(o.status) },
                timeline = o.history,
                productsMap = productsMap,
                onDismiss = { viewModel.closeModal() }
            )
        }

        is ModalState.OutputOrderDetail -> {
            val o = modal.order
            val cust = partnerMap[o.customerId]
            val wh = warehouseMap[o.warehouseId]
            OrderDetailModal(
                orderId = o.id,
                orderType = "Đơn xuất hàng (Outbound)",
                timestamp = o.timestamp,
                creator = o.createdBy,
                sourceLocation = "${o.warehouseId} - ${wh?.name ?: ""}",
                destinationLocation = "${o.customerId} - ${cust?.name ?: ""}",
                partnerName = cust?.name,
                items = o.items,
                statusBadge = { OutputOrderStatusBadge(o.status) },
                timeline = o.history,
                productsMap = productsMap,
                onDismiss = { viewModel.closeModal() }
            )
        }

        is ModalState.TransferOrderDetail -> {
            val trf = modal.transfer
            val fromWh = warehouseMap[trf.fromId]
            val toWh = warehouseMap[trf.toId]
            OrderDetailModal(
                orderId = trf.id,
                orderType = "Phiếu luân chuyển nội bộ (Transfer)",
                timestamp = trf.timestamp,
                creator = trf.createdBy,
                sourceLocation = "${trf.fromId} - ${fromWh?.name ?: ""}",
                destinationLocation = "${trf.toId} - ${toWh?.name ?: ""}",
                partnerName = null,
                items = trf.items,
                statusBadge = { TransferStatusBadge(trf.status) },
                timeline = trf.history,
                productsMap = productsMap,
                onDismiss = { viewModel.closeModal() }
            )
        }

        is ModalState.ResetConfirm -> {
            ResetConfirmDialog(
                onConfirm = { viewModel.resetDemoData() },
                onDismiss = { viewModel.closeModal() }
            )
        }

        ModalState.None -> Unit
    }
}

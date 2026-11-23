package vn.edu.vlu.projectandroid.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.model.Order
import vn.edu.vlu.projectandroid.data.model.OrderStatus
import vn.edu.vlu.projectandroid.data.repository.OrderRepository
import vn.edu.vlu.projectandroid.ui.components.EmptyState
import vn.edu.vlu.projectandroid.ui.components.formatPrice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderListScreen(
    orderRepository: OrderRepository,
    onOrderClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val orders by orderRepository.orders.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tabs = listOf("Tất cả (${orders.size})") + OrderStatus.entries.map { status ->
        val count = orders.count { it.status == status }
        "${status.displayName} ($count)"
    }

    // Lọc theo tab trạng thái và từ khóa tìm kiếm
    val filteredOrders = orders.filter { order ->
        val matchesTab = if (selectedTab == 0) true else {
            val targetStatus = OrderStatus.entries[selectedTab - 1]
            order.status == targetStatus
        }

        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isBlank() ||
                order.id.lowercase().contains(q) ||
                order.phoneNumber.contains(q) ||
                order.customerName.lowercase().contains(q) ||
                order.userEmail.lowercase().contains(q) ||
                order.shippingAddress.lowercase().contains(q)

        matchesTab && matchesQuery
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Thanh tìm kiếm
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm theo mã đơn, SĐT, tên khách, email...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Tabs trạng thái đơn
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Receipt,
                    title = "Không tìm thấy đơn hàng nào",
                    subtitle = if (searchQuery.isNotBlank()) "Thử đổi từ khóa tìm kiếm" else "Chưa có đơn hàng trong mục này"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        AdminOrderItemCard(
                            order = order,
                            onClick = { onOrderClick(order.id) },
                            onUpdateStatus = { newStatus ->
                                val result = orderRepository.updateOrderStatus(order.id, newStatus)
                                scope.launch {
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("Đã chuyển đơn ${order.id} sang ${newStatus.displayName}")
                                    } else {
                                        snackbarHostState.showSnackbar("Lỗi: ${result.exceptionOrNull()?.message}")
                                    }
                                }
                            },
                            onCancelOrder = { orderToCancel = order }
                        )
                    }
                }
            }
        }
    }

    // Dialog xác nhận hủy đơn
    orderToCancel?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToCancel = null },
            title = { Text("Xác nhận hủy đơn hàng?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn hủy đơn hàng ${order.id} của khách hàng ${order.customerName.ifBlank { order.phoneNumber }}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val result = orderRepository.updateOrderStatus(order.id, OrderStatus.CANCELLED)
                        orderToCancel = null
                        scope.launch {
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Đã hủy đơn ${order.id}")
                            }
                        }
                    }
                ) {
                    Text("Hủy đơn hàng", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToCancel = null }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun AdminOrderItemCard(
    order: Order,
    onClick: () -> Unit,
    onUpdateStatus: (OrderStatus) -> Unit,
    onCancelOrder: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")) }
    var menuExpanded by remember { mutableStateOf(false) }

    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFFA000)
        OrderStatus.PROCESSING -> Color(0xFF1976D2)
        OrderStatus.SHIPPING -> Color(0xFF7B1FA2)
        OrderStatus.DELIVERED -> Color(0xFF388E3C)
        OrderStatus.CANCELLED -> Color(0xFFD32F2F)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Mã đơn + Trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.id,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateFormat.format(Date(order.createdAt)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Thông tin người nhận
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (order.customerName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(order.customerName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(order.phoneNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }

                if (order.userEmail.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(order.userEmail, fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(order.shippingAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Danh sách sản phẩm tóm tắt
            Text(
                text = "${order.items.size} mặt hàng: ${order.items.joinToString(", ") { "${it.productName} (x${it.quantity})" }}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tổng tiền & Phương thức thanh toán
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PTTT: ${order.paymentMethod}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatPrice(order.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thanh thao tác nhanh cho Admin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút chuyển trạng thái nhanh theo quy trình
                when (order.status) {
                    OrderStatus.PENDING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.PROCESSING) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Duyệt đơn", fontSize = 13.sp)
                        }
                    }
                    OrderStatus.PROCESSING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.SHIPPING) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bắt đầu giao", fontSize = 13.sp)
                        }
                    }
                    OrderStatus.SHIPPING -> {
                        Button(
                            onClick = { onUpdateStatus(OrderStatus.DELIVERED) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Đã giao hàng", fontSize = 13.sp)
                        }
                    }
                    else -> {}
                }

                // Menu chọn trạng thái bất kỳ
                Box {
                    OutlinedButton(
                        onClick = { menuExpanded = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Đổi trạng thái", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        OrderStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    menuExpanded = false
                                    onUpdateStatus(status)
                                }
                            )
                        }
                    }
                }

                // Nút hủy đơn (nếu chưa hoàn tất hoặc chưa hủy)
                if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED) {
                    OutlinedButton(
                        onClick = onCancelOrder,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Hủy", color = Color(0xFFD32F2F), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

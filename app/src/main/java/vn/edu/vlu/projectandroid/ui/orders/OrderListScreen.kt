package vn.edu.vlu.projectandroid.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.model.Order
import vn.edu.vlu.projectandroid.data.model.OrderStatus
import vn.edu.vlu.projectandroid.data.repository.AuthRepository
import vn.edu.vlu.projectandroid.data.repository.OrderRepository
import vn.edu.vlu.projectandroid.ui.components.EmptyState
import vn.edu.vlu.projectandroid.ui.components.formatPrice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    orderRepository: OrderRepository,
    authRepository: AuthRepository,
    onOrderClick: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit = {}
) {
    val allOrders by orderRepository.orders.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState(initial = null)
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    // Người dùng thông thường chỉ xem đơn hàng của chính mình; Admin xem toàn bộ
    val myOrders = remember(allOrders, currentUser) {
        if (currentUser?.isAdmin == true) {
            allOrders
        } else {
            val uid = currentUser?.id ?: ""
            val email = currentUser?.email ?: ""
            allOrders.filter { order ->
                (uid.isNotBlank() && order.userId == uid) ||
                        (email.isNotBlank() && order.userEmail.equals(email, ignoreCase = true))
            }
        }
    }

    val tabs = listOf("Tất cả") + OrderStatus.entries.map { it.displayName }

    val filteredOrders = if (selectedTab == 0) {
        myOrders
    } else {
        val status = OrderStatus.entries[selectedTab - 1]
        myOrders.filter { it.status == status }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng") },
                actions = {
                    if (currentUser?.isAdmin == true) {
                        IconButton(onClick = onNavigateToAdmin) {
                            Icon(
                                Icons.Filled.AdminPanelSettings,
                                contentDescription = "Trang quản trị",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            authRepository.logout()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Đăng xuất")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (filteredOrders.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Receipt,
                    title = "Chưa có đơn hàng",
                    subtitle = "Các đơn hàng của bạn sẽ hiển thị ở đây"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(filteredOrders, key = { it.id }) { order ->
                        OrderCard(order = order, onClick = { onOrderClick(order.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")) }
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFFA000)
        OrderStatus.PROCESSING -> Color(0xFF1976D2)
        OrderStatus.SHIPPING -> Color(0xFF7B1FA2)
        OrderStatus.DELIVERED -> Color(0xFF4CAF50)
        OrderStatus.CANCELLED -> Color(0xFFE53935)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.status.displayName,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateFormat.format(Date(order.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "${order.items.size} sản phẩm",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tổng:", color = Color.Gray)
                Text(
                    formatPrice(order.totalAmount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

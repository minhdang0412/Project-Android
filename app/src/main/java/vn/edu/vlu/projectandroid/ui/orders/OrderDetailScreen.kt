package vn.edu.vlu.projectandroid.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import vn.edu.vlu.projectandroid.data.model.Order
import vn.edu.vlu.projectandroid.data.model.OrderStatus
import vn.edu.vlu.projectandroid.data.repository.OrderRepository
import vn.edu.vlu.projectandroid.ui.components.formatPrice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    orderRepository: OrderRepository,
    onNavigateBack: () -> Unit
) {
    val orders by orderRepository.orders.collectAsState()
    val order = orders.find { it.id == orderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        if (order == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Không tìm thấy đơn hàng")
            }
            return@Scaffold
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Order ID & Date
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mã đơn: ${order.id}", fontWeight = FontWeight.Bold)
                    Text(
                        "Ngày đặt: ${dateFormat.format(Date(order.createdAt))}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order timeline
            Text("Trạng thái đơn hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            OrderTimeline(currentStatus = order.status)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Shipping info
            Text("Thông tin giao hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Địa chỉ: ${order.shippingAddress}", fontSize = 14.sp)
            Text("SĐT: ${order.phoneNumber}", fontSize = 14.sp)
            Text(
                "Thanh toán: ${
                    when (order.paymentMethod) {
                        "COD" -> "Thanh toán khi nhận hàng"
                        "BANK" -> "Chuyển khoản ngân hàng"
                        "EWALLET" -> "Ví điện tử"
                        else -> order.paymentMethod
                    }
                }",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Items
            Text("Sản phẩm (${order.items.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.productName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp
                        )
                        Text("x${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(formatPrice(item.totalPrice), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Summary
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tạm tính:", color = Color.Gray)
                Text(formatPrice(order.subtotal))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Phí giao hàng:", color = Color.Gray)
                Text(formatPrice(order.shippingFee))
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tổng cộng:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    formatPrice(order.totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Cancel button
            if (order.status == OrderStatus.PENDING || order.status == OrderStatus.PROCESSING) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { orderRepository.cancelOrder(orderId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hủy đơn hàng", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun OrderTimeline(currentStatus: OrderStatus) {
    data class TimelineStep(
        val status: OrderStatus,
        val icon: ImageVector,
        val label: String
    )

    val steps = listOf(
        TimelineStep(OrderStatus.PENDING, Icons.Filled.Pending, "Chờ xác nhận"),
        TimelineStep(OrderStatus.PROCESSING, Icons.Filled.Settings, "Đang xử lý"),
        TimelineStep(OrderStatus.SHIPPING, Icons.Filled.LocalShipping, "Đang giao"),
        TimelineStep(OrderStatus.DELIVERED, Icons.Filled.CheckCircle, "Đã giao")
    )

    val currentIndex = if (currentStatus == OrderStatus.CANCELLED) -1
    else steps.indexOfFirst { it.status == currentStatus }

    Column {
        if (currentStatus == OrderStatus.CANCELLED) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Đơn hàng đã bị hủy", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
            }
        } else {
            steps.forEachIndexed { index, step ->
                val isCompleted = index <= currentIndex
                val activeColor = if (isCompleted) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.4f)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(activeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            step.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        step.label,
                        fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                }
                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .width(2.dp)
                            .height(24.dp)
                            .background(
                                if (index < currentIndex) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

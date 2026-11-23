package vn.edu.vlu.projectandroid.ui.checkout

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.model.Order
import vn.edu.vlu.projectandroid.data.model.OrderStatus
import vn.edu.vlu.projectandroid.data.repository.AuthRepository
import vn.edu.vlu.projectandroid.data.repository.CartRepository
import vn.edu.vlu.projectandroid.data.repository.OrderRepository
import vn.edu.vlu.projectandroid.ui.components.formatPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    authRepository: AuthRepository,
    onOrderPlaced: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val cartItems by cartRepository.cartItems.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var address by remember { mutableStateOf(currentUser?.address ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phoneNumber?.filter { it.isDigit() } ?: "") }
    var paymentMethod by remember { mutableStateOf("COD") }

    // Tự động điền số điện thoại và địa chỉ theo tài khoản khi dữ liệu người dùng được tải
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (phone.isBlank() && user.phoneNumber.isNotBlank()) {
                phone = user.phoneNumber.filter { it.isDigit() }
            }
            if (address.isBlank() && user.address.isNotBlank()) {
                address = user.address
            }
        }
    }

    val paymentOptions = listOf(
        "COD" to "Thanh toán khi nhận hàng",
        "BANK" to "Chuyển khoản ngân hàng",
        "EWALLET" to "Ví điện tử"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tổng thanh toán:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        formatPrice(cartRepository.totalAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val cleanPhone = phone.trim()
                        val cleanAddress = address.trim()

                        if (cleanAddress.isBlank() || cleanPhone.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập đầy đủ địa chỉ và số điện thoại")
                            }
                            return@Button
                        }
                        if (cleanPhone.length < 9) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Số điện thoại không hợp lệ (tối thiểu 9 số)")
                            }
                            return@Button
                        }

                        val order = orderRepository.placeOrder(
                            Order(
                                userId = currentUser?.id ?: "",
                                customerName = currentUser?.name ?: "",
                                userEmail = currentUser?.email ?: "",
                                items = cartItems,
                                subtotal = cartRepository.subtotal,
                                shippingFee = cartRepository.shippingFee,
                                totalAmount = cartRepository.totalAmount,
                                shippingAddress = cleanAddress,
                                phoneNumber = cleanPhone,
                                paymentMethod = paymentMethod,
                                status = OrderStatus.PENDING
                            )
                        )
                        cartRepository.clearCart()
                        onOrderPlaced(order.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Xác nhận đặt hàng", fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Shipping info
            Text("Thông tin giao hàng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Địa chỉ giao hàng") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    phone = input.filter { it.isDigit() }
                },
                label = { Text("Số điện thoại *") },
                placeholder = { Text("Ví dụ: 0912345678") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Payment method
            Text("Phương thức thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            paymentOptions.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = paymentMethod == value,
                        onClick = { paymentMethod = value }
                    )
                    Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Order items
            Text("Đơn hàng (${cartItems.size} sản phẩm)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            cartItems.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
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
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "x${item.quantity}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            formatPrice(item.totalPrice),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tạm tính:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(formatPrice(cartRepository.subtotal))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Phí giao hàng:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(formatPrice(cartRepository.shippingFee))
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

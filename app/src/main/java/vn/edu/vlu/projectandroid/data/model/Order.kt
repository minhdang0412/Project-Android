package vn.edu.vlu.projectandroid.data.model

enum class OrderStatus(val displayName: String) {
    PENDING("Chờ xác nhận"),
    PROCESSING("Đang xử lý"),
    SHIPPING("Đang giao"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy")
}

data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val userEmail: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shippingFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val shippingAddress: String = "",
    val phoneNumber: String = "",
    val paymentMethod: String = "COD",
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

package vn.edu.vlu.projectandroid.data.model

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1
) {
    val totalPrice: Double get() = price * quantity
}

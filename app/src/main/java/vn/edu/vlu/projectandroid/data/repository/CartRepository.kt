package vn.edu.vlu.projectandroid.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.vlu.projectandroid.data.model.CartItem

class CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartCount: Int get() = _cartItems.value.sumOf { it.quantity }

    val subtotal: Double get() = _cartItems.value.sumOf { it.totalPrice }

    val shippingFee: Double get() = if (_cartItems.value.isEmpty()) 0.0 else 30000.0

    val totalAmount: Double get() = subtotal + shippingFee

    fun addToCart(item: CartItem) {
        _cartItems.update { current ->
            val existing = current.find { it.productId == item.productId }
            if (existing != null) {
                current.map {
                    if (it.productId == item.productId) {
                        it.copy(quantity = it.quantity + item.quantity)
                    } else it
                }
            } else {
                current + item
            }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        _cartItems.update { current ->
            current.map {
                if (it.productId == productId) it.copy(quantity = quantity) else it
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.update { current ->
            current.filter { it.productId != productId }
        }
    }

    fun clearCart() {
        _cartItems.update { emptyList() }
    }

    fun getCartItems(): List<CartItem> = _cartItems.value
}

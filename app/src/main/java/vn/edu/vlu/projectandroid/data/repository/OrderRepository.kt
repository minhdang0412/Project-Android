package vn.edu.vlu.projectandroid.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.vlu.projectandroid.data.model.Order
import vn.edu.vlu.projectandroid.data.model.OrderStatus

class OrderRepository {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun placeOrder(order: Order): Order {
        val newOrder = order.copy(
            id = "ORD${System.currentTimeMillis()}",
            createdAt = System.currentTimeMillis()
        )
        _orders.update { current -> listOf(newOrder) + current }
        return newOrder
    }

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return _orders.value.filter { it.status == status }
    }

    fun cancelOrder(orderId: String): Boolean {
        val order = getOrderById(orderId) ?: return false
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.PROCESSING) {
            return false
        }
        _orders.update { current ->
            current.map {
                if (it.id == orderId) it.copy(status = OrderStatus.CANCELLED) else it
            }
        }
        return true
    }

    fun getAllOrders(): List<Order> = _orders.value
}

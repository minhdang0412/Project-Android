package vn.edu.vlu.projectandroid.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.vlu.projectandroid.data.model.CartItem
import vn.edu.vlu.projectandroid.data.model.Order
import vn.edu.vlu.projectandroid.data.model.OrderStatus

class OrderRepository(customFirestore: FirebaseFirestore? = null) {

    private val firestore: FirebaseFirestore = customFirestore ?: FirebaseFirestore.getInstance()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    init {
        listenToOrders()
    }

    private fun listenToOrders() {
        try {
            firestore.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("OrderRepository", "Lỗi lắng nghe orders từ Firestore: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                val itemsRaw = doc.get("items") as? List<*>
                                val items = itemsRaw?.mapNotNull { itemMap ->
                                    val map = itemMap as? Map<*, *> ?: return@mapNotNull null
                                    CartItem(
                                        productId = map["productId"]?.toString() ?: "",
                                        productName = map["productName"]?.toString() ?: "",
                                        price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                                        imageUrl = map["imageUrl"]?.toString() ?: "",
                                        quantity = (map["quantity"] as? Number)?.toInt() ?: 1
                                    )
                                } ?: emptyList()

                                val statusStr = doc.getString("status") ?: OrderStatus.PENDING.name
                                val status = try {
                                    OrderStatus.valueOf(statusStr)
                                } catch (_: Exception) {
                                    OrderStatus.PENDING
                                }

                                Order(
                                    id = doc.getString("id") ?: doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    customerName = doc.getString("customerName") ?: "",
                                    userEmail = doc.getString("userEmail") ?: "",
                                    items = items,
                                    subtotal = doc.getDouble("subtotal") ?: 0.0,
                                    shippingFee = doc.getDouble("shippingFee") ?: 0.0,
                                    totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                    shippingAddress = doc.getString("shippingAddress") ?: "",
                                    phoneNumber = doc.getString("phoneNumber") ?: "",
                                    paymentMethod = doc.getString("paymentMethod") ?: "COD",
                                    status = status,
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.e("OrderRepository", "Lỗi phân tích đơn hàng ${doc.id}: ${e.message}")
                                null
                            }
                        }
                        _orders.value = list
                        Log.d("OrderRepository", "Đã đồng bộ ${list.size} đơn hàng từ Firestore")
                    }
                }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Không thể kết nối Firestore orders: ${e.message}")
        }
    }

    fun placeOrder(order: Order): Order {
        val newOrder = order.copy(
            id = if (order.id.isBlank()) "ORD${System.currentTimeMillis()}" else order.id,
            createdAt = if (order.createdAt > 0) order.createdAt else System.currentTimeMillis()
        )

        // 1. Cập nhật StateFlow ngay lập tức cho UI
        _orders.update { current -> listOf(newOrder) + current.filter { it.id != newOrder.id } }

        // 2. Lưu vào Cloud Firestore Database
        try {
            val itemsList = newOrder.items.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "price" to item.price,
                    "imageUrl" to item.imageUrl,
                    "quantity" to item.quantity
                )
            }

            val orderMap = hashMapOf(
                "id" to newOrder.id,
                "userId" to newOrder.userId,
                "customerName" to newOrder.customerName,
                "userEmail" to newOrder.userEmail,
                "items" to itemsList,
                "subtotal" to newOrder.subtotal,
                "shippingFee" to newOrder.shippingFee,
                "totalAmount" to newOrder.totalAmount,
                "shippingAddress" to newOrder.shippingAddress,
                "phoneNumber" to newOrder.phoneNumber,
                "paymentMethod" to newOrder.paymentMethod,
                "status" to newOrder.status.name,
                "createdAt" to newOrder.createdAt
            )

            firestore.collection("orders").document(newOrder.id).set(orderMap)
                .addOnSuccessListener {
                    Log.i("OrderRepository", "✅ Đã lưu đơn hàng ${newOrder.id} vào Firestore Database thành công!")
                }
                .addOnFailureListener { e ->
                    Log.e("OrderRepository", "❌ Lỗi lưu đơn hàng vào Firestore: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Lỗi thực thi lưu đơn hàng Firestore: ${e.message}")
        }

        return newOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            // 1. Cập nhật StateFlow
            var found = false
            _orders.update { current ->
                current.map {
                    if (it.id == orderId) {
                        found = true
                        it.copy(status = newStatus)
                    } else it
                }
            }

            // 2. Cập nhật trên Cloud Firestore
            firestore.collection("orders").document(orderId).update("status", newStatus.name)
                .addOnSuccessListener {
                    Log.i("OrderRepository", "✅ Đã cập nhật trạng thái đơn $orderId thành $newStatus trên Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e("OrderRepository", "❌ Lỗi cập nhật trạng thái đơn $orderId trên Firestore: ${e.message}", e)
                }

            if (found) Result.success(Unit)
            else Result.failure(Exception("Không tìm thấy đơn hàng $orderId"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cancelOrder(orderId: String): Boolean {
        val order = getOrderById(orderId) ?: return false
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.PROCESSING) {
            return false
        }
        val result = updateOrderStatus(orderId, OrderStatus.CANCELLED)
        return result.isSuccess
    }

    fun getOrderById(orderId: String): Order? {
        return _orders.value.find { it.id == orderId }
    }

    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return _orders.value.filter { it.status == status }
    }

    fun getAllOrders(): List<Order> = _orders.value
}

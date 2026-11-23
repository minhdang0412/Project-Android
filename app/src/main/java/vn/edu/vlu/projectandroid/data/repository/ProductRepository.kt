package vn.edu.vlu.projectandroid.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.vlu.projectandroid.data.local.MockData
import vn.edu.vlu.projectandroid.data.model.Category
import vn.edu.vlu.projectandroid.data.model.Product

class ProductRepository(customFirestore: FirebaseFirestore? = null) {

    private val firestore: FirebaseFirestore = customFirestore ?: FirebaseFirestore.getInstance()

    private val _products = MutableStateFlow<List<Product>>(MockData.products)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(MockData.categories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        listenToCategories()
        listenToProducts()
    }

    /**
     * Lắng nghe cập nhật thời gian thực của Danh mục từ Firestore
     */
    private fun listenToCategories() {
        try {
            firestore.collection("categories").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ProductRepository", "Lỗi lắng nghe categories từ Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        Category(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            iconName = doc.getString("iconName") ?: "PhoneAndroid"
                        )
                    }
                    _categories.value = list
                    Log.d("ProductRepository", "Đã tải ${list.size} danh mục từ Firestore")
                } else {
                    // Nếu trên Firestore chưa có, tự động nạp MockData lên Firestore
                    seedCategoriesToFirestore()
                }
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Không thể kết nối categories Firestore: ${e.message}")
        }
    }

    /**
     * Lắng nghe cập nhật thời gian thực của Sản phẩm từ Firestore
     */
    private fun listenToProducts() {
        try {
            firestore.collection("products").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ProductRepository", "Lỗi lắng nghe products từ Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val imageUrlsList = (doc.get("imageUrls") as? List<*>)?.mapNotNull { it?.toString() }
                            ?: doc.getString("imageUrl")?.let { listOf(it) }
                            ?: emptyList()

                        Product(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            originalPrice = doc.getDouble("originalPrice") ?: 0.0,
                            categoryId = doc.getString("categoryId") ?: "",
                            imageUrls = imageUrlsList,
                            rating = doc.getDouble("rating") ?: 5.0,
                            reviewCount = (doc.getLong("reviewCount") ?: 0L).toInt(),
                            stockQuantity = (doc.getLong("stockQuantity") ?: 50L).toInt()
                        )
                    }
                    _products.value = list
                    Log.d("ProductRepository", "Đã tải ${list.size} sản phẩm từ Firestore")
                } else {
                    // Nếu trên Firestore chưa có, tự động nạp MockData lên Firestore
                    seedProductsToFirestore()
                }
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Không thể kết nối products Firestore: ${e.message}")
        }
    }

    /**
     * Tự động khởi tạo dữ liệu danh mục mẫu lên Firestore nếu database trống
     */
    fun seedCategoriesToFirestore() {
        try {
            MockData.categories.forEach { category ->
                val data = hashMapOf(
                    "id" to category.id,
                    "name" to category.name,
                    "iconName" to category.iconName
                )
                firestore.collection("categories").document(category.id).set(data)
            }
            Log.i("ProductRepository", "Đã tự động khởi tạo danh mục mẫu lên Firestore!")
        } catch (e: Exception) {
            Log.w("ProductRepository", "Không thể khởi tạo categories lên Firestore: ${e.message}")
        }
    }

    /**
     * Tự động khởi tạo dữ liệu sản phẩm mẫu lên Firestore nếu database trống
     */
    fun seedProductsToFirestore() {
        try {
            MockData.products.forEach { product ->
                val data = hashMapOf(
                    "id" to product.id,
                    "name" to product.name,
                    "description" to product.description,
                    "price" to product.price,
                    "originalPrice" to product.originalPrice,
                    "categoryId" to product.categoryId,
                    "imageUrls" to product.imageUrls,
                    "rating" to product.rating,
                    "reviewCount" to product.reviewCount,
                    "stockQuantity" to product.stockQuantity
                )
                firestore.collection("products").document(product.id).set(data)
            }
            Log.i("ProductRepository", "Đã tự động khởi tạo sản phẩm mẫu lên Firestore!")
        } catch (e: Exception) {
            Log.w("ProductRepository", "Không thể khởi tạo products lên Firestore: ${e.message}")
        }
    }

    fun getCategories(): List<Category> = _categories.value

    fun getBannerUrls(): List<String> = MockData.bannerUrls

    fun getFeaturedProducts(): List<Product> {
        return _products.value.sortedByDescending { it.rating }.take(6)
    }

    fun getBestSellerProducts(): List<Product> {
        return _products.value.sortedByDescending { it.reviewCount }.take(6)
    }

    fun getProductsByCategory(categoryId: String): List<Product> {
        return _products.value.filter { it.categoryId == categoryId }
    }

    fun getProductById(productId: String): Product? {
        return _products.value.find { it.id == productId }
    }

    fun getCategoryById(categoryId: String): Category? {
        return _categories.value.find { it.id == categoryId }
    }

    fun getProductCountForCategory(categoryId: String): Int {
        return _products.value.count { it.categoryId == categoryId }
    }

    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return _products.value
        val q = query.lowercase()
        return _products.value.filter {
            it.name.lowercase().contains(q) ||
            it.description.lowercase().contains(q)
        }
    }

    fun filterProducts(
        query: String = "",
        categoryId: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: SortOption = SortOption.DEFAULT
    ): List<Product> {
        var results = if (query.isNotBlank()) searchProducts(query) else _products.value.toList()
        if (categoryId != null) {
            results = results.filter { it.categoryId == categoryId }
        }
        if (minPrice != null) {
            results = results.filter { it.price >= minPrice }
        }
        if (maxPrice != null) {
            results = results.filter { it.price <= maxPrice }
        }
        results = when (sortBy) {
            SortOption.PRICE_ASC -> results.sortedBy { it.price }
            SortOption.PRICE_DESC -> results.sortedByDescending { it.price }
            SortOption.RATING_DESC -> results.sortedByDescending { it.rating }
            SortOption.DEFAULT -> results
        }
        return results
    }

    // ================= ADMIN CRUD CHO PRODUCT =================

    fun addProduct(product: Product): Result<Product> {
        return try {
            val newProduct = if (product.id.isBlank()) {
                product.copy(id = "prod_${System.currentTimeMillis()}")
            } else product

            // 1. Cập nhật StateFlow ngay lập tức cho giao diện người dùng
            _products.update { current ->
                listOf(newProduct) + current.filter { it.id != newProduct.id }
            }

            // 2. Lưu vào Cloud Firestore Database
            val data = hashMapOf(
                "id" to newProduct.id,
                "name" to newProduct.name,
                "description" to newProduct.description,
                "price" to newProduct.price,
                "originalPrice" to newProduct.originalPrice,
                "categoryId" to newProduct.categoryId,
                "imageUrls" to newProduct.imageUrls,
                "rating" to newProduct.rating,
                "reviewCount" to newProduct.reviewCount,
                "stockQuantity" to newProduct.stockQuantity
            )
            firestore.collection("products").document(newProduct.id).set(data)
                .addOnSuccessListener {
                    Log.i("ProductRepository", "Đã thêm sản phẩm ${newProduct.name} vào Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e("ProductRepository", "Lỗi lưu sản phẩm lên Firestore: ${e.message}", e)
                }

            Result.success(newProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateProduct(product: Product): Result<Product> {
        return try {
            var found = false
            // 1. Cập nhật StateFlow
            _products.update { current ->
                current.map {
                    if (it.id == product.id) {
                        found = true
                        product
                    } else it
                }
            }

            // 2. Cập nhật Firestore Database
            val data = hashMapOf(
                "id" to product.id,
                "name" to product.name,
                "description" to product.description,
                "price" to product.price,
                "originalPrice" to product.originalPrice,
                "categoryId" to product.categoryId,
                "imageUrls" to product.imageUrls,
                "rating" to product.rating,
                "reviewCount" to product.reviewCount,
                "stockQuantity" to product.stockQuantity
            )
            firestore.collection("products").document(product.id).set(data)
                .addOnSuccessListener {
                    Log.i("ProductRepository", "Đã cập nhật sản phẩm ${product.name} trên Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e("ProductRepository", "Lỗi cập nhật sản phẩm Firestore: ${e.message}", e)
                }

            if (found) Result.success(product)
            else Result.failure(Exception("Không tìm thấy sản phẩm có mã ${product.id}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteProduct(productId: String): Result<Unit> {
        return try {
            // 1. Xóa khỏi StateFlow
            _products.update { current ->
                current.filter { it.id != productId }
            }

            // 2. Xóa khỏi Firestore Database
            firestore.collection("products").document(productId).delete()
                .addOnSuccessListener {
                    Log.i("ProductRepository", "Đã xóa sản phẩm $productId khỏi Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e("ProductRepository", "Lỗi xóa sản phẩm khỏi Firestore: ${e.message}", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================= ADMIN CRUD CHO CATEGORY =================

    fun addCategory(category: Category): Result<Category> {
        return try {
            val newCategory = if (category.id.isBlank()) {
                category.copy(id = "cat_${System.currentTimeMillis()}")
            } else category

            // 1. Cập nhật StateFlow
            _categories.update { current ->
                current.filter { it.id != newCategory.id } + newCategory
            }

            // 2. Lưu vào Firestore Database
            val data = hashMapOf(
                "id" to newCategory.id,
                "name" to newCategory.name,
                "iconName" to newCategory.iconName
            )
            firestore.collection("categories").document(newCategory.id).set(data)
                .addOnSuccessListener {
                    Log.i("ProductRepository", "Đã thêm danh mục ${newCategory.name} vào Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e("ProductRepository", "Lỗi lưu danh mục lên Firestore: ${e.message}", e)
                }

            Result.success(newCategory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateCategory(category: Category): Result<Category> {
        return try {
            var found = false
            // 1. Cập nhật StateFlow
            _categories.update { current ->
                current.map {
                    if (it.id == category.id) {
                        found = true
                        category
                    } else it
                }
            }

            // 2. Cập nhật Firestore Database
            val data = hashMapOf(
                "id" to category.id,
                "name" to category.name,
                "iconName" to category.iconName
            )
            firestore.collection("categories").document(category.id).set(data)
                .addOnSuccessListener {
                    Log.i("ProductRepository", "Đã cập nhật danh mục ${category.name} trên Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e("ProductRepository", "Lỗi cập nhật danh mục Firestore: ${e.message}", e)
                }

            if (found) Result.success(category)
            else Result.failure(Exception("Không tìm thấy danh mục"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            // 1. Xóa khỏi StateFlow
            _categories.update { current ->
                current.filter { it.id != categoryId }
            }
            _products.update { current ->
                current.map {
                    if (it.categoryId == categoryId) it.copy(categoryId = "") else it
                }
            }

            // 2. Xóa khỏi Firestore Database
            firestore.collection("categories").document(categoryId).delete()
                .addOnSuccessListener {
                    Log.i("ProductRepository", "Đã xóa danh mục $categoryId khỏi Firestore!")
                }
                .addOnFailureListener { e ->
                    Log.e("ProductRepository", "Lỗi xóa danh mục khỏi Firestore: ${e.message}", e)
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

enum class SortOption(val displayName: String) {
    DEFAULT("Mặc định"),
    PRICE_ASC("Giá tăng dần"),
    PRICE_DESC("Giá giảm dần"),
    RATING_DESC("Đánh giá cao nhất")
}

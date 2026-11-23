package vn.edu.vlu.projectandroid.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import vn.edu.vlu.projectandroid.data.local.MockData
import vn.edu.vlu.projectandroid.data.model.Category
import vn.edu.vlu.projectandroid.data.model.Product

class ProductRepository {

    private val _products = MutableStateFlow<List<Product>>(MockData.products)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(MockData.categories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

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

            _products.update { current ->
                listOf(newProduct) + current
            }
            Result.success(newProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateProduct(product: Product): Result<Product> {
        return try {
            var found = false
            _products.update { current ->
                current.map {
                    if (it.id == product.id) {
                        found = true
                        product
                    } else it
                }
            }
            if (found) Result.success(product)
            else Result.failure(Exception("Không tìm thấy sản phẩm có mã ${product.id}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteProduct(productId: String): Result<Unit> {
        return try {
            _products.update { current ->
                current.filter { it.id != productId }
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

            _categories.update { current ->
                current + newCategory
            }
            Result.success(newCategory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateCategory(category: Category): Result<Category> {
        return try {
            var found = false
            _categories.update { current ->
                current.map {
                    if (it.id == category.id) {
                        found = true
                        category
                    } else it
                }
            }
            if (found) Result.success(category)
            else Result.failure(Exception("Không tìm thấy danh mục"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            // Xóa danh mục
            _categories.update { current ->
                current.filter { it.id != categoryId }
            }
            // Gỡ categoryId của các sản phẩm thuộc danh mục này
            _products.update { current ->
                current.map {
                    if (it.categoryId == categoryId) it.copy(categoryId = "") else it
                }
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

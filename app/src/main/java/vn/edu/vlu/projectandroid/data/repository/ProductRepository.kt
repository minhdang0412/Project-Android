package vn.edu.vlu.projectandroid.data.repository

import vn.edu.vlu.projectandroid.data.local.MockData
import vn.edu.vlu.projectandroid.data.model.Category
import vn.edu.vlu.projectandroid.data.model.Product

class ProductRepository {

    private val allProducts = MockData.products.toMutableList()

    fun getCategories(): List<Category> = MockData.categories

    fun getBannerUrls(): List<String> = MockData.bannerUrls

    fun getFeaturedProducts(): List<Product> {
        return allProducts.sortedByDescending { it.rating }.take(6)
    }

    fun getBestSellerProducts(): List<Product> {
        return allProducts.sortedByDescending { it.reviewCount }.take(6)
    }

    fun getProductsByCategory(categoryId: String): List<Product> {
        return allProducts.filter { it.categoryId == categoryId }
    }

    fun getProductById(productId: String): Product? {
        return allProducts.find { it.id == productId }
    }

    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return allProducts
        val q = query.lowercase()
        return allProducts.filter {
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
        var results = if (query.isNotBlank()) searchProducts(query) else allProducts.toList()
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
}

enum class SortOption(val displayName: String) {
    DEFAULT("Mặc định"),
    PRICE_ASC("Giá tăng dần"),
    PRICE_DESC("Giá giảm dần"),
    RATING_DESC("Đánh giá cao nhất")
}

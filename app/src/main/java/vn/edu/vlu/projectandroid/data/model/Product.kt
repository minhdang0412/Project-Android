package vn.edu.vlu.projectandroid.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val categoryId: String = "",
    val imageUrls: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val stockQuantity: Int = 0,
    val isFavorite: Boolean = false
) {
    val discountPercent: Int
        get() = if (originalPrice > 0 && originalPrice > price) {
            ((1 - price / originalPrice) * 100).toInt()
        } else 0

    val imageUrl: String get() = imageUrls.firstOrNull() ?: ""
}

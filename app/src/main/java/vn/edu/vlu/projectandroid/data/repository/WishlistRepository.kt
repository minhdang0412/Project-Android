package vn.edu.vlu.projectandroid.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WishlistRepository {

    private val _wishlistProductIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistProductIds: StateFlow<Set<String>> = _wishlistProductIds.asStateFlow()

    fun toggleWishlist(productId: String) {
        _wishlistProductIds.update { current ->
            if (current.contains(productId)) {
                current - productId
            } else {
                current + productId
            }
        }
    }

    fun isInWishlist(productId: String): Boolean {
        return _wishlistProductIds.value.contains(productId)
    }

    fun getWishlistIds(): Set<String> = _wishlistProductIds.value
}

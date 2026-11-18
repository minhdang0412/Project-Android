package vn.edu.vlu.projectandroid.ui.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.edu.vlu.projectandroid.data.model.CartItem
import vn.edu.vlu.projectandroid.data.repository.CartRepository
import vn.edu.vlu.projectandroid.data.repository.ProductRepository
import vn.edu.vlu.projectandroid.data.repository.WishlistRepository
import vn.edu.vlu.projectandroid.ui.components.EmptyState
import vn.edu.vlu.projectandroid.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    productRepository: ProductRepository,
    wishlistRepository: WishlistRepository,
    cartRepository: CartRepository,
    onProductClick: (String) -> Unit
) {
    val wishlistIds by wishlistRepository.wishlistProductIds.collectAsState()
    val wishlistProducts = wishlistIds.mapNotNull { productRepository.getProductById(it) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Yêu thích (${wishlistProducts.size})") })
        }
    ) { padding ->
        if (wishlistProducts.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.FavoriteBorder,
                title = "Chưa có sản phẩm yêu thích",
                subtitle = "Nhấn vào biểu tượng trái tim để thêm sản phẩm",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(wishlistProducts) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = true,
                        onProductClick = onProductClick,
                        onFavoriteClick = { wishlistRepository.toggleWishlist(it) }
                    )
                }
            }
        }
    }
}

package vn.edu.vlu.projectandroid.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import vn.edu.vlu.projectandroid.data.model.Category
import vn.edu.vlu.projectandroid.data.repository.ProductRepository
import vn.edu.vlu.projectandroid.data.repository.WishlistRepository
import vn.edu.vlu.projectandroid.ui.components.ProductCard

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "PhoneAndroid" -> Icons.Filled.PhoneAndroid
        "Laptop" -> Icons.Filled.Laptop
        "Headphones" -> Icons.Filled.Headphones
        "Watch" -> Icons.Filled.Watch
        "Tablet" -> Icons.Filled.Tablet
        "CameraAlt" -> Icons.Filled.CameraAlt
        "Speaker" -> Icons.Filled.Speaker
        "Cable" -> Icons.Filled.Cable
        else -> Icons.Filled.PhoneAndroid
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    productRepository: ProductRepository,
    wishlistRepository: WishlistRepository,
    authRepository: vn.edu.vlu.projectandroid.data.repository.AuthRepository,
    onProductClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToAdmin: () -> Unit = {}
) {
    val products by productRepository.products.collectAsState()
    val categories by productRepository.categories.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState(initial = null)

    val featuredProducts = remember(products) {
        products.sortedByDescending { it.rating }.take(6)
    }
    val bestSellers = remember(products) {
        products.sortedByDescending { it.reviewCount }.take(6)
    }
    val bannerUrls = productRepository.getBannerUrls()
    val wishlistIds by wishlistRepository.wishlistProductIds.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Banner Quản trị viên (nếu người dùng là Admin)
        if (currentUser?.isAdmin == true) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAdmin() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bảng điều khiển Quản trị",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Thêm, xóa, sửa sản phẩm và danh mục",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Search bar
        item(span = { GridItemSpan(2) }) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Tìm kiếm sản phẩm...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSearchClick() },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Banner carousel
        item(span = { GridItemSpan(2) }) {
            BannerCarousel(bannerUrls = bannerUrls)
        }

        // Categories
        item(span = { GridItemSpan(2) }) {
            Column {
                SectionTitle("Danh mục")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(category = category, onClick = {})
                    }
                }
            }
        }

        // Featured products header
        item(span = { GridItemSpan(2) }) {
            SectionTitle("Sản phẩm nổi bật")
        }

        // Featured product cards
        items(featuredProducts) { product ->
            ProductCard(
                product = product,
                isFavorite = wishlistIds.contains(product.id),
                onProductClick = onProductClick,
                onFavoriteClick = { wishlistRepository.toggleWishlist(it) }
            )
        }

        // Best sellers header
        item(span = { GridItemSpan(2) }) {
            SectionTitle("Bán chạy nhất")
        }

        // Best seller cards
        items(bestSellers) { product ->
            ProductCard(
                product = product,
                isFavorite = wishlistIds.contains(product.id),
                onProductClick = onProductClick,
                onFavoriteClick = { wishlistRepository.toggleWishlist(it) }
            )
        }

        // Bottom spacing
        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BannerCarousel(bannerUrls: List<String>) {
    if (bannerUrls.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { bannerUrls.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            val next = (pagerState.currentPage + 1) % bannerUrls.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
        ) { page ->
            AsyncImage(
                model = bannerUrls[page],
                contentDescription = "Banner ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(bannerUrls.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier.size(56.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.iconName),
                    contentDescription = category.name,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 11.sp
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

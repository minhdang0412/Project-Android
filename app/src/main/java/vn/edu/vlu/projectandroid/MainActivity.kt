package vn.edu.vlu.projectandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import vn.edu.vlu.projectandroid.data.repository.AuthRepository
import vn.edu.vlu.projectandroid.data.repository.CartRepository
import vn.edu.vlu.projectandroid.data.repository.OrderRepository
import vn.edu.vlu.projectandroid.data.repository.ProductRepository
import vn.edu.vlu.projectandroid.data.repository.WishlistRepository
import vn.edu.vlu.projectandroid.navigation.AppNavGraph
import vn.edu.vlu.projectandroid.navigation.Screen
import vn.edu.vlu.projectandroid.ui.theme.ProjectAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepository = AuthRepository(applicationContext)
        val productRepository = ProductRepository()
        val cartRepository = CartRepository()
        val orderRepository = OrderRepository()
        val wishlistRepository = WishlistRepository()

        setContent {
            ProjectAndroidTheme {
                ShopApp(
                    authRepository = authRepository,
                    productRepository = productRepository,
                    cartRepository = cartRepository,
                    orderRepository = orderRepository,
                    wishlistRepository = wishlistRepository
                )
            }
        }
    }
}

@Composable
fun ShopApp(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    wishlistRepository: WishlistRepository
) {
    val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = null)

    when (isLoggedIn) {
        null -> {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        true -> {
            AppNavGraph(
                authRepository = authRepository,
                productRepository = productRepository,
                cartRepository = cartRepository,
                orderRepository = orderRepository,
                wishlistRepository = wishlistRepository,
                startDestination = Screen.Home.route
            )
        }
        false -> {
            AppNavGraph(
                authRepository = authRepository,
                productRepository = productRepository,
                cartRepository = cartRepository,
                orderRepository = orderRepository,
                wishlistRepository = wishlistRepository,
                startDestination = Screen.Login.route
            )
        }
    }
}

package vn.edu.vlu.projectandroid.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import vn.edu.vlu.projectandroid.data.repository.AuthRepository
import vn.edu.vlu.projectandroid.data.repository.CartRepository
import vn.edu.vlu.projectandroid.data.repository.OrderRepository
import vn.edu.vlu.projectandroid.data.repository.ProductRepository
import vn.edu.vlu.projectandroid.data.repository.WishlistRepository
import vn.edu.vlu.projectandroid.ui.admin.AdminCategoryListScreen
import vn.edu.vlu.projectandroid.ui.admin.AdminDashboardScreen
import vn.edu.vlu.projectandroid.ui.admin.AdminEditProductScreen
import vn.edu.vlu.projectandroid.ui.admin.AdminProductListScreen
import vn.edu.vlu.projectandroid.ui.auth.EmailVerificationScreen
import vn.edu.vlu.projectandroid.ui.auth.ForgotPasswordScreen
import vn.edu.vlu.projectandroid.ui.auth.LoginScreen
import vn.edu.vlu.projectandroid.ui.auth.RegisterScreen
import vn.edu.vlu.projectandroid.ui.cart.CartScreen
import vn.edu.vlu.projectandroid.ui.checkout.CheckoutScreen
import vn.edu.vlu.projectandroid.ui.detail.ProductDetailScreen
import vn.edu.vlu.projectandroid.ui.home.HomeScreen
import vn.edu.vlu.projectandroid.ui.orders.OrderDetailScreen
import vn.edu.vlu.projectandroid.ui.orders.OrderSuccessScreen
import vn.edu.vlu.projectandroid.ui.orders.OrderListScreen
import vn.edu.vlu.projectandroid.ui.search.SearchScreen
import vn.edu.vlu.projectandroid.ui.wishlist.WishlistScreen

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun AppNavGraph(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    orderRepository: OrderRepository,
    wishlistRepository: WishlistRepository,
    startDestination: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val cartItems by cartRepository.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }

    val bottomNavItems = listOf(
        BottomNavItem("Trang chủ", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Tìm kiếm", Screen.Search.route, Icons.Filled.Search, Icons.Outlined.Search),
        BottomNavItem("Yêu thích", Screen.Wishlist.route, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        BottomNavItem("Giỏ hàng", Screen.Cart.route, Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, badgeCount = cartCount),
        BottomNavItem("Đơn hàng", Screen.Orders.route, Icons.Filled.Receipt, Icons.Outlined.Receipt)
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badgeCount > 0) {
                                            Badge { Text("${item.badgeCount}") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                }
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth
            composable(Screen.Login.route) {
                LoginScreen(
                    authRepository = authRepository,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onNavigateToVerification = {
                        navController.navigate(Screen.EmailVerification.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    authRepository = authRepository,
                    onRegisterSuccess = {
                        navController.navigate(Screen.EmailVerification.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authRepository = authRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.EmailVerification.route) {
                EmailVerificationScreen(
                    authRepository = authRepository,
                    onVerificationSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Main tabs
            composable(Screen.Home.route) {
                HomeScreen(
                    productRepository = productRepository,
                    wishlistRepository = wishlistRepository,
                    authRepository = authRepository,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Screen.AdminDashboard.route)
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    productRepository = productRepository,
                    wishlistRepository = wishlistRepository,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }
            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    productRepository = productRepository,
                    wishlistRepository = wishlistRepository,
                    cartRepository = cartRepository,
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    cartRepository = cartRepository,
                    onCheckout = { navController.navigate(Screen.Checkout.route) },
                    onContinueShopping = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Orders.route) {
                OrderListScreen(
                    orderRepository = orderRepository,
                    authRepository = authRepository,
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToAdmin = {
                        navController.navigate(Screen.AdminDashboard.route)
                    }
                )
            }

            // Detail screens
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    productRepository = productRepository,
                    cartRepository = cartRepository,
                    wishlistRepository = wishlistRepository,
                    onNavigateBack = { navController.popBackStack() },
                    onBuyNow = {
                        navController.navigate(Screen.Checkout.route)
                    }
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    cartRepository = cartRepository,
                    orderRepository = orderRepository,
                    authRepository = authRepository,
                    onOrderPlaced = { orderId ->
                        navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.OrderSuccess.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderSuccessScreen(
                    orderId = orderId,
                    onViewOrder = {
                        navController.navigate(Screen.OrderDetail.createRoute(orderId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onContinueShopping = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderDetailScreen(
                    orderId = orderId,
                    orderRepository = orderRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Admin destinations
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    productRepository = productRepository,
                    onNavigateToProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onNavigateToCategories = { navController.navigate(Screen.AdminCategories.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminProducts.route) {
                AdminProductListScreen(
                    productRepository = productRepository,
                    onNavigateToAddProduct = {
                        navController.navigate(Screen.AdminEditProduct.createRoute("new"))
                    },
                    onNavigateToEditProduct = { productId ->
                        navController.navigate(Screen.AdminEditProduct.createRoute(productId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.AdminEditProduct.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: "new"
                AdminEditProductScreen(
                    productId = productId,
                    productRepository = productRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminCategories.route) {
                AdminCategoryListScreen(
                    productRepository = productRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

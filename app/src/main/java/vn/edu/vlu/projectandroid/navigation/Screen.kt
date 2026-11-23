package vn.edu.vlu.projectandroid.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    object Home : Screen("home")
    object Search : Screen("search")
    object Wishlist : Screen("wishlist")
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: String) = "order_success/$orderId"
    }
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    // Admin Routes
    object AdminDashboard : Screen("admin_dashboard")
    object AdminProducts : Screen("admin_products")
    object AdminEditProduct : Screen("admin_edit_product/{productId}") {
        fun createRoute(productId: String = "new") = "admin_edit_product/$productId"
    }
    object AdminCategories : Screen("admin_categories")
    object AdminOrders : Screen("admin_orders")
}

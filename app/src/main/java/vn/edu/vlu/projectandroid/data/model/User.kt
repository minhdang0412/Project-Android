package vn.edu.vlu.projectandroid.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val role: String = "USER" // "USER" hoặc "ADMIN"
) {
    val isAdmin: Boolean
        get() = role.equals("ADMIN", ignoreCase = true) || 
                email.lowercase().startsWith("admin")
}

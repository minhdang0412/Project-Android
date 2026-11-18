package vn.edu.vlu.projectandroid.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import vn.edu.vlu.projectandroid.data.model.User

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class AuthRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_ADDRESS = stringPreferencesKey("user_address")
    }

    /**
     * Flow theo dõi trạng thái đăng nhập từ Firebase Auth và DataStore
     */
    val isLoggedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Thông tin người dùng hiện tại từ DataStore
     */
    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val currentFirebaseUser = auth.currentUser
        if (currentFirebaseUser != null) {
            User(
                id = prefs[USER_ID] ?: currentFirebaseUser.uid,
                name = prefs[USER_NAME] ?: (currentFirebaseUser.displayName ?: ""),
                email = prefs[USER_EMAIL] ?: (currentFirebaseUser.email ?: ""),
                phoneNumber = prefs[USER_PHONE] ?: "",
                address = prefs[USER_ADDRESS] ?: ""
            )
        } else null
    }

    /**
     * Đăng ký tài khoản với Firebase Authentication và lưu dữ liệu người dùng vào Firestore
     */
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        val trimmedEmail = email.lowercase().trim()
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()

        return try {
            // 1. Tạo tài khoản trên Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Không thể tạo tài khoản người dùng trên Firebase"))

            // 2. Cập nhật DisplayName trên Firebase Auth profile
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // 3. Chuẩn bị User model
            val user = User(
                id = firebaseUser.uid,
                name = trimmedName,
                email = trimmedEmail,
                phoneNumber = trimmedPhone,
                address = ""
            )

            // 4. Lưu thông tin người dùng vào Cloud Firestore database (collection "users")
            val userMap = hashMapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "address" to user.address,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(user.id).set(userMap).await()

            // 5. Lưu phiên đăng nhập cục bộ
            saveUserSession(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    /**
     * Đăng nhập với Firebase Authentication và đồng bộ dữ liệu từ Firestore
     */
    suspend fun login(email: String, password: String): Result<User> {
        val trimmedEmail = email.lowercase().trim()

        return try {
            // 1. Đăng nhập qua Firebase Authentication
            val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Đăng nhập thất bại"))

            // 2. Lấy thông tin chi tiết từ Cloud Firestore
            val user = try {
                val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
                if (doc.exists()) {
                    User(
                        id = firebaseUser.uid,
                        name = doc.getString("name") ?: (firebaseUser.displayName ?: ""),
                        email = doc.getString("email") ?: (firebaseUser.email ?: trimmedEmail),
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        address = doc.getString("address") ?: ""
                    )
                } else {
                    User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: trimmedEmail,
                        phoneNumber = "",
                        address = ""
                    )
                }
            } catch (e: Exception) {
                // Fallback nếu Firestore rules chặn hoặc chưa tạo doc
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: trimmedEmail,
                    phoneNumber = "",
                    address = ""
                )
            }

            // 3. Lưu phiên đăng nhập
            saveUserSession(user)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    /**
     * Đăng xuất khỏi Firebase và xóa phiên cục bộ
     */
    suspend fun logout() {
        try {
            auth.signOut()
        } catch (_: Exception) {}

        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun getCurrentUser(): User? {
        return currentUser.first()
    }

    private suspend fun saveUserSession(user: User) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = user.id
            prefs[USER_NAME] = user.name
            prefs[USER_EMAIL] = user.email
            prefs[USER_PHONE] = user.phoneNumber
            prefs[USER_ADDRESS] = user.address
        }
    }

    /**
     * Chuyển đổi mã lỗi Firebase thành thông báo tiếng Việt thân thiện
     */
    private fun getReadableErrorMessage(e: Exception): String {
        val message = e.message ?: ""
        return when (e) {
            is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu (yêu cầu tối thiểu 6 ký tự)"
            is FirebaseAuthUserCollisionException -> "Email này đã được sử dụng cho một tài khoản khác"
            is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu không chính xác"
            is FirebaseNetworkException -> "Lỗi kết nối mạng đến Firebase. Vui lòng kiểm tra internet"
            else -> {
                if (message.contains("API key not valid", ignoreCase = true) ||
                    message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)
                ) {
                    "Chưa cấu hình API Key hợp lệ trong file google-services.json từ Firebase Console"
                } else if (message.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    "Không có quyền truy cập Firestore. Vui lòng kiểm tra Security Rules"
                } else if (message.contains("no user record", ignoreCase = true)) {
                    "Tài khoản không tồn tại"
                } else {
                    e.localizedMessage ?: "Có lỗi xảy ra, vui lòng thử lại"
                }
            }
        }
    }
}

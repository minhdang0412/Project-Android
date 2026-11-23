package vn.edu.vlu.projectandroid.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import kotlinx.coroutines.withTimeoutOrNull
import vn.edu.vlu.projectandroid.data.FirebaseInitializer
import vn.edu.vlu.projectandroid.data.model.User

class EmailNotVerifiedException(val email: String) : 
    Exception("Email chưa được xác thực. Vui lòng kiểm tra hộp thư để kích hoạt tài khoản.")

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class AuthRepository(
    private val context: Context,
    customAuth: FirebaseAuth? = null,
    customFirestore: FirebaseFirestore? = null
) {
    init {
        FirebaseInitializer.init(context)
    }

    private val auth: FirebaseAuth = customAuth ?: FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = customFirestore ?: FirebaseFirestore.getInstance()

    // Email đang chờ xác thực để hiển thị trên màn hình xác thực
    var pendingVerificationEmail: String = ""

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_ADDRESS = stringPreferencesKey("user_address")
        val USER_ROLE = stringPreferencesKey("user_role")
    }

    /**
     * Flow theo dõi trạng thái đăng nhập:
     * Người dùng chỉ được coi là đã đăng nhập khi có tài khoản VÀ email đã được xác thực
     */
    val isLoggedIn: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user != null && user.isEmailVerified)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Thông tin người dùng hiện tại từ DataStore
     */
    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val currentFirebaseUser = auth.currentUser
        if (currentFirebaseUser != null && currentFirebaseUser.isEmailVerified) {
            val userEmail = prefs[USER_EMAIL] ?: (currentFirebaseUser.email ?: "")
            val role = prefs[USER_ROLE] ?: if (userEmail.lowercase().startsWith("admin")) "ADMIN" else "USER"
            User(
                id = prefs[USER_ID] ?: currentFirebaseUser.uid,
                name = prefs[USER_NAME] ?: (currentFirebaseUser.displayName ?: ""),
                email = userEmail,
                phoneNumber = prefs[USER_PHONE] ?: "",
                address = prefs[USER_ADDRESS] ?: "",
                role = role
            )
        } else null
    }

    /**
     * Lấy email của tài khoản đang đăng nhập (kể cả chưa verify)
     */
    fun getCurrentUserEmail(): String {
        return pendingVerificationEmail.ifBlank { auth.currentUser?.email ?: "" }
    }

    /**
     * Đăng ký tài khoản:
     * 1. Tạo user trên Firebase Auth
     * 2. Gửi email xác thực tới hòm thư người dùng
     * 3. Lưu hồ sơ sơ bộ vào Firestore
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
        pendingVerificationEmail = trimmedEmail

        return try {
            // 1. Tạo tài khoản trên Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Không thể tạo tài khoản người dùng trên Firebase"))

            // 2. Cập nhật DisplayName
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                Log.w("AuthRepository", "Could not update displayName: ${e.message}")
            }

            // 3. Tự động gửi Email xác thực (giới hạn 3s, không làm treo)
            try {
                withTimeoutOrNull(3000) {
                    firebaseUser.sendEmailVerification().await()
                }
                Log.d("AuthRepository", "Verification email sent to $trimmedEmail")
            } catch (e: Exception) {
                Log.e("AuthRepository", "Failed to send verification email", e)
            }

            // 4. Chuẩn bị User model & lưu vào Firestore
            val userRole = if (trimmedEmail.startsWith("admin")) "ADMIN" else "USER"
            val user = User(
                id = firebaseUser.uid,
                name = trimmedName,
                email = trimmedEmail,
                phoneNumber = trimmedPhone,
                address = "",
                role = userRole
            )

            // Lưu Firestore: ghi hồ sơ người dùng vào collection "users"
            try {
                val userMap = hashMapOf(
                    "id" to user.id,
                    "name" to user.name,
                    "email" to user.email,
                    "phoneNumber" to user.phoneNumber,
                    "address" to user.address,
                    "role" to user.role,
                    "isEmailVerified" to firebaseUser.isEmailVerified,
                    "createdAt" to System.currentTimeMillis()
                )

                // Gọi ghi dữ liệu vào collection "users"
                val writeTask = firestore.collection("users").document(user.id).set(userMap)
                writeTask.addOnSuccessListener {
                    Log.i("AuthRepository", "✅ Đã lưu tài khoản ${user.email} vào Firestore Database thành công!")
                }.addOnFailureListener { e ->
                    Log.e("AuthRepository", "❌ Lỗi lưu tài khoản vào Firestore: ${e.message}", e)
                    if (e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                        Log.e("AuthRepository", "👉 BẠN CẦN MỞ RULES TRÊN FIREBASE CONSOLE: Cập nhật Rules thành 'allow read, write: if true;' và bấm Publish.")
                    }
                }

                // Chờ tối đa 5 giây để đồng bộ trực tiếp lên server
                withTimeoutOrNull(5000) {
                    writeTask.await()
                }
            } catch (e: Exception) {
                Log.w("AuthRepository", "Firestore write timeout hoặc chặn bởi Rules: ${e.message}")
            }

            // Lưu thông tin người dùng vào DataStore (chưa set is_logged_in vì chưa xác thực)
            context.dataStore.edit { prefs ->
                prefs[USER_ID] = user.id
                prefs[USER_NAME] = user.name
                prefs[USER_EMAIL] = user.email
                prefs[USER_PHONE] = user.phoneNumber
                prefs[USER_ADDRESS] = user.address
                prefs[USER_ROLE] = user.role
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    /**
     * Gửi lại email xác thực
     */
    suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Không tìm thấy phiên người dùng. Vui lòng đăng nhập lại."))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    /**
     * Kiểm tra xem người dùng đã nhấn liên kết xác thực trong email chưa
     */
    suspend fun checkEmailVerified(): Result<Boolean> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("Không tìm thấy người dùng."))
            
            // Reload user từ Firebase server để cập nhật trạng thái mới nhất
            user.reload().await()
            val isVerified = user.isEmailVerified

            if (isVerified) {
                context.dataStore.edit { prefs ->
                    prefs[IS_LOGGED_IN] = true
                }
                try {
                    firestore.collection("users").document(user.uid)
                        .update("isEmailVerified", true)
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Could not update isEmailVerified: ${e.message}")
                }
            }
            Result.success(isVerified)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    /**
     * Gửi email đặt lại mật khẩu (Quên mật khẩu)
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val trimmedEmail = email.lowercase().trim()
        if (trimmedEmail.isBlank()) {
            return Result.failure(Exception("Vui lòng nhập địa chỉ email."))
        }
        return try {
            auth.sendPasswordResetEmail(trimmedEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(getReadableErrorMessage(e)))
        }
    }

    /**
     * Đăng nhập: Kiểm tra mật khẩu và trạng thái xác thực email
     */
    suspend fun login(email: String, password: String): Result<User> {
        val trimmedEmail = email.lowercase().trim()

        return try {
            val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = authResult.user
                ?: return Result.failure(Exception("Đăng nhập thất bại."))

            // Tải lại dữ liệu mới nhất từ server để kiểm tra emailVerified
            firebaseUser.reload().await()

            if (!firebaseUser.isEmailVerified) {
                pendingVerificationEmail = firebaseUser.email ?: trimmedEmail
                // Đăng xuất ngay lập tức để không lưu phiên nếu chưa xác thực email
                auth.signOut()
                context.dataStore.edit { prefs ->
                    prefs.clear()
                }
                return Result.failure(EmailNotVerifiedException(pendingVerificationEmail))
            }

            // Lấy thông tin chi tiết từ Firestore (tối đa 3s không làm treo app)
            val user = try {
                val doc = withTimeoutOrNull(3000) {
                    firestore.collection("users").document(firebaseUser.uid).get().await()
                }
                val defaultRole = if (trimmedEmail.startsWith("admin")) "ADMIN" else "USER"
                if (doc != null && doc.exists()) {
                    User(
                        id = firebaseUser.uid,
                        name = doc.getString("name") ?: (firebaseUser.displayName ?: ""),
                        email = doc.getString("email") ?: (firebaseUser.email ?: trimmedEmail),
                        phoneNumber = doc.getString("phoneNumber") ?: "",
                        address = doc.getString("address") ?: "",
                        role = doc.getString("role") ?: defaultRole
                    )
                } else {
                    val newUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: trimmedEmail,
                        phoneNumber = "",
                        address = "",
                        role = defaultRole
                    )
                    // Tự động lưu tài khoản vào Firestore nếu chưa tồn tại
                    try {
                        val userMap = hashMapOf(
                            "id" to newUser.id,
                            "name" to newUser.name,
                            "email" to newUser.email,
                            "phoneNumber" to newUser.phoneNumber,
                            "address" to newUser.address,
                            "role" to newUser.role,
                            "isEmailVerified" to firebaseUser.isEmailVerified,
                            "createdAt" to System.currentTimeMillis()
                        )
                        firestore.collection("users").document(newUser.id).set(userMap)
                            .addOnSuccessListener {
                                Log.i("AuthRepository", "Đã tự động đồng bộ tài khoản vào Firestore khi đăng nhập")
                            }
                    } catch (e: Exception) {
                        Log.w("AuthRepository", "Auto-sync to Firestore failed: ${e.message}")
                    }
                    newUser
                }
            } catch (e: Exception) {
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: trimmedEmail,
                    phoneNumber = "",
                    address = "",
                    role = if (trimmedEmail.startsWith("admin")) "ADMIN" else "USER"
                )
            }

            // Lưu phiên đăng nhập
            saveUserSession(user)

            Result.success(user)
        } catch (e: EmailNotVerifiedException) {
            Result.failure(e)
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
            prefs[USER_ROLE] = user.role
        }
    }

    /**
     * Chuyển đổi mã lỗi Firebase thành thông báo tiếng Việt thân thiện
     */
    private fun getReadableErrorMessage(e: Exception): String {
        Log.e("AuthRepository", "Firebase Auth error: ${e.message}", e)
        val message = e.message ?: ""
        return when (e) {
            is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu (yêu cầu tối thiểu 6 ký tự)"
            is FirebaseAuthUserCollisionException -> "Email này đã được sử dụng cho một tài khoản khác"
            is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu không chính xác"
            is FirebaseNetworkException -> "Lỗi kết nối mạng đến Firebase. Vui lòng kiểm tra internet"
            else -> {
                when {
                    message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                        "Chưa kích hoạt Authentication trên Firebase Console. Vui lòng vào Firebase Console > Authentication > Sign-in method và bật 'Email/Password'."
                    message.contains("API key not valid", ignoreCase = true) ->
                        "API Key chưa hợp lệ. Vui lòng Clean Project và Rebuild lại ứng dụng trong Android Studio."
                    message.contains("ADMIN_ONLY_OPERATION", ignoreCase = true) ->
                        "Chưa cho phép người dùng tự đăng ký (vào Authentication Settings tắt User signup disabled)."
                    message.contains("PERMISSION_DENIED", ignoreCase = true) ->
                        "Không có quyền truy cập Firestore. Vui lòng kiểm tra Security Rules (chọn Test mode)."
                    message.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) ->
                        "Bạn đã thử quá nhiều lần. Vui lòng thử lại sau vài phút."
                    message.contains("user-not-found", ignoreCase = true) || message.contains("no user record", ignoreCase = true) ->
                        "Tài khoản không tồn tại"
                    else -> e.localizedMessage ?: "Có lỗi xảy ra, vui lòng thử lại (${e.javaClass.simpleName})"
                }
            }
        }
    }
}

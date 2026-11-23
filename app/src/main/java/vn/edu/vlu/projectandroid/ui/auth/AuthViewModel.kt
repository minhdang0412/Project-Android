package vn.edu.vlu.projectandroid.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.repository.AuthRepository
import vn.edu.vlu.projectandroid.data.repository.EmailNotVerifiedException

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isEmailNotVerified: Boolean = false,
    val unverifiedEmail: String = "",
    val isResetPasswordSent: Boolean = false,
    val isVerificationEmailSent: Boolean = false,
    val isEmailVerified: Boolean = false
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập đầy đủ thông tin")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Email không hợp lệ")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } else {
                val exception = result.exceptionOrNull()
                if (exception is EmailNotVerifiedException) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEmailNotVerified = true,
                        unverifiedEmail = exception.email,
                        error = exception.message
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception?.message ?: "Đăng nhập thất bại"
                    )
                }
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()
        val cleanPassword = password.trim()
        val cleanConfirm = confirmPassword.trim()

        if (cleanName.isBlank() || cleanEmail.isBlank() || cleanPhone.isBlank() || cleanPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập đầy đủ tất cả các trường thông tin.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = "Địa chỉ email không đúng định dạng. Vui lòng kiểm tra lại.")
            return
        }
        if (cleanPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Mật khẩu phải có ít nhất 6 ký tự.")
            return
        }
        if (cleanPassword != cleanConfirm) {
            _uiState.value = _uiState.value.copy(error = "Mật khẩu nhập lại không khớp.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.register(cleanName, cleanEmail, cleanPhone, cleanPassword)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    unverifiedEmail = cleanEmail,
                    error = null
                )
            } else {
                val ex = result.exceptionOrNull()
                val errorMsg = ex?.message ?: "Đăng ký thất bại"
                val isCollision = ex is FirebaseAuthUserCollisionException ||
                        errorMsg.contains("đã được sử dụng", ignoreCase = true) ||
                        errorMsg.contains("already in use", ignoreCase = true)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    isEmailNotVerified = isCollision,
                    unverifiedEmail = cleanEmail,
                    error = errorMsg
                )
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.sendEmailVerification()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isVerificationEmailSent = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Không thể gửi email xác thực"
                )
            }
        }
    }

    fun checkEmailVerified() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.checkEmailVerified()
            if (result.isSuccess) {
                val verified = result.getOrDefault(false)
                if (verified) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEmailVerified = true,
                        isSuccess = true,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Email vẫn chưa được kích hoạt. Hãy mở email và nhấn vào link xác thực."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Lỗi kiểm tra trạng thái email"
                )
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập email hợp lệ để đặt lại mật khẩu")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isResetPasswordSent = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Không thể gửi email đặt lại mật khẩu"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}

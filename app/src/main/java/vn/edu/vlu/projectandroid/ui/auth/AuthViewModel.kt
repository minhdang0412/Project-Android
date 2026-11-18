package vn.edu.vlu.projectandroid.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.repository.AuthRepository

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Vui lòng nhập đầy đủ thông tin")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState(error = "Email không hợp lệ")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.login(email, password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(isSuccess = true)
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message ?: "Đăng nhập thất bại")
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Vui lòng nhập đầy đủ thông tin")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = AuthUiState(error = "Email không hợp lệ")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "Mật khẩu phải có ít nhất 6 ký tự")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState(error = "Mật khẩu nhập lại không khớp")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = authRepository.register(name, email, phone, password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(isSuccess = true)
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message ?: "Đăng ký thất bại")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

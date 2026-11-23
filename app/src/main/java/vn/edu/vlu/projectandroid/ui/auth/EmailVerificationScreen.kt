package vn.edu.vlu.projectandroid.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    authRepository: AuthRepository,
    onVerificationSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    email: String = ""
) {
    val viewModel = remember { AuthViewModel(authRepository) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val displayEmail = email.ifBlank { authRepository.getCurrentUserEmail() }

    // Bộ đếm ngược 60 giây gửi lại email
    var countdown by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            countdown = 60
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            isTimerRunning = false
        }
    }

    LaunchedEffect(uiState.isEmailVerified) {
        if (uiState.isEmailVerified) {
            scope.launch {
                snackbarHostState.showSnackbar("Email đã kích hoạt thành công! Đang chuyển sang Đăng nhập...")
                authRepository.logout()
                delay(1000)
                onVerificationSuccess()
            }
        }
    }

    LaunchedEffect(uiState.isVerificationEmailSent) {
        if (uiState.isVerificationEmailSent) {
            snackbarHostState.showSnackbar("Đã gửi lại email xác thực. Vui lòng kiểm tra hộp thư!")
            isTimerRunning = true
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhắc nhở xác thực email") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            authRepository.logout()
                            onBackToLogin()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.size(110.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.MarkEmailRead,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Xác thực email để đăng nhập",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tài khoản của bạn đã được tạo thành công!\nĐể bảo mật và kích hoạt quyền đăng nhập, vui lòng kiểm tra hộp thư:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = displayEmail.ifBlank { "Email của bạn" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📌 Các bước thực hiện:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Mở ứng dụng Gmail hoặc hòm thư của bạn.\n" +
                               "2. Tìm thư gửi từ hệ thống (kiểm tra cả mục Spam / Rác).\n" +
                               "3. Bấm vào liên kết xác nhận trong email.\n" +
                               "4. Sau khi bấm liên kết, nhấn nút bên dưới để tiến hành đăng nhập.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Nút kiểm tra trạng thái xác thực và tiến hành đăng nhập
            Button(
                onClick = {
                    // Nếu còn phiên tạm -> reload kiểm tra
                    // Nếu đã xác thực -> chuyển sang đăng nhập
                    viewModel.checkEmailVerified()
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tôi đã xác thực ➔ Đến Đăng nhập", fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nút gửi lại email xác thực
            OutlinedButton(
                onClick = { viewModel.resendVerificationEmail() },
                enabled = !isTimerRunning && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                if (isTimerRunning) {
                    Text("Gửi lại sau (${countdown}s)", fontSize = 15.sp)
                } else {
                    Text("Gửi lại email xác thực", fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút quay lại đăng nhập
            TextButton(
                onClick = {
                    scope.launch {
                        authRepository.logout()
                        onBackToLogin()
                    }
                }
            ) {
                Text(
                    text = "Quay lại trang Đăng nhập",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

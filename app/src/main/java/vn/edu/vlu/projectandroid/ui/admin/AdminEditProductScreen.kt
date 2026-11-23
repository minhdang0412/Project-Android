package vn.edu.vlu.projectandroid.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.model.Product
import vn.edu.vlu.projectandroid.data.repository.ProductRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditProductScreen(
    productId: String,
    productRepository: ProductRepository,
    onNavigateBack: () -> Unit
) {
    val categories by productRepository.categories.collectAsState()
    val isEditMode = productId.isNotBlank() && productId != "new"

    val existingProduct = remember(productId) {
        if (isEditMode) productRepository.getProductById(productId) else null
    }

    var name by remember { mutableStateOf(existingProduct?.name ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(existingProduct?.categoryId ?: categories.firstOrNull()?.id ?: "")
    }
    var priceText by remember {
        mutableStateOf(existingProduct?.let { String.format(java.util.Locale.US, "%.0f", it.price) } ?: "")
    }
    var originalPriceText by remember {
        mutableStateOf(existingProduct?.originalPrice?.let { String.format(java.util.Locale.US, "%.0f", it) } ?: "")
    }
    var stockText by remember {
        mutableStateOf(existingProduct?.stockQuantity?.toString() ?: "50")
    }
    var imageUrl by remember {
        mutableStateOf(existingProduct?.imageUrl ?: "https://picsum.photos/400/400?random=${System.currentTimeMillis() % 1000}")
    }
    var description by remember {
        mutableStateOf(existingProduct?.description ?: "")
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Chỉnh sửa sản phẩm" else "Thêm sản phẩm mới",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Xem trước hình ảnh
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Xem trước ảnh",
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEEEEE)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Xem trước hình ảnh sản phẩm",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tên sản phẩm
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text("Tên sản phẩm *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Chọn danh mục
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
            ) {
                val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Chọn danh mục"
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Danh mục *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoryId = cat.id
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Giá bán & Giá gốc
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it; errorMessage = null },
                    label = { Text("Giá bán (đ) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = originalPriceText,
                    onValueChange = { originalPriceText = it },
                    label = { Text("Giá gốc (đ)") },
                    placeholder = { Text("Không bắt buộc") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Số lượng kho
            OutlinedTextField(
                value = stockText,
                onValueChange = { stockText = it; errorMessage = null },
                label = { Text("Số lượng trong kho *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Đường dẫn hình ảnh
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Link URL ảnh sản phẩm") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Mô tả chi tiết
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả sản phẩm") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Thông báo lỗi nếu có
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nút Lưu
            Button(
                onClick = {
                    val cleanName = name.trim()
                    val cleanPrice = priceText.toDoubleOrNull()
                    val cleanOriginalPrice = originalPriceText.toDoubleOrNull()
                    val cleanStock = stockText.toIntOrNull()

                    if (cleanName.isBlank()) {
                        errorMessage = "Vui lòng nhập tên sản phẩm"
                        return@Button
                    }
                    if (cleanPrice == null || cleanPrice <= 0) {
                        errorMessage = "Vui lòng nhập giá bán hợp lệ (> 0)"
                        return@Button
                    }
                    if (cleanStock == null || cleanStock < 0) {
                        errorMessage = "Vui lòng nhập số lượng tồn kho hợp lệ (>= 0)"
                        return@Button
                    }

                    isSaving = true
                    scope.launch {
                        val productToSave = Product(
                            id = if (isEditMode) existingProduct!!.id else "",
                            name = cleanName,
                            description = description.trim(),
                            price = cleanPrice,
                            originalPrice = cleanOriginalPrice ?: 0.0,
                            imageUrls = listOf(imageUrl.trim().ifBlank { "https://picsum.photos/400/400" }),
                            categoryId = selectedCategoryId,
                            rating = existingProduct?.rating ?: 5.0,
                            reviewCount = existingProduct?.reviewCount ?: 0,
                            stockQuantity = cleanStock
                        )

                        val result = if (isEditMode) {
                            productRepository.updateProduct(productToSave)
                        } else {
                            productRepository.addProduct(productToSave)
                        }

                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar(
                                if (isEditMode) "Cập nhật sản phẩm thành công!" else "Thêm sản phẩm mới thành công!"
                            )
                            delay(600)
                            onNavigateBack()
                        } else {
                            isSaving = false
                            errorMessage = result.exceptionOrNull()?.message ?: "Lỗi khi lưu sản phẩm"
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaving) "Đang lưu..." else if (isEditMode) "Cập nhật sản phẩm" else "Thêm sản phẩm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

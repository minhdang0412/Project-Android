package vn.edu.vlu.projectandroid.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import vn.edu.vlu.projectandroid.data.model.Category
import vn.edu.vlu.projectandroid.data.repository.ProductRepository
import vn.edu.vlu.projectandroid.ui.components.EmptyState
import vn.edu.vlu.projectandroid.ui.home.getCategoryIcon

val availableCategoryIcons = listOf(
    "PhoneAndroid" to Icons.Filled.PhoneAndroid,
    "Laptop" to Icons.Filled.Laptop,
    "Headphones" to Icons.Filled.Headphones,
    "Watch" to Icons.Filled.Watch,
    "Tablet" to Icons.Filled.Tablet,
    "CameraAlt" to Icons.Filled.CameraAlt,
    "Speaker" to Icons.Filled.Speaker,
    "Cable" to Icons.Filled.Cable
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryListScreen(
    productRepository: ProductRepository,
    onNavigateBack: () -> Unit
) {
    val categories by productRepository.categories.collectAsState()
    val products by productRepository.products.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý danh mục", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thêm danh mục", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Tổng cộng: ${categories.size} danh mục",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (categories.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Category,
                    title = "Chưa có danh mục nào",
                    subtitle = "Bấm nút Thêm danh mục để tạo mới"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories, key = { it.id }) { category ->
                        val productCount = products.count { it.categoryId == category.id }
                        AdminCategoryItemCard(
                            category = category,
                            productCount = productCount,
                            onEdit = {
                                editingCategory = category
                                showEditDialog = true
                            },
                            onDelete = { categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }

    // Dialog Thêm / Sửa Danh mục
    if (showEditDialog) {
        CategoryEditDialog(
            category = editingCategory,
            onDismiss = { showEditDialog = false },
            onSave = { name, iconName ->
                scope.launch {
                    val result = if (editingCategory != null) {
                        productRepository.updateCategory(editingCategory!!.copy(name = name, iconName = iconName))
                    } else {
                        productRepository.addCategory(Category(id = "", name = name, iconName = iconName))
                    }

                    if (result.isSuccess) {
                        snackbarHostState.showSnackbar(
                            if (editingCategory != null) "Cập nhật danh mục thành công!" else "Thêm danh mục mới thành công!"
                        )
                        showEditDialog = false
                    } else {
                        snackbarHostState.showSnackbar("Lỗi: ${result.exceptionOrNull()?.message}")
                    }
                }
            }
        )
    }

    // Dialog Xác nhận xóa danh mục
    categoryToDelete?.let { cat ->
        val productCount = products.count { it.categoryId == cat.id }
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Xác nhận xóa danh mục?") },
            text = {
                Text(
                    "Bạn có chắc muốn xóa danh mục \"${cat.name}\"?\n" +
                            if (productCount > 0) "Hiện có $productCount sản phẩm đang thuộc danh mục này, chúng sẽ chuyển về trạng thái Chưa phân loại."
                            else "Danh mục này hiện chưa có sản phẩm nào."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val result = productRepository.deleteCategory(cat.id)
                        categoryToDelete = null
                        scope.launch {
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Đã xóa danh mục \"${cat.name}\" thành công!")
                            } else {
                                snackbarHostState.showSnackbar("Lỗi: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun AdminCategoryItemCard(
    category: Category,
    productCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(category.iconName),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$productCount sản phẩm",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Chỉnh sửa",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CategoryEditDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(category?.iconName ?: "PhoneAndroid") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (category != null) "Chỉnh sửa danh mục" else "Thêm danh mục mới", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Tên danh mục *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Chọn biểu tượng đại diện:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCategoryIcons) { (iconKey, iconVector) ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = iconKey,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.trim().isBlank()) {
                        errorMessage = "Vui lòng nhập tên danh mục"
                    } else {
                        onSave(name.trim(), selectedIcon)
                    }
                }
            ) {
                Text("Lưu", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

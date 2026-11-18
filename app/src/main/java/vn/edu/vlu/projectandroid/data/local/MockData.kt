package vn.edu.vlu.projectandroid.data.local

import vn.edu.vlu.projectandroid.data.model.Category
import vn.edu.vlu.projectandroid.data.model.Product

object MockData {

    val categories = listOf(
        Category("cat_phone", "Điện thoại", "PhoneAndroid"),
        Category("cat_laptop", "Laptop", "Laptop"),
        Category("cat_headphone", "Tai nghe", "Headphones"),
        Category("cat_watch", "Đồng hồ", "Watch"),
        Category("cat_tablet", "Máy tính bảng", "Tablet"),
        Category("cat_camera", "Máy ảnh", "CameraAlt"),
        Category("cat_speaker", "Loa", "Speaker"),
        Category("cat_accessory", "Phụ kiện", "Cable")
    )

    val products = listOf(
        // Điện thoại
        Product(
            id = "p1",
            name = "Samsung Galaxy S25 Ultra",
            description = "Điện thoại flagship Samsung Galaxy S25 Ultra với chip Snapdragon 8 Elite, camera 200MP, màn hình Dynamic AMOLED 2X 6.9 inch, pin 5000mAh. Thiết kế Titanium cao cấp.",
            price = 28990000.0,
            originalPrice = 33990000.0,
            categoryId = "cat_phone",
            imageUrls = listOf(
                "https://picsum.photos/seed/s25ultra/400/400",
                "https://picsum.photos/seed/s25ultra2/400/400"
            ),
            rating = 4.8,
            reviewCount = 1250,
            stockQuantity = 50
        ),
        Product(
            id = "p2",
            name = "iPhone 16 Pro Max",
            description = "Apple iPhone 16 Pro Max với chip A18 Pro, camera 48MP, màn hình Super Retina XDR OLED 6.9 inch, pin cải tiến. Thiết kế Titanium Desert.",
            price = 34990000.0,
            originalPrice = 36990000.0,
            categoryId = "cat_phone",
            imageUrls = listOf(
                "https://picsum.photos/seed/ip16promax/400/400",
                "https://picsum.photos/seed/ip16promax2/400/400"
            ),
            rating = 4.9,
            reviewCount = 2340,
            stockQuantity = 30
        ),
        Product(
            id = "p3",
            name = "Xiaomi 15 Pro",
            description = "Xiaomi 15 Pro với Snapdragon 8 Elite, Leica camera 50MP, màn hình LTPO AMOLED 6.73 inch 2K, sạc nhanh 120W.",
            price = 16990000.0,
            originalPrice = 19990000.0,
            categoryId = "cat_phone",
            imageUrls = listOf("https://picsum.photos/seed/xiaomi15/400/400"),
            rating = 4.6,
            reviewCount = 890,
            stockQuantity = 100
        ),
        // Laptop
        Product(
            id = "p4",
            name = "MacBook Pro 14\" M4 Pro",
            description = "MacBook Pro 14 inch với chip Apple M4 Pro, 18GB RAM, 512GB SSD, màn hình Liquid Retina XDR, pin 17 giờ.",
            price = 49990000.0,
            originalPrice = 54990000.0,
            categoryId = "cat_laptop",
            imageUrls = listOf("https://picsum.photos/seed/macbookm4/400/400"),
            rating = 4.9,
            reviewCount = 567,
            stockQuantity = 25
        ),
        Product(
            id = "p5",
            name = "Dell XPS 15 (2025)",
            description = "Dell XPS 15 với Intel Core Ultra 7, 16GB RAM, 512GB SSD, màn hình OLED 3.5K 15.6 inch, card đồ họa RTX 4050.",
            price = 38990000.0,
            originalPrice = 42990000.0,
            categoryId = "cat_laptop",
            imageUrls = listOf("https://picsum.photos/seed/dellxps15/400/400"),
            rating = 4.7,
            reviewCount = 320,
            stockQuantity = 40
        ),
        // Tai nghe
        Product(
            id = "p6",
            name = "Sony WH-1000XM6",
            description = "Tai nghe chụp tai chống ồn Sony WH-1000XM6, driver 40mm, pin 40 giờ, LDAC Hi-Res, Multipoint kết nối 3 thiết bị.",
            price = 7990000.0,
            originalPrice = 8990000.0,
            categoryId = "cat_headphone",
            imageUrls = listOf("https://picsum.photos/seed/sonyxm6/400/400"),
            rating = 4.8,
            reviewCount = 1890,
            stockQuantity = 200
        ),
        Product(
            id = "p7",
            name = "AirPods Pro 3",
            description = "Apple AirPods Pro 3 với chip H3, chống ồn thích ứng, Spatial Audio, cổng USB-C, chống nước IP54.",
            price = 6790000.0,
            originalPrice = 7490000.0,
            categoryId = "cat_headphone",
            imageUrls = listOf("https://picsum.photos/seed/airpodspro3/400/400"),
            rating = 4.7,
            reviewCount = 3200,
            stockQuantity = 150
        ),
        // Đồng hồ
        Product(
            id = "p8",
            name = "Apple Watch Ultra 3",
            description = "Apple Watch Ultra 3, vỏ Titanium 49mm, GPS + Cellular, pin 72 giờ, chống nước 100m, cảm biến sức khỏe nâng cao.",
            price = 21990000.0,
            originalPrice = 23990000.0,
            categoryId = "cat_watch",
            imageUrls = listOf("https://picsum.photos/seed/applewatchultra3/400/400"),
            rating = 4.6,
            reviewCount = 450,
            stockQuantity = 60
        ),
        Product(
            id = "p9",
            name = "Samsung Galaxy Watch 7",
            description = "Samsung Galaxy Watch 7, màn hình AMOLED 1.5 inch, Wear OS 5, đo huyết áp, SpO2, pin 2 ngày.",
            price = 7490000.0,
            originalPrice = 8490000.0,
            categoryId = "cat_watch",
            imageUrls = listOf("https://picsum.photos/seed/galaxywatch7/400/400"),
            rating = 4.5,
            reviewCount = 780,
            stockQuantity = 120
        ),
        // Máy tính bảng
        Product(
            id = "p10",
            name = "iPad Pro M4 11\"",
            description = "iPad Pro 11 inch với chip M4, màn hình Ultra Retina XDR, 256GB, hỗ trợ Apple Pencil Pro.",
            price = 25990000.0,
            originalPrice = 28990000.0,
            categoryId = "cat_tablet",
            imageUrls = listOf("https://picsum.photos/seed/ipadprom4/400/400"),
            rating = 4.8,
            reviewCount = 1100,
            stockQuantity = 45
        ),
        // Máy ảnh
        Product(
            id = "p11",
            name = "Sony Alpha A7 IV",
            description = "Máy ảnh mirrorless Full-frame Sony A7 IV, cảm biến 33MP, quay 4K 60fps, lấy nét AI realtime.",
            price = 42990000.0,
            originalPrice = 48990000.0,
            categoryId = "cat_camera",
            imageUrls = listOf("https://picsum.photos/seed/sonya7iv/400/400"),
            rating = 4.9,
            reviewCount = 230,
            stockQuantity = 15
        ),
        // Loa
        Product(
            id = "p12",
            name = "JBL Charge 6",
            description = "Loa bluetooth di động JBL Charge 6, công suất 30W, pin 24 giờ, chống nước IP67, PartyBoost.",
            price = 3990000.0,
            originalPrice = 4590000.0,
            categoryId = "cat_speaker",
            imageUrls = listOf("https://picsum.photos/seed/jblcharge6/400/400"),
            rating = 4.6,
            reviewCount = 1560,
            stockQuantity = 300
        )
    )

    val bannerUrls = listOf(
        "https://picsum.photos/seed/banner1/800/300",
        "https://picsum.photos/seed/banner2/800/300",
        "https://picsum.photos/seed/banner3/800/300"
    )
}

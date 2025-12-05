# Hướng dẫn tạo dữ liệu trên MongoDB Compass

## Bước 1: Kết nối MongoDB Compass

1. Mở MongoDB Compass
2. Nhập connection string:
   ```
   mongodb+srv://dev:dev@test.0ao4iyu.mongodb.net/CafeShop?appName=test
   ```
3. Click "Connect"

## Bước 2: Tạo Database và Collections

Sau khi kết nối, bạn sẽ thấy database `CafeShop`. Nếu chưa có, tạo mới:

1. Click "Create Database"
2. Database Name: `CafeShop`
3. Collection Name: `users` (hoặc bất kỳ collection nào bạn muốn tạo trước)

## Bước 3: Tạo dữ liệu mẫu

### 3.1. Tạo Collection `users`

1. Chọn database `CafeShop`
2. Click "Create Collection" → Tên: `users`
3. Click vào collection `users`
4. Click "Add Data" → "Insert Document"
5. Chèn document sau (lặp lại cho user thứ 2):

**User Admin:**
```json
{
  "username": "admin",
  "password": "$2a$10$rOzJqZqZqZqZqZqZqZqZqOqZqZqZqZqZqZqZqZqZqZqZqZqZqZq",
  "fullName": "Admin User",
  "email": "admin@coffeeshop.com",
  "phone": "0123456789",
  "role": "admin",
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

**Lưu ý:** Password cần được hash bằng bcrypt. Để tạo password hash, bạn có thể:
- Sử dụng script seed (khuyến nghị)
- Hoặc tạo user qua API `/api/auth/register`

**User thường:**
```json
{
  "username": "user1",
  "password": "$2a$10$rOzJqZqZqZqZqZqZqZqZqOqZqZqZqZqZqZqZqZqZqZqZqZqZq",
  "fullName": "Nguyễn Văn A",
  "email": "user1@example.com",
  "phone": "0987654321",
  "role": "user",
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

### 3.2. Tạo Collection `items`

1. Tạo collection `items`
2. Click "Add Data" → "Insert Document"
3. Chèn document mẫu:

```json
{
  "name": "Cà phê Đen Đá",
  "category": "coffee",
  "image_url": "ca_phe_den_da.jpg",
  "basePrice": 25000,
  "description": "Cà phê đen đá truyền thống",
  "sizes": [
    {
      "name": "S",
      "modifier": 0,
      "label": "S"
    },
    {
      "name": "M",
      "modifier": 5000,
      "label": "M"
    },
    {
      "name": "L",
      "modifier": 10000,
      "label": "L"
    }
  ],
  "tempOptions": [
    {
      "name": "Hot",
      "modifier": 0,
      "label": "Hot"
    },
    {
      "name": "Iced",
      "modifier": 0,
      "label": "Iced"
    }
  ],
  "iceLevels": ["0%", "50%", "100%", "N/A"],
  "sugarLevels": ["0%", "50%", "100%"],
  "toppings": [
    {
      "name": "Trân châu",
      "price": 5000
    },
    {
      "name": "Thạch",
      "price": 3000
    },
    {
      "name": "Kem tươi",
      "price": 8000
    }
  ],
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

**Thêm các món khác tương tự:**
- Cà phê Sữa Đá (coffee, 35000)
- Latte Caramel (coffee, 55000)
- Socola Nóng (chocolate, 40000)
- Trà Đào Cam Sả (other, 44000)

### 3.3. Tạo Collection `orders`

1. Tạo collection `orders`
2. Click "Add Data" → "Insert Document"
3. Chèn document mẫu:

```json
{
  "userId": "PASTE_USER_ID_HERE",
  "orderDate": 1704067200000,
  "status": "Pending",
  "paymentMethod": "COD",
  "note": "Giao hàng cẩn thận",
  "subtotal": 108000,
  "discountAmount": 0,
  "shippingFee": 15000,
  "taxes": 0,
  "totalAmount": 123000,
  "deliveryAddress": {
    "fullName": "Trần Thị B",
    "phone": "0987654321",
    "street": "250 Đường Sư Vạn Hạnh",
    "ward": "Phường 13",
    "district": "Quận 10",
    "city": "TP. Hồ Chí Minh"
  },
  "items": [
    {
      "productId": "PASTE_ITEM_ID_HERE",
      "productName": "Cà phê Sữa Đá",
      "quantity": 1,
      "finalUnitPrice": 35000,
      "sizeChosen": "L",
      "tempChosen": "Iced",
      "iceLevel": "50%",
      "sugarLevel": "70%",
      "chosenToppings": [],
      "itemNote": "Ít ngọt thôi"
    }
  ],
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

**Lưu ý:** 
- Thay `PASTE_USER_ID_HERE` bằng `_id` của user từ collection `users`
- Thay `PASTE_ITEM_ID_HERE` bằng `_id` của item từ collection `items`
- Có thể tạo nhiều orders với các status khác nhau: "Pending", "Confirmed", "Delivering", "Delivered", "Cancelled"

### 3.4. Tạo Collection `combos` (tùy chọn)

1. Tạo collection `combos`
2. Click "Add Data" → "Insert Document"
3. Chèn document mẫu:

```json
{
  "name": "Combo Buổi Sáng",
  "description": "Combo gồm cà phê và bánh mì",
  "image_url": "combo_buoi_sang.jpg",
  "basePrice": 80000,
  "isActive": true,
  "items": [
    {
      "productId": "PASTE_ITEM_ID_1",
      "productName": "Cà phê Đen Đá",
      "quantity": 1,
      "finalUnitPrice": 25000,
      "sizeChosen": "M",
      "tempChosen": "Iced",
      "iceLevel": "50%",
      "sugarLevel": "50%",
      "chosenToppings": [],
      "itemNote": ""
    },
    {
      "productId": "PASTE_ITEM_ID_2",
      "productName": "Bánh mì",
      "quantity": 1,
      "finalUnitPrice": 55000,
      "sizeChosen": "",
      "tempChosen": "",
      "iceLevel": "N/A",
      "sugarLevel": "N/A",
      "chosenToppings": [],
      "itemNote": ""
    }
  ],
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z"
}
```

## Bước 4: Sử dụng Script Seed (Khuyến nghị)

Thay vì tạo thủ công, bạn có thể chạy script seed:

```bash
cd backend
node scripts/seedData.js
```

Script này sẽ tự động:
- Tạo users (admin và user thường) với password đã hash
- Tạo items mẫu
- Tạo orders mẫu
- Tạo combos (nếu cần)

## Lưu ý quan trọng

1. **Password Hash:** Password phải được hash bằng bcrypt. Script seed sẽ tự động làm điều này.

2. **ObjectId:** Khi tạo orders và combos, cần sử dụng `_id` thực tế từ các collection khác.

3. **Timestamps:** MongoDB sẽ tự động thêm `createdAt` và `updatedAt` nếu bạn set `timestamps: true` trong schema.

4. **Validation:** Đảm bảo dữ liệu tuân theo schema đã định nghĩa trong models.

## Kiểm tra dữ liệu

Sau khi tạo xong, bạn có thể:
1. Xem dữ liệu trong MongoDB Compass
2. Test API bằng Postman hoặc curl
3. Kiểm tra từ ứng dụng Android


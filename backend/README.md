# Coffee Shop Backend API

Backend API cho ứng dụng Coffee Shop Android được xây dựng với Node.js, Express và MongoDB.

## Cài đặt

1. Cài đặt dependencies:
```bash
npm install
```

2. Tạo file `.env` từ `.env.example`:
```bash
cp .env.example .env
```

3. Cập nhật các biến môi trường trong `.env`:
```
PORT=3000
MONGODB_URI=mongodb://localhost:27017/coffeeshop
JWT_SECRET=your-secret-key-here-change-in-production
NODE_ENV=development
```

4. Đảm bảo MongoDB đang chạy trên máy của bạn hoặc sử dụng MongoDB Atlas.

5. Chạy server:
```bash
# Development mode (với nodemon)
npm run dev

# Production mode
npm start
```

## API Endpoints

### Items
- `GET /api/items` - Lấy tất cả items (có thể filter: `?category=coffee`, `?search=latte`)
- `GET /api/items/:id` - Lấy item theo ID
- `POST /api/items` - Tạo item mới
- `PUT /api/items/:id` - Cập nhật item
- `DELETE /api/items/:id` - Xóa item (soft delete)

### Orders
- `GET /api/orders` - Lấy tất cả orders (có thể filter: `?userId=xxx`, `?status=Pending`)
- `GET /api/orders/:id` - Lấy order theo ID
- `POST /api/orders/create` - Tạo order mới
- `PUT /api/orders/:id` - Cập nhật order (chủ yếu để thay đổi status)
- `DELETE /api/orders/:id` - Hủy order

### Combos
- `GET /api/combos` - Lấy tất cả combos
- `GET /api/combos/:id` - Lấy combo theo ID
- `POST /api/combos` - Tạo combo mới
- `PUT /api/combos/:id` - Cập nhật combo
- `DELETE /api/combos/:id` - Xóa combo (soft delete)

### Authentication
- `POST /api/auth/register` - Đăng ký user mới
- `POST /api/auth/login` - Đăng nhập
- `GET /api/auth/me` - Lấy thông tin user hiện tại

## Cấu trúc Project

```
backend/
├── models/          # MongoDB models
│   ├── Item.js
│   ├── Order.js
│   ├── Combo.js
│   └── User.js
├── routes/          # API routes
│   ├── items.js
│   ├── orders.js
│   ├── combos.js
│   └── auth.js
├── server.js        # Main server file
├── package.json
└── README.md
```

## Lưu ý

- Đảm bảo MongoDB đang chạy trước khi start server
- Thay đổi `JWT_SECRET` trong production
- CORS đã được enable để frontend có thể gọi API
- Tất cả timestamps được tự động thêm bởi Mongoose


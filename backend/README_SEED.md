# Hướng dẫn Seed Data

## Cách 1: Sử dụng Script Seed (Khuyến nghị)

Script seed sẽ tự động tạo:
- Users (admin và user thường) với password đã được hash
- Items mẫu (5 món đồ uống)
- Orders mẫu (3 đơn hàng với các trạng thái khác nhau)

### Chạy script:

```bash
cd backend
npm run seed
```

Hoặc:

```bash
cd backend
node scripts/seedData.js
```

### Thông tin đăng nhập sau khi seed:

- **Admin:**
  - Username: `admin`
  - Password: `admin123`
  - Role: `admin`

- **User:**
  - Username: `user1`
  - Password: `user123`
  - Role: `user`

## Cách 2: Tạo dữ liệu thủ công trên MongoDB Compass

Xem file `GUIDE_MONGODB_COMPASS.md` để biết hướng dẫn chi tiết.

## Lưu ý

- Script sẽ không xóa dữ liệu cũ (đã comment out phần delete)
- Nếu muốn xóa và tạo lại, uncomment các dòng delete trong `scripts/seedData.js`
- Đảm bảo file `.env` đã được tạo với đúng thông tin MongoDB


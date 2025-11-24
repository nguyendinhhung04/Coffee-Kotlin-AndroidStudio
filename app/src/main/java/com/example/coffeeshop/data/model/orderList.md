// --- ORDER 1: Trần Thị B (Đơn hàng phức tạp - Giữ nguyên từ trước) ---
{
"userId": ObjectId("65b0e50f55e3a3c9e6d0a7a2"), // Trần Thị B
"orderDate": new Date("2024-01-25T11:45:00.000Z"),
"status": "Delivered",
"paymentMethod": "COD",
"note": "Giao hàng cẩn thận, không làm đổ.",

    // Billing: Subtotal 108,000 | Discount 0 | Shipping 15,000 | Total 123,000
    "subtotal": 108000.0,
    "discountAmount": 0.0,
    "shippingFee": 15000.0,
    "taxes": 0.0,
    "totalAmount": 123000.0, 

    "deliveryAddress": {
      "fullName": "Trần Thị B",
      "phone": "0987654321",
      "street": "250 Đường Sư Vạn Hạnh",
      "ward": "Phường 13",
      "district": "Quận 10",
      "city": "TP. Hồ Chí Minh"
    },
    
    "items": [
      // Món 1: Cà phê Sữa Đá size L (29k + 6k size L = 35k)
      {
        "productId": ObjectId("65b0e50f55e3a3c9e6d0a7b1"),
        "productName": "Cà phê Sữa Đá",
        "quantity": 1,
        "finalUnitPrice": 35000.0, 
        "sizeChosen": "L",
        "tempChosen": "Iced",
        "iceLevel": "50%",
        "sugarLevel": "70%",
        "chosenToppings": [],
        "itemNote": "Ít ngọt thôi."
      },
      // Món 2: Latte Caramel Đá Xay size M + Shot Espresso (55k + 5k blended + 12k shot = 72k)
      {
        "productId": ObjectId("65b0e50f55e3a3c9e6d0a7b2"),
        "productName": "Latte Caramel Đá Xay",
        "quantity": 1,
        "finalUnitPrice": 73000.0, // Đã điều chỉnh cho topping
        "sizeChosen": "M",
        "tempChosen": "Blended",
        "iceLevel": "100%",
        "sugarLevel": "100%",
        "chosenToppings": [
          {"name": "Shot Espresso Thêm", "price": 12000.0}
        ],
        "itemNote": "Nhiều kem tươi."
      }
    ]
},

// --- ORDER 2: Lê Văn C (Đơn hàng có giảm giá) ---
{
"userId": ObjectId("65b0e50f55e3a3c9e6d0a7a3"), // Lê Văn C
"orderDate": new Date("2024-03-01T09:30:00.000Z"),
"status": "Confirmed",
"paymentMethod": "COD",
"note": "Gọi điện trước khi giao hàng.",

    // Billing: Subtotal 44,000 | Discount 5,000 | Shipping 15,000 | Total 54,000
    "subtotal": 44000.0,
    "discountAmount": 5000.0,
    "shippingFee": 15000.0,
    "taxes": 0.0,
    "totalAmount": 54000.0, 

    "deliveryAddress": {
      "fullName": "Lê Văn C",
      "phone": "0912345678",
      "street": "123 Đường Điện Biên Phủ",
      "ward": "Phường 25",
      "district": "Quận Bình Thạnh",
      "city": "TP. Hồ Chí Minh"
    },
    
    "items": [
      // Món 1: Trà Đào Cam Sả size L (39k + 5k size L = 44k)
      {
        "productId": ObjectId("65b0e50f55e3a3c9e6d0a7b4"),
        "productName": "Trà Đào Cam Sả",
        "quantity": 1,
        "finalUnitPrice": 44000.0, 
        "sizeChosen": "L",
        "tempChosen": "Iced",
        "iceLevel": "50%",
        "sugarLevel": "50%",
        "chosenToppings": [],
        "itemNote": "Ít ngọt."
      }
    ]
},

// --- ORDER 3: Phạm Thị D (Đơn hàng hỗn hợp, có bánh ngọt) ---
{
"userId": ObjectId("65b0e50f55e3a3c9e6d0a7a4"), // Phạm Thị D
"orderDate": new Date("2024-04-10T14:00:00.000Z"),
"status": "Delivering",
"paymentMethod": "COD",
"note": "Không cần ghi chú gì thêm.",

    // Billing: Subtotal 70,000 | Discount 0 | Shipping 15,000 | Total 85,000
    "subtotal": 70000.0, // 25k + 45k
    "discountAmount": 0.0,
    "shippingFee": 15000.0,
    "taxes": 0.0,
    "totalAmount": 85000.0, 

    "deliveryAddress": {
      "fullName": "Phạm Thị D",
      "phone": "0909998887",
      "street": "700 Đường Lý Thường Kiệt",
      "ward": "Phường 1",
      "district": "Quận Tân Bình",
      "city": "TP. Hồ Chí Minh"
    },
    
    "items": [
      // Món 1: Cà phê Đen Đá Hot size M (25k)
      {
        "productId": ObjectId("65b0e50f55e3a3c9e6d0a7b7"),
        "productName": "Cà phê Đen Đá",
        "quantity": 1,
        "finalUnitPrice": 25000.0, 
        "sizeChosen": "M",
        "tempChosen": "Hot",
        "iceLevel": "N/A", // Không áp dụng cho Hot
        "sugarLevel": "100%",
        "chosenToppings": [],
        "itemNote": "Rất nóng."
      },
      // Món 2: Bánh Tiramisu (45k)
      {
        "productId": ObjectId("65b0e50f55e3a3c9e6d0a7b3"),
        "productName": "Bánh Tiramisu",
        "quantity": 1,
        "finalUnitPrice": 45000.0, 
        "sizeChosen": "Slice",
        "tempChosen": "N/A",
        "iceLevel": "N/A",
        "sugarLevel": "N/A",
        "chosenToppings": [],
        "itemNote": ""
      }
    ]
}
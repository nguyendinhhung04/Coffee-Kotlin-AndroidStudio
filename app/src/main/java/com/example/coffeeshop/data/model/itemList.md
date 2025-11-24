Mongo_Url=mongodb+srv://dev:dev@test.0ao4iyu.mongodb.net/CafeShop?appName=test
[
// --- 1. Cà phê Truyền thống: Cà phê Sữa Đá ---
{
"name": "Cà phê Sữa Đá",
"description": "Cà phê Robusta đậm đà pha phin truyền thống, kết hợp cùng sữa đặc ngọt béo.",
"category": "Coffee",
"basePrice": 29000.0,
"image_url": "https://placehold.co/600x400/000/fff?text=Ca+Phe+Sua+Da",
"sizes": [
{"name": "M", "modifier": 0.0, "label": "Vừa"},
{"name": "L", "modifier": 6000.0, "label": "Lớn"}
],
"tempOptions": [
{"name": "Iced", "modifier": 0.0, "label": "Lạnh"},
{"name": "Hot", "modifier": 0.0, "label": "Nóng"}
],
"iceLevels": ["100%", "70%", "50%", "0%"],
"sugarLevels": ["100%", "70%", "50%", "0%"],
"toppings": [
{"name": "Trân Châu Trắng", "price": 8000.0},
{"name": "Kem Phô Mai", "price": 10000.0}
],
"isActive": true
},

// --- 2. Thức uống hiện đại: Latte Caramel Đá Xay ---
{
"name": "Latte Caramel Đá Xay",
"description": "Sự kết hợp giữa Espresso, sữa tươi, sốt Caramel và đá xay, phủ kem tươi.",
"category": "Modern Coffee",
"basePrice": 55000.0,
"image_url": "https://placehold.co/600x400/8B4513/fff?text=Latte+Caramel",
"sizes": [
{"name": "M", "modifier": 0.0, "label": "Tiêu chuẩn"},
{"name": "L", "modifier": 8000.0, "label": "Extra Large"}
],
"tempOptions": [
{"name": "Blended", "modifier": 5000.0, "label": "Đá Xay"},
{"name": "Hot", "modifier": 0.0, "label": "Nóng"}
],
"iceLevels": ["100%", "70%", "50%", "0%"],
"sugarLevels": ["100%", "70%", "50%", "0%"],
"toppings": [
{"name": "Shot Espresso Thêm", "price": 12000.0},
{"name": "Sốt Chocolate", "price": 7000.0}
],
"isActive": true
},

// --- 3. Bánh ngọt: Bánh Tiramisu ---
{
"name": "Bánh Tiramisu",
"description": "Bánh Tiramisu truyền thống với phô mai Mascarpone, lòng đỏ trứng và cà phê.",
"category": "Cake",
"basePrice": 45000.0,
"image_url": "https://placehold.co/600x400/A0522D/fff?text=Tiramisu",
"sizes": [
{"name": "Slice", "modifier": 0.0, "label": "Miếng"}
],
"tempOptions": [],
"iceLevels": ["N/A"],
"sugarLevels": ["N/A"],
"toppings": [
{"name": "Nến", "price": 2000.0}
],
"isActive": true
},

// --- 4. Trà: Trà Đào Cam Sả ---
{
"name": "Trà Đào Cam Sả",
"description": "Vị trà thanh mát, thơm mùi sả và đào tươi.",
"category": "Tea",
"basePrice": 39000.0,
"image_url": "https://placehold.co/600x400/FFD700/000?text=Tra+Dao",
"sizes": [
{"name": "M", "modifier": 0.0, "label": "Vừa"},
{"name": "L", "modifier": 5000.0, "label": "Lớn"}
],
"tempOptions": [
{"name": "Iced", "modifier": 0.0, "label": "Lạnh"},
{"name": "Hot", "modifier": 0.0, "label": "Nóng"}
],
"iceLevels": ["100%", "70%", "50%", "0%"],
"sugarLevels": ["100%", "70%", "50%", "0%"],
"toppings": [
{"name": "Thạch Vải", "price": 8000.0}
],
"isActive": true
},

// --- 5. Cà phê Đặc biệt: Espresso Macchiato ---
{
"name": "Espresso Macchiato",
"description": "Một shot Espresso mạnh mẽ với một lượng nhỏ bọt sữa tươi.",
"category": "Specialty Coffee",
"basePrice": 40000.0,
"image_url": "https://placehold.co/600x400/6F4E37/fff?text=Macchiato",
"sizes": [
{"name": "S", "modifier": 0.0, "label": "Nhỏ"}
],
"tempOptions": [
{"name": "Hot", "modifier": 0.0, "label": "Nóng"}
],
"iceLevels": ["N/A"],
"sugarLevels": ["100%", "50%", "0%"],
"toppings": [],
"isActive": true
},

// --- 6. Bánh ngọt: Red Velvet Cupcake ---
{
"name": "Red Velvet Cupcake",
"description": "Bánh Cupcake Red Velvet ẩm mịn, phủ kem phô mai béo ngậy.",
"category": "Cake",
"basePrice": 35000.0,
"image_url": "https://placehold.co/600x400/DC143C/fff?text=Red+Velvet",
"sizes": [
{"name": "Unit", "modifier": 0.0, "label": "Chiếc"}
],
"tempOptions": [],
"iceLevels": ["N/A"],
"sugarLevels": ["N/A"],
"toppings": [
{"name": "Viên Chocolate", "price": 3000.0}
],
"isActive": true
},

// --- 7. Cà phê Truyền thống: Cà phê Đen Đá ---
{
"name": "Cà phê Đen Đá",
"description": "Cà phê đen nguyên chất pha phin truyền thống, không sữa.",
"category": "Coffee",
"basePrice": 25000.0,
"image_url": "https://placehold.co/600x400/121212/fff?text=Ca+Phe+Den",
"sizes": [
{"name": "M", "modifier": 0.0, "label": "Vừa"},
{"name": "L", "modifier": 5000.0, "label": "Lớn"}
],
"tempOptions": [
{"name": "Iced", "modifier": 0.0, "label": "Lạnh"},
{"name": "Hot", "modifier": 0.0, "label": "Nóng"}
],
"iceLevels": ["100%", "70%", "50%", "0%"],
"sugarLevels": ["100%", "70%", "50%", "0%"],
"toppings": [],
"isActive": true
},

// --- 8. Trà Sữa: Trà Sữa Trân Châu Đường Đen ---
{
"name": "Trà Sữa Trân Châu Đường Đen",
"description": "Trà sữa béo ngậy, đi kèm trân châu dẻo dai nấu cùng đường đen thơm lừng.",
"category": "Milk Tea",
"basePrice": 45000.0,
"image_url": "https://placehold.co/600x400/964B00/fff?text=Tra+Sua+Duong+Den",
"sizes": [
{"name": "M", "modifier": 0.0, "label": "Tiêu chuẩn"},
{"name": "L", "modifier": 7000.0, "label": "Đặc biệt"}
],
"tempOptions": [
{"name": "Iced", "modifier": 0.0, "label": "Lạnh"},
{"name": "Hot", "modifier": 0.0, "label": "Nóng"}
],
"iceLevels": ["100%", "70%", "50%", "0%"],
"sugarLevels": ["100%", "70%", "50%", "0%"],
"toppings": [
{"name": "Thạch Cà Phê", "price": 5000.0},
{"name": "Pudding Trứng", "price": 6000.0}
],
"isActive": true
},

// --- 9. Nước Ép/Smoothie: Sinh Tố Bơ ---
{
"name": "Sinh Tố Bơ",
"description": "Bơ tươi xay cùng sữa, vị béo tự nhiên, mát lạnh.",
"category": "Smoothie",
"basePrice": 50000.0,
"image_url": "https://placehold.co/600x400/50C878/fff?text=Sinh+To+Bo",
"sizes": [
{"name": "One Size", "modifier": 0.0, "label": "Cỡ Lớn"}
],
"tempOptions": [
{"name": "Blended", "modifier": 0.0, "label": "Đá Xay"}
],
// Đá/đường có thể tùy chỉnh
"iceLevels": ["100%", "70%", "50%", "0%"],
"sugarLevels": ["100%", "70%", "50%", "0%"],
"toppings": [
{"name": "Hạt Điều", "price": 5000.0}
],
"isActive": true
},

// --- 10. Bánh ngọt: Bánh Socola Lava ---
{
"name": "Bánh Socola Lava",
"description": "Bánh socola nóng hổi, nhân chảy lỏng, thường dùng kèm kem lạnh.",
"category": "Cake",
"basePrice": 60000.0,
"image_url": "https://placehold.co/600x400/654321/fff?text=Lava+Cake",
"sizes": [
{"name": "Unit", "modifier": 0.0, "label": "Một Phần"}
],
"tempOptions": [],
"iceLevels": ["N/A"],
"sugarLevels": ["N/A"],
"toppings": [
{"name": "Kem Vanilla", "price": 10000.0}
],
"isActive": true
}
]
const mongoose = require('mongoose');
const path = require('path');
require('dotenv').config({ path: path.join(__dirname, '..', '.env') });

const Item = require('../models/Item');
const Order = require('../models/Order');
const Combo = require('../models/Combo');
const User = require('../models/User');
const bcrypt = require('bcryptjs');

// Sample data
const sampleItems = [
  {
    name: "Cà phê Đen Đá",
    category: "coffee",
    image_url: "ca_phe_den_da.jpg",
    basePrice: 25000,
    description: "Cà phê đen đá truyền thống",
    sizes: [
      { name: "S", modifier: 0, label: "S" },
      { name: "M", modifier: 5000, label: "M" },
      { name: "L", modifier: 10000, label: "L" }
    ],
    tempOptions: [
      { name: "Hot", modifier: 0, label: "Hot" },
      { name: "Iced", modifier: 0, label: "Iced" }
    ],
    iceLevels: ["0%", "50%", "100%", "N/A"],
    sugarLevels: ["0%", "50%", "100%"],
    toppings: [
      { name: "Trân châu", price: 5000 },
      { name: "Thạch", price: 3000 },
      { name: "Kem tươi", price: 8000 }
    ],
    isActive: true
  },
  {
    name: "Cà phê Sữa Đá",
    category: "coffee",
    image_url: "ca_phe_sua_da.jpg",
    basePrice: 35000,
    description: "Cà phê sữa đá thơm ngon",
    sizes: [
      { name: "S", modifier: 0, label: "S" },
      { name: "M", modifier: 5000, label: "M" },
      { name: "L", modifier: 10000, label: "L" }
    ],
    tempOptions: [
      { name: "Hot", modifier: 0, label: "Hot" },
      { name: "Iced", modifier: 0, label: "Iced" }
    ],
    iceLevels: ["0%", "50%", "100%", "N/A"],
    sugarLevels: ["0%", "50%", "100%"],
    toppings: [
      { name: "Trân châu", price: 5000 },
      { name: "Thạch", price: 3000 }
    ],
    isActive: true
  },
  {
    name: "Latte Caramel",
    category: "coffee",
    image_url: "latte_caramel.jpg",
    basePrice: 55000,
    description: "Latte với vị caramel ngọt ngào",
    sizes: [
      { name: "S", modifier: 0, label: "S" },
      { name: "M", modifier: 5000, label: "M" },
      { name: "L", modifier: 10000, label: "L" }
    ],
    tempOptions: [
      { name: "Hot", modifier: 0, label: "Hot" },
      { name: "Iced", modifier: 0, label: "Iced" },
      { name: "Blended", modifier: 5000, label: "Blended" }
    ],
    iceLevels: ["0%", "50%", "100%", "N/A"],
    sugarLevels: ["0%", "50%", "100%"],
    toppings: [
      { name: "Kem tươi", price: 8000 },
      { name: "Shot Espresso Thêm", price: 12000 }
    ],
    isActive: true
  },
  {
    name: "Socola Nóng",
    category: "chocolate",
    image_url: "socola_nong.jpg",
    basePrice: 40000,
    description: "Socola nóng ấm áp",
    sizes: [
      { name: "S", modifier: 0, label: "S" },
      { name: "M", modifier: 5000, label: "M" },
      { name: "L", modifier: 10000, label: "L" }
    ],
    tempOptions: [
      { name: "Hot", modifier: 0, label: "Hot" },
      { name: "Iced", modifier: 0, label: "Iced" }
    ],
    iceLevels: ["0%", "50%", "100%", "N/A"],
    sugarLevels: ["0%", "50%", "100%"],
    toppings: [
      { name: "Kem tươi", price: 8000 },
      { name: "Marshmallow", price: 5000 }
    ],
    isActive: true
  },
  {
    name: "Trà Đào Cam Sả",
    category: "other",
    image_url: "tra_dao_cam_sa.jpg",
    basePrice: 44000,
    description: "Trà đào cam sả thanh mát",
    sizes: [
      { name: "S", modifier: 0, label: "S" },
      { name: "M", modifier: 5000, label: "M" },
      { name: "L", modifier: 10000, label: "L" }
    ],
    tempOptions: [
      { name: "Iced", modifier: 0, label: "Iced" }
    ],
    iceLevels: ["0%", "50%", "100%"],
    sugarLevels: ["0%", "50%", "100%"],
    toppings: [
      { name: "Thạch đào", price: 5000 },
      { name: "Trân châu", price: 5000 }
    ],
    isActive: true
  }
];

const sampleUsers = [
  {
    username: "admin",
    password: "admin123",
    fullName: "Admin User",
    email: "admin@coffeeshop.com",
    phone: "0123456789",
    role: "admin"
  },
  {
    username: "nguyenvana",
    password: "admin",
    fullName: "Nguyễn Văn A",
    email: "nguyenvana@coffeeshop.com",
    phone: "0123456789",
    role: "admin"
  },
  {
    username: "user1",
    password: "user123",
    fullName: "Nguyễn Văn B",
    email: "user1@example.com",
    phone: "0987654321",
    role: "user"
  }
];

async function seedDatabase() {
  try {
    // Connect to MongoDB
    const MONGO_URI = process.env.MONGO_URI || process.env.MONGODB_URI;
    await mongoose.connect(MONGO_URI);
    console.log('Connected to MongoDB');

    // Clear existing data (optional - comment out if you want to keep existing data)
    // await Item.deleteMany({});
    // await User.deleteMany({});
    // await Order.deleteMany({});
    // await Combo.deleteMany({});

    // Seed Users
    console.log('Seeding users...');
    for (const userData of sampleUsers) {
      const existingUser = await User.findOne({ username: userData.username });
      if (!existingUser) {
        const user = new User(userData);
        await user.save();
        console.log(`Created user: ${userData.username}`);
      } else {
        console.log(`User ${userData.username} already exists`);
      }
    }

    // Seed Items
    console.log('Seeding items...');
    for (const itemData of sampleItems) {
      const existingItem = await Item.findOne({ name: itemData.name });
      if (!existingItem) {
        const item = new Item(itemData);
        await item.save();
        console.log(`Created item: ${itemData.name}`);
      } else {
        console.log(`Item ${itemData.name} already exists`);
      }
    }

    // Seed Sample Orders
    console.log('Seeding sample orders...');
    const users = await User.find({ role: 'user' });
    const items = await Item.find();

    if (users.length > 0 && items.length > 0) {
      const sampleOrders = [
        {
          userId: users[0]._id.toString(),
          orderDate: Date.now() - 86400000, // 1 day ago
          status: "Pending",
          paymentMethod: "COD",
          note: "Giao hàng cẩn thận",
          subtotal: 108000,
          discountAmount: 0,
          shippingFee: 15000,
          taxes: 0,
          totalAmount: 123000,
          deliveryAddress: {
            fullName: "Trần Thị B",
            phone: "0987654321",
            street: "250 Đường Sư Vạn Hạnh",
            ward: "Phường 13",
            district: "Quận 10",
            city: "TP. Hồ Chí Minh"
          },
          items: [
            {
              productId: items[1]._id.toString(),
              productName: items[1].name,
              quantity: 1,
              finalUnitPrice: 35000,
              sizeChosen: "L",
              tempChosen: "Iced",
              iceLevel: "50%",
              sugarLevel: "70%",
              chosenToppings: [],
              itemNote: "Ít ngọt thôi"
            },
            {
              productId: items[2]._id.toString(),
              productName: items[2].name,
              quantity: 1,
              finalUnitPrice: 73000,
              sizeChosen: "M",
              tempChosen: "Blended",
              iceLevel: "100%",
              sugarLevel: "100%",
              chosenToppings: [
                { name: "Shot Espresso Thêm", price: 12000 }
              ],
              itemNote: "Nhiều kem tươi"
            }
          ]
        },
        {
          userId: users[0]._id.toString(),
          orderDate: Date.now() - 3600000, // 1 hour ago
          status: "Confirmed",
          paymentMethod: "COD",
          note: "Gọi điện trước khi giao",
          subtotal: 44000,
          discountAmount: 5000,
          shippingFee: 15000,
          taxes: 0,
          totalAmount: 54000,
          deliveryAddress: {
            fullName: "Lê Văn C",
            phone: "0912345678",
            street: "123 Đường Điện Biên Phủ",
            ward: "Phường 25",
            district: "Quận Bình Thạnh",
            city: "TP. Hồ Chí Minh"
          },
          items: [
            {
              productId: items[4]._id.toString(),
              productName: items[4].name,
              quantity: 1,
              finalUnitPrice: 44000,
              sizeChosen: "L",
              tempChosen: "Iced",
              iceLevel: "50%",
              sugarLevel: "50%",
              chosenToppings: [],
              itemNote: "Ít ngọt"
            }
          ]
        },
        {
          userId: users[0]._id.toString(),
          orderDate: Date.now(),
          status: "Delivering",
          paymentMethod: "COD",
          note: "",
          subtotal: 70000,
          discountAmount: 0,
          shippingFee: 15000,
          taxes: 0,
          totalAmount: 85000,
          deliveryAddress: {
            fullName: "Phạm Thị D",
            phone: "0909998887",
            street: "700 Đường Lý Thường Kiệt",
            ward: "Phường 1",
            district: "Quận Tân Bình",
            city: "TP. Hồ Chí Minh"
          },
          items: [
            {
              productId: items[0]._id.toString(),
              productName: items[0].name,
              quantity: 1,
              finalUnitPrice: 25000,
              sizeChosen: "M",
              tempChosen: "Hot",
              iceLevel: "N/A",
              sugarLevel: "100%",
              chosenToppings: [],
              itemNote: "Rất nóng"
            },
            {
              productId: items[3]._id.toString(),
              productName: items[3].name,
              quantity: 1,
              finalUnitPrice: 45000,
              sizeChosen: "Slice",
              tempChosen: "N/A",
              iceLevel: "N/A",
              sugarLevel: "N/A",
              chosenToppings: [],
              itemNote: ""
            }
          ]
        }
      ];

      for (const orderData of sampleOrders) {
        const order = new Order(orderData);
        await order.save();
        console.log(`Created order with status: ${orderData.status}`);
      }
    }

    console.log('\n✅ Database seeding completed!');
    console.log('\n📝 Login credentials:');
    console.log('   Admin: username="admin", password="admin123"');
    console.log('   Admin: username="nguyenvana", password="admin"');
    console.log('   User:  username="user1", password="user123"');
    
    process.exit(0);
  } catch (error) {
    console.error('Error seeding database:', error);
    process.exit(1);
  }
}

seedDatabase();


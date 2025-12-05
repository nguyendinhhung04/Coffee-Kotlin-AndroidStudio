const mongoose = require('mongoose');

const DeliveryAddressSchema = new mongoose.Schema({
  fullName: { type: String, required: true },
  phone: { type: String, required: true },
  street: { type: String, required: true },
  ward: { type: String, required: true },
  district: { type: String, required: true },
  city: { type: String, required: true }
});

const ToppingSchema = new mongoose.Schema({
  name: { type: String, required: true },
  price: { type: Number, required: true }
});

const OrderItemSchema = new mongoose.Schema({
  productId: { type: String },
  productName: { type: String, required: true },
  quantity: { type: Number, required: true, min: 1 },
  finalUnitPrice: { type: Number, required: true },
  sizeChosen: { type: String, default: '' },
  tempChosen: { type: String, default: '' },
  iceLevel: { type: String, default: 'N/A' },
  sugarLevel: { type: String, default: 'N/A' },
  chosenToppings: [ToppingSchema],
  itemNote: { type: String, default: '' }
});

const OrderSchema = new mongoose.Schema({
  userId: { type: String, required: true },
  orderDate: { type: Number, required: true, default: Date.now },
  status: { 
    type: String, 
    required: true,
    enum: ['Pending', 'Confirmed', 'Delivering', 'Delivered', 'Cancelled'],
    default: 'Pending'
  },
  paymentMethod: { 
    type: String, 
    required: true,
    enum: ['COD', 'Transfer', 'Card'],
    default: 'COD'
  },
  note: { type: String, default: '' },
  subtotal: { type: Number, required: true },
  discountAmount: { type: Number, default: 0 },
  shippingFee: { type: Number, default: 0 },
  taxes: { type: Number, default: 0 },
  totalAmount: { type: Number, required: true },
  deliveryAddress: { type: DeliveryAddressSchema, required: true },
  items: [OrderItemSchema]
}, {
  timestamps: true
});

module.exports = mongoose.model('Order', OrderSchema);


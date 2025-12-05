const mongoose = require('mongoose');

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

const ComboSchema = new mongoose.Schema({
  name: { type: String, required: true },
  description: { type: String, default: '' },
  image_url: { type: String, required: true },
  basePrice: { type: Number, required: true },
  items: [OrderItemSchema],
  isActive: { type: Boolean, default: true }
}, {
  timestamps: true
});

module.exports = mongoose.model('Combo', ComboSchema);


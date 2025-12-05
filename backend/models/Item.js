const mongoose = require('mongoose');

const SizeSchema = new mongoose.Schema({
  name: { type: String, required: true },
  modifier: { type: Number, required: true },
  label: { type: String, required: true }
});

const TempOptionSchema = new mongoose.Schema({
  name: { type: String, required: true },
  modifier: { type: Number, required: true },
  label: { type: String, required: true }
});

const ToppingSchema = new mongoose.Schema({
  name: { type: String, required: true },
  price: { type: Number, required: true }
});

const ItemSchema = new mongoose.Schema({
  name: { type: String, required: true },
  category: { 
    type: String, 
    required: true,
    enum: ['coffee', 'chocolate', 'other']
  },
  image_url: { type: String, required: true },
  basePrice: { type: Number, required: true },
  description: { type: String, default: '' },
  sizes: [SizeSchema],
  tempOptions: [TempOptionSchema],
  iceLevels: [String],
  sugarLevels: [String],
  toppings: [ToppingSchema],
  isActive: { type: Boolean, default: true }
}, {
  timestamps: true
});

module.exports = mongoose.model('Item', ItemSchema);


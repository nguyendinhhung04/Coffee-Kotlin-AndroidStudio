const express = require('express');
const router = express.Router();
const Item = require('../models/Item');

// GET /items - Get all items, with optional filtering
router.get('/', async (req, res) => {
  try {
    const { category, search } = req.query;
    let query = { isActive: true };

    // Filter by category
    if (category) {
      query.category = category;
    }

    // Search by name or description
    if (search) {
      query.$or = [
        { name: { $regex: search, $options: 'i' } },
        { description: { $regex: search, $options: 'i' } }
      ];
    }

    const items = await Item.find(query).sort({ createdAt: -1 });
    res.json(items);
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error fetching items', 
      error: error.message 
    });
  }
});

// GET /items/:id - Get single item
router.get('/:id', async (req, res) => {
  try {
    const item = await Item.findById(req.params.id);
    if (!item) {
      return res.status(404).json({ 
        success: false, 
        message: 'Item not found' 
      });
    }
    res.json(item);
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error fetching item', 
      error: error.message 
    });
  }
});

// POST /items - Create new item
router.post('/', async (req, res) => {
  try {
    const item = new Item(req.body);
    const savedItem = await item.save();
    res.status(201).json(savedItem);
  } catch (error) {
    res.status(400).json({ 
      success: false, 
      message: 'Error creating item', 
      error: error.message 
    });
  }
});

// PUT /items/:id - Update item
router.put('/:id', async (req, res) => {
  try {
    const item = await Item.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true, runValidators: true }
    );
    if (!item) {
      return res.status(404).json({ 
        success: false, 
        message: 'Item not found' 
      });
    }
    res.json(item);
  } catch (error) {
    res.status(400).json({ 
      success: false, 
      message: 'Error updating item', 
      error: error.message 
    });
  }
});

// DELETE /items/:id - Delete item (soft delete)
router.delete('/:id', async (req, res) => {
  try {
    const item = await Item.findByIdAndUpdate(
      req.params.id,
      { isActive: false },
      { new: true }
    );
    if (!item) {
      return res.status(404).json({ 
        success: false, 
        message: 'Item not found' 
      });
    }
    res.json({ success: true, message: 'Item deleted successfully' });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error deleting item', 
      error: error.message 
    });
  }
});

module.exports = router;


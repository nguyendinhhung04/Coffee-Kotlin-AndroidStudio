const express = require('express');
const router = express.Router();
const Combo = require('../models/Combo');

// GET /combos - Get all combos
router.get('/', async (req, res) => {
  try {
    const combos = await Combo.find({ isActive: true }).sort({ createdAt: -1 });
    res.json(combos);
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error fetching combos', 
      error: error.message 
    });
  }
});

// GET /combos/:id - Get single combo
router.get('/:id', async (req, res) => {
  try {
    const combo = await Combo.findById(req.params.id);
    if (!combo) {
      return res.status(404).json({ 
        success: false, 
        message: 'Combo not found' 
      });
    }
    res.json(combo);
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error fetching combo', 
      error: error.message 
    });
  }
});

// POST /combos - Create new combo
router.post('/', async (req, res) => {
  try {
    const combo = new Combo(req.body);
    const savedCombo = await combo.save();
    res.status(201).json(savedCombo);
  } catch (error) {
    res.status(400).json({ 
      success: false, 
      message: 'Error creating combo', 
      error: error.message 
    });
  }
});

// PUT /combos/:id - Update combo
router.put('/:id', async (req, res) => {
  try {
    const combo = await Combo.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true, runValidators: true }
    );
    if (!combo) {
      return res.status(404).json({ 
        success: false, 
        message: 'Combo not found' 
      });
    }
    res.json(combo);
  } catch (error) {
    res.status(400).json({ 
      success: false, 
      message: 'Error updating combo', 
      error: error.message 
    });
  }
});

// DELETE /combos/:id - Delete combo (soft delete)
router.delete('/:id', async (req, res) => {
  try {
    const combo = await Combo.findByIdAndUpdate(
      req.params.id,
      { isActive: false },
      { new: true }
    );
    if (!combo) {
      return res.status(404).json({ 
        success: false, 
        message: 'Combo not found' 
      });
    }
    res.json({ success: true, message: 'Combo deleted successfully' });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error deleting combo', 
      error: error.message 
    });
  }
});

module.exports = router;


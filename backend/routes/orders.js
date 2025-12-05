const express = require('express');
const router = express.Router();
const Order = require('../models/Order');

// GET /orders - Get all orders, with optional filtering
router.get('/', async (req, res) => {
  try {
    const { userId, status } = req.query;
    let query = {};

    // Filter by userId
    if (userId) {
      query.userId = userId;
    }

    // Filter by status
    if (status) {
      query.status = status;
    }

    const orders = await Order.find(query).sort({ orderDate: -1 });
    res.json(orders);
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error fetching orders', 
      error: error.message 
    });
  }
});

// GET /orders/:id - Get single order
router.get('/:id', async (req, res) => {
  try {
    const order = await Order.findById(req.params.id);
    if (!order) {
      return res.status(404).json({ 
        success: false, 
        message: 'Order not found' 
      });
    }
    res.json(order);
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error fetching order', 
      error: error.message 
    });
  }
});

// POST /orders/create - Create new order
router.post('/create', async (req, res) => {
  try {
    const order = new Order(req.body);
    const savedOrder = await order.save();
    res.status(201).json(savedOrder);
  } catch (error) {
    res.status(400).json({ 
      success: false, 
      message: 'Error creating order', 
      error: error.message 
    });
  }
});

// PUT /orders/:id - Update order (mainly for status updates)
router.put('/:id', async (req, res) => {
  try {
    const order = await Order.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true, runValidators: true }
    );
    if (!order) {
      return res.status(404).json({ 
        success: false, 
        message: 'Order not found' 
      });
    }
    res.json(order);
  } catch (error) {
    res.status(400).json({ 
      success: false, 
      message: 'Error updating order', 
      error: error.message 
    });
  }
});

// DELETE /orders/:id - Cancel order
router.delete('/:id', async (req, res) => {
  try {
    const order = await Order.findByIdAndUpdate(
      req.params.id,
      { status: 'Cancelled' },
      { new: true }
    );
    if (!order) {
      return res.status(404).json({ 
        success: false, 
        message: 'Order not found' 
      });
    }
    res.json({ success: true, message: 'Order cancelled successfully', order });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error cancelling order', 
      error: error.message 
    });
  }
});

module.exports = router;


const express = require('express');
const router = express.Router();
const User = require('../models/User');
const jwt = require('jsonwebtoken');

// POST /auth/register - Register new user
router.post('/register', async (req, res) => {
  try {
    const { username, password, fullName, email, phone, role } = req.body;

    // Check if user already exists
    const existingUser = await User.findOne({ username });
    if (existingUser) {
      return res.status(400).json({ 
        success: false, 
        message: 'Username already exists' 
      });
    }

    // Create new user
    const user = new User({
      username,
      password,
      fullName,
      email,
      phone,
      role: role || 'user'
    });

    const savedUser = await user.save();

    // Generate JWT token
    const token = jwt.sign(
      { userId: savedUser._id, username: savedUser.username, role: savedUser.role },
      process.env.JWT_SECRET || 'your-secret-key',
      { expiresIn: '7d' }
    );

    res.status(201).json({
      success: true,
      message: 'User registered successfully',
      user: {
        _id: savedUser._id,
        username: savedUser.username,
        fullName: savedUser.fullName,
        email: savedUser.email,
        phone: savedUser.phone,
        role: savedUser.role
      },
      token
    });
  } catch (error) {
    res.status(400).json({ 
      success: false, 
      message: 'Error registering user', 
      error: error.message 
    });
  }
});

// POST /auth/login - Login user
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;

    // Find user
    const user = await User.findOne({ username });
    if (!user) {
      return res.status(401).json({ 
        success: false, 
        message: 'Invalid credentials' 
      });
    }

    // Check password
    const isPasswordValid = await user.comparePassword(password);
    if (!isPasswordValid) {
      return res.status(401).json({ 
        success: false, 
        message: 'Invalid credentials' 
      });
    }

    // Generate JWT token
    const token = jwt.sign(
      { userId: user._id, username: user.username, role: user.role },
      process.env.JWT_SECRET || 'your-secret-key',
      { expiresIn: '7d' }
    );

    res.json({
      success: true,
      message: 'Login successful',
      user: {
        _id: user._id,
        username: user.username,
        fullName: user.fullName,
        email: user.email,
        phone: user.phone,
        role: user.role
      },
      token
    });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error during login', 
      error: error.message 
    });
  }
});

// GET /auth/me - Get current user (requires authentication middleware)
router.get('/me', async (req, res) => {
  try {
    // This would require authentication middleware
    // For now, return a placeholder
    res.json({ message: 'Authentication endpoint - implement middleware' });
  } catch (error) {
    res.status(500).json({ 
      success: false, 
      message: 'Error fetching user', 
      error: error.message 
    });
  }
});

module.exports = router;


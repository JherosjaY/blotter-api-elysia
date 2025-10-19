import { Elysia, t } from 'elysia';
import { db } from '../db';
import { users } from '../db/schema';
import { eq } from 'drizzle-orm';

export const authRoutes = new Elysia({ prefix: '/api/auth' })
  // Login
  .post('/login', async ({ body }) => {
    try {
      const { username, password } = body;
      
      // Find user
      const [user] = await db.select().from(users).where(eq(users.username, username)).limit(1);
      
      if (!user) {
        return {
          success: false,
          message: 'Invalid username or password'
        };
      }
      
      // In production, use bcrypt to compare hashed passwords
      // For now, direct comparison (CHANGE THIS IN PRODUCTION!)
      if (user.password !== password) {
        return {
          success: false,
          message: 'Invalid username or password'
        };
      }
      
      // Return user data (excluding password)
      const { password: _, ...userData } = user;
      
      return {
        success: true,
        message: 'Login successful',
        data: userData,
        token: `bearer_${user.id}` // Simple token for now
      };
    } catch (error) {
      return {
        success: false,
        message: 'Login failed',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      username: t.String(),
      password: t.String()
    }),
    detail: {
      tags: ['Auth'],
      summary: 'User login',
      description: 'Authenticate user and return user data with token'
    }
  })
  
  // Register
  .post('/register', async ({ body }) => {
    try {
      const { username, password, firstName, lastName, role = 'User' } = body;
      
      // Check if username exists
      const [existing] = await db.select().from(users).where(eq(users.username, username)).limit(1);
      
      if (existing) {
        return {
          success: false,
          message: 'Username already exists'
        };
      }
      
      // Create new user
      const [newUser] = await db.insert(users).values({
        username,
        password, // In production, hash this with bcrypt!
        firstName,
        lastName,
        role,
        status: 'active'
      }).returning();
      
      const { password: _, ...userData } = newUser;
      
      return {
        success: true,
        message: 'User registered successfully',
        data: userData
      };
    } catch (error) {
      return {
        success: false,
        message: 'Registration failed',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      username: t.String(),
      password: t.String(),
      firstName: t.String(),
      lastName: t.String(),
      role: t.Optional(t.String())
    }),
    detail: {
      tags: ['Auth'],
      summary: 'Register new user',
      description: 'Create a new user account'
    }
  })
  
  // Get current user
  .get('/me/:userId', async ({ params }) => {
    try {
      const [user] = await db.select().from(users).where(eq(users.id, params.userId)).limit(1);
      
      if (!user) {
        return {
          success: false,
          message: 'User not found'
        };
      }
      
      const { password: _, ...userData } = user;
      
      return {
        success: true,
        data: userData
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to get user',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Auth'],
      summary: 'Get current user',
      description: 'Get authenticated user information'
    }
  });

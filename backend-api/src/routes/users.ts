import { Elysia, t } from 'elysia';
import { db } from '../db';
import { users } from '../db/schema';
import { eq } from 'drizzle-orm';

export const usersRoutes = new Elysia({ prefix: '/api/users' })
  // Get all users
  .get('/', async () => {
    try {
      const allUsers = await db.select().from(users);
      
      // Remove passwords
      const usersData = allUsers.map(({ password, ...user }) => user);
      
      return {
        success: true,
        data: usersData,
        count: usersData.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch users',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Users'],
      summary: 'Get all users',
      description: 'Retrieve all users (Admin only)'
    }
  })
  
  // Get user by ID
  .get('/:id', async ({ params }) => {
    try {
      const [user] = await db.select().from(users).where(eq(users.id, params.id)).limit(1);
      
      if (!user) {
        return {
          success: false,
          message: 'User not found'
        };
      }
      
      const { password, ...userData } = user;
      
      return {
        success: true,
        data: userData
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch user',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Users'],
      summary: 'Get user by ID'
    }
  })
  
  // Create user
  .post('/', async ({ body }) => {
    try {
      const [newUser] = await db.insert(users).values(body).returning();
      
      const { password, ...userData } = newUser;
      
      return {
        success: true,
        message: 'User created successfully',
        data: userData
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create user',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      username: t.String(),
      password: t.String(),
      firstName: t.String(),
      lastName: t.String(),
      role: t.Optional(t.String()),
      email: t.Optional(t.String()),
      phone: t.Optional(t.String())
    }),
    detail: {
      tags: ['Users'],
      summary: 'Create new user'
    }
  })
  
  // Update user
  .put('/:id', async ({ params, body }) => {
    try {
      const [updated] = await db.update(users)
        .set({ ...body, updatedAt: new Date() })
        .where(eq(users.id, params.id))
        .returning();
      
      if (!updated) {
        return {
          success: false,
          message: 'User not found'
        };
      }
      
      const { password, ...userData } = updated;
      
      return {
        success: true,
        message: 'User updated successfully',
        data: userData
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to update user',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Users'],
      summary: 'Update user'
    }
  })
  
  // Delete user
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(users).where(eq(users.id, params.id));
      
      return {
        success: true,
        message: 'User deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete user',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Users'],
      summary: 'Delete user'
    }
  });

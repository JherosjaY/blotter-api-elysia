import { Elysia, t } from 'elysia';
import { db } from '../db';
import { officers, users } from '../db/schema';
import { eq } from 'drizzle-orm';

export const officersRoutes = new Elysia({ prefix: '/api/officers' })
  .get('/', async () => {
    try {
      const allOfficers = await db.select({
        id: officers.id,
        userId: officers.userId,
        badgeNumber: officers.badgeNumber,
        rank: officers.rank,
        specialization: officers.specialization,
        assignedCasesCount: officers.assignedCasesCount,
        createdAt: officers.createdAt,
        firstName: users.firstName,
        lastName: users.lastName,
        email: users.email,
        phone: users.phone
      })
      .from(officers)
      .leftJoin(users, eq(officers.userId, users.id));
      
      return {
        success: true,
        data: allOfficers,
        count: allOfficers.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch officers',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Officers'],
      summary: 'Get all officers'
    }
  })
  
  .get('/:id', async ({ params }) => {
    try {
      const [officer] = await db.select().from(officers).where(eq(officers.id, params.id)).limit(1);
      
      if (!officer) {
        return {
          success: false,
          message: 'Officer not found'
        };
      }
      
      return {
        success: true,
        data: officer
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch officer',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Officers'],
      summary: 'Get officer by ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      const [newOfficer] = await db.insert(officers).values(body).returning();
      
      return {
        success: true,
        message: 'Officer created successfully',
        data: newOfficer
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create officer',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      userId: t.String(),
      badgeNumber: t.Optional(t.String()),
      rank: t.Optional(t.String()),
      specialization: t.Optional(t.String())
    }),
    detail: {
      tags: ['Officers'],
      summary: 'Create new officer'
    }
  })
  
  .put('/:id', async ({ params, body }) => {
    try {
      const [updated] = await db.update(officers)
        .set(body)
        .where(eq(officers.id, params.id))
        .returning();
      
      if (!updated) {
        return {
          success: false,
          message: 'Officer not found'
        };
      }
      
      return {
        success: true,
        message: 'Officer updated successfully',
        data: updated
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to update officer',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Officers'],
      summary: 'Update officer'
    }
  })
  
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(officers).where(eq(officers.id, params.id));
      
      return {
        success: true,
        message: 'Officer deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete officer',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Officers'],
      summary: 'Delete officer'
    }
  });

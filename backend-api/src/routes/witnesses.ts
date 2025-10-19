import { Elysia, t } from 'elysia';
import { db } from '../db';
import { witnesses } from '../db/schema';
import { eq } from 'drizzle-orm';

export const witnessesRoutes = new Elysia({ prefix: '/api/witnesses' })
  .get('/report/:reportId', async ({ params }) => {
    try {
      const reportWitnesses = await db.select().from(witnesses).where(eq(witnesses.reportId, params.reportId));
      
      return {
        success: true,
        data: reportWitnesses,
        count: reportWitnesses.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch witnesses',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Witnesses'],
      summary: 'Get witnesses by report ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      const [newWitness] = await db.insert(witnesses).values(body).returning();
      
      return {
        success: true,
        message: 'Witness created successfully',
        data: newWitness
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create witness',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      reportId: t.String(),
      name: t.String(),
      age: t.Optional(t.Number()),
      gender: t.Optional(t.String()),
      contact: t.Optional(t.String()),
      address: t.Optional(t.String()),
      statement: t.Optional(t.String())
    }),
    detail: {
      tags: ['Witnesses'],
      summary: 'Create new witness'
    }
  })
  
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(witnesses).where(eq(witnesses.id, params.id));
      
      return {
        success: true,
        message: 'Witness deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete witness',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Witnesses'],
      summary: 'Delete witness'
    }
  });

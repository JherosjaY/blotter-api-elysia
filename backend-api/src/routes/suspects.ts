import { Elysia, t } from 'elysia';
import { db } from '../db';
import { suspects } from '../db/schema';
import { eq } from 'drizzle-orm';

export const suspectsRoutes = new Elysia({ prefix: '/api/suspects' })
  .get('/report/:reportId', async ({ params }) => {
    try {
      const reportSuspects = await db.select().from(suspects).where(eq(suspects.reportId, params.reportId));
      
      return {
        success: true,
        data: reportSuspects,
        count: reportSuspects.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch suspects',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Suspects'],
      summary: 'Get suspects by report ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      const [newSuspect] = await db.insert(suspects).values(body).returning();
      
      return {
        success: true,
        message: 'Suspect created successfully',
        data: newSuspect
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create suspect',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      reportId: t.String(),
      name: t.String(),
      alias: t.Optional(t.String()),
      age: t.Optional(t.Number()),
      gender: t.Optional(t.String()),
      contact: t.Optional(t.String()),
      address: t.Optional(t.String())
    }),
    detail: {
      tags: ['Suspects'],
      summary: 'Create new suspect'
    }
  })
  
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(suspects).where(eq(suspects.id, params.id));
      
      return {
        success: true,
        message: 'Suspect deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete suspect',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Suspects'],
      summary: 'Delete suspect'
    }
  });

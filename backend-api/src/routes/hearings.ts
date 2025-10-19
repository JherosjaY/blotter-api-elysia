import { Elysia, t } from 'elysia';
import { db } from '../db';
import { hearings } from '../db/schema';
import { eq } from 'drizzle-orm';

export const hearingsRoutes = new Elysia({ prefix: '/api/hearings' })
  .get('/', async () => {
    try {
      const allHearings = await db.select().from(hearings);
      
      return {
        success: true,
        data: allHearings,
        count: allHearings.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch hearings',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Hearings'],
      summary: 'Get all hearings'
    }
  })
  
  .get('/report/:reportId', async ({ params }) => {
    try {
      const reportHearings = await db.select().from(hearings).where(eq(hearings.reportId, params.reportId));
      
      return {
        success: true,
        data: reportHearings,
        count: reportHearings.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch hearings',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Hearings'],
      summary: 'Get hearings by report ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      const [newHearing] = await db.insert(hearings).values(body).returning();
      
      return {
        success: true,
        message: 'Hearing created successfully',
        data: newHearing
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create hearing',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      reportId: t.String(),
      hearingDate: t.String(),
      hearingTime: t.String(),
      location: t.String(),
      purpose: t.Optional(t.String())
    }),
    detail: {
      tags: ['Hearings'],
      summary: 'Create new hearing'
    }
  })
  
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(hearings).where(eq(hearings.id, params.id));
      
      return {
        success: true,
        message: 'Hearing deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete hearing',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Hearings'],
      summary: 'Delete hearing'
    }
  });

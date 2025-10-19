import { Elysia, t } from 'elysia';
import { db } from '../db';
import { resolutions } from '../db/schema';
import { eq } from 'drizzle-orm';

export const resolutionsRoutes = new Elysia({ prefix: '/api/resolutions' })
  .get('/report/:reportId', async ({ params }) => {
    try {
      const reportResolutions = await db.select().from(resolutions).where(eq(resolutions.reportId, params.reportId));
      
      return {
        success: true,
        data: reportResolutions,
        count: reportResolutions.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch resolutions',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Resolutions'],
      summary: 'Get resolutions by report ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      const [newResolution] = await db.insert(resolutions).values(body).returning();
      
      return {
        success: true,
        message: 'Resolution created successfully',
        data: newResolution
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create resolution',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      reportId: t.String(),
      resolutionType: t.String(),
      details: t.String(),
      resolvedBy: t.Optional(t.String())
    }),
    detail: {
      tags: ['Resolutions'],
      summary: 'Create new resolution'
    }
  });

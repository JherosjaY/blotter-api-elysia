import { Elysia, t } from 'elysia';
import { db } from '../db';
import { respondents } from '../db/schema';
import { eq } from 'drizzle-orm';

export const respondentsRoutes = new Elysia({ prefix: '/api/respondents' })
  .get('/report/:reportId', async ({ params }) => {
    try {
      const reportRespondents = await db.select().from(respondents).where(eq(respondents.reportId, params.reportId));
      
      return {
        success: true,
        data: reportRespondents,
        count: reportRespondents.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch respondents',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Respondents'],
      summary: 'Get respondents by report ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      const [newRespondent] = await db.insert(respondents).values(body).returning();
      
      return {
        success: true,
        message: 'Respondent created successfully',
        data: newRespondent
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create respondent',
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
      address: t.Optional(t.String())
    }),
    detail: {
      tags: ['Respondents'],
      summary: 'Create new respondent'
    }
  })
  
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(respondents).where(eq(respondents.id, params.id));
      
      return {
        success: true,
        message: 'Respondent deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete respondent',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Respondents'],
      summary: 'Delete respondent'
    }
  });

import { Elysia, t } from 'elysia';
import { db } from '../db';
import { evidence } from '../db/schema';
import { eq } from 'drizzle-orm';

export const evidenceRoutes = new Elysia({ prefix: '/api/evidence' })
  .get('/report/:reportId', async ({ params }) => {
    try {
      const reportEvidence = await db.select().from(evidence).where(eq(evidence.reportId, params.reportId));
      
      return {
        success: true,
        data: reportEvidence,
        count: reportEvidence.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch evidence',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Evidence'],
      summary: 'Get evidence by report ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      const [newEvidence] = await db.insert(evidence).values(body).returning();
      
      return {
        success: true,
        message: 'Evidence created successfully',
        data: newEvidence
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create evidence',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      reportId: t.String(),
      type: t.String(),
      description: t.String(),
      filePath: t.Optional(t.String()),
      collectedBy: t.Optional(t.String())
    }),
    detail: {
      tags: ['Evidence'],
      summary: 'Create new evidence'
    }
  })
  
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(evidence).where(eq(evidence.id, params.id));
      
      return {
        success: true,
        message: 'Evidence deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete evidence',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Evidence'],
      summary: 'Delete evidence'
    }
  });

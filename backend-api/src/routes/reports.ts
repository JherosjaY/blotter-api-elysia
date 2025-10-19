import { Elysia, t } from 'elysia';
import { db } from '../db';
import { blotterReports } from '../db/schema';
import { eq } from 'drizzle-orm';

export const reportsRoutes = new Elysia({ prefix: '/api/reports' })
  .get('/', async () => {
    try {
      const allReports = await db.select().from(blotterReports);
      
      return {
        success: true,
        data: allReports,
        count: allReports.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch reports',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Reports'],
      summary: 'Get all blotter reports'
    }
  })
  
  .get('/:id', async ({ params }) => {
    try {
      const [report] = await db.select().from(blotterReports).where(eq(blotterReports.id, params.id)).limit(1);
      
      if (!report) {
        return {
          success: false,
          message: 'Report not found'
        };
      }
      
      return {
        success: true,
        data: report
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch report',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Reports'],
      summary: 'Get report by ID'
    }
  })
  
  .get('/user/:userId', async ({ params }) => {
    try {
      const reports = await db.select().from(blotterReports).where(eq(blotterReports.userId, params.userId));
      
      return {
        success: true,
        data: reports,
        count: reports.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch user reports',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Reports'],
      summary: 'Get reports by user ID'
    }
  })
  
  .post('/', async ({ body }) => {
    try {
      // Generate blotter number
      const blotterNumber = `BLT-${Date.now()}`;
      
      const [newReport] = await db.insert(blotterReports).values({
        userId: body.userId,
        blotterNumber,
        incidentType: body.incidentType,
        incidentDate: new Date(body.incidentDate),
        incidentTime: body.incidentTime,
        location: body.location,
        description: body.description,
        complainantName: body.complainantName,
        complainantContact: body.complainantContact,
        complainantAddress: body.complainantAddress,
        priority: body.priority || 'Normal'
      }).returning();
      
      return {
        success: true,
        message: 'Report created successfully',
        data: newReport
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to create report',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      userId: t.String(),
      incidentType: t.String(),
      incidentDate: t.String(),
      incidentTime: t.String(),
      location: t.String(),
      description: t.String(),
      complainantName: t.String(),
      complainantContact: t.Optional(t.String()),
      complainantAddress: t.Optional(t.String()),
      priority: t.Optional(t.String())
    }),
    detail: {
      tags: ['Reports'],
      summary: 'Create new report'
    }
  })
  
  .put('/:id', async ({ params, body }) => {
    try {
      const updateData: any = { updatedAt: new Date() };
      if (body.incidentType) updateData.incidentType = body.incidentType;
      if (body.location) updateData.location = body.location;
      if (body.description) updateData.description = body.description;
      if (body.status) updateData.status = body.status;
      
      const [updated] = await db.update(blotterReports)
        .set(updateData)
        .where(eq(blotterReports.id, params.id))
        .returning();
      
      if (!updated) {
        return {
          success: false,
          message: 'Report not found'
        };
      }
      
      return {
        success: true,
        message: 'Report updated successfully',
        data: updated
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to update report',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Reports'],
      summary: 'Update report'
    }
  })
  
  .patch('/:id/assign-officer', async ({ params, body }) => {
    try {
      const [updated] = await db.update(blotterReports)
        .set({ 
          assignedOfficerId: body.officerId,
          updatedAt: new Date()
        })
        .where(eq(blotterReports.id, params.id))
        .returning();
      
      if (!updated) {
        return {
          success: false,
          message: 'Report not found'
        };
      }
      
      return {
        success: true,
        message: 'Officer assigned successfully',
        data: updated
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to assign officer',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      officerId: t.String()
    }),
    detail: {
      tags: ['Reports'],
      summary: 'Assign officer to report'
    }
  })
  
  .patch('/:id/status', async ({ params, body }) => {
    try {
      const [updated] = await db.update(blotterReports)
        .set({ 
          status: body.status,
          updatedAt: new Date()
        })
        .where(eq(blotterReports.id, params.id))
        .returning();
      
      if (!updated) {
        return {
          success: false,
          message: 'Report not found'
        };
      }
      
      return {
        success: true,
        message: 'Status updated successfully',
        data: updated
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to update status',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      status: t.String()
    }),
    detail: {
      tags: ['Reports'],
      summary: 'Update report status'
    }
  })
  
  .delete('/:id', async ({ params }) => {
    try {
      await db.delete(blotterReports).where(eq(blotterReports.id, params.id));
      
      return {
        success: true,
        message: 'Report deleted successfully'
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to delete report',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Reports'],
      summary: 'Delete report'
    }
  });

import { Elysia } from 'elysia';
import { db } from '../db';
import { blotterReports, officers, users } from '../db/schema';
import { eq, count, sql } from 'drizzle-orm';

export const analyticsRoutes = new Elysia({ prefix: '/api/analytics' })
  .get('/dashboard', async () => {
    try {
      // Get total reports
      const [totalReportsResult] = await db.select({ count: count() }).from(blotterReports);
      const totalReports = totalReportsResult.count;
      
      // Get pending reports
      const [pendingResult] = await db.select({ count: count() })
        .from(blotterReports)
        .where(eq(blotterReports.status, 'Pending'));
      const pendingReports = pendingResult.count;
      
      // Get resolved reports
      const [resolvedResult] = await db.select({ count: count() })
        .from(blotterReports)
        .where(eq(blotterReports.status, 'Resolved'));
      const resolvedReports = resolvedResult.count;
      
      // Get total officers
      const [officersResult] = await db.select({ count: count() }).from(officers);
      const totalOfficers = officersResult.count;
      
      // Get total users
      const [usersResult] = await db.select({ count: count() }).from(users);
      const totalUsers = usersResult.count;
      
      return {
        success: true,
        data: {
          totalReports,
          pendingReports,
          resolvedReports,
          totalOfficers,
          totalUsers,
          activeReports: totalReports - resolvedReports
        }
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch analytics',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Analytics'],
      summary: 'Get dashboard analytics'
    }
  })
  
  .get('/officer/:officerId', async ({ params }) => {
    try {
      // Get officer's assigned cases
      const [assignedResult] = await db.select({ count: count() })
        .from(blotterReports)
        .where(eq(blotterReports.assignedOfficerId, params.officerId));
      const assignedCases = assignedResult.count;
      
      // Get resolved cases
      const [resolvedResult] = await db.select({ count: count() })
        .from(blotterReports)
        .where(sql`${blotterReports.assignedOfficerId} = ${params.officerId} AND ${blotterReports.status} = 'Resolved'`);
      const resolvedCases = resolvedResult.count;
      
      return {
        success: true,
        data: {
          assignedCases,
          resolvedCases,
          activeCases: assignedCases - resolvedCases,
          resolutionRate: assignedCases > 0 ? Math.round((resolvedCases / assignedCases) * 100) : 0
        }
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch officer analytics',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['Analytics'],
      summary: 'Get officer analytics'
    }
  });

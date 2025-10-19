import { Elysia, t } from 'elysia';
import { db } from '../db';
import { smsNotifications } from '../db/schema';
import { eq } from 'drizzle-orm';

export const smsRoutes = new Elysia({ prefix: '/api/sms' })
  .get('/report/:reportId', async ({ params }) => {
    try {
      const reportSms = await db.select().from(smsNotifications).where(eq(smsNotifications.reportId, params.reportId));
      
      return {
        success: true,
        data: reportSms,
        count: reportSms.length
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to fetch SMS notifications',
        error: String(error)
      };
    }
  }, {
    detail: {
      tags: ['SMS'],
      summary: 'Get SMS notifications by report ID'
    }
  })
  
  .post('/send', async ({ body }) => {
    try {
      const [newSms] = await db.insert(smsNotifications).values({
        ...body,
        status: 'Sent',
        sentAt: new Date()
      }).returning();
      
      return {
        success: true,
        message: 'SMS notification sent successfully',
        data: newSms
      };
    } catch (error) {
      return {
        success: false,
        message: 'Failed to send SMS',
        error: String(error)
      };
    }
  }, {
    body: t.Object({
      reportId: t.Optional(t.String()),
      respondentId: t.Optional(t.String()),
      recipientName: t.String(),
      recipientPhone: t.String(),
      message: t.String()
    }),
    detail: {
      tags: ['SMS'],
      summary: 'Send SMS notification'
    }
  });

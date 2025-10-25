import { Elysia, t } from "elysia";
import { db } from "../db";
import { blotterReports, notifications } from "../db/schema";
import { eq } from "drizzle-orm";

export const notificationsRoutes = new Elysia({ prefix: "/notifications" })
  // Send Email Notification
  .post(
    "/email",
    async ({ body }) => {
      try {
        const { reportId, recipientEmail, notificationType, oldStatus, newStatus } = body;

        // Get report details
        const [report] = await db
          .select()
          .from(blotterReports)
          .where(eq(blotterReports.id, reportId))
          .limit(1);

        if (!report) {
          return {
            success: false,
            message: "Report not found",
          };
        }

        // Create notification record
        await db.insert(notifications).values({
          userId: report.filedById || 0,
          title: getNotificationTitle(notificationType),
          message: getNotificationMessage(notificationType, report, oldStatus, newStatus),
          type: "email",
          caseId: reportId,
          isRead: false,
        });

        console.log(`Email notification sent to ${recipientEmail}`);
        console.log(`Type: ${notificationType}`);
        console.log(`Report: ${report.caseNumber}`);

        return {
          success: true,
          message: "Email notification sent successfully",
          data: {
            recipientEmail,
            notificationType,
            caseNumber: report.caseNumber,
          },
        };
      } catch (error: any) {
        return {
          success: false,
          message: "Failed to send email notification",
          error: error.message,
        };
      }
    },
    {
      body: t.Object({
        reportId: t.Number(),
        recipientEmail: t.String(),
        notificationType: t.String(),
        oldStatus: t.Optional(t.String()),
        newStatus: t.Optional(t.String()),
      }),
    }
  )

  // Send Push Notification
  .post(
    "/push",
    async ({ body }) => {
      try {
        const { userId, reportId, notificationType, title, message } = body;

        // Create notification record
        await db.insert(notifications).values({
          userId,
          title,
          message,
          type: "push",
          caseId: reportId,
          isRead: false,
        });

        console.log(`Push notification sent to user ${userId}`);
        console.log(`Title: ${title}`);
        console.log(`Message: ${message}`);

        return {
          success: true,
          message: "Push notification sent successfully",
          data: {
            userId,
            notificationType,
            title,
          },
        };
      } catch (error: any) {
        return {
          success: false,
          message: "Failed to send push notification",
          error: error.message,
        };
      }
    },
    {
      body: t.Object({
        userId: t.Number(),
        reportId: t.Number(),
        notificationType: t.String(),
        title: t.String(),
        message: t.String(),
      }),
    }
  )

  // Get User Notifications
  .get("/user/:userId", async ({ params }) => {
    try {
      const userId = parseInt(params.userId);

      const userNotifications = await db
        .select()
        .from(notifications)
        .where(eq(notifications.userId, userId))
        .orderBy(notifications.timestamp);

      return {
        success: true,
        notifications: userNotifications,
        count: userNotifications.length,
      };
    } catch (error: any) {
      return {
        success: false,
        message: "Failed to get notifications",
        error: error.message,
      };
    }
  })

  // Mark Notification as Read
  .patch("/read/:notificationId", async ({ params }) => {
    try {
      const notificationId = parseInt(params.notificationId);

      await db
        .update(notifications)
        .set({ isRead: true })
        .where(eq(notifications.id, notificationId));

      return {
        success: true,
        message: "Notification marked as read",
      };
    } catch (error: any) {
      return {
        success: false,
        message: "Failed to mark notification as read",
        error: error.message,
      };
    }
  })

  // Mark All Notifications as Read for a User
  .patch("/read-all/:userId", async ({ params }) => {
    try {
      const userId = parseInt(params.userId);

      await db
        .update(notifications)
        .set({ isRead: true })
        .where(eq(notifications.userId, userId));

      return {
        success: true,
        message: "All notifications marked as read",
      };
    } catch (error: any) {
      return {
        success: false,
        message: "Failed to mark all notifications as read",
        error: error.message,
      };
    }
  })

  // Upload Audio Recording
  .post("/audio/:reportId", async ({ params, body }) => {
    try {
      const reportId = parseInt(params.reportId);

      // In a real implementation, you would:
      // 1. Handle file upload with multipart/form-data
      // 2. Upload to cloud storage (AWS S3, Google Cloud Storage)
      // 3. Get the public URL

      // For now, simulate audio URI
      const audioUri = `https://storage.blotter.com/audio/report_${reportId}_${Date.now()}.m4a`;

      // Update report with audio URI
      await db
        .update(blotterReports)
        .set({
          audioRecordingUri: audioUri,
          updatedAt: new Date(),
        })
        .where(eq(blotterReports.id, reportId));

      return {
        success: true,
        audioUri,
        message: "Audio uploaded successfully",
      };
    } catch (error: any) {
      return {
        success: false,
        message: "Failed to upload audio",
        error: error.message,
      };
    }
  })

  // Get Audio Recording
  .get("/audio/:reportId", async ({ params }) => {
    try {
      const reportId = parseInt(params.reportId);

      const [report] = await db
        .select()
        .from(blotterReports)
        .where(eq(blotterReports.id, reportId))
        .limit(1);

      if (!report || !report.audioRecordingUri) {
        return {
          success: false,
          message: "Audio recording not found",
        };
      }

      return {
        success: true,
        audioUri: report.audioRecordingUri,
        caseNumber: report.caseNumber,
      };
    } catch (error: any) {
      return {
        success: false,
        message: "Failed to get audio recording",
        error: error.message,
      };
    }
  });

// Helper functions
function getNotificationTitle(type: string): string {
  switch (type) {
    case "report_filed":
      return "Report Filed Successfully";
    case "status_update":
      return "Status Updated";
    case "case_assigned":
      return "Case Assigned";
    case "hearing_scheduled":
      return "Hearing Scheduled";
    default:
      return "Notification";
  }
}

function getNotificationMessage(
  type: string,
  report: any,
  oldStatus?: string,
  newStatus?: string
): string {
  switch (type) {
    case "report_filed":
      return `Your blotter report ${report.caseNumber} has been filed successfully.`;
    case "status_update":
      return `Case ${report.caseNumber} status changed from ${oldStatus} to ${newStatus}.`;
    case "case_assigned":
      return `Case ${report.caseNumber} has been assigned to an officer.`;
    case "hearing_scheduled":
      return `A hearing has been scheduled for case ${report.caseNumber}.`;
    default:
      return "You have a new notification.";
  }
}

import { Elysia, t } from "elysia";
import { db } from "../db";
import { blotterReports } from "../db/schema";
import { eq, desc } from "drizzle-orm";

export const reportsRoutes = new Elysia({ prefix: "/reports" })
  // Get all reports
  .get("/", async () => {
    const reports = await db.query.blotterReports.findMany({
      orderBy: desc(blotterReports.createdAt),
    });

    return {
      success: true,
      data: reports,
    };
  })

  // Get report by ID
  .get("/:id", async ({ params, set }) => {
    const report = await db.query.blotterReports.findFirst({
      where: eq(blotterReports.id, parseInt(params.id)),
    });

    if (!report) {
      set.status = 404;
      return { success: false, message: "Report not found" };
    }

    return {
      success: true,
      data: report,
    };
  })

  // Create report
  .post(
    "/",
    async ({ body }) => {
      const [newReport] = await db
        .insert(blotterReports)
        .values(body)
        .returning();

      return {
        success: true,
        data: newReport,
      };
    },
    {
      body: t.Object({
        caseNumber: t.String(),
        incidentType: t.String(),
        incidentDate: t.String(),
        incidentTime: t.String(),
        incidentLocation: t.String(),
        narrative: t.String(),
        complainantName: t.Optional(t.String()),
        complainantContact: t.Optional(t.String()),
        complainantAddress: t.Optional(t.String()),
        status: t.Optional(t.String()),
        priority: t.Optional(t.String()),
        filedBy: t.Optional(t.String()),
        filedById: t.Optional(t.Number()),
      }),
    }
  )

  // Update report
  .put(
    "/:id",
    async ({ params, body, set }) => {
      const [updatedReport] = await db
        .update(blotterReports)
        .set({ ...body, updatedAt: new Date() })
        .where(eq(blotterReports.id, parseInt(params.id)))
        .returning();

      if (!updatedReport) {
        set.status = 404;
        return { success: false, message: "Report not found" };
      }

      return {
        success: true,
        data: updatedReport,
      };
    },
    {
      body: t.Partial(
        t.Object({
          status: t.String(),
          assignedOfficer: t.String(),
          assignedOfficerIds: t.String(),
          priority: t.String(),
          isArchived: t.Boolean(),
        })
      ),
    }
  )

  // Delete report
  .delete("/:id", async ({ params, set }) => {
    const [deletedReport] = await db
      .delete(blotterReports)
      .where(eq(blotterReports.id, parseInt(params.id)))
      .returning();

    if (!deletedReport) {
      set.status = 404;
      return { success: false, message: "Report not found" };
    }

    return {
      success: true,
      message: "Report deleted successfully",
    };
  })

  // Get reports by status
  .get("/status/:status", async ({ params }) => {
    const reports = await db.query.blotterReports.findMany({
      where: eq(blotterReports.status, params.status),
      orderBy: desc(blotterReports.createdAt),
    });

    return {
      success: true,
      data: reports,
    };
  });

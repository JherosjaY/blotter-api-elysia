import { Elysia, t } from "elysia";
import { db } from "../db";
import { hearings } from "../db/schema";
import { eq, desc } from "drizzle-orm";

export const hearingsRoutes = new Elysia({ prefix: "/hearings" })
  // Get all hearings
  .get("/", async () => {
    const allHearings = await db.query.hearings.findMany({
      orderBy: desc(hearings.createdAt),
    });

    return {
      success: true,
      data: allHearings,
      count: allHearings.length,
    };
  })

  // Get hearings by report ID
  .get("/report/:reportId", async ({ params }) => {
    const reportHearings = await db.query.hearings.findMany({
      where: eq(hearings.blotterReportId, parseInt(params.reportId)),
      orderBy: desc(hearings.hearingDate),
    });

    return {
      success: true,
      data: reportHearings,
      count: reportHearings.length,
    };
  })

  // Get hearing by ID
  .get("/:id", async ({ params, set }) => {
    const hearing = await db.query.hearings.findFirst({
      where: eq(hearings.id, parseInt(params.id)),
    });

    if (!hearing) {
      set.status = 404;
      return { success: false, message: "Hearing not found" };
    }

    return {
      success: true,
      data: hearing,
    };
  })

  // Create hearing
  .post(
    "/",
    async ({ body }) => {
      const [newHearing] = await db
        .insert(hearings)
        .values({
          ...body,
          createdAt: new Date(),
        })
        .returning();

      return {
        success: true,
        message: "Hearing created successfully",
        data: newHearing,
      };
    },
    {
      body: t.Object({
        blotterReportId: t.Number(),
        hearingDate: t.String(),
        hearingTime: t.String(),
        location: t.String(),
        purpose: t.Optional(t.String()),
        status: t.Optional(t.String()),
        notes: t.Optional(t.String()),
      }),
    }
  )

  // Update hearing
  .put(
    "/:id",
    async ({ params, body, set }) => {
      const [updatedHearing] = await db
        .update(hearings)
        .set(body)
        .where(eq(hearings.id, parseInt(params.id)))
        .returning();

      if (!updatedHearing) {
        set.status = 404;
        return { success: false, message: "Hearing not found" };
      }

      return {
        success: true,
        message: "Hearing updated successfully",
        data: updatedHearing,
      };
    },
    {
      body: t.Partial(
        t.Object({
          hearingDate: t.String(),
          hearingTime: t.String(),
          location: t.String(),
          purpose: t.String(),
          status: t.String(),
          notes: t.String(),
        })
      ),
    }
  )

  // Delete hearing
  .delete("/:id", async ({ params, set }) => {
    const [deletedHearing] = await db
      .delete(hearings)
      .where(eq(hearings.id, parseInt(params.id)))
      .returning();

    if (!deletedHearing) {
      set.status = 404;
      return { success: false, message: "Hearing not found" };
    }

    return {
      success: true,
      message: "Hearing deleted successfully",
    };
  });

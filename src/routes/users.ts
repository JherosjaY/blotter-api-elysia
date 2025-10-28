import { Elysia, t } from "elysia";
import { db } from "../db";
import { users, fcmTokens } from "../db/schema";
import { eq, and } from "drizzle-orm";

export const usersRoutes = new Elysia({ prefix: "/users" })
  // Get all users
  .get("/", async () => {
    const allUsers = await db.query.users.findMany({
      columns: {
        password: false, // Exclude password
      },
    });

    return {
      success: true,
      data: allUsers,
    };
  })

  // Get user by ID
  .get("/:id", async ({ params, set }) => {
    const user = await db.query.users.findFirst({
      where: eq(users.id, parseInt(params.id)),
      columns: {
        password: false,
      },
    });

    if (!user) {
      set.status = 404;
      return { success: false, message: "User not found" };
    }

    return {
      success: true,
      data: user,
    };
  })

  // Update user
  .put(
    "/:id",
    async ({ params, body, set }) => {
      const [updatedUser] = await db
        .update(users)
        .set({ ...body, updatedAt: new Date() })
        .where(eq(users.id, parseInt(params.id)))
        .returning({
          id: users.id,
          username: users.username,
          firstName: users.firstName,
          lastName: users.lastName,
          role: users.role,
          isActive: users.isActive,
        });

      if (!updatedUser) {
        set.status = 404;
        return { success: false, message: "User not found" };
      }

      return {
        success: true,
        data: updatedUser,
      };
    },
    {
      body: t.Partial(
        t.Object({
          firstName: t.String(),
          lastName: t.String(),
          username: t.String(),
          password: t.String(),
          isActive: t.Boolean(),
          profilePhotoUri: t.String(),
          profileCompleted: t.Boolean(),
        })
      ),
    }
  )

  // Delete user
  .delete("/:id", async ({ params, set }) => {
    const [deletedUser] = await db
      .delete(users)
      .where(eq(users.id, parseInt(params.id)))
      .returning();

    if (!deletedUser) {
      set.status = 404;
      return { success: false, message: "User not found" };
    }

    return {
      success: true,
      message: "User deleted successfully",
    };
  })

  // Save FCM token (Multi-device support)
  .post(
    "/fcm-token",
    async ({ body, set }) => {
      try {
        const { userId, fcmToken, deviceId } = body;

        // Check if this device already has a token
        const existingToken = await db.query.fcmTokens.findFirst({
          where: and(
            eq(fcmTokens.userId, userId),
            eq(fcmTokens.deviceId, deviceId || "")
          ),
        });

        if (existingToken) {
          // Update existing token
          await db
            .update(fcmTokens)
            .set({
              fcmToken,
              lastUsed: new Date(),
              updatedAt: new Date(),
              isActive: true,
            })
            .where(eq(fcmTokens.id, existingToken.id));
        } else {
          // Insert new token for this device
          await db.insert(fcmTokens).values({
            userId,
            fcmToken,
            deviceId,
            isActive: true,
            lastUsed: new Date(),
          });
        }

        // Also update the main users table (for backward compatibility)
        await db
          .update(users)
          .set({
            fcmToken,
            deviceId,
            updatedAt: new Date(),
          })
          .where(eq(users.id, userId));

        console.log(`✅ FCM token saved for user ${userId} on device ${deviceId || "unknown"}`);

        return {
          success: true,
          message: "FCM token saved successfully",
        };
      } catch (error) {
        set.status = 500;
        return {
          success: false,
          message: "Failed to save FCM token",
          error: error instanceof Error ? error.message : "Unknown error",
        };
      }
    },
    {
      body: t.Object({
        userId: t.Number(),
        fcmToken: t.String(),
        deviceId: t.Optional(t.String()),
      }),
    }
  );

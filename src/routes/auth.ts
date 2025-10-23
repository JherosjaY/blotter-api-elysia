import { Elysia, t } from "elysia";
import { db } from "../db";
import { users } from "../db/schema";
import { eq } from "drizzle-orm";

export const authRoutes = new Elysia({ prefix: "/auth" })
  // Login
  .post(
    "/login",
    async ({ body, set }) => {
      const { username, password } = body;

      const user = await db.query.users.findFirst({
        where: eq(users.username, username),
      });

      if (!user) {
        set.status = 401;
        return { success: false, message: "Invalid credentials" };
      }

      // TODO: Add password hashing verification (bcrypt)
      if (user.password !== password) {
        set.status = 401;
        return { success: false, message: "Invalid credentials" };
      }

      if (!user.isActive) {
        set.status = 403;
        return { success: false, message: "Account is inactive" };
      }

      return {
        success: true,
        data: {
          id: user.id,
          username: user.username,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role,
          profilePhotoUri: user.profilePhotoUri,
        },
      };
    },
    {
      body: t.Object({
        username: t.String(),
        password: t.String(),
      }),
    }
  )

  // Register
  .post(
    "/register",
    async ({ body, set }) => {
      const { username, password, firstName, lastName, role } = body;

      // Check if username exists
      const existingUser = await db.query.users.findFirst({
        where: eq(users.username, username),
      });

      if (existingUser) {
        set.status = 400;
        return { success: false, message: "Username already exists" };
      }

      // TODO: Add password hashing (bcrypt)
      const [newUser] = await db
        .insert(users)
        .values({
          username,
          password, // Should be hashed
          firstName,
          lastName,
          role,
        })
        .returning();

      return {
        success: true,
        data: {
          id: newUser.id,
          username: newUser.username,
          firstName: newUser.firstName,
          lastName: newUser.lastName,
          role: newUser.role,
        },
      };
    },
    {
      body: t.Object({
        username: t.String(),
        password: t.String(),
        firstName: t.String(),
        lastName: t.String(),
        role: t.String(),
      }),
    }
  );

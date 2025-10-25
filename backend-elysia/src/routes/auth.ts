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

      // Generate simple token (in production, use JWT)
      const token = Buffer.from(`${user.id}:${user.username}:${Date.now()}`).toString('base64');

      return {
        success: true,
        message: "Login successful",
        data: {
          user: {
            id: user.id,
            username: user.username,
            firstName: user.firstName,
            lastName: user.lastName,
            role: user.role,
            profilePhotoUri: user.profilePhotoUri,
            profileCompleted: user.profileCompleted,
          },
          token: token,
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

  // Register (Public - User role only)
  .post(
    "/register",
    async ({ body, set }) => {
      const { username, password, firstName, lastName } = body;

      // Check if username exists
      const existingUser = await db.query.users.findFirst({
        where: eq(users.username, username),
      });

      if (existingUser) {
        set.status = 400;
        return { success: false, message: "Username already exists" };
      }

      // Public registration always creates "User" role
      // Officers and Admins must be created by Admin through user management
      const [newUser] = await db
        .insert(users)
        .values({
          username,
          password, // TODO: Add password hashing (bcrypt)
          firstName,
          lastName,
          role: "User", // Force User role for public registration
          isActive: true,
          profileCompleted: false,
          mustChangePassword: false,
        })
        .returning();

      // Generate token
      const token = Buffer.from(`${newUser.id}:${newUser.username}:${Date.now()}`).toString('base64');

      return {
        success: true,
        message: "Registration successful",
        data: {
          user: {
            id: newUser.id,
            username: newUser.username,
            firstName: newUser.firstName,
            lastName: newUser.lastName,
            role: newUser.role,
            profileCompleted: newUser.profileCompleted,
          },
          token: token,
        },
      };
    },
    {
      body: t.Object({
        username: t.String(),
        password: t.String(),
        firstName: t.String(),
        lastName: t.String(),
      }),
    }
  );

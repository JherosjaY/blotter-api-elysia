import { Elysia } from "elysia";
import { bearer } from "@elysiajs/bearer";
import { cors } from "@elysiajs/cors";
import { swagger } from "@elysiajs/swagger";
import { autoload } from "elysia-autoload";

// Import routes
import { authRoutes } from "./routes/auth";
import { reportsRoutes } from "./routes/reports";
import { usersRoutes } from "./routes/users";
import { officersRoutes } from "./routes/officers";
import { witnessesRoutes } from "./routes/witnesses";
import { suspectsRoutes } from "./routes/suspects";

export const app = new Elysia()
  .use(bearer())
  .use(
    cors({
      origin: process.env.ALLOWED_ORIGINS?.split(",") || "*",
    })
  )
  .use(
    swagger({
      path: "/swagger",
      documentation: {
        info: {
          title: "Blotter Management System API",
          version: "1.0.0",
          description: "API for Blotter Management System - Elysia.js + Drizzle ORM",
        },
        tags: [
          { name: "Auth", description: "Authentication endpoints" },
          { name: "Reports", description: "Blotter reports management" },
          { name: "Users", description: "User management" },
          { name: "Officers", description: "Officer management" },
          { name: "Witnesses", description: "Witness management" },
          { name: "Suspects", description: "Suspect management" },
        ],
      },
    })
  )
  // Health check
  .get("/", () => ({
    success: true,
    message: "Blotter API is running! 🚀",
    timestamp: new Date().toISOString(),
    endpoints: {
      swagger: "/swagger",
      auth: "/api/auth",
      reports: "/api/reports",
      users: "/api/users",
      officers: "/api/officers",
      witnesses: "/api/witnesses",
      suspects: "/api/suspects",
    },
  }))
  .get("/health", () => ({
    success: true,
    status: "healthy",
    timestamp: new Date().toISOString(),
  }))
  // Mount routes
  .group("/api", (app) =>
    app
      .use(authRoutes)
      .use(reportsRoutes)
      .use(usersRoutes)
      .use(officersRoutes)
      .use(witnessesRoutes)
      .use(suspectsRoutes)
  )
  .listen(process.env.PORT || 3000);

console.log(
  `🦊 Elysia is running at ${app.server?.hostname}:${app.server?.port}`
);

export type ElysiaApp = typeof app;

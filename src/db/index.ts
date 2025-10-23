import { drizzle } from "drizzle-orm/postgres-js";
import postgres from "postgres";
import * as schema from "./schema";

const connectionString = process.env.DATABASE_URL || "postgresql://user:password@localhost:5432/blotter_db";

const client = postgres(connectionString);
export const db = drizzle(client, { schema });

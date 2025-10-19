import { Elysia } from 'elysia';
import { cors } from '@elysiajs/cors';
import { swagger } from '@elysiajs/swagger';

// Import routes
import { authRoutes } from './routes/auth';
import { usersRoutes } from './routes/users';
import { officersRoutes } from './routes/officers';
import { reportsRoutes } from './routes/reports';
import { respondentsRoutes } from './routes/respondents';
import { suspectsRoutes } from './routes/suspects';
import { witnessesRoutes } from './routes/witnesses';
import { evidenceRoutes } from './routes/evidence';
import { hearingsRoutes } from './routes/hearings';
import { resolutionsRoutes } from './routes/resolutions';
import { smsRoutes } from './routes/sms';
import { analyticsRoutes } from './routes/analytics';

const app = new Elysia()
  .use(cors())
  .use(swagger({
    documentation: {
      info: {
        title: 'Blotter Management System API',
        version: '1.0.0',
        description: 'Complete REST API for Barangay Blotter Management System'
      },
      tags: [
        { name: 'Auth', description: 'Authentication endpoints' },
        { name: 'Users', description: 'User management (Clerk/Admin)' },
        { name: 'Officers', description: 'Officer management' },
        { name: 'Reports', description: 'Blotter reports CRUD' },
        { name: 'Respondents', description: 'Respondent management' },
        { name: 'Suspects', description: 'Suspect management' },
        { name: 'Witnesses', description: 'Witness management' },
        { name: 'Evidence', description: 'Evidence management' },
        { name: 'Hearings', description: 'Hearing schedules' },
        { name: 'Resolutions', description: 'Case resolutions' },
        { name: 'SMS', description: 'SMS notifications' },
        { name: 'Analytics', description: 'Statistics and analytics' }
      ]
    }
  }))
  .get('/', () => ({
    message: 'Blotter Management System API',
    version: '1.0.0',
    status: 'running',
    documentation: '/swagger'
  }))
  // Register all routes
  .use(authRoutes)
  .use(usersRoutes)
  .use(officersRoutes)
  .use(reportsRoutes)
  .use(respondentsRoutes)
  .use(suspectsRoutes)
  .use(witnessesRoutes)
  .use(evidenceRoutes)
  .use(hearingsRoutes)
  .use(resolutionsRoutes)
  .use(smsRoutes)
  .use(analyticsRoutes)
  .listen({
    port: process.env.PORT || 3000,
    hostname: '0.0.0.0'
  });

console.log(`🚀 Blotter API running at http://localhost:${app.server?.port}`);
console.log(`📚 API Documentation: http://localhost:${app.server?.port}/swagger`);

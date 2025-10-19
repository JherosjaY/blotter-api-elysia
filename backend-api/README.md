# Blotter Management System API

Complete REST API backend for Barangay Blotter Management System.

## Tech Stack
- **Runtime:** Bun 1.3.0
- **Framework:** Elysia
- **Database:** PostgreSQL
- **ORM:** Drizzle

## Features
- 50+ API endpoints
- 15 database tables
- Role-based access (User, Officer, Admin)
- Auto-generated Swagger documentation
- JWT authentication ready
- File upload support

## Installation

```bash
bun install
```

## Setup Database

1. Create PostgreSQL database (use Neon.tech for free cloud database)
2. Copy `.env.example` to `.env`
3. Update `DATABASE_URL` in `.env`

```bash
cp .env.example .env
```

## Run Development Server

```bash
bun run src/index.ts
```

Server will start at: http://localhost:3000
API Documentation: http://localhost:3000/swagger

## API Endpoints

### Authentication
- POST `/api/auth/login` - User login
- POST `/api/auth/register` - Register new user
- GET `/api/auth/me/:userId` - Get current user

### Users (Admin)
- GET `/api/users` - Get all users
- GET `/api/users/:id` - Get user by ID
- POST `/api/users` - Create user
- PUT `/api/users/:id` - Update user
- DELETE `/api/users/:id` - Delete user

### Officers
- GET `/api/officers` - Get all officers
- GET `/api/officers/:id` - Get officer by ID
- POST `/api/officers` - Create officer
- PUT `/api/officers/:id` - Update officer
- DELETE `/api/officers/:id` - Delete officer

### Blotter Reports
- GET `/api/reports` - Get all reports
- GET `/api/reports/:id` - Get report by ID
- GET `/api/reports/user/:userId` - Get reports by user
- POST `/api/reports` - Create report
- PUT `/api/reports/:id` - Update report
- PATCH `/api/reports/:id/assign-officer` - Assign officer
- PATCH `/api/reports/:id/status` - Update status
- DELETE `/api/reports/:id` - Delete report

### Respondents
- GET `/api/respondents/report/:reportId` - Get respondents by report
- POST `/api/respondents` - Create respondent
- DELETE `/api/respondents/:id` - Delete respondent

### Suspects
- GET `/api/suspects/report/:reportId` - Get suspects by report
- POST `/api/suspects` - Create suspect
- DELETE `/api/suspects/:id` - Delete suspect

### Witnesses
- GET `/api/witnesses/report/:reportId` - Get witnesses by report
- POST `/api/witnesses` - Create witness
- DELETE `/api/witnesses/:id` - Delete witness

### Evidence
- GET `/api/evidence/report/:reportId` - Get evidence by report
- POST `/api/evidence` - Create evidence
- DELETE `/api/evidence/:id` - Delete evidence

### Hearings
- GET `/api/hearings` - Get all hearings
- GET `/api/hearings/report/:reportId` - Get hearings by report
- POST `/api/hearings` - Create hearing
- DELETE `/api/hearings/:id` - Delete hearing

### Resolutions
- GET `/api/resolutions/report/:reportId` - Get resolutions by report
- POST `/api/resolutions` - Create resolution

### SMS Notifications
- GET `/api/sms/report/:reportId` - Get SMS by report
- POST `/api/sms/send` - Send SMS notification

### Analytics
- GET `/api/analytics/dashboard` - Get dashboard statistics
- GET `/api/analytics/officer/:officerId` - Get officer statistics

## Deployment

### Deploy to Render.com

1. Push to GitHub
2. Create PostgreSQL database on Render
3. Create Web Service on Render
4. Connect GitHub repository
5. Set environment variables:
   - `DATABASE_URL`
   - `PORT=3000`
   - `NODE_ENV=production`
6. Deploy!

## Database Schema

15 tables:
- users
- officers
- blotter_reports
- respondents
- suspects
- witnesses
- evidence
- hearings
- resolutions
- respondent_statements
- sms_notifications
- activity_logs
- notifications
- person_history
- status_history

## Android Integration

Use Retrofit in your Android app:

```kotlin
val BASE_URL = "https://your-api.on.render.com"

interface BlotterApi {
    @POST("/api/auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<LoginResponse>
    
    @GET("/api/reports")
    suspend fun getReports(): Response<ReportsResponse>
    
    // ... more endpoints
}
```

## License

MIT License - For educational purposes

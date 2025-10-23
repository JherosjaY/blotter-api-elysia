# Blotter Management System API

Backend API built with **Elysia.js** (Bun framework) and **Drizzle ORM** (PostgreSQL).

## 🚀 Features

- ⚡ **Elysia.js** - Fast and modern web framework for Bun
- 🗄️ **Drizzle ORM** - Type-safe SQL ORM
- 🐘 **PostgreSQL** - Robust relational database
- 📚 **Swagger UI** - Auto-generated API documentation
- 🐳 **Docker** - Containerized deployment
- 🔒 **CORS** - Cross-origin resource sharing
- 🔑 **Bearer Auth** - Token-based authentication

## 📋 Prerequisites

- [Bun](https://bun.sh/) >= 1.0.0
- [PostgreSQL](https://www.postgresql.org/) >= 14
- [Docker](https://www.docker.com/) (optional)

## 🛠️ Installation

### 1. Install dependencies

```bash
bun install
```

### 2. Setup environment variables

```bash
cp .env.example .env
```

Edit `.env` with your database credentials:

```env
DATABASE_URL=postgresql://user:password@localhost:5432/blotter_db
PORT=3000
NODE_ENV=development
JWT_SECRET=your-secret-key
```

### 3. Setup database

Start PostgreSQL (or use Docker):

```bash
# Using Docker Compose (recommended for development)
docker-compose -f docker-compose.dev.yml up -d
```

Generate and run migrations:

```bash
bun run db:generate
bun run db:push
```

### 4. Start development server

```bash
bun run dev
```

The API will be available at `http://localhost:3000`

## 📚 API Documentation

Once the server is running, visit:

- **Swagger UI**: http://localhost:3000/swagger
- **Health Check**: http://localhost:3000/health

## 🗂️ Project Structure

```
backend-elysia/
├── src/
│   ├── db/
│   │   ├── schema.ts      # Drizzle schema definitions
│   │   └── index.ts       # Database connection
│   ├── routes/
│   │   ├── auth.ts        # Authentication routes
│   │   ├── reports.ts     # Blotter reports routes
│   │   ├── users.ts       # User management routes
│   │   ├── officers.ts    # Officer management routes
│   │   ├── witnesses.ts   # Witness routes
│   │   └── suspects.ts    # Suspect routes
│   └── index.ts           # Main application entry
├── drizzle/               # Generated migrations
├── Dockerfile
├── docker-compose.yml
├── docker-compose.dev.yml
├── drizzle.config.ts
├── package.json
└── tsconfig.json
```

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Reports
- `GET /api/reports` - Get all reports
- `GET /api/reports/:id` - Get report by ID
- `POST /api/reports` - Create new report
- `PUT /api/reports/:id` - Update report
- `DELETE /api/reports/:id` - Delete report
- `GET /api/reports/status/:status` - Get reports by status

### Users
- `GET /api/users` - Get all users
- `GET /api/users/:id` - Get user by ID
- `PUT /api/users/:id` - Update user
- `DELETE /api/users/:id` - Delete user

### Officers
- `GET /api/officers` - Get all officers
- `GET /api/officers/:id` - Get officer by ID
- `POST /api/officers` - Create officer
- `PUT /api/officers/:id` - Update officer
- `DELETE /api/officers/:id` - Delete officer

### Witnesses
- `GET /api/witnesses/report/:reportId` - Get witnesses by report
- `POST /api/witnesses` - Add witness
- `DELETE /api/witnesses/:id` - Delete witness

### Suspects
- `GET /api/suspects/report/:reportId` - Get suspects by report
- `POST /api/suspects` - Add suspect
- `DELETE /api/suspects/:id` - Delete suspect

## 🐳 Docker Deployment

### Development

```bash
docker-compose -f docker-compose.dev.yml up -d
```

### Production

```bash
docker-compose up -d
```

## 🗄️ Database Commands

```bash
# Generate migrations
bun run db:generate

# Push schema to database
bun run db:push

# Run migrations
bun run db:migrate

# Open Drizzle Studio (GUI)
bun run db:studio
```

## 🧪 Testing

```bash
# Run tests (TODO)
bun test
```

## 📝 Notes

- Password hashing with bcrypt is marked as TODO - implement before production
- JWT authentication is marked as TODO - implement for secure API access
- Add input validation middleware for production use
- Consider adding rate limiting for API endpoints

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

MIT License

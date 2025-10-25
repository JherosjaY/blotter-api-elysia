# 🚀 Quick Start Guide - Blotter Backend API

## ⚡ Super Fast Setup (5 Minutes!)

### **Step 1: Install Bun** (if not installed)

```powershell
powershell -c "irm bun.sh/install.ps1|iex"
```

Close and reopen terminal, then verify:
```bash
bun --version
```

---

### **Step 2: Install Dependencies**

```bash
cd backend-elysia
bun install
```

---

### **Step 3: Setup Database**

#### **Option A: Use Neon (Free Cloud Database - RECOMMENDED)**

1. Go to https://neon.tech
2. Sign up (free, no credit card)
3. Create project: "blotter-management"
4. Copy connection string

#### **Option B: Local PostgreSQL**

```sql
-- Install PostgreSQL first, then:
psql -U postgres
CREATE DATABASE blotter_db;
```

---

### **Step 4: Configure Environment**

Edit `.env` file:

```env
# Neon Database (recommended)
DATABASE_URL=postgresql://username:password@ep-xxx.neon.tech/blotter_db?sslmode=require

# OR Local Database
# DATABASE_URL=postgresql://postgres:password@localhost:5432/blotter_db

PORT=3000
NODE_ENV=development
JWT_SECRET=your-secret-key-change-this
ALLOWED_ORIGINS=http://localhost:3000,http://10.0.2.2:3000
```

---

### **Step 5: Create Tables**

```bash
bun run db:push
```

You should see:
```
✅ Tables created successfully!
```

---

### **Step 6: Seed Sample Data**

```bash
bun run db:seed
```

This creates:
- ✅ 3 Users (Admin, Officer, User)
- ✅ 1 Officer record
- ✅ 3 Sample reports
- ✅ 2 Notifications

**Test Credentials:**
- Admin: `admin` / `admin123`
- Officer: `officer1` / `officer123`
- User: `user1` / `user123`

---

### **Step 7: Start Backend**

```bash
bun run dev
```

You should see:
```
🦊 Elysia is running at localhost:3000
```

---

### **Step 8: Test API**

#### **Option A: Browser**
Open: http://localhost:3000

Should see:
```json
{
  "success": true,
  "message": "Blotter API is running!",
  ...
}
```

#### **Option B: Swagger UI**
Open: http://localhost:3000/swagger

Try the endpoints!

#### **Option C: PowerShell Test Script**
```bash
bun run test
```

This tests all major endpoints automatically!

---

## 🧪 Verify Everything Works

### **1. Check Dashboard Analytics**

```powershell
curl http://localhost:3000/api/dashboard/analytics
```

Should show:
```json
{
  "success": true,
  "data": {
    "totalReports": 3,
    "pendingReports": 1,
    "ongoingReports": 1,
    "resolvedReports": 1,
    "totalOfficers": 1,
    "totalUsers": 3
  }
}
```

### **2. Test Login**

```powershell
$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:3000/api/auth/login -Method Post -Body $body -ContentType "application/json"
```

Should return user data and token!

### **3. Get All Reports**

```powershell
curl http://localhost:3000/api/reports
```

Should return 3 sample reports!

---

## 📱 Connect Android App

### **Android Emulator:**
Already configured! Just run the app.

URL: `http://10.0.2.2:3000/`

### **Physical Device:**

1. Find your computer's IP:
```powershell
ipconfig
# Look for IPv4 Address (e.g., 192.168.1.5)
```

2. Update `ApiConfig.kt`:
```kotlin
private const val BASE_URL = "http://192.168.1.5:3000/"
```

3. Make sure firewall allows port 3000!

---

## ✅ Complete Workflow Test

### **1. Start Backend**
```bash
cd backend-elysia
bun run dev
```

### **2. Run Test Script**
```bash
bun run test
```

All tests should pass! ✅

### **3. Open Swagger**
http://localhost:3000/swagger

### **4. Try Login in Swagger**
- Find `POST /api/auth/login`
- Click "Try it out"
- Enter:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- Click "Execute"
- Should return token! ✅

### **5. Run Android App**
- Open Android Studio
- Run app on emulator
- Login with: `admin` / `admin123`
- Dashboard should load with 3 reports! ✅

---

## 🎯 Available Commands

```bash
# Development
bun run dev          # Start with hot reload
bun run start        # Start production

# Database
bun run db:push      # Create/update tables
bun run db:seed      # Add sample data
bun run db:studio    # Open Drizzle Studio

# Testing
bun run test         # Run API tests
```

---

## 🐛 Troubleshooting

### **Backend won't start**
```bash
# Check if port 3000 is in use
netstat -ano | findstr :3000

# Kill process
taskkill /PID <PID> /F
```

### **Database connection error**
- Check `.env` file has correct DATABASE_URL
- Verify database exists
- Check internet connection (if using Neon)

### **Android app can't connect**
- Backend must be running: `curl http://localhost:3000/health`
- Emulator: Use `http://10.0.2.2:3000/`
- Physical device: Use your IP `http://192.168.1.XXX:3000/`
- Check firewall settings

### **No data in dashboard**
```bash
# Re-seed database
bun run db:seed
```

---

## 📊 What You Have Now

✅ **Backend API** - 60+ endpoints  
✅ **Database** - 17 tables with sample data  
✅ **Test Users** - Admin, Officer, User  
✅ **Sample Reports** - 3 test cases  
✅ **Documentation** - Swagger UI  
✅ **Test Script** - Automated testing  

---

## 🎉 Success Checklist

- [ ] Bun installed
- [ ] Dependencies installed (`bun install`)
- [ ] Database configured (`.env`)
- [ ] Tables created (`bun run db:push`)
- [ ] Sample data loaded (`bun run db:seed`)
- [ ] Backend running (`bun run dev`)
- [ ] API responding (`curl http://localhost:3000/health`)
- [ ] Tests passing (`bun run test`)
- [ ] Swagger accessible (http://localhost:3000/swagger)
- [ ] Android app connected

---

## 🚀 You're Ready!

**Backend is running!**  
**Sample data is loaded!**  
**Android app can connect!**

**Just run your Android app and test! 🎯**

---

**Mao na pre! Complete na tanan! 🎉**

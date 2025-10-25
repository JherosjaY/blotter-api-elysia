# 🚀 START HERE - Backend Setup

## ✅ **What We Fixed:**

### **1. Backend Auth - FIXED!**
- ✅ Public registration (`/api/auth/register`) now **ONLY creates User role**
- ✅ Login returns proper response with token
- ✅ Role comes from database (no auto-detection)
- ✅ Officers/Admins must be created by Admin

### **2. Android App - FIXED!**
- ✅ Removed auto-role detection bug
- ✅ Sign up always creates "User" role
- ✅ Login uses actual role from database

---

## 🎯 **Quick Start (3 Commands!):**

### **Step 1: Configure Database**

Edit `.env` file and add your database URL:

**Option A: Neon (Free Cloud - RECOMMENDED)**
```env
DATABASE_URL=postgresql://username:password@ep-xxx.neon.tech/blotter_db?sslmode=require
```

Get it from: https://neon.tech (free, no credit card)

**Option B: Local PostgreSQL**
```env
DATABASE_URL=postgresql://postgres:password@localhost:5432/blotter_db
```

### **Step 2: Setup Database**
```powershell
bun install
bun run db:push
bun run db:seed
```

### **Step 3: Start Backend**
```powershell
bun run dev
```

---

## 🧪 **Test It:**

### **1. Health Check**
```powershell
curl http://localhost:3000/health
```

### **2. Test Login**
```powershell
$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:3000/api/auth/login -Method Post -Body $body -ContentType "application/json"
```

### **3. Test Register (Creates User role only!)**
```powershell
$body = @{
    username = "testuser"
    password = "test123"
    firstName = "Test"
    lastName = "User"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:3000/api/auth/register -Method Post -Body $body -ContentType "application/json"
```

### **4. Run All Tests**
```powershell
bun run test
```

---

## 🔑 **Test Credentials:**

```
Admin:   username=admin,    password=admin123
Officer: username=officer1, password=officer123
User:    username=user1,    password=user123
```

---

## 📱 **Connect Android App:**

1. Make sure backend is running: `bun run dev`
2. Open Android Studio
3. Run app on emulator
4. Login with test credentials
5. ✅ **Works!**

---

## 🎉 **What's Different Now:**

### **Before (BUG):**
```
Sign up with "off.test" → Auto-assigned Officer role ❌
Login checks username prefix → Changes role ❌
```

### **After (FIXED):**
```
Sign up with any username → Always creates User role ✅
Login uses database role → No auto-detection ✅
Only Admin can create Officers → Correct! ✅
```

---

## 📊 **API Endpoints:**

### **Auth:**
- `POST /api/auth/login` - Login (returns token)
- `POST /api/auth/register` - Register (User role only!)

### **Dashboard:**
- `GET /api/dashboard/analytics` - Get statistics

### **Reports:**
- `GET /api/reports` - Get all reports
- `POST /api/reports` - Create report
- `PUT /api/reports/:id` - Update report
- `DELETE /api/reports/:id` - Delete report

### **Users:**
- `GET /api/users` - Get all users
- `POST /api/users` - Create user (Admin only)
- `PUT /api/users/:id` - Update user
- `DELETE /api/users/:id` - Delete user

**...and 50+ more endpoints!**

See Swagger: http://localhost:3000/swagger

---

## 🐛 **Troubleshooting:**

### **Port 3000 in use:**
```powershell
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

### **Database connection error:**
- Check `.env` file
- Verify DATABASE_URL is correct
- Test connection to database

### **Android app can't connect:**
- Backend must be running
- Use `http://10.0.2.2:3000/` for emulator
- Use `http://YOUR_IP:3000/` for physical device

---

## ✅ **Summary:**

**Backend:**
- ✅ Auth fixed (User role only for public registration)
- ✅ 60+ API endpoints
- ✅ 17 database tables
- ✅ Sample data seeded
- ✅ Swagger documentation

**Android:**
- ✅ Auto-role detection removed
- ✅ Sign up creates User role only
- ✅ Login uses database role
- ✅ Officer profile correct

**Status:** 🎉 **ALL FIXED!**

---

**Mao na pre! Backend ug Android both fixed na! 🚀**

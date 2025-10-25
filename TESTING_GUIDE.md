# 🧪 Backend API Testing Guide

## ✅ Backend is Running!

Your backend is now live at: **http://localhost:3000**

---

## 🌐 **Browser Testing (Easy Way!)**

### **1. Open Swagger UI (Best for Testing!)**

**URL:** http://localhost:3000/swagger

This gives you a beautiful interactive interface where you can:
- ✅ See all 60+ endpoints
- ✅ Test each endpoint with "Try it out" button
- ✅ See request/response examples
- ✅ No need for curl or Postman!

**How to use:**
1. Open http://localhost:3000/swagger in browser
2. Find an endpoint (e.g., POST /api/auth/login)
3. Click "Try it out"
4. Enter test data
5. Click "Execute"
6. See the response!

---

### **2. Quick Browser Tests**

Open these URLs directly in your browser:

#### **Health Check:**
```
http://localhost:3000/health
```
**Expected:** `{"success":true,"status":"healthy"}`

#### **Root Endpoint:**
```
http://localhost:3000/
```
**Expected:** API info with version, endpoints count

#### **Dashboard Analytics:**
```
http://localhost:3000/api/dashboard/analytics
```
**Expected:** Statistics (total reports, users, officers, etc.)

#### **Get All Reports:**
```
http://localhost:3000/api/reports
```
**Expected:** Array of 3 sample reports

#### **Get All Users:**
```
http://localhost:3000/api/users
```
**Expected:** Array of 3 users (Admin, Officer, User)

#### **Get All Officers:**
```
http://localhost:3000/api/officers
```
**Expected:** Array of 1 officer

---

## 🧪 **PowerShell Testing (Detailed)**

### **Test 1: Health Check**
```powershell
curl http://localhost:3000/health
```

### **Test 2: Login (Admin)**
```powershell
$body = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:3000/api/auth/login -Method Post -Body $body -ContentType "application/json"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "user": {
      "id": 1,
      "username": "admin",
      "firstName": "Admin",
      "lastName": "User",
      "role": "Admin",
      "profileCompleted": true
    },
    "token": "..."
  }
}
```

### **Test 3: Register New User (User Role Only!)**
```powershell
$body = @{
    username = "testuser"
    password = "test123"
    firstName = "Test"
    lastName = "User"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:3000/api/auth/register -Method Post -Body $body -ContentType "application/json"
```

**Expected:** Creates user with "User" role (not Officer!)

### **Test 4: Dashboard Analytics**
```powershell
Invoke-RestMethod -Uri http://localhost:3000/api/dashboard/analytics -Method Get
```

**Expected:**
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

### **Test 5: Get All Reports**
```powershell
Invoke-RestMethod -Uri http://localhost:3000/api/reports -Method Get
```

### **Test 6: Create New Report**
```powershell
$body = @{
    caseNumber = "TEST-2025-001"
    incidentType = "Test Incident"
    incidentDate = "2025-10-25"
    incidentTime = "19:00"
    incidentLocation = "Test Location"
    narrative = "This is a test report"
    complainantName = "Test Complainant"
    complainantContact = "09991234567"
    status = "Pending"
    priority = "Normal"
} | ConvertTo-Json

Invoke-RestMethod -Uri http://localhost:3000/api/reports -Method Post -Body $body -ContentType "application/json"
```

---

## 🚀 **Automated Test Script**

Run all tests at once:

```powershell
bun run test
```

This will test:
- ✅ Health check
- ✅ Dashboard analytics
- ✅ Get all reports
- ✅ Get all users
- ✅ Get all officers
- ✅ Login
- ✅ Create report

---

## 📊 **What to Check:**

### **1. Database Has Data**
- Go to your database (Neon dashboard or pgAdmin)
- Check tables have data:
  - `users` table: 3 users
  - `officers` table: 1 officer
  - `blotter_reports` table: 3 reports
  - `notifications` table: 2 notifications

### **2. API Responses**
- All endpoints return `{"success": true}`
- Data is properly formatted
- No errors in console

### **3. Role-Based Logic**
- Public registration creates "User" role only
- Login returns correct role from database
- No auto-role detection based on username

---

## 🎯 **Testing Checklist:**

### **Basic Tests:**
- [ ] Health check responds (http://localhost:3000/health)
- [ ] Root endpoint responds (http://localhost:3000/)
- [ ] Swagger UI loads (http://localhost:3000/swagger)

### **Auth Tests:**
- [ ] Login with admin works
- [ ] Login with officer works
- [ ] Login with user works
- [ ] Register creates User role only
- [ ] Invalid credentials rejected

### **Dashboard Tests:**
- [ ] Analytics returns correct counts
- [ ] Total reports = 3
- [ ] Total users = 3
- [ ] Total officers = 1

### **CRUD Tests:**
- [ ] Get all reports works
- [ ] Create report works
- [ ] Update report works
- [ ] Delete report works

### **Other Endpoints:**
- [ ] Get all users works
- [ ] Get all officers works
- [ ] Get all persons works
- [ ] Get all evidence works
- [ ] Get all hearings works

---

## 🌐 **Browser URLs (Copy & Paste):**

```
Health Check:
http://localhost:3000/health

Swagger UI (Interactive Testing):
http://localhost:3000/swagger

Dashboard Analytics:
http://localhost:3000/api/dashboard/analytics

All Reports:
http://localhost:3000/api/reports

All Users:
http://localhost:3000/api/users

All Officers:
http://localhost:3000/api/officers

All Persons:
http://localhost:3000/api/persons

All Evidence:
http://localhost:3000/api/evidence

All Hearings:
http://localhost:3000/api/hearings

All Resolutions:
http://localhost:3000/api/resolutions

All Activity Logs:
http://localhost:3000/api/activity-logs
```

---

## 🐛 **Common Issues:**

### **"Cannot connect" Error:**
- Check if backend is running: `curl http://localhost:3000/health`
- Restart backend: `bun run dev`

### **"Empty data" Response:**
- Run seeder: `bun run db:seed`
- Check database connection in `.env`

### **"Port 3000 in use" Error:**
```powershell
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

---

## 🎉 **Expected Results:**

### **After Seeding:**
- ✅ 3 Users (Admin, Officer, User)
- ✅ 1 Officer record
- ✅ 3 Blotter reports
- ✅ 2 Notifications

### **API Responses:**
- ✅ All return `{"success": true}`
- ✅ Data is properly formatted
- ✅ No errors

### **Role Testing:**
- ✅ Register with "off.test" → Creates User role
- ✅ Login returns database role
- ✅ No auto-detection

---

## 📱 **Test with Android App:**

1. Make sure backend is running
2. Open Android Studio
3. Run app on emulator
4. Login with test credentials:
   - Admin: `admin` / `admin123`
   - Officer: `officer1` / `officer123`
   - User: `user1` / `user123`
5. Dashboard should load with 3 reports!

---

## 🔑 **Test Credentials:**

```
┌─────────────────────────────────────────┐
│ Admin:   admin / admin123               │
│ Officer: officer1 / officer123          │
│ User:    user1 / user123                │
└─────────────────────────────────────────┘
```

---

## 💡 **Pro Tips:**

1. **Use Swagger UI** - Easiest way to test all endpoints
2. **Check browser console** - See any errors
3. **Use PowerShell** - For automated testing
4. **Check database** - Verify data is saved
5. **Test with Android app** - End-to-end testing

---

**Mao na pre! Backend is ready for testing! 🚀**

**Just open http://localhost:3000/swagger and test away! 🎉**

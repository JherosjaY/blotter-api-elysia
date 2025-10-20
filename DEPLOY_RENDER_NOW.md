# ⚡ DEPLOY TO RENDER.COM - 10 MINUTES!

## ✅ GITHUB DONE!

Your code is at: https://github.com/JherosqM/BlotterManagementSystem

---

## 🚀 DEPLOY TO RENDER.COM (10 mins)

### **Step 1: Go to Render.com (2 mins)**

1. Open: **https://render.com**
2. Click **"Get Started"** or **"Sign In"**
3. Sign in with **GitHub**
4. Authorize Render to access your repos

### **Step 2: Create Web Service (3 mins)**

1. Click **"New +"** (top right)
2. Select **"Web Service"**
3. Find and select: **BlotterManagementSystem**
4. Click **"Connect"**

### **Step 3: Configure (3 mins)**

Fill in these settings:

- **Name:** `blotter-api`
- **Region:** `Oregon (US West)`
- **Root Directory:** `backend-api`
- **Runtime:** `Docker`
- **Branch:** `main`
- **Instance Type:** `Free`

### **Step 4: Add Environment Variable (2 mins)**

Click **"Add Environment Variable"**

- **Key:** `DATABASE_URL`
- **Value:** 
```
postgresql://neondb_owner:npg_SOgU1jVmYys4@ep-broad-unit-adplsl01-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require
```

### **Step 5: Deploy!**

1. Click **"Create Web Service"**
2. Wait 5-10 minutes for deployment
3. You'll get a URL like: `https://blotter-api.onrender.com`

**COPY THAT URL!**

---

## 📱 UPDATE ANDROID APP (2 mins)

### **Open:** `app/src/main/java/com/example/blottermanagementsystem/data/api/ApiConfig.kt`

### **Change line 9:**

From:
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"
```

To:
```kotlin
private const val BASE_URL = "https://blotter-api.onrender.com/"
```

**Save the file!**

---

## 🎉 GENERATE APK (5 mins)

### **In Android Studio:**

1. **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Click **Next**
4. Create keystore (if first time):
   - Click "Create new..."
   - Key store path: Choose location
   - Password: (your password)
   - Alias: blotter-key
   - Validity: 25 years
   - Fill in your details
5. Click **Next**
6. Select **release**
7. Check **V2 (Full APK Signature)**
8. Click **Finish**

**APK will be at:** `app/release/app-release.apk`

---

## ✅ DONE!

Your app now:
- ✅ Works with cloud database
- ✅ Works from any device
- ✅ Works anywhere (with internet)
- ✅ Multi-device sync
- ✅ Production ready!

---

## 🧪 TEST IT:

1. Install APK on phone
2. Login: admin / admin123
3. Create a report
4. Install on another phone
5. Login with same account
6. See the same data! 🎉

---

**BOSS, TAPOS NA!** 🔥💯

**Your backend URL will be:** `https://blotter-api.onrender.com`

**Just update ApiConfig.kt and generate APK!**

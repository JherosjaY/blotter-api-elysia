# 🔥 FCM Backend Setup Guide

Complete guide to set up Firebase Cloud Messaging in your Elysia.js backend.

---

## 📋 **Prerequisites**

- Node.js installed
- Elysia.js backend running
- Firebase project created
- Android app with FCM integrated

---

## 🚀 **Step 1: Install Firebase Admin SDK**

```bash
cd your-backend-directory
npm install firebase-admin
```

---

## 🔑 **Step 2: Get Firebase Service Account Key**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **blotter-FCM**
3. Click **⚙️ Settings** → **Project settings**
4. Go to **Service accounts** tab
5. Click **Generate new private key**
6. Save the JSON file as `firebase-service-account.json`
7. Place it in your backend root directory

**⚠️ IMPORTANT:** Add `firebase-service-account.json` to `.gitignore`!

```
# .gitignore
firebase-service-account.json
```

---

## 📁 **Step 3: Copy FCM Helper Files**

Copy these files to your backend:

1. `backend-fcm-helper.js` → Your backend root
2. `backend-fcm-usage-examples.js` → Your backend root

---

## 🔧 **Step 4: Initialize FCM in Your App**

In your main Elysia.js file (e.g., `index.js`):

```javascript
import { Elysia } from 'elysia';
import FCM from './backend-fcm-helper.js';

// Initialize FCM when app starts
FCM.initializeFCM();

const app = new Elysia()
  .get('/', () => 'Blotter API Running')
  // ... your routes
  .listen(3000);

console.log('🚀 Server running on http://localhost:3000');
```

---

## 📊 **Step 5: Update Database Schema**

Add `fcmToken` field to your users table:

```sql
ALTER TABLE users ADD COLUMN fcmToken VARCHAR(255);
ALTER TABLE users ADD COLUMN deviceId VARCHAR(255);
ALTER TABLE users ADD COLUMN tokenUpdatedAt TIMESTAMP;
```

Or if using MongoDB:

```javascript
// users collection schema
{
  id: Number,
  username: String,
  role: String,
  fcmToken: String,        // Add this
  deviceId: String,        // Add this
  tokenUpdatedAt: Date     // Add this
}
```

---

## 🔌 **Step 6: Create FCM Token Endpoint**

Add this route to save FCM tokens from Android app:

```javascript
app.post('/api/users/fcm-token', async ({ body, db }) => {
  const { userId, fcmToken, deviceId } = body;
  
  try {
    await db.users.update(
      { id: userId },
      { 
        fcmToken,
        deviceId,
        tokenUpdatedAt: new Date()
      }
    );
    
    return { success: true, message: 'FCM token saved' };
  } catch (error) {
    return { success: false, error: error.message };
  }
});
```

---

## 📱 **Step 7: Test FCM Integration**

### **Test 1: Send notification to specific user**

```javascript
app.post('/api/test/notify-user', async ({ body, db }) => {
  const { userId } = body;
  
  await FCM.notifyUserCaseFiled(db, userId, '2025-001');
  
  return { success: true, message: 'Test notification sent' };
});
```

### **Test 2: Send notification to all admins**

```javascript
app.post('/api/test/notify-admins', async () => {
  await FCM.notifyAdminsNewCase('2025-001', 123, 'Juan Dela Cruz');
  
  return { success: true, message: 'Test notification sent to admins' };
});
```

---

## 🎯 **Step 8: Implement Real Notifications**

### **Example: When case is assigned**

```javascript
app.post('/api/cases/assign', async ({ body, db }) => {
  const { caseId, officerId } = body;
  
  // 1. Update database
  await db.cases.update({ id: caseId }, { assignedTo: officerId });
  
  // 2. Get case data
  const caseData = await db.cases.findOne({ id: caseId });
  
  // 3. Send notification to officer
  await FCM.notifyOfficerCaseAssigned(
    db,
    officerId,
    caseData.caseNumber,
    caseId,
    caseData.incidentType
  );
  
  // 4. Send notification to complainant
  await FCM.notifyUserOfficerAssigned(
    db,
    caseData.complainantId,
    caseData.caseNumber,
    'Officer Name',
    'Badge123'
  );
  
  return { success: true };
});
```

---

## ⏰ **Step 9: Set Up Cron Jobs (Optional)**

For daily hearing reminders:

```bash
npm install node-cron
```

```javascript
import cron from 'node-cron';
import { sendHearingReminders } from './backend-fcm-usage-examples.js';

// Run every day at 9:00 AM
cron.schedule('0 9 * * *', async () => {
  console.log('📅 Sending hearing reminders...');
  await sendHearingReminders(db);
});
```

---

## 📝 **All Available Notification Functions**

### **Admin Notifications:**
- `notifyAdminsNewCase(caseNumber, reportId, complainantName)`
- `notifyAdminsUrgentCase(caseNumber, reportId, incidentType)`
- `notifyAdminCaseClosureRequest(db, adminId, caseNumber, officerName)`

### **Officer Notifications:**
- `notifyOfficerCaseAssigned(db, officerId, caseNumber, reportId, incidentType)`
- `notifyOfficerCaseReassigned(db, officerId, caseNumber, reason)`
- `notifyOfficerHearingScheduled(db, officerId, caseNumber, hearingDate, location)`
- `notifyOfficerHearingReminder(db, officerId, caseNumber, hearingDate, location)`
- `notifyOfficerNewEvidence(db, officerId, caseNumber, evidenceType)`
- `notifyOfficerNewWitness(db, officerId, caseNumber, witnessName)`

### **User Notifications:**
- `notifyUserCaseFiled(db, userId, caseNumber)`
- `notifyUserStatusUpdate(db, userId, caseNumber, oldStatus, newStatus)`
- `notifyUserOfficerAssigned(db, userId, caseNumber, officerName, badgeNumber)`
- `notifyUserHearingScheduled(db, userId, caseNumber, hearingDate, location, time)`
- `notifyUserHearingReminder(db, userId, caseNumber, hearingDate, location, time)`
- `notifyUserHearingRescheduled(db, userId, caseNumber, oldDate, newDate, reason)`
- `notifyUserCaseResolved(db, userId, caseNumber, resolution)`
- `notifyUserCaseClosed(db, userId, caseNumber, reason)`
- `notifyUserSummonsIssued(db, userId, caseNumber, summonsDate)`
- `notifyRespondentComplaintFiled(db, userId, caseNumber, complainantName, incidentType)`

### **Bulk Notifications:**
- `notifyAllUsersSystemMaintenance(maintenanceDate, duration)`
- `notifyAllUsersAppUpdate(version, features)`

---

## 🔍 **Troubleshooting**

### **Issue: "No FCM token for user"**
**Solution:** User needs to login on Android app first to generate token.

### **Issue: "Failed to send notification"**
**Solution:** Check if `firebase-service-account.json` is correct.

### **Issue: "User not found"**
**Solution:** Make sure userId exists in database.

### **Issue: Notifications not received on Android**
**Solution:** 
1. Check if app has notification permission
2. Check if FCM token was sent to backend
3. Check logcat for FCM errors

---

## ✅ **Testing Checklist**

- [ ] Firebase Admin SDK installed
- [ ] Service account key downloaded and placed
- [ ] FCM initialized in backend
- [ ] Database has `fcmToken` field
- [ ] `/api/users/fcm-token` endpoint created
- [ ] Android app sends token on login
- [ ] Test notification sent successfully
- [ ] Notification received on Android device

---

## 📚 **Additional Resources**

- [Firebase Admin SDK Documentation](https://firebase.google.com/docs/admin/setup)
- [FCM HTTP v1 API](https://firebase.google.com/docs/cloud-messaging/http-server-ref)
- [Elysia.js Documentation](https://elysiajs.com/)

---

## 🎉 **You're Done!**

Your backend is now ready to send automatic push notifications to all 3 roles (Admin, Officer, User)!

**No more manual work in Firebase Console!** 🚀

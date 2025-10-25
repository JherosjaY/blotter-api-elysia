# 🔐 Render Environment Variables Setup

---

## **ADD FIREBASE CREDENTIALS TO RENDER**

Since `firebase-service-account.json` is not pushed to GitHub (for security), you need to add the credentials as environment variables on Render.

---

## **STEP 1: GET YOUR FIREBASE CREDENTIALS**

Open your `firebase-service-account.json` file locally and find these values:

```json
{
  "project_id": "blotter-fcm",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBg...\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@blotter-fcm.iam.gserviceaccount.com"
}
```

---

## **STEP 2: ADD TO RENDER ENVIRONMENT**

1. **Go to Render Dashboard**
2. **Select your backend service** (blotter-backend)
3. **Go to "Environment" tab** (left sidebar)
4. **Click "Add Environment Variable"**

---

## **STEP 3: ADD THESE 3 VARIABLES**

### **Variable 1: FIREBASE_PROJECT_ID**
- **Key:** `FIREBASE_PROJECT_ID`
- **Value:** `blotter-fcm` (your project ID)

### **Variable 2: FIREBASE_CLIENT_EMAIL**
- **Key:** `FIREBASE_CLIENT_EMAIL`
- **Value:** `firebase-adminsdk-xxxxx@blotter-fcm.iam.gserviceaccount.com` (your client email)

### **Variable 3: FIREBASE_PRIVATE_KEY**
- **Key:** `FIREBASE_PRIVATE_KEY`
- **Value:** The entire private key (including `-----BEGIN PRIVATE KEY-----` and `-----END PRIVATE KEY-----`)

**IMPORTANT:** Copy the ENTIRE private key value, including the newlines (`\n`).

Example:
```
-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...\n-----END PRIVATE KEY-----\n
```

---

## **STEP 4: SAVE AND REDEPLOY**

1. **Click "Save Changes"**
2. Render will automatically redeploy
3. Wait for deployment to complete

---

## **VERIFICATION**

After deployment, check the logs. You should see:

```
✅ Firebase Admin SDK initialized (from environment variables)
```

If you see this, it's working! ✅

---

## **TROUBLESHOOTING**

### **Error: "Failed to initialize Firebase Admin SDK"**

**Solution:** Check that:
- All 3 environment variables are set
- Private key includes `-----BEGIN PRIVATE KEY-----` and `-----END PRIVATE KEY-----`
- No extra spaces or quotes

### **Error: "Invalid private key"**

**Solution:** Make sure you copied the ENTIRE private key with all the `\n` characters.

---

## **SUMMARY**

You need to add 3 environment variables on Render:

1. ✅ `FIREBASE_PROJECT_ID`
2. ✅ `FIREBASE_CLIENT_EMAIL`
3. ✅ `FIREBASE_PRIVATE_KEY`

**DO NOT commit the JSON file to GitHub!** 🔒

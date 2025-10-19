# ⚡ CONNECT FRONTEND TO BACKEND - 10 MINUTES!

## 🎯 MODIFY AuthViewModel TO USE API

### **File:** `app/src/main/java/com/example/blottermanagementsystem/viewmodel/AuthViewModel.kt`

### **Add these imports at the top:**
```kotlin
import com.example.blottermanagementsystem.data.api.ApiRepository
import com.example.blottermanagementsystem.data.api.models.CreateReportRequest
```

### **Add ApiRepository:**
```kotlin
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BlotterRepository(BlotterDatabase.getDatabase(application))
    private val apiRepository = ApiRepository() // ADD THIS LINE
    
    // ... rest of code
}
```

### **Replace login function:**

Find the `login()` function and replace it with:

```kotlin
fun login(username: String, password: String) {
    viewModelScope.launch {
        _isLoading.value = true
        
        // Try API login first
        val apiResult = apiRepository.login(username, password)
        
        apiResult.onSuccess { user ->
            // Save to preferences
            preferencesManager.apply {
                this.userId = user.id.hashCode() // Convert UUID to Int
                this.username = user.username
                this.userRole = user.role
                this.firstName = user.firstName
                this.lastName = user.lastName
                this.isLoggedIn = true
            }
            
            _loginResult.value = LoginResult.Success(user.role)
            Log.d("AuthViewModel", "✅ API Login Success: ${user.firstName}")
        }.onFailure { error ->
            // Fallback to Room if API fails
            Log.e("AuthViewModel", "API login failed, trying Room: ${error.message}")
            
            val roomUser = repository.getUserByUsername(username)
            if (roomUser != null && roomUser.password == password) {
                preferencesManager.apply {
                    this.userId = roomUser.id
                    this.username = roomUser.username
                    this.userRole = roomUser.role
                    this.firstName = roomUser.firstName
                    this.lastName = roomUser.lastName
                    this.isLoggedIn = true
                }
                _loginResult.value = LoginResult.Success(roomUser.role)
            } else {
                _loginResult.value = LoginResult.Error("Invalid credentials")
            }
        }
        
        _isLoading.value = false
    }
}
```

### **That's it! Login now uses API!** ✅

---

## 🧪 TEST IT:

1. Run the app
2. Login with: **admin** / **admin123**
3. Check Logcat for "✅ API Login Success"
4. If it works, YOU'RE CONNECTED! 🎉

---

## 🚀 NEXT: Modify Other ViewModels

### **BlotterViewModel - Get Reports from API:**

```kotlin
class BlotterViewModel(application: Application) : AndroidViewModel(application) {
    private val apiRepository = ApiRepository() // ADD THIS
    
    fun loadReportsFromApi() {
        viewModelScope.launch {
            val result = apiRepository.getAllReports()
            result.onSuccess { apiReports ->
                // Convert API models to Room entities
                val reports = apiReports.map { it.toBlotterReport() }
                _reports.value = reports
            }
        }
    }
}
```

### **Create Report via API:**

```kotlin
fun createReportViaApi(
    userId: String,
    incidentType: String,
    incidentDate: String,
    incidentTime: String,
    location: String,
    description: String,
    complainantName: String
) {
    viewModelScope.launch {
        val request = CreateReportRequest(
            userId = userId,
            incidentType = incidentType,
            incidentDate = incidentDate,
            incidentTime = incidentTime,
            location = location,
            description = description,
            complainantName = complainantName
        )
        
        val result = apiRepository.createReport(request)
        result.onSuccess { report ->
            Log.d("BlotterViewModel", "✅ Report created: ${report.blotterNumber}")
            loadReportsFromApi() // Refresh list
        }
    }
}
```

---

## ⚡ SUPER QUICK VERSION:

**Just modify AuthViewModel login function** = Your app is connected!

**Time: 5 minutes!** 🔥

package com.example.blottermanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blottermanagementsystem.data.database.BlotterDatabase
import com.example.blottermanagementsystem.data.entity.Person
import com.example.blottermanagementsystem.data.entity.PersonHistory
import com.example.blottermanagementsystem.data.repository.BlotterRepository
import com.example.blottermanagementsystem.data.repository.ApiRepository
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PersonViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = BlotterDatabase.getDatabase(application)
    private val apiRepository = ApiRepository()
    private val repository = BlotterRepository(
        userDao = database.userDao(),
        blotterReportDao = database.blotterReportDao(),
        suspectDao = database.suspectDao(),
        witnessDao = database.witnessDao(),
        evidenceDao = database.evidenceDao(),
        hearingDao = database.hearingDao(),
        statusHistoryDao = database.statusHistoryDao(),
        resolutionDao = database.resolutionDao(),
        officerDao = database.officerDao(),
        activityLogDao = database.activityLogDao(),
        notificationDao = database.notificationDao(),
        statusDao = database.statusDao(),
        personDao = database.personDao(),
        respondentDao = database.respondentDao(),
        personHistoryDao = database.personHistoryDao(),
        smsNotificationDao = database.smsNotificationDao(),
        respondentStatementDao = database.respondentStatementDao(),
        summonsDao = database.summonsDao(),
        kpFormDao = database.kpFormDao(),
        mediationSessionDao = database.mediationSessionDao(),
        caseTimelineDao = database.caseTimelineDao(),
        caseTemplateDao = database.caseTemplateDao()
    )
    
    val allPersons: Flow<List<Person>> = repository.getAllActivePerson()
    
    private val _searchResults = MutableStateFlow<List<Person>>(emptyList())
    val searchResults: StateFlow<List<Person>> = _searchResults
    
    private val _selectedPerson = MutableStateFlow<Person?>(null)
    val selectedPerson: StateFlow<Person?> = _selectedPerson
    
    private val _personHistory = MutableStateFlow<List<PersonHistory>>(emptyList())
    val personHistory: StateFlow<List<PersonHistory>> = _personHistory
    
    fun searchPerson(query: String) {
        viewModelScope.launch {
            try {
                _searchResults.value = repository.searchPerson(query).first()
            } catch (e: Exception) {
                Log.e("PersonViewModel", "Error searching person", e)
                _searchResults.value = emptyList()
            }
        }
    }
    
    fun selectPerson(personId: Int) {
        viewModelScope.launch {
            try {
                val person = repository.getPersonById(personId)
                _selectedPerson.value = person
                
                // Load person history - collect ONCE
                _personHistory.value = repository.getHistoryByPersonId(personId).first()
            } catch (e: Exception) {
                Log.e("PersonViewModel", "Error selecting person", e)
                _personHistory.value = emptyList()
            }
        }
    }
    
    suspend fun findOrCreatePerson(
        firstName: String,
        lastName: String,
        contactNumber: String,
        address: String? = null,
        personType: String = "Complainant"
    ): Long {
        // Check if person exists
        val existing = repository.findExistingPerson(firstName, lastName, contactNumber)
        
        return if (existing != null) {
            // Update person type if needed
            if (!existing.personType.contains(personType)) {
                val updatedType = if (existing.personType == "Multiple") {
                    existing.personType
                } else {
                    "Multiple" // Person has multiple roles
                }
                repository.updatePerson(existing.copy(personType = updatedType))
            }
            existing.id.toLong()
        } else {
            // Create new person
            val newPerson = Person(
                firstName = firstName,
                lastName = lastName,
                contactNumber = contactNumber,
                address = address,
                personType = personType
            )
            repository.insertPerson(newPerson)
        }
    }
    
    suspend fun logPersonActivity(
        personId: Int,
        blotterReportId: Int?,
        activityType: String,
        description: String,
        performedByPersonId: Int? = null
    ) {
        val history = PersonHistory(
            personId = personId,
            blotterReportId = blotterReportId,
            activityType = activityType,
            description = description,
            performedByPersonId = performedByPersonId
        )
        
        // Save to local database
        repository.insertHistory(history)
        
        // Sync to cloud
        viewModelScope.launch {
            try {
                Log.d("PersonViewModel", "📤 Syncing person history to cloud...")
                val result = apiRepository.createPersonHistory(history)
                if (result.isSuccess) {
                    Log.d("PersonViewModel", "✅ Person history synced to cloud!")
                } else {
                    Log.e("PersonViewModel", "❌ Failed to sync person history to cloud")
                }
            } catch (e: Exception) {
                Log.e("PersonViewModel", "❌ Error syncing person history: ${e.message}", e)
            }
        }
    }
    
    fun getPersonHistory(personId: Int): Flow<List<PersonHistory>> {
        // Sync from cloud first
        viewModelScope.launch {
            try {
                Log.d("PersonViewModel", "🔄 Syncing person history from cloud...")
                val result = apiRepository.getPersonHistoryFromCloud(personId)
                if (result.isSuccess) {
                    val cloudHistory = result.getOrNull() ?: emptyList()
                    // Save to local database
                    cloudHistory.forEach { history ->
                        repository.insertHistory(history)
                    }
                    Log.d("PersonViewModel", "✅ Synced ${cloudHistory.size} history records from cloud")
                } else {
                    Log.e("PersonViewModel", "❌ Failed to sync person history from cloud")
                }
            } catch (e: Exception) {
                Log.e("PersonViewModel", "❌ Error syncing person history: ${e.message}", e)
            }
        }
        
        // Return local database flow
        return repository.getHistoryByPersonId(personId)
    }
    
    fun getPersonByIdFlow(personId: Int): Flow<Person?> {
        return repository.getPersonByIdFlow(personId)
    }
    
    fun getHistoryByPersonId(personId: Int): Flow<List<PersonHistory>> {
        return repository.getHistoryByPersonId(personId)
    }
    
    suspend fun getPersonInvolvementSummary(personId: Int): PersonInvolvementSummary {
        val asComplainant = repository.getHistoryCountByPersonIdAndType(personId, "FILED_REPORT")
        val asRespondent = repository.getHistoryCountByPersonIdAndType(personId, "ADDED_AS_RESPONDENT")
        val asWitness = repository.getHistoryCountByPersonIdAndType(personId, "ADDED_AS_WITNESS")
        val asSuspect = repository.getHistoryCountByPersonIdAndType(personId, "ELEVATED_TO_SUSPECT")
        
        return PersonInvolvementSummary(
            asComplainant = asComplainant,
            asRespondent = asRespondent,
            asWitness = asWitness,
            asSuspect = asSuspect
        )
    }
}

data class PersonInvolvementSummary(
    val asComplainant: Int = 0,
    val asRespondent: Int = 0,
    val asWitness: Int = 0,
    val asSuspect: Int = 0
)

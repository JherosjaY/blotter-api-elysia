package com.example.blottermanagementsystem.data.dao

import androidx.room.*
import com.example.blottermanagementsystem.data.entity.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM person WHERE isActive = 1 ORDER BY lastName ASC, firstName ASC")
    fun getAllActivePerson(): Flow<List<Person>>
    
    @Query("SELECT * FROM person WHERE id = :personId")
    suspend fun getPersonById(personId: Int): Person?
    
    @Query("SELECT * FROM person WHERE id = :personId")
    fun getPersonByIdFlow(personId: Int): Flow<Person?>
    
    @Query("SELECT * FROM person WHERE contactNumber = :contactNumber AND isActive = 1 LIMIT 1")
    suspend fun getPersonByContactNumber(contactNumber: String): Person?
    
    @Query("SELECT * FROM person WHERE LOWER(firstName || ' ' || lastName) LIKE '%' || LOWER(:searchQuery) || '%' AND isActive = 1")
    fun searchPerson(searchQuery: String): Flow<List<Person>>
    
    @Query("SELECT * FROM person WHERE personType LIKE '%' || :personType || '%' AND isActive = 1")
    fun getPersonByType(personType: String): Flow<List<Person>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long
    
    @Update
    suspend fun updatePerson(person: Person)
    
    @Delete
    suspend fun deletePerson(person: Person)
    
    @Query("UPDATE person SET isActive = 0 WHERE id = :personId")
    suspend fun deactivatePerson(personId: Int)
    
    @Query("SELECT COUNT(*) FROM person WHERE isActive = 1")
    suspend fun getActivePersonCount(): Int
    
    // Check if person exists by name and contact
    @Query("SELECT * FROM person WHERE LOWER(firstName) = LOWER(:firstName) AND LOWER(lastName) = LOWER(:lastName) AND contactNumber = :contactNumber AND isActive = 1 LIMIT 1")
    suspend fun findExistingPerson(firstName: String, lastName: String, contactNumber: String): Person?
}

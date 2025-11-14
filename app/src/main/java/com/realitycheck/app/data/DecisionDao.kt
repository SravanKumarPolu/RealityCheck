package com.realitycheck.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decisions ORDER BY createdAt DESC")
    fun getAllDecisions(): Flow<List<Decision>>
    
    @Query("SELECT * FROM decisions WHERE id = :id")
    suspend fun getDecisionById(id: Long): Decision?
    
    @Query("SELECT * FROM decisions WHERE outcome IS NOT NULL ORDER BY createdAt DESC")
    fun getCompletedDecisions(): Flow<List<Decision>>
    
    @Query("SELECT AVG(CASE WHEN outcome IS NOT NULL AND prediction IS NOT NULL THEN 1 ELSE 0 END) * 100 FROM decisions")
    fun getCompletionRate(): Flow<Float>
    
    @Query("SELECT * FROM decisions WHERE category = :category ORDER BY createdAt DESC")
    fun getDecisionsByCategory(category: String): Flow<List<Decision>>
    
    @Query("SELECT DISTINCT category FROM decisions WHERE category IS NOT NULL")
    suspend fun getAllCategories(): List<String>
    
    @Query("SELECT DISTINCT tags FROM decisions WHERE tags IS NOT NULL AND tags != ''")
    suspend fun getAllTags(): List<String>
    
    @Insert
    suspend fun insertDecision(decision: Decision): Long
    
    @Update
    suspend fun updateDecision(decision: Decision)
    
    @Delete
    suspend fun deleteDecision(decision: Decision)
    
    @Query("DELETE FROM decisions")
    suspend fun deleteAll()
}


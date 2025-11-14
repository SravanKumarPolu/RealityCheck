package com.realitycheck.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionGroupDao {
    @Query("SELECT * FROM decision_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<DecisionGroup>>
    
    @Query("SELECT * FROM decision_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): DecisionGroup?
    
    @Insert
    suspend fun insertGroup(group: DecisionGroup): Long
    
    @Update
    suspend fun updateGroup(group: DecisionGroup)
    
    @Delete
    suspend fun deleteGroup(group: DecisionGroup)
    
    @Query("DELETE FROM decision_groups WHERE id = :id")
    suspend fun deleteGroupById(id: Long)
    
    @Query("SELECT COUNT(*) FROM decisions WHERE groupId = :groupId")
    suspend fun getDecisionCountForGroup(groupId: Long): Int
}


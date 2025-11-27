package com.realitycheck.app.data

import android.content.Context

object DatabaseProvider {
    private var database: DecisionDatabase? = null
    
    fun init(context: Context) {
        if (database == null) {
            database = DecisionDatabase.getDatabase(context)
        }
    }
    
    fun getDatabase(): DecisionDatabase {
        return database ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }
    
    fun getRepository(): DecisionRepository {
        val db = getDatabase()
        return DecisionRepository(db.decisionDao(), db.decisionGroupDao())
    }
}


package com.realitycheck.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

@TypeConverters(DateConverters::class, TagsConverter::class)
@Database(entities = [Decision::class], version = 4, exportSchema = true)
abstract class DecisionDatabase : RoomDatabase() {
    abstract fun decisionDao(): DecisionDao
    
    companion object {
        @Volatile
        private var INSTANCE: DecisionDatabase? = null
        
        /**
         * Migration from version 1 to 2
         * Adds quantitative prediction fields if they don't exist
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns if they don't exist (safe migration)
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN predictedEnergy24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN predictedMood24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN predictedStress24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN predictedRegretChance24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN predictedOverallImpact7d REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN predictionConfidence REAL
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 2 to 3
         * Adds quantitative outcome fields and follow decision field
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add outcome columns if they don't exist
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN actualEnergy24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN actualMood24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN actualStress24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN actualRegret24h REAL
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN followedDecision INTEGER
                """.trimIndent())
                
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN outcomeRecordedAt INTEGER
                """.trimIndent())
            }
        }
        
        /**
         * Migration from version 3 to 4
         * Adds tags field for better organization
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE decisions 
                    ADD COLUMN tags TEXT
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): DecisionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DecisionDatabase::class.java,
                    "decision_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}


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
@Database(entities = [Decision::class, DecisionGroup::class], version = 5, exportSchema = true)
abstract class DecisionDatabase : RoomDatabase() {
    abstract fun decisionDao(): DecisionDao
    abstract fun decisionGroupDao(): DecisionGroupDao
    
    companion object {
        @Volatile
        private var INSTANCE: DecisionDatabase? = null
        
        /**
         * Migration from version 1 to 2
         * Adds quantitative prediction fields if they don't exist
         * 
         * Schema changes:
         * - predictedEnergy24h: REAL (nullable) - Energy prediction for next 24h (-5 to +5)
         * - predictedMood24h: REAL (nullable) - Mood prediction for next 24h (-5 to +5)
         * - predictedStress24h: REAL (nullable) - Stress prediction for next 24h (-5 to +5)
         * - predictedRegretChance24h: REAL (nullable) - Regret chance prediction (-5 to +5)
         * - predictedOverallImpact7d: REAL (nullable) - Overall impact prediction for 7 days (-5 to +5)
         * - predictionConfidence: REAL (nullable) - Confidence level (0-100)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Add new columns - SQLite will fail if column already exists
                    // Room manages this, but we add try-catch for safety
                    addColumnIfNotExists(database, "decisions", "predictedEnergy24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "predictedMood24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "predictedStress24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "predictedRegretChance24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "predictedOverallImpact7d", "REAL")
                    addColumnIfNotExists(database, "decisions", "predictionConfidence", "REAL")
                } catch (e: Exception) {
                    // Log error in production - migrations should not fail silently
                    // If this fails, the app will crash on database access, which is better than silent data corruption
                    throw IllegalStateException("Migration 1->2 failed: ${e.message}", e)
                }
            }
        }
        
        /**
         * Migration from version 2 to 3
         * Adds quantitative outcome fields and follow decision field
         * 
         * Schema changes:
         * - actualEnergy24h: REAL (nullable) - Actual energy outcome (-5 to +5)
         * - actualMood24h: REAL (nullable) - Actual mood outcome (-5 to +5)
         * - actualStress24h: REAL (nullable) - Actual stress outcome (-5 to +5)
         * - actualRegret24h: REAL (nullable) - Actual regret outcome (0-10)
         * - followedDecision: INTEGER (nullable) - Whether user followed the decision (0/1/null)
         * - outcomeRecordedAt: INTEGER (nullable) - Timestamp when outcome was recorded
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Add outcome columns
                    addColumnIfNotExists(database, "decisions", "actualEnergy24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "actualMood24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "actualStress24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "actualRegret24h", "REAL")
                    addColumnIfNotExists(database, "decisions", "followedDecision", "INTEGER")
                    addColumnIfNotExists(database, "decisions", "outcomeRecordedAt", "INTEGER")
                } catch (e: Exception) {
                    throw IllegalStateException("Migration 2->3 failed: ${e.message}", e)
                }
            }
        }
        
        /**
         * Migration from version 3 to 4
         * Adds tags field for better organization
         * 
         * Schema changes:
         * - tags: TEXT (nullable) - Comma-separated list of tags for categorization
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    addColumnIfNotExists(database, "decisions", "tags", "TEXT")
                } catch (e: Exception) {
                    throw IllegalStateException("Migration 3->4 failed: ${e.message}", e)
                }
            }
        }
        
        /**
         * Migration from version 4 to 5
         * Adds decision groups feature
         * 
         * Schema changes:
         * - Creates decision_groups table
         * - Adds groupId column to decisions table
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Create decision_groups table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS decision_groups (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            description TEXT NOT NULL,
                            color TEXT NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL
                        )
                    """.trimIndent())
                    
                    // Add groupId column to decisions table
                    addColumnIfNotExists(database, "decisions", "groupId", "INTEGER")
                    
                    // Create index for faster queries
                    database.execSQL("""
                        CREATE INDEX IF NOT EXISTS index_decisions_groupId 
                        ON decisions(groupId)
                    """.trimIndent())
                    
                    // Insert default groups
                    val now = System.currentTimeMillis()
                    database.execSQL("""
                        INSERT OR IGNORE INTO decision_groups (id, name, description, color, createdAt, updatedAt)
                        VALUES 
                        (1, 'Personal', 'Personal life decisions', '#6C5CE7', $now, $now),
                        (2, 'Work', 'Work and career decisions', '#00B894', $now, $now),
                        (3, 'Health', 'Health and fitness decisions', '#FD79A8', $now, $now)
                    """.trimIndent())
                } catch (e: Exception) {
                    throw IllegalStateException("Migration 4->5 failed: ${e.message}", e)
                }
            }
        }
        
        /**
         * Helper function to safely add a column if it doesn't exist
         * SQLite doesn't support IF NOT EXISTS for ALTER TABLE ADD COLUMN,
         * so we check the schema first
         */
        private fun addColumnIfNotExists(
            database: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnType: String
        ) {
            // Check if column already exists by querying the schema
            val cursor = database.query(
                "PRAGMA table_info($tableName)",
                emptyArray<String>()
            )
            
            var columnExists = false
            try {
                while (cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex >= 0) {
                        val name = cursor.getString(nameIndex)
                        if (name == columnName) {
                            columnExists = true
                            break
                        }
                    }
                }
            } finally {
                cursor.close()
            }
            
            // Only add column if it doesn't exist
            if (!columnExists) {
                database.execSQL("""
                    ALTER TABLE $tableName 
                    ADD COLUMN $columnName $columnType
                """.trimIndent())
            }
        }
        
        /**
         * Get or create the database instance
         * 
         * Migration strategy:
         * - All migrations are explicitly defined and added to the builder
         * - No fallbackToDestructiveMigration() - prevents data loss
         * - If a migration path doesn't exist, the app will fail at runtime
         *   (better than silently destroying user data)
         * 
         * Migration path:
         * - Version 1 -> 2: MIGRATION_1_2 (adds prediction fields)
         * - Version 2 -> 3: MIGRATION_2_3 (adds outcome fields)
         * - Version 3 -> 4: MIGRATION_3_4 (adds tags field)
         * 
         * For future migrations:
         * - Increment database version in @Database annotation
         * - Create new Migration class (e.g., MIGRATION_4_5)
         * - Add to .addMigrations() call below
         */
        fun getDatabase(context: Context): DecisionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DecisionDatabase::class.java,
                    "decision_database"
                )
                    // Add all migrations explicitly - prevents data loss
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                        // Add future migrations here, e.g.:
                        // MIGRATION_4_5,
                        // MIGRATION_5_6
                    )
                    // DO NOT use fallbackToDestructiveMigration() - it causes data loss
                    // If a migration is missing, the app will fail at startup
                    // This is intentional - prevents silent data corruption
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


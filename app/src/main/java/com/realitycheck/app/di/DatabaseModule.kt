package com.realitycheck.app.di

import android.content.Context
import androidx.room.Room
import com.realitycheck.app.data.DecisionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DecisionDatabase {
        return DecisionDatabase.getDatabase(context)
    }

    @Provides
    fun provideDecisionDao(database: DecisionDatabase) = database.decisionDao()
}


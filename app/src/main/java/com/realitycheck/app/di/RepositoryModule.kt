package com.realitycheck.app.di

import com.realitycheck.app.data.DecisionDao
import com.realitycheck.app.data.DecisionGroupDao
import com.realitycheck.app.data.DecisionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDecisionRepository(
        decisionDao: DecisionDao,
        decisionGroupDao: DecisionGroupDao
    ): DecisionRepository {
        return DecisionRepository(decisionDao, decisionGroupDao)
    }
}



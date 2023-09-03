package com.example.multicastndsapplication

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The hilt app module
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModules {

    @Singleton
    @Provides
    fun provideRepository(nsdHelper: NSDHelper): Repository {
        return Repository(nsdHelper)
    }

    @Singleton
    @Provides
    fun provideNSDHelper(@ApplicationContext context: Context): NSDHelper {
        return NSDHelper(context)
    }
}
package com.eywa.projectclava.main.common

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {
    @Singleton
    @Provides
    fun provideClavaMediaPlayer(
        @ApplicationContext context: Context,
    ): ClavaMediaPlayer = ClavaMediaPlayerImpl(context)

    @Singleton
    @Provides
    fun provideClavaNotifications(
        @ApplicationContext context: Context,
    ): ClavaNotifications = ClavaNotifications(context)
}

package com.eywa.projectclava.main.common

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object UtilsModule {
    @SingleIn(AppScope::class)
    @Provides
    fun provideClavaMediaPlayer(
        context: Context,
    ): ClavaMediaPlayer = ClavaMediaPlayerImpl(context)

    @SingleIn(AppScope::class)
    @Provides
    fun provideClavaNotifications(
        context: Context,
    ): ClavaNotifications = ClavaNotifications(context)
}

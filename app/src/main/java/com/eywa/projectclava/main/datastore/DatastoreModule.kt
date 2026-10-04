package com.eywa.projectclava.main.datastore

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object DatastoreModule {
    @SingleIn(AppScope::class)
    @Provides
    fun provideClavaDatastore(
            context: Context
    ): ClavaDatastore = ClavaDatastore(context)
}

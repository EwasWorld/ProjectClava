package com.eywa.projectclava.main.database

import android.content.Context
import androidx.room.Room
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object DatabaseModule {
    @SingleIn(AppScope::class)
    @Provides
    fun provideClavaDatabase(
            context: Context
    ): ClavaDatabase = Room
            .databaseBuilder(
                    context,
                    ClavaDatabase::class.java, ClavaDatabase.DATABASE_NAME
            )
            .addMigrations(ClavaDatabase.Migrations.MIGRATION_2_3)
            .build()
}

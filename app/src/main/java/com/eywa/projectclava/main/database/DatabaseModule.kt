package com.eywa.projectclava.main.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideClavaDatabase(
            @ApplicationContext context: Context
    ): ClavaDatabase = Room
            .databaseBuilder(
                    context,
                    ClavaDatabase::class.java, ClavaDatabase.DATABASE_NAME
            )
            .addMigrations(ClavaDatabase.Migrations.MIGRATION_2_3)
            .build()
}

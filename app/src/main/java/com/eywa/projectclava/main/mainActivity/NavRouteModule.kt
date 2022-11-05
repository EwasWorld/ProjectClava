package com.eywa.projectclava.main.mainActivity

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey


@InstallIn(SingletonComponent::class)
@Module
object NavRouteModule {
    @Provides
    @IntoMap
    @StringKey("main")
    fun provideMainNavRoutes(): Array<out NavRoute> = MainNavRoute.values()

    @Provides
    fun provideNavRoutes(
            availableNavRoutes: Map<String, @JvmSuppressWildcards Array<out NavRoute>?>
    ): Array<out NavRoute> = availableNavRoutes.flatMap { it.value?.toList() ?: listOf() }.toTypedArray()
}
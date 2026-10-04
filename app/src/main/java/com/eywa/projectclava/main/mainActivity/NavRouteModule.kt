package com.eywa.projectclava.main.mainActivity

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.StringKey


@BindingContainer
@ContributesTo(AppScope::class)
object NavRouteModule {
    @Provides
    @IntoMap
    @StringKey("main")
    fun provideMainNavRoutes(): Array<out NavRoute> = MainNavRoute.entries.toTypedArray()

    @Provides
    fun provideNavRoutes(
            availableNavRoutes: Map<String, Array<out NavRoute>>
    ): Array<out NavRoute> = availableNavRoutes.flatMap { it.value.toList() }.toTypedArray()
}

package com.eywa.projectclava.mainActivity

import com.eywa.projectclava.main.mainActivity.NavRoute
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.StringKey

@BindingContainer
@ContributesTo(AppScope::class)
object DebugNavRouteModule {
    @Provides
    @IntoMap
    @StringKey("debug")
    fun provideDebugNavRoutes(): Array<out NavRoute> = DebugNavRoute.values()
}

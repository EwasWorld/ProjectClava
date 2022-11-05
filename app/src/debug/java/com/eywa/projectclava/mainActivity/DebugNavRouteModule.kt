package com.eywa.projectclava.mainActivity

import com.eywa.projectclava.main.mainActivity.NavRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
@InstallIn(SingletonComponent::class)
object DebugNavRouteModule {
    @Provides
    @IntoMap
    @StringKey("debug")
    fun provideDebugNavRoutes(): Array<out NavRoute> = DebugNavRoute.values()
}
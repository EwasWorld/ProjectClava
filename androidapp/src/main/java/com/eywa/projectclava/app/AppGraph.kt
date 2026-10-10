package com.eywa.projectclava.app

import android.content.Context
import com.eywa.projectclava.main.app.ClavaGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AppGraph : ClavaGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides applicationContext: Context): AppGraph
    }
}

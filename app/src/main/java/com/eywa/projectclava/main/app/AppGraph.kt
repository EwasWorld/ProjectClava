package com.eywa.projectclava.main.app

import android.app.Activity
import android.content.Context
import com.eywa.projectclava.main.mainActivity.MainActivity
import com.eywa.projectclava.main.mainActivity.viewModel.MainViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
interface AppGraph {
    val mainViewModel: MainViewModel

    fun inject(activity: MainActivity)

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides applicationContext: Context): AppGraph
    }
}

val Activity.appGraph: AppGraph
    get() = (application as ClavaApplication).appGraph
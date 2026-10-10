package com.eywa.projectclava.main.app

import android.app.Activity
import com.eywa.projectclava.main.mainActivity.MainActivity
import com.eywa.projectclava.main.mainActivity.viewModel.MainViewModel

/**
 * What this module needs from the dependency graph.
 * The concrete `@DependencyGraph` lives in the application module so that it can aggregate all contributions.
 */
interface ClavaGraph {
    val mainViewModel: MainViewModel

    fun inject(activity: MainActivity)
}

/**
 * Implemented by the [android.app.Application]
 */
interface ClavaGraphOwner {
    val clavaGraph: ClavaGraph
}

val Activity.appGraph: ClavaGraph
    get() = (application as ClavaGraphOwner).clavaGraph

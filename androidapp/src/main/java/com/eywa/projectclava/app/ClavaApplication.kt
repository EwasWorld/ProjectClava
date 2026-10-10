package com.eywa.projectclava.app

import android.app.Application
import com.eywa.projectclava.main.app.ClavaGraph
import com.eywa.projectclava.main.app.ClavaGraphOwner
import dev.zacsweers.metro.createGraphFactory

class ClavaApplication : Application(), ClavaGraphOwner {
    override val clavaGraph: ClavaGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }
}

package com.eywa.projectclava.main.app

import android.app.Application
import dev.zacsweers.metro.createGraphFactory

class ClavaApplication : Application() {
    val appGraph: AppGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }
}
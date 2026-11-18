package com.lafamilia.app

import android.app.Application

/**
 * Application entry point. It runs once when the process starts, before any Activity,
 * so it is the right place to build the objects the whole app shares (see [AppGraph]).
 */
class LaFamiliaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.init(this)
    }
}

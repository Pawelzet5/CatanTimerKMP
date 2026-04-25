package io.github.pawelzielinski.catantimer

import android.app.Application
import io.github.pawelzielinski.catantimer.di.initKoin
import org.koin.android.ext.koin.androidContext

class CatanTimerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CatanTimerApplication)
        }
    }
}
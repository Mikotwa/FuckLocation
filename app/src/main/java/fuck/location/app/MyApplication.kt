package fuck.location.app

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    companion object {
        lateinit var appContext: Context
        val isModuleActivated = false
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
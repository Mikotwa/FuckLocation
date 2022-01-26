package fuck.location.xposed.helpers

import android.app.AndroidAppHelper
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import de.robv.android.xposed.XposedBridge

class ServiceHelper {
    lateinit var fuckLocationServiceConnection: FuckLocationServiceConnection
    var serviceStarted = false

    fun startService(): IBinder {
        XposedBridge.log("FL: in startService")
        val intent = Intent(AndroidAppHelper.currentApplication().applicationContext, FuckLocationService::class.java)

        if (!this::fuckLocationServiceConnection.isInitialized) {
            fuckLocationServiceConnection = FuckLocationServiceConnection()
        }
        AndroidAppHelper.currentApplication().bindService(intent, fuckLocationServiceConnection, Context.BIND_AUTO_CREATE)
        serviceStarted = true

        XposedBridge.log("FL: Over in startService")
        return fuckLocationServiceConnection.returnIBinder()
    }

    fun stopService() {
        XposedBridge.log("FL: in stopService")
        AndroidAppHelper.currentApplication().unbindService(fuckLocationServiceConnection)
        serviceStarted = false

        XposedBridge.log("FL: Over in stopService")
    }

    fun isServiceStarted(): Boolean {
        return serviceStarted
    }
}
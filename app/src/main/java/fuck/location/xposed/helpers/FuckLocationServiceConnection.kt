package fuck.location.xposed.helpers

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import de.robv.android.xposed.XposedBridge
import fuck.location.IFuckLocationManager

class FuckLocationServiceConnection : ServiceConnection {
    lateinit var iFuckLocationManager : IFuckLocationManager
    lateinit var iBinder : IBinder

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        XposedBridge.log("FL: onServiceConnected!")
        iFuckLocationManager = IFuckLocationManager.Stub.asInterface(p1)
        if (p1 != null) {
            iBinder = p1
        }
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        XposedBridge.log("FL: onServiceDisconnected!")
    }

    fun returnIBinder(): IBinder {
        return iBinder
    }
}
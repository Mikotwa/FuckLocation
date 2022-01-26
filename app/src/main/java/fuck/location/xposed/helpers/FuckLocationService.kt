package fuck.location.xposed.helpers

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import fuck.location.IFuckLocationManager

class FuckLocationService : Service() {
    lateinit var binder: FuckBinder

    override fun onBind(p0: Intent?): IBinder? {
        if (this::binder.isInitialized) return binder
        binder = FuckBinder()
        return binder
    }

}
package fuck.location.xposed.helpers

import android.app.Service
import android.content.Intent
import android.os.IBinder

class FuckLocationService : Service() {
    lateinit var binder: FuckBinder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        if (this::binder.isInitialized) return binder
        binder = FuckBinder()
        return binder
    }
}
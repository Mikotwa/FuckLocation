package fuck.location.xposed.helpers

import fuck.location.IFuckLocationManager

class FuckBinder : IFuckLocationManager.Stub() {
    override fun inWhiteList(packageName: String?): Boolean {
        return false
    }
}
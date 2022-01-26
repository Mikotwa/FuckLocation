package fuck.location.xposed.helpers

import fuck.location.IFuckLocationManager

//TODO: Implement real service
class FuckBinder : IFuckLocationManager.Stub() {
    var bool = false

    override fun inWhiteList(packageName: String?): Boolean {
        return bool
    }

    override fun setWhiteList(packageName: String?) {
        bool = !bool
    }
}
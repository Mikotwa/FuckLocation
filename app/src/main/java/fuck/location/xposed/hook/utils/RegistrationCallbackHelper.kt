package io.github.mikotwa.yucklocation.hook.utils

import fuck.location.xposed.hook.data.FakeLocationListenerRegistration
import org.lsposed.hiddenapibypass.HiddenApiBypass

class RegistrationCallbackHelper private constructor(){
    private val mRegistrations = mutableListOf<FakeLocationListenerRegistration>()

    companion object {
        private var instance: RegistrationCallbackHelper? = null
            get() {
                if (field == null) {
                    field = RegistrationCallbackHelper()
                }
                return field
            }

        fun get(): RegistrationCallbackHelper {
            return instance!!
        }
    }

    fun registerListener(listener: FakeLocationListenerRegistration) {
        mRegistrations.add(listener)
    }

    fun unregisterListener(listener: Any){
        lateinit var target: Any

        mRegistrations.clear()  //todo: 更好的清理方法
    }

    fun updateLocation() {
        mRegistrations.forEach { register ->
            HiddenApiBypass.invoke(register.locationListener!!.javaClass, register.locationListener, "onLocationChanged", listOf(LocationHelper().generateMockedLocation(null)), null)
        }
    }
}
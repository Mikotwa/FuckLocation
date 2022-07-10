package io.github.mikotwa.yucklocation.hook.utils

import io.github.mikotwa.yucklocation.hook.data.FakeLocationListenerRegistration

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

    fun updateLocation() {
        mRegistrations.forEach { register ->
            register.locationListener.onLocationChanged(LocationHelper().generateMockedLocation(null))
        }
    }
}
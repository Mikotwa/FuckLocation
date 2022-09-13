package fuck.location.xposed.hook.utils

import fuck.location.xposed.hook.data.*
import org.lsposed.hiddenapibypass.HiddenApiBypass

object GnssListenerHelper {
    private val mStatusRegistrations = mutableListOf<FakeGnssStatusRegistration>()
    private val mNmeaRegistrations = mutableListOf<FakeNmeaRegistration>()
    private val mGnssMeasurementRegistrations = mutableListOf<FakeGnssMeasurementRegistration>()
    private val mGnssNavigationRegistrations = mutableListOf<FakeGnssNavigationRegistration>()
    private val mGnssAntennaInfoRegistrations = mutableListOf<FakeGnssAntennaRegistration>()

    fun registerStatusListener(listener: FakeGnssStatusRegistration) {
        mStatusRegistrations.add(listener)
    }

    fun unregisterStatusListener(listener: FakeGnssStatusRegistration) {
        mStatusRegistrations.clear()
    }

    fun registerNmeaListener(listener: FakeNmeaRegistration) {
        mNmeaRegistrations.add(listener)
    }

    fun unregisterNmeaListener(listener: FakeNmeaRegistration) {
        mNmeaRegistrations.clear()
    }

    fun registerGnssMeasurementListener(listener: FakeGnssMeasurementRegistration) {
        mGnssMeasurementRegistrations.add(listener)
    }

    fun unregisterGnssMeasurementListener(listener: FakeGnssMeasurementRegistration) {
        mGnssMeasurementRegistrations.clear()
    }

    fun registerGnssNavigationListener(listener: FakeGnssNavigationRegistration) {
        mGnssNavigationRegistrations.add(listener)
    }

    fun unregisterGnssNavigationListener(listener: FakeGnssNavigationRegistration) {
        mGnssNavigationRegistrations.clear()
    }

    fun registerGnssAntennaInfoListener(listener: FakeGnssAntennaRegistration) {
        mGnssAntennaInfoRegistrations.add(listener)
    }

    fun unregisterGnssAntennaInfoListener(listener: FakeGnssAntennaRegistration) {
        mGnssAntennaInfoRegistrations.clear()
    }

    fun updateStatus() {
        mStatusRegistrations.forEach { register ->
            HiddenApiBypass.invoke(register.listener!!.javaClass, register.listener, "onSatelliteStatusChanged", null)
        }
    }
}
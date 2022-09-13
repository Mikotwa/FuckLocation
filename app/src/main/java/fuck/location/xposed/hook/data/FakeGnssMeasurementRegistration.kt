package fuck.location.xposed.hook.data

import android.location.GnssMeasurementRequest

class FakeGnssMeasurementRegistration (
    val gnssMeasurementRequest: GnssMeasurementRequest,
    val packageName: String,
    val listener: Any?
    ) {

}
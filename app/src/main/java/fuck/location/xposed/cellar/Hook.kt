package fuck.location.xposed.cellar

import android.os.Build
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.legacy.PhoneInterfaceManagerHooker

class Hook {
    @ExperimentalStdlibApi
    fun hookAllNetTypeR(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PhoneInterfaceManagerHooker().HookCellLocation(lpparam)   // TODO: Read config file
        }
    }
}
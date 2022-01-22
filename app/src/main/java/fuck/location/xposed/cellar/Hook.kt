package fuck.location.xposed.cellar

import de.robv.android.xposed.callbacks.XC_LoadPackage
import fuck.location.xposed.cellar.legacy.PhoneInterfaceManagerHooker
import fuck.location.xposed.cellar.r.*

class Hook {
    fun hookAllNetTypeR(lpparam: XC_LoadPackage.LoadPackageParam) {
        /*Cdma().HookCellIdentity(lpparam)
        Gsm().HookCellIdentity(lpparam)
        Lte().HookCellIdentity(lpparam)
        Nr().HookCellIdentity(lpparam)
        Tdscdma().HookCellIdentity(lpparam)
        Wcdma().HookCellIdentity(lpparam)*/

        PhoneInterfaceManagerHooker().HookCellLocation(lpparam)
    }
}
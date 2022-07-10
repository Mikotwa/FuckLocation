package io.github.mikotwa.yucklocation.hook.location

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.java.IntType
import io.github.mikotwa.yucklocation.hook.utils.PackageNameHelper

class LocationHooker : YukiBaseHooker() {
    private val LocationProvider = VariousClass(
        "com.android.server.location.provider.LocationProviderManager"
    )

    private fun lastLocation() {
        // 一些 param 里的 class
        val LastLocationRequestClass = VariousClass(
            "android.location.LastLocationRequest"
        )
        
        val CallerIdentityClass = VariousClass(
            "android.location.util.identity.CallerIdentity"
        )

        LocationProvider.hook {
            injectMember {
                method {
                    name = "getLastLocation"
                    param(LastLocationRequestClass, CallerIdentityClass, IntType)
                }
                afterHook {
                    val packageName = PackageNameHelper().callerIdentityToPackageName(args[1])
                    val location = result
                }
            }
        }
    }

    override fun onHook() = YukiHookAPI.encase {
        loggerD(msg = "Hello!")

        lastLocation()
    }
}
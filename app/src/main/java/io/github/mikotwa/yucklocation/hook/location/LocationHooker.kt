package io.github.mikotwa.yucklocation.hook.location

import android.location.Location
import android.location.LocationManager
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.java.IntType
import io.github.mikotwa.yucklocation.hook.utils.LocationHelper
import io.github.mikotwa.yucklocation.hook.utils.PackageNameHelper

class LocationHooker : YukiBaseHooker() {
    private val demoName = "com.borneq.heregpslocation"  // 目前，我们在 demo 里硬编码这个作用域，来测试代码效果

    private val LocationProvider = VariousClass(
        "com.android.server.location.provider.LocationProviderManager"
    )

    private fun lastLocation() {
        // 一些 param 里的 class
        val LastLocationRequestClass = VariousClass(
            "android.location.LastLocationRequest"
        )

        val LocationRequestClass = VariousClass(
            "android.location.LocationRequest"
        )

        val ILocationCallbackClass = VariousClass(
            "android.location.ILocationCallback"
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

                    loggerD(msg = "in getLastLocation! Caller package name: $packageName")

                    if (true) {
                        loggerD(msg = "$packageName is in scoop! Return custom location")

                        result = LocationHelper().generateMockedLocation(result)
                    }
                }
            }
        }

        LocationProvider.hook {
            injectMember {
                method {
                    name = "getCurrentLocation"
                    param(LocationRequestClass, CallerIdentityClass, IntType, ILocationCallbackClass)
                }
                afterHook {
                    val packageName = PackageNameHelper().callerIdentityToPackageName(args[1])

                    loggerD(msg = "in getCurrentLocation! Caller package name: $packageName")

                    if (true) {
                        loggerD(msg = "$packageName is in scoop! Return null")

                        result = null
                    }
                }
            }
        }
    }

    override fun onHook() = YukiHookAPI.encase {
        loggerD(msg = "Hello!")

        lastLocation()
    }
}
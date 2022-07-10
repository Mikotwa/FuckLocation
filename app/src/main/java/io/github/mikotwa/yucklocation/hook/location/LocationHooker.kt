package io.github.mikotwa.yucklocation.hook.location

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringType

import io.github.mikotwa.yucklocation.hook.utils.ConfigHelper
import io.github.mikotwa.yucklocation.hook.utils.LocationHelper
import io.github.mikotwa.yucklocation.hook.utils.PackageNameHelper

class LocationHooker : YukiBaseHooker() {
    private val LocationProvider = VariousClass(
        "com.android.server.location.provider.LocationProviderManager"
    )
    
    private val LocationManagerService = VariousClass(
        "com.android.server.location.LocationManagerService"
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

        val IGnssStatusListener = VariousClass(
            "android.location.IGnssStatusListener"
        )

        val IGnssNmeaListener = VariousClass(
            "android.location.IGnssNmeaListener"
        )

        val GeofenceClass = VariousClass(
            "android.location.Geofence"
        )

        val PendingIntentClass = VariousClass(
            "android.app.PendingIntent"
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

                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "$packageName is in scope! Return custom location")

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

                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "$packageName is in scope! Return null")

                        result = null
                    }
                }
            }
        }

        LocationManagerService.hook {
            injectMember {
                method {
                    name = "getLastLocation"
                    param(StringType, LastLocationRequestClass, StringType, StringType)
                }
                afterHook {
                    val packageName = args[2] as String

                    loggerD(msg = "in getLastLocation (LocationManagerService)! Caller package name: $packageName")

                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "$packageName is in scope! Return custom location")

                        result = LocationHelper().generateMockedLocation(result)
                    }
                }
            }
        }

        LocationManagerService.hook {
            injectMember {
                method {
                    name = "getCurrentLocation"
                    param(StringType, LocationRequestClass, ILocationCallbackClass, StringType, StringType)
                }
                afterHook {
                    val packageName = args[3] as String

                    loggerD(msg = "in getCurrentLocation (LocationManagerService)! Caller package name: $packageName")

                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "$packageName is in scope! Return null")

                        result = null
                    }
                }
            }
        }

        LocationManagerService.hook {
            injectMember {
                method {
                    name = "registerGnssStatusCallback"
                    param(IGnssStatusListener, StringType, StringType, StringType)
                }
                beforeHook {
                    val packageName = args[1] as String

                    loggerD(msg = "in registerGnssStatusCallback (LocationManagerService)! Caller package name: $packageName")

                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "$packageName is in scope! Dropping register request...")
                        result = null
                    }
                }
            }
        }

        LocationManagerService.hook {
            injectMember {
                method {
                    name = "registerGnssNmeaCallback"
                    param(IGnssNmeaListener, StringType, StringType, StringType)
                }
                beforeHook {
                    val packageName = args[1] as String

                    loggerD(msg = "in registerGnssNmeaCallback (LocationManagerService)! Caller package name: $packageName")

                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "$packageName is in scope! Dropping register request...")
                        result = null
                    }
                }
            }
        }

        LocationManagerService.hook {
            injectMember {
                method {
                    name = "requestGeofence"
                    param(GeofenceClass, PendingIntentClass, StringType, StringType)
                }
                beforeHook {
                    val packageName = args[2] as String

                    loggerD(msg = "in requestGeofence (LocationManagerService)! Caller package name: $packageName")

                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "$packageName is in scope! Dropping register request...")
                        result = null
                    }
                }
            }
        }
    }

    override fun onHook() = YukiHookAPI.encase {
        lastLocation()
    }
}
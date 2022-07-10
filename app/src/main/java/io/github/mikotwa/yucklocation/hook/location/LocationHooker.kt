package io.github.mikotwa.yucklocation.hook.location

import android.location.LocationListener
import android.location.LocationRequest
import android.os.Build
import android.util.ArrayMap
import androidx.annotation.RequiresApi
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.java.FunctionClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringType
import io.github.mikotwa.yucklocation.hook.data.FakeLocationListenerRegistration

import io.github.mikotwa.yucklocation.hook.utils.ConfigHelper
import io.github.mikotwa.yucklocation.hook.utils.LocationHelper
import io.github.mikotwa.yucklocation.hook.utils.PackageNameHelper
import io.github.mikotwa.yucklocation.hook.utils.RegistrationCallbackHelper

class LocationHooker : YukiBaseHooker() {
    private val LocationProvider = VariousClass(
        "com.android.server.location.provider.LocationProviderManager"
    )
    
    private val LocationManagerService = VariousClass(
        "com.android.server.location.LocationManagerService"
    )

    private val ListenerMultiplexer = VariousClass(
        "com.android.server.location.listeners.ListenerMultiplexer"
    )

    @RequiresApi(Build.VERSION_CODES.S)
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
        
        val ILocationListenerClass = VariousClass(
            "android.location.ILocationListener"
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

        // 让名单里的应用注册到模块自己的回调实现里
        LocationProvider.hook {
            injectMember {
                method {
                    name = "registerLocationRequest"
                    param(LocationRequestClass, CallerIdentityClass, IntType, ILocationListenerClass)
                }
                beforeHook {
                    val packageName = PackageNameHelper().callerIdentityToPackageName(args[1])

                    loggerD(msg = "in registerLocationRequest! Caller package name: $packageName")
                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                        loggerD(msg = "in the scope! Now register this to our custom implement...")

                        val fakeRegistration = FakeLocationListenerRegistration(args[0] as LocationRequest, packageName,
                            args[2] as LocationListener,
                            args[3] as Int
                        )

                        RegistrationCallbackHelper.get().registerListener(fakeRegistration)
                    }

                    result = null
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onHook() = YukiHookAPI.encase {
        lastLocation()
    }
}
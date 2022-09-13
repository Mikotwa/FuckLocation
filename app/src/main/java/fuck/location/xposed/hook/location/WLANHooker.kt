package fuck.location.xposed.hook.location

import android.net.wifi.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.classOf
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.type.java.JavaClassLoader
import com.highcapable.yukihookapi.hook.type.java.StringType
import fuck.location.xposed.hook.utils.ConfigHelper
import io.github.mikotwa.yucklocation.hook.utils.WifiHelper

object WLANHooker : YukiBaseHooker() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onHook() {
        val ServiceServiceManagerClass = VariousClass(
            "com.android.server.SystemServiceManager"
        )

        ServiceServiceManagerClass.hook {
            injectMember {
                method {
                    name = "loadClassFromLoader"
                    param(StringType, JavaClassLoader)
                }

                afterHook {
                    val serviceName = args[0] as String
                    if (serviceName == "com.android.server.wifi.WifiService") {
                        loggerD(msg = "Found WifiService! Using its classLoader to find WifiServiceImpl...")

                        val wifiClazz = result as Class<*>
                        val wifiClass = classOf("com.android.server.wifi.WifiServiceImpl", wifiClazz.classLoader)

                        wifiClass.hook(false) {
                            injectMember {
                                method {
                                    name = "getScanResults"
                                    param(StringType, StringType)
                                }
                                beforeHook {
                                    val packageName = args[0] as String
                                    loggerD(msg = "In getScanResults with caller: $packageName")

                                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                                        loggerD(msg = "In scope! Return custom WiFi information")

                                        val customResult = ScanResult()
                                        customResult.BSSID = ""
                                        customResult.SSID = "AndroidAP"
                                        customResult.capabilities = "WPA-2"
                                        customResult.level = -1

                                        val resultReturn: List<ScanResult> = listOf()
                                        result = resultReturn
                                    }
                                }
                            }
                        }

                        wifiClass.hook(false) {
                            injectMember {
                                method {
                                    name = "getConnectionInfo"
                                    param(StringType, StringType)
                                }
                                beforeHook {
                                    val packageName = args[0] as String
                                    loggerD(msg = "In getConnectionInfo with caller: $packageName")

                                    if (ConfigHelper.get().isPackageInScope(packageName)) {
                                        loggerD(msg = "In scope! Return custom WiFi information")

                                        result = WifiHelper.get().buildCustomWifiInfo()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
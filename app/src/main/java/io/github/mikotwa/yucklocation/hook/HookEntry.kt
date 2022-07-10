package io.github.mikotwa.yucklocation.hook

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.mikotwa.yucklocation.hook.location.LocationHooker

@InjectYukiHookWithXposed
class HookEntry() : IYukiHookXposedInit{
    override fun onInit() = configs {
        debugTag = "YuckLocation"
        isDebug = true
    }

    override fun onHook() = YukiHookAPI.encase{
        loadApp("android") {
            loggerD(msg = "Hooking android!")

            loadHooker(LocationHooker())
        }
    }
}
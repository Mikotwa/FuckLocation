package io.github.mikotwa.yucklocation.hook

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import io.github.mikotwa.yucklocation.hook.location.LocationHooker
import io.github.mikotwa.yucklocation.hook.utils.ConfigHelper

@InjectYukiHookWithXposed
class HookEntry() : IYukiHookXposedInit{
    override fun onInit() = configs {
        debugTag = "YuckLocation"
        isDebug = true

        ConfigHelper.get().setDataPath()
    }

    override fun onHook() = YukiHookAPI.encase{
        loadApp("android") {
            loggerD(msg = "Hooking android!")

            loadHooker(LocationHooker())
        }
    }
}
package fuck.location.xposed

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed(
    modulePackageName = "fuck.location.xposed.HookEntry"
)

class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugTag = "YuckLocation"
        isDebug = true
    }

    override fun onHook() {
        YukiHookAPI.encase {
            loadSystem {
                loggerD(msg = "hello")
            }
        }
    }
}
package fuck.location.xposed

import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import fuck.location.xposed.hook.location.LocationHooker
import fuck.location.xposed.hook.location.WLANHooker
import fuck.location.xposed.hook.utils.ConfigHelper

@InjectYukiHookWithXposed(
    modulePackageName = "fuck.location.xposed.HookEntry"
)

class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugTag = "YuckLocation"
        isDebug = true

        // 加载配置文件
        ConfigHelper.get().setDataPath()
    }

    override fun onHook() {
        YukiHookAPI.encase {
            loadSystem {
                loadHooker(LocationHooker)
                loadHooker(WLANHooker)
            }
        }
    }
}
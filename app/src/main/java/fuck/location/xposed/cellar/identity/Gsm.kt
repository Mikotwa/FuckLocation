package fuck.location.xposed.cellar.identity

import android.app.AndroidAppHelper
import android.os.Build
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.lsposed.hiddenapibypass.HiddenApiBypass

class Gsm {
    @RequiresApi(Build.VERSION_CODES.R)
    fun HookCellIdentity(param: XC_MethodHook.MethodHookParam): CellIdentityGsm {
        val constructor = HiddenApiBypass.getDeclaredConstructor(
            CellIdentityGsm::class.java,
            Int::class.java,    // lac
            Int::class.java,    // cid
            Int::class.java,    // arfcn
            Int::class.java,    // bsic
            String::class.java, // mccStr
            String::class.java, // mncStr
            String::class.java, // alphal
            String::class.java, // alphas
            Collection::class.java, // additionalPlmns
        )

        val existedResult = param.result as CellIdentityGsm
        val customResult = constructor.newInstance(
            0,
            0,
            0,
            0,
            existedResult.mccString,
            existedResult.mncString,
            existedResult.operatorAlphaLong,
            existedResult.operatorAlphaShort,
            existedResult.additionalPlmns
        ) as CellIdentityGsm

        XposedBridge.log("FL: [Cellar] Returning custom result: $customResult")

        return customResult
    }
}
package fuck.location.xposed.cellar.identity

import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.ClosedSubscriberGroupInfo
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.hookMethod
import com.github.kyuubiran.ezxhelper.utils.isPublic
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.lsposed.hiddenapibypass.HiddenApiBypass

class Lte {
    @RequiresApi(Build.VERSION_CODES.R)
    fun hookCellIdentity(param: XC_MethodHook.MethodHookParam): CellIdentityLte {
        val constructor = HiddenApiBypass.getDeclaredConstructor(
            CellIdentityLte::class.java,
            Int::class.java,    // ci
            Int::class.java,    // pci
            Int::class.java,    // tac
            Int::class.java,    // earfcn
            IntArray::class.java,  // bands
            Int::class.java,    // bandwidth
            String::class.java, // mccStr
            String::class.java, // mncStr
            String::class.java, // alphal
            String::class.java, // alphas
            Collection::class.java, // additionalPlmns
            ClosedSubscriberGroupInfo::class.java,  // csgInfo
        )

        val existedResult = param.result as CellIdentityLte
        val customResult = constructor.newInstance(
            0,  // ECI
            0,  // Pci
            0,  // Tac
            0,  // Earfcn
            existedResult.bands,
            0,  // bandwidth
            existedResult.mccString,
            existedResult.mncString,
            existedResult.operatorAlphaLong,
            existedResult.operatorAlphaShort,
            existedResult.additionalPlmns,
            existedResult.closedSubscriberGroupInfo
        ) as CellIdentityLte
        XposedBridge.log("FL: [Cellar] Returning custom result: $customResult")

        return customResult
    }
}
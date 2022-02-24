package fuck.location.xposed.cellar.identity

import android.app.AndroidAppHelper
import android.os.Build
import android.telephony.CellIdentityNr
import androidx.annotation.RequiresApi
import de.robv.android.xposed.XposedBridge
import fuck.location.xposed.helpers.ConfigGateway
import org.lsposed.hiddenapibypass.HiddenApiBypass

class Nr {
    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(ExperimentalStdlibApi::class)
    fun alterCellIdentity(cellIdentityNr: CellIdentityNr): CellIdentityNr {
        val constructor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HiddenApiBypass.getDeclaredConstructor(
                CellIdentityNr::class.java,
                Int::class.java,    // pci
                Int::class.java,    // tac
                Int::class.java,    // nrArfcn
                IntArray::class.java,  // bands
                String::class.java, // mccStr
                String::class.java, // mncStr
                Long::class.java,   // nci
                String::class.java, // alphal
                String::class.java, // alphas
                Collection::class.java, // additionalPlmns
            )
        } else {
            HiddenApiBypass.getDeclaredConstructor(
                CellIdentityNr::class.java,
                Int::class.java,    // pci
                Int::class.java,    // tac
                Int::class.java,    // nrArfcn
                String::class.java, // mccStr
                String::class.java, // mncStr
                Long::class.java,   // nci
                String::class.java, // alphal
                String::class.java, // alphas
            )
        }

        val customResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            constructor.newInstance(
                ConfigGateway.get().readFakeLocation()!!.pci,
                ConfigGateway.get().readFakeLocation()!!.tac,
                ConfigGateway.get().readFakeLocation()!!.earfcn,
                cellIdentityNr.bands,
                cellIdentityNr.mccString,
                cellIdentityNr.mncString,
                ConfigGateway.get().readFakeLocation()!!.eci.toLong(),
                cellIdentityNr.operatorAlphaLong,
                cellIdentityNr.operatorAlphaShort,
                cellIdentityNr.additionalPlmns
            )
        } else {
            constructor.newInstance(
                ConfigGateway.get().readFakeLocation()!!.pci,
                ConfigGateway.get().readFakeLocation()!!.tac,
                ConfigGateway.get().readFakeLocation()!!.earfcn,
                cellIdentityNr.mccString,
                cellIdentityNr.mncString,
                ConfigGateway.get().readFakeLocation()!!.eci.toLong(),
                cellIdentityNr.operatorAlphaLong,
                cellIdentityNr.operatorAlphaShort
            )
        } as CellIdentityNr
        XposedBridge.log("FL: [Cellar] Returning custom result: $customResult")

        return customResult
    }
}
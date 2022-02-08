package fuck.location.xposed.cellar.info

import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellInfoLte
import android.telephony.ClosedSubscriberGroupInfo
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.findField
import fuck.location.xposed.helpers.ConfigGateway
import org.lsposed.hiddenapibypass.HiddenApiBypass

class Lte {
    @ExperimentalStdlibApi
    @RequiresApi(Build.VERSION_CODES.R)
    fun constructNewCellInfoLte(existedCellInfoLte: CellInfoLte): CellInfoLte {
        val existedResultField = findField(existedCellInfoLte.javaClass) {
            name == "mCellIdentityLte"
        }
        val existedResult = existedResultField.get(existedCellInfoLte) as CellIdentityLte

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

        val customResultInner = constructor.newInstance(
            ConfigGateway.get().readFakeLocation()!!.eci,  // ECI
            ConfigGateway.get().readFakeLocation()!!.pci,  // Pci
            ConfigGateway.get().readFakeLocation()!!.tac,  // Tac
            ConfigGateway.get().readFakeLocation()!!.earfcn,  // Earfcn
            existedResult.bands,
            ConfigGateway.get().readFakeLocation()!!.bandwidth,  // bandwidth
            existedResult.mccString,
            existedResult.mncString,
            existedResult.operatorAlphaLong,
            existedResult.operatorAlphaShort,
            existedResult.additionalPlmns,
            existedResult.closedSubscriberGroupInfo
        ) as CellIdentityLte

        existedResultField.set(existedCellInfoLte, customResultInner)
        return existedCellInfoLte
    }
}
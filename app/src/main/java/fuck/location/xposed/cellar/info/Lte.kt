package fuck.location.xposed.cellar.info

import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellInfoLte
import android.telephony.ClosedSubscriberGroupInfo
import androidx.annotation.RequiresApi
import com.github.kyuubiran.ezxhelper.utils.findField
import fuck.location.xposed.cellar.identity.Lte
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

        existedResultField.set(existedCellInfoLte, Lte().alterCellIdentity(existedResult))
        return existedCellInfoLte
    }
}
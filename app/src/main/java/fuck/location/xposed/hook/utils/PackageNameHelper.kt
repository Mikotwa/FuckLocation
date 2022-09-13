package io.github.mikotwa.yucklocation.hook.utils

import android.os.Build
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.IllegalArgumentException
import java.lang.reflect.Field

class PackageNameHelper {
    // 这里的判定是假设参数不为空的。使用前需检查！
    // todo: 这个判定挺傻的，有时间改一下
    fun callerIdentityToPackageName(callerIdentity: Any?): String {
        val fields = HiddenApiBypass.getInstanceFields(callerIdentity!!.javaClass)

        val targetFieldName = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> "private final java.lang.String android.location.util.identity.CallerIdentity.mPackageName"
            Build.VERSION.SDK_INT == Build.VERSION_CODES.R -> "public final java.lang.String com.android.server.location.CallerIdentity.packageName"
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> "public final java.lang.String com.android.server.location.CallerIdentity.mPackageName"
            Build.VERSION.SDK_INT == Build.VERSION_CODES.P -> "final java.lang.String com.android.server.LocationManagerService.Identity.mPackageName"
            else -> ""
        }

        for (field in fields) {
            if (field.toString() == targetFieldName) {
                val targetField = field as Field
                targetField.isAccessible = true
                return targetField.get(callerIdentity) as String
            }
        }

        // Workaround for pure string
        if (callerIdentity is String) return callerIdentity

        throw IllegalArgumentException("FL: Invalid CallerIdentity! This should never happen, please report to developer. $callerIdentity")
    }
}
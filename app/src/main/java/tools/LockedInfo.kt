package im.see.again.tools

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject

class LockedInfo constructor(private val context: Context) {
    /**
     * 获取解锁状态
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLockedStatus(): JSONObject {
        val jsonObject = JSONObject()
        val keyguardManager = context.getSystemService(KeyguardManager::class.java)
        jsonObject.put("locked", keyguardManager.isKeyguardLocked)
        return jsonObject
    }
}
package im.see.again.tools

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject

class BatteryInfo constructor(private val context: Context) {
    /**
     * 获取电池信息
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getBattery(): JSONObject {
        val jsonObject = JSONObject()
        val batteryManager = context.getSystemService(BatteryManager::class.java)
        val battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        jsonObject.put("battery", battery)
        when (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)) {
            BatteryManager.BATTERY_STATUS_UNKNOWN -> jsonObject.put("charging", "unknown")
            BatteryManager.BATTERY_STATUS_DISCHARGING
                , BatteryManager.BATTERY_STATUS_NOT_CHARGING-> jsonObject.put("charging", "false")
            BatteryManager.BATTERY_STATUS_CHARGING
                , BatteryManager.BATTERY_STATUS_FULL -> jsonObject.put("charging", "true")
        }
        return jsonObject
    }
}
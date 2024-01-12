package im.see.again.tools

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import org.json.JSONObject

class NetworkInfo constructor(private val context: Context) {
    /**
     * 获取联网信息
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    fun getNetworking(): JSONObject {
        val jsonObject = JSONObject()
        // 获取网络连接状况
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork) ?: return JSONObject()
        // 判断WIFI连接
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            jsonObject.put("type", "WIFI")
            val wifiManager = context.getSystemService(WifiManager::class.java)
            val wifiInfo = wifiManager.connectionInfo
            jsonObject.put("name", wifiInfo.ssid.replace("\"", ""))
        }
        // 移动数据
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            jsonObject.put("type", "CELLULAR")
            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            // 判断移动网络类型
            when (val dataNetworkType = telephonyManager.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> jsonObject.put("sub_type", "unknown")
                TelephonyManager.NETWORK_TYPE_GPRS
                    , TelephonyManager.NETWORK_TYPE_EDGE
                    , TelephonyManager.NETWORK_TYPE_CDMA
                    , TelephonyManager.NETWORK_TYPE_1xRTT
                    , TelephonyManager.NETWORK_TYPE_IDEN -> jsonObject.put("sub_type", "2G")
                TelephonyManager.NETWORK_TYPE_UMTS
                    , TelephonyManager.NETWORK_TYPE_EVDO_0
                    , TelephonyManager.NETWORK_TYPE_EVDO_A
                    , TelephonyManager.NETWORK_TYPE_HSDPA
                    , TelephonyManager.NETWORK_TYPE_HSUPA
                    , TelephonyManager.NETWORK_TYPE_HSPA
                    , TelephonyManager.NETWORK_TYPE_EVDO_B
                    , TelephonyManager.NETWORK_TYPE_EHRPD
                    , TelephonyManager.NETWORK_TYPE_HSPAP -> jsonObject.put("sub_type", "3G")
                TelephonyManager.NETWORK_TYPE_LTE -> jsonObject.put("sub_type", "4G")
                TelephonyManager.NETWORK_TYPE_NR -> jsonObject.put("sub_type", "5G")
                else -> jsonObject.put("sub_type", dataNetworkType.toString())
            }
        }
        return jsonObject
    }
}
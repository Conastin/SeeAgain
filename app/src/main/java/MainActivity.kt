package im.see.again

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.highcapable.betterandroid.ui.component.activity.AppBindingActivity
import com.highcapable.betterandroid.ui.extension.view.toast
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import im.see.again.databinding.ActivityMainBinding
import im.see.again.util.LocationUtil


class MainActivity : AppBindingActivity<ActivityMainBinding>() {
    private val _tag: String = "MainActivity"
    private var locationStatus: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 检查权限授予情况
        checkPermission(this)
        // 获取联网状态
        findViewById<Button>(R.id.buttonGetNetworking).setOnClickListener {
            getNetworking(this)
        }
        // 获取定位
        findViewById<Button>(R.id.buttonGetLocation).setOnClickListener {
            if (!locationStatus) getLocation(this)
            else closeLocation()
        }
        // 获取电量
        findViewById<Button>(R.id.buttonGetBattery).setOnClickListener {
            getBattery()
        }
    }

    /**
     * TODO: 临时方法
     * 设置文本控件
     */
    private fun setText(text: String) {
        findViewById<TextView>(R.id.textView).text = text
    }

    private fun addText(text: String) {
        val textView = findViewById<TextView>(R.id.textView)
        val temp = textView.text as String
        textView.text = temp.plus(text)
    }

    /**
     * 获取联网信息
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    fun getNetworking(context: Context) {
        XXPermissions.with(context).permission(Permission.READ_PHONE_STATE)
            .request(object : OnPermissionCallback {
                @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    // 获取网络连接状况
                    val connectivityManager = getSystemService(ConnectivityManager::class.java)
                    val currentNetwork = connectivityManager.activeNetwork
                    val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
                    if (caps == null) {
                        Log.w(_tag, "NetworkCapabilities is null")
                        return
                    }
                    // 判断WIFI连接
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        var msg = "网络连接状态：WIFI"
                        val wifiManager = getSystemService(WifiManager::class.java)
                        val wifiInfo = wifiManager.connectionInfo
                        msg += "\nWIFI名称：${wifiInfo.ssid}"
                        setText(msg)
                    }
                    // 移动数据
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        var msg = "网络连接状态：数据"
                        val telephonyManager = getSystemService(TelephonyManager::class.java)
                        // 判断移动网络类型
                        when (val dataNetworkType = telephonyManager.dataNetworkType) {
                            TelephonyManager.NETWORK_TYPE_UNKNOWN -> msg += "\n移动网络类型：未知"
                            TelephonyManager.NETWORK_TYPE_GPRS
                                , TelephonyManager.NETWORK_TYPE_EDGE
                                , TelephonyManager.NETWORK_TYPE_CDMA
                                , TelephonyManager.NETWORK_TYPE_1xRTT
                                , TelephonyManager.NETWORK_TYPE_IDEN -> msg += "\n移动网络类型：2G"
                            TelephonyManager.NETWORK_TYPE_UMTS
                                , TelephonyManager.NETWORK_TYPE_EVDO_0
                                , TelephonyManager.NETWORK_TYPE_EVDO_A
                                , TelephonyManager.NETWORK_TYPE_HSDPA
                                , TelephonyManager.NETWORK_TYPE_HSUPA
                                , TelephonyManager.NETWORK_TYPE_HSPA
                                , TelephonyManager.NETWORK_TYPE_EVDO_B
                                , TelephonyManager.NETWORK_TYPE_EHRPD
                                , TelephonyManager.NETWORK_TYPE_HSPAP -> msg += "\n移动网络类型：3G"
                            TelephonyManager.NETWORK_TYPE_LTE -> msg += "\n移动网络类型：4G"
                            TelephonyManager.NETWORK_TYPE_NR -> msg += "\n移动网络类型：5G"
                            else -> msg += "\n移动网络类型：$dataNetworkType"
                        }
                        setText(msg)
                    }
                }

                override fun onDenied(
                    permissions: MutableList<String>, doNotAskAgain: Boolean
                ) {
                    if (doNotAskAgain) {
                        toast("被永久拒绝授权，请手动授予电话相关权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        toast("获取电话相关权限失败")
                    }
                }
            })
    }

    /**
     * 获取位置信息
     */
    private fun getLocation(context: Context) {
        XXPermissions.with(context).permission(Permission.ACCESS_FINE_LOCATION)
            .request(object : OnPermissionCallback {
                @RequiresApi(Build.VERSION_CODES.S)
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    setText("正在获取定位中.....")
                    locationStatus = true
                    LocationUtil.getInstance(context)!!
                        .getLocation(object : LocationUtil.LocationCallBack {
                            override fun setLocation(location: Location?) {
                                if (location != null) {
                                    setText("")
                                    addText("经度: ${location.longitude}\n")
                                    addText("纬度: ${location.latitude}\n")
                                    LocationUtil.getInstance(this@MainActivity)!!.getAddress(
                                        location.latitude, location.longitude
                                    )
                                }
                            }

                            override fun setAddress(address: Address?) {
                                if (address != null) {
                                    addText("国家:${address.countryName}\n")
                                    addText("城市名:${address.locality}\n")
                                    addText(
                                        "周边信息:${
                                            LocationUtil.getInstance(this@MainActivity)!!
                                                .getAddressLine(address)
                                        }\n\n"
                                    )
                                }
                            }
                        })
                }

                override fun onDenied(
                    permissions: MutableList<String>, doNotAskAgain: Boolean
                ) {
                    if (doNotAskAgain) {
                        toast("被永久拒绝授权，请手动授予定位相关权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        toast("获取定位相关权限失败")
                    }
                }
            })
    }

    fun closeLocation() {
        LocationUtil.getInstance(this)!!.stopLocationUpdates()
        locationStatus = false
    }

    /**
     * 获取电池信息
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBattery() {
        var msg = ""
        val batteryManager = getSystemService(BatteryManager::class.java)
        val battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        msg += "当前电量: $battery\n"
        when (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)) {
            BatteryManager.BATTERY_STATUS_UNKNOWN -> msg += "充电状态: 未知\n"
            BatteryManager.BATTERY_STATUS_DISCHARGING
                , BatteryManager.BATTERY_STATUS_NOT_CHARGING-> msg += "充电状态: 未充电\n"
            BatteryManager.BATTERY_STATUS_CHARGING
                , BatteryManager.BATTERY_STATUS_FULL -> msg += "充电状态: 充电中\n"
        }
        setText(msg)
    }

    /**
     * 检查权限是否授予
     */
    private fun checkPermission(context: Context) {
        // 获取电话相关信息权限
        if (!XXPermissions.isGranted(context, Permission.READ_PHONE_STATE)) {
            XXPermissions.with(context).permission(Permission.READ_PHONE_STATE)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        toast("获取电话相关信息权限成功")
                    }

                    override fun onDenied(
                        permissions: MutableList<String>, doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            toast("被永久拒绝授权，请手动授予电话相关权限")
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context, permissions)
                        } else {
                            toast("获取电话相关权限失败")
                        }
                    }
                })
        }
        // 获取电话号码权限
        if (!XXPermissions.isGranted(context, Permission.READ_PHONE_NUMBERS)) {
            XXPermissions.with(context).permission(Permission.READ_PHONE_NUMBERS)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        toast("获取电话号码权限成功")
                    }

                    override fun onDenied(
                        permissions: MutableList<String>, doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            toast("被永久拒绝授权，请手动授予电话号码权限")
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context, permissions)
                        } else {
                            toast("获取电话号码信息权限失败")
                        }
                    }
                })
        }
        // 获取位置权限
        if (!XXPermissions.isGranted(context, Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)) {
            XXPermissions.with(context).permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        toast("获取位置权限成功")
                    }

                    override fun onDenied(
                        permissions: MutableList<String>, doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            toast("被永久拒绝授权，请手动授予电话相关权限")
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context, permissions)
                        } else {
                            toast("获取位置权限失败")
                        }
                    }
                })
        }
    }
}
package im.see.again

import android.content.Context
import android.location.Address
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap.CancelableCallback
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.highcapable.betterandroid.system.extension.component.startServiceOrElse
import com.highcapable.betterandroid.ui.component.activity.AppBindingActivity
import com.highcapable.betterandroid.ui.extension.component.startActivity
import com.highcapable.betterandroid.ui.extension.view.toast
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import im.see.again.databinding.ActivityMainBinding
import im.see.again.util.BackgroundTimerService
import im.see.again.util.LocationUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppBindingActivity<ActivityMainBinding>() {
    private val _tag: String = "MainActivity"
    private var locationStatus: Boolean = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 检查权限授予情况
        checkPermission(this)
        // 初始化
        init()
        // 绑定监听事件
        bindOnclickListener()

        // 隐私设置更新
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
        binding.map.onCreate(savedInstanceState)
        binding.map.map.uiSettings.isZoomControlsEnabled = false
        binding.map.map.uiSettings.setLogoBottomMargin(-60)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        // 开启监听
        Log.d(_tag, "启动Service - ${BackgroundTimerService::class.simpleName}")
        if (!startServiceOrElse(packageName, BackgroundTimerService::class.qualifiedName!!)) {
            toast("后台Service启用失败，无法自动上传数据")
        }
        var locationClient: AMapLocationClient? = null
        val mLocationListener = AMapLocationListener {
            if (it != null) {
                if (it.errorCode == 0) {
                    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                    val date = Date(it.time)
                    val msg = """
                                ${it.locationType} //获取当前定位结果来源，如网络定位结果，详见定位类型表
                                ${it.latitude} //获取纬度
                                ${it.longitude} //获取经度
                                ${it.accuracy} //获取精度信息
                                ${it.address} //地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                                ${it.country} //国家信息
                                ${it.province} //省信息
                                ${it.city} //城市信息
                                ${it.district} //城区信息
                                ${it.street} //街道信息
                                ${it.streetNum} //街道门牌号信息
                                ${it.cityCode} //城市编码
                                ${it.adCode} //地区编码
                                ${it.aoiName} //获取当前定位点的AOI信息
                                ${it.buildingId} //获取当前室内定位的建筑物Id
                                ${it.floor} //获取当前室内定位的楼层
                                ${it.gpsAccuracyStatus} //获取GPS的当前状态
                                ${df.format(date)} //定位时间
                             """.trimIndent()
                    Log.d("定位", msg)
                    val mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                        CameraPosition(
                            LatLng(it.latitude, it.longitude), 18f, 30f, 0f
                        )
                    )
                    binding.map.map.animateCamera(mCameraUpdate, 1000, MyCancelableCallback())
                    val myLocationStyle = MyLocationStyle()
                    myLocationStyle.showMyLocation(true)
                    binding.map.map.myLocationStyle = myLocationStyle
                } else {
                    Log.e(
                        "AmapError",
                        "location Error, ErrCode:${it.errorCode}, errInfo:${it.errorInfo}"
                    )
                }
            }
        }
        locationClient = AMapLocationClient(applicationContext)
        locationClient.setLocationListener(mLocationListener)
        val option = AMapLocationClientOption()
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn)
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy)
        option.setOnceLocationLatest(true)
        locationClient.setLocationOption(option)
        //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        locationClient.stopLocation()
        locationClient.startLocation()
        // binding.map.map.isMyLocationEnabled = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindOnclickListener() {
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_history -> {
                    this.startActivity<HistoryActivity>()
                    return@setOnItemSelectedListener true
                }

                R.id.nav_news -> {
//                    loadFragment(NewsFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.nav_user -> {
//                    loadFragment(UserFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    /**
     * 获取位置信息
     */
    private fun getLocation(context: Context) {
        XXPermissions.with(context).permission(Permission.ACCESS_FINE_LOCATION)
            .request(object : OnPermissionCallback {
                @RequiresApi(Build.VERSION_CODES.S)
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
//                    setText("正在获取定位中.....")
                    locationStatus = true
                    LocationUtil.getInstance(context)!!
                        .getLocation(object : LocationUtil.LocationCallBack {
                            override fun setLocation(location: Location?) {
                                if (location != null) {
//                                    setText("")
//                                    addText("经度: ${location.longitude}\n")
//                                    addText("纬度: ${location.latitude}\n")
                                    LocationUtil.getInstance(this@MainActivity)!!.getAddress(
                                        location.latitude, location.longitude
                                    )
                                }
                            }

                            override fun setAddress(address: Address?) {
                                if (address != null) {
//                                    addText("国家:${address.countryName}\n")
//                                    addText("城市名:${address.locality}\n")
//                                    addText(
//                                        "周边信息:${
//                                            LocationUtil.getInstance(this@MainActivity)!!
//                                                .getAddressLine(address)
//                                        }\n\n"
//                                    )
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

    private fun closeLocation() {
        LocationUtil.getInstance(this)!!.stopLocationUpdates()
        locationStatus = false
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
        if (!XXPermissions.isGranted(
                context, Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION
            )
        ) {
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
        // 请求忽略电池优化
        if (!XXPermissions.isGranted(context, Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
            toast("需要更改设置为无限制")
            XXPermissions.with(context).permission(Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        toast("忽略电池优化成功")
                    }

                    override fun onDenied(
                        permissions: MutableList<String>, doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            toast("被永久拒绝授权，请手动忽略电池优化")
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(context, permissions)
                        } else {
                            toast("忽略电池优化失败")
                        }
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }
}

class MyCancelableCallback : CancelableCallback {
    override fun onFinish() {
        return
    }

    override fun onCancel() {
        return
    }
}
package im.see.again.util

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import im.see.again.database.SeeAgainDataBase
import im.see.again.entity.HistoryMyself
import im.see.again.tools.BatteryInfo
import im.see.again.tools.LockedInfo
import im.see.again.tools.NetworkInfo
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class BackgroundTimerService : Service() {
    private val _tag = "BackgroundTimerUtil"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private lateinit var networkInfo: NetworkInfo
    private lateinit var batteryInfo: BatteryInfo
    private lateinit var lockedInfo: LockedInfo

    private val _time = 60 * 1000L

    override fun onCreate() {
        Log.d(_tag, "Service创建")
        super.onCreate()
        batteryInfo = BatteryInfo(this)
        networkInfo = NetworkInfo(this)
        lockedInfo = LockedInfo(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA)
        // 在这里定义你的定时任务逻辑
        runnable = object : Runnable {
            override fun run() {
                Log.d(_tag, "${simpleDateFormat.format(Date())} - 定时任务正在运行中...")
                // TODO 执行你的任务逻辑
                val lockedStatus = lockedInfo.getLockedStatus()
                val networking = networkInfo.getNetworking()
                val battery = batteryInfo.getBattery()
                checkChange(Constants.typeLocked, lockedStatus)
                if (lockedStatus.get("locked") == false && ((networking.has("name") && networking.get(
                        "name"
                    ) != "<unknown ssid>") || !networking.has("name"))
                ) {
                    checkChange(Constants.typeNetwork, networking)
                }
                checkChange(Constants.typeBattery, battery)
                handler.postDelayed(this, _time)
            }
        }
        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(_tag, "定时任务服务销毁")
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun checkChange(type: Int, content: JSONObject) {
        val seeAgainDao = SeeAgainDataBase.getInstance(this).historyMyselfDao() ?: return
        Thread {
            val latest = seeAgainDao.getLatest(type)
            Log.d(_tag, "最新的：$latest")
            if ((latest != null) && (latest.content == content.toString())) {
                return@Thread
            }
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.CHINA)
            val new = HistoryMyself(
                time = simpleDateFormat.format(Date()).toString(),
                content = content.toString(),
                type = type
            )
            seeAgainDao.insertAll(new)
            Log.d(_tag, "插入数据库：$content")
        }.start()
    }
}
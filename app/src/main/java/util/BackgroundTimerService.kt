package im.see.again.util

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date


class BackgroundTimerService : Service() {
    private val _tag = "BackgroundTimerUtil"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private val _time = 5 * 1000L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        // 在这里定义你的定时任务逻辑
        runnable = object : Runnable {
            override fun run() {
                Log.d(_tag, "${simpleDateFormat.format(Date())} - 定时任务正在运行中...")
                // TODO 执行你的任务逻辑
                handler.postDelayed(this, _time)
            }
        }
        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(_tag, "定时任务服务销毁")

        // 移除定时任务
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}
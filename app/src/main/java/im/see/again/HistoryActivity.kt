package im.see.again

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.highcapable.betterandroid.ui.component.activity.AppBindingActivity
import im.see.again.adapter.HistoryListAdapter
import im.see.again.database.SeeAgainDataBase
import im.see.again.databinding.ActivityHistoryBinding
import im.see.again.entity.HistoryMyself

class HistoryActivity : AppBindingActivity<ActivityHistoryBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHistory()
        binding.swipeRefreshLayout.setOnRefreshListener {
            showHistory()
        }
    }

    private fun showHistory() {
        val seeAgainDao =
            SeeAgainDataBase.getInstance(this).historyMyselfDao() ?: return
        Thread {
            val historyMyselfList = seeAgainDao.getAll()
            val tempList = arrayListOf<HistoryMyself>()
            var lastDateTime: String = ""
            for (historyMyself in historyMyselfList) {
                val date = historyMyself.time.substring(0, 10)
                if (lastDateTime == "" || lastDateTime != date) {
                    lastDateTime = date
                    tempList.add(HistoryMyself(id = null, time = date, content = "", type = 0))
                }
                tempList.add(historyMyself)
            }
            runOnUiThread {
                binding.recyclerView.layoutManager = LinearLayoutManager(this)
                binding.recyclerView.adapter = HistoryListAdapter(tempList)
                binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
                binding.swipeRefreshLayout.isRefreshing = false
                Log.d("HistoryActivity", "查询历史数据")
            }
        }.start()
    }
}
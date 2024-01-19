package im.see.again.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.see.again.R
import im.see.again.entity.HistoryMyself
import im.see.again.util.Constants
import org.json.JSONObject

class HistoryListAdapter(private val historyMyselfList: List<HistoryMyself>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_TYPE_DATA = 0     // 数据类型
    private val ITEM_TYPE_DATE = 1 // 日期时间类型

    class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView
        val changeItem: TextView
        val time: TextView
        val value: TextView

        init {
            imageView = view.findViewById(R.id.imageView)
            changeItem = view.findViewById(R.id.changeItem)
            time = view.findViewById(R.id.time)
            value = view.findViewById(R.id.value)
        }
    }

    class DateTimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemDividerView: TextView

        init {
            itemDividerView = view.findViewById(R.id.itemDividerView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val id = historyMyselfList[position].id
        val content = historyMyselfList[position].content
        return if (id == null && content == "") {
            ITEM_TYPE_DATE
        } else {
            ITEM_TYPE_DATA
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_DATE) {
            DateTimeViewHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.history_list_item_divider, viewGroup, false)
            )
        } else {
            DataViewHolder(
                LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.history_list_item, viewGroup, false)
            )
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        viewHolder.itemView.animation =
            AnimationUtils.loadAnimation(viewHolder.itemView.context, R.anim.anim_history_list)
        if (viewHolder is DateTimeViewHolder) {
            viewHolder.itemDividerView.text = historyMyselfList[position].time
        } else {
            val contentJson = JSONObject(historyMyselfList[position].content)
            when (historyMyselfList[position].type) {
                Constants.typeLocked -> {
                    (viewHolder as DataViewHolder).imageView.setImageResource(R.drawable.icon_lock)
                    viewHolder.changeItem.text = "锁屏"
                    viewHolder.time.text = historyMyselfList[position].time
                    viewHolder.value.text =
                        if (contentJson.get("locked") == true) "锁屏" else "解锁"
                }

                Constants.typeNetwork -> {
                    when (contentJson.get("type")) {
                        "WIFI" -> {
                            (viewHolder as DataViewHolder).imageView.setImageResource(R.drawable.icon_wifi)
                            viewHolder.changeItem.text = "WIFI"
                            viewHolder.value.text = contentJson.get("name") as String
                        }

                        "CELLULAR" -> {
                            (viewHolder as DataViewHolder).imageView.setImageResource(R.drawable.icon_cellular)
                            viewHolder.changeItem.text = "移动数据"
                            viewHolder.value.text = contentJson.get("sub_type") as String
                        }
                    }
                    (viewHolder as DataViewHolder).time.text = historyMyselfList[position].time
                }

                Constants.typeBattery -> {
                    (viewHolder as DataViewHolder).imageView.setImageResource(R.drawable.icon_barrery)
                    viewHolder.changeItem.text = "电量"
                    viewHolder.time.text = historyMyselfList[position].time
                    viewHolder.value.text = contentJson.get("battery").toString() + "%"
                }
            }
        }
    }

    override fun getItemCount() = historyMyselfList.size

}
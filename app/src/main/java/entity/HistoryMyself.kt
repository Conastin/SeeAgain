package im.see.again.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_myself")
data class HistoryMyself(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val time: String,
    val content: String,
    val type: Int,
    val uploadFlag: Boolean? = false
)
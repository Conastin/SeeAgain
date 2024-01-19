package im.see.again.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import im.see.again.entity.HistoryMyself

@Dao
interface HistoryMyselfDao {
    @Query("SELECT * FROM history_myself ORDER BY id DESC")
    fun getAll(): List<HistoryMyself>

    @Query("SELECT * FROM history_myself WHERE type = :type ORDER BY id DESC LIMIT 1")
    fun getLatest(type: Int): HistoryMyself?

    @Insert
    fun insertAll(vararg historyMyself: HistoryMyself)

    @Delete
    fun delete(historyMyself: HistoryMyself)

    //    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User
}
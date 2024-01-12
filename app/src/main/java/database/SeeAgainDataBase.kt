package im.see.again.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import im.see.again.dao.HistoryMyselfDao
import im.see.again.entity.HistoryMyself

@Database(entities = [HistoryMyself::class], version = 1)
abstract class SeeAgainDataBase: RoomDatabase() {
    abstract fun historyMyselfDao(): HistoryMyselfDao?

    companion object {
        private const val DB_NAME = "seeAgain.db"

        @Volatile
        private var instance: SeeAgainDataBase? = null
        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    SeeAgainDataBase::class.java, DB_NAME
                ).build()
            }
    }
}
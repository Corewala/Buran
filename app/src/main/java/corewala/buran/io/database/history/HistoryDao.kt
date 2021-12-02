package corewala.buran.io.database.history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAll(): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE uid = :uid LIMIT 1")
    fun getEntry(uid: Int): HistoryEntity

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 1")
    fun getLastAdded(): HistoryEntity?

    @Insert
    fun insert(vararg history: HistoryEntity)

    @Delete
    fun delete(history: HistoryEntity)

    @Query("DELETE FROM history")
    fun clear()
}
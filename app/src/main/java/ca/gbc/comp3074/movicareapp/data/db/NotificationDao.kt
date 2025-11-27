package ca.gbc.comp3074.movicareapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity)

    // Get all notifications for a list of seniors (which the family member is linked to)
    @Query("SELECT * FROM notifications WHERE seniorId IN (:seniorIds) ORDER BY timestamp DESC")
    fun getNotificationsForSeniors(seniorIds: List<Long>): Flow<List<NotificationEntity>>
}

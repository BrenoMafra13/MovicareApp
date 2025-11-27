package ca.gbc.comp3074.movicareapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRelationshipDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRelationship(relationship: UserRelationshipEntity)

    @Update
    suspend fun updateRelationship(relationship: UserRelationshipEntity)

    // Get pending requests where the user is the target
    @Query("SELECT * FROM user_relationships WHERE targetId = :userId AND status = 'PENDING'")
    fun getPendingRequests(userId: Long): Flow<List<UserRelationshipEntity>>

    // Get all accepted relationships where the user is involved
    @Query("SELECT * FROM user_relationships WHERE (requesterId = :userId OR targetId = :userId) AND status = 'ACCEPTED'")
    fun getAllAcceptedRelationships(userId: Long): Flow<List<UserRelationshipEntity>>

    // Get relationship status between two users
    @Query("SELECT * FROM user_relationships WHERE (requesterId = :id1 AND targetId = :id2) OR (requesterId = :id2 AND targetId = :id1)")
    suspend fun getRelationship(id1: Long, id2: Long): UserRelationshipEntity?

    @Query("DELETE FROM user_relationships WHERE requesterId = :requesterId AND targetId = :targetId")
    suspend fun deleteRelationship(requesterId: Long, targetId: Long)
}

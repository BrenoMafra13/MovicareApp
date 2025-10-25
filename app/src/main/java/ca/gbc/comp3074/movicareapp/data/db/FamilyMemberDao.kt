package ca.gbc.comp3074.movicareapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FamilyMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMemberEntity): Long

    @Query("SELECT * FROM family_members WHERE ownerUserId = :userId ORDER BY name")
    suspend fun getByUser(userId: Long): List<FamilyMemberEntity>

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteById(id: Long)
}

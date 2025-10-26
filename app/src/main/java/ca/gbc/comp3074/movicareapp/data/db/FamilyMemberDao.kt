package ca.gbc.comp3074.movicareapp.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMemberEntity)

    @Delete
    suspend fun deleteFamilyMember(familyMember: FamilyMemberEntity)

    @Query("SELECT * FROM family_members WHERE ownerUserId = :userId ORDER BY name")
    fun getFamilyMembersByUserId(userId: Long): Flow<List<FamilyMemberEntity>>

    @Query("SELECT COUNT(*) FROM family_members WHERE ownerUserId = :userId")
    fun getFamilyMemberCount(userId: Long): Flow<Int>
}

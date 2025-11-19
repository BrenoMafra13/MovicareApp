package ca.gbc.comp3074.movicareapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val role: String,
    val avatarUri: String? = null,
    val street: String? = null,
    val unit: String? = null,
    val postalCode: String? = null
)

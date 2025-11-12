package ca.gbc.comp3074.movicareapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ownerUserId"])]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUserId: Long,
    val type: String,
    val day: String,
    val time: String
)

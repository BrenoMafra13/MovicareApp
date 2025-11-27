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
    val role: String, // SENIOR, FAMILY_MEMBER, CAREGIVER
    val avatarUri: String? = null,
    val street: String? = null,
    val unit: String? = null,
    val postalCode: String? = null,
    
    // Caregiver Verification Fields
    val specialty: String? = null,
    val licenseNumber: String? = null,
    val isVerified: Boolean = false,
    val verificationStatus: String = "NONE", // NONE, PENDING, VERIFIED, REJECTED
    
    // Identity Documents (URIs)
    val idDocumentUri: String? = null,
    val selfieUri: String? = null,
    val certificationUri: String? = null,

    // Adding phone number field for invitation flow
    val phoneNumber: String? = null,

    // Health information
    val conditions: String = "",
    val allergies: String = "",
    val height: String = "",
    val weight: String = ""
)

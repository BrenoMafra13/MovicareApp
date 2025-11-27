package ca.gbc.comp3074.movicareapp.sync

import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreSync {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun syncUser(user: UserEntity) {
        val data = mapOf(
            "id" to user.id,
            "fullName" to user.fullName,
            "username" to user.username,
            "email" to user.email,
            "role" to user.role,
            "avatarUri" to user.avatarUri
        )

        firestore
            .collection("users")
            .document(user.id.toString())
            .set(data)
    }
}

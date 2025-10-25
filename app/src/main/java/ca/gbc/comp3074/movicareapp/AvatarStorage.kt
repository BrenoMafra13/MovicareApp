package ca.gbc.comp3074.movicareapp.auth

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object AvatarStorage {
    fun savePickedImage(context: Context, pickedUri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val ext = resolver.getType(pickedUri)?.substringAfterLast('/') ?: "jpg"
            val dir = File(context.filesDir, "avatars").apply { mkdirs() }
            val file = File(dir, "avatar_${System.currentTimeMillis()}.$ext")
            resolver.openInputStream(pickedUri).use { input ->
                FileOutputStream(file).use { output ->
                    if (input != null) input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

package ca.gbc.comp3074.movicareapp.auth

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordUtils {
    private const val ITERATIONS = 12000
    private const val KEY_LENGTH_BITS = 256   // 32 bytes
    private const val SALT_BYTES = 16         // 128-bit salt

    fun newSaltB64(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashB64(password: String, saltB64: String): String {
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun verify(password: String, saltB64: String, expectedHashB64: String): Boolean {
        return hashB64(password, saltB64) == expectedHashB64
    }
}

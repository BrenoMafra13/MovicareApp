package ca.gbc.comp3074.movicareapp.auth

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITER = 12000
    private const val KEY_LEN = 256
    private const val ALG = "PBKDF2WithHmacSHA256"

    @RequiresApi(Build.VERSION_CODES.O)
    fun newSalt(bytes: Int = 16): String {
        val s = ByteArray(bytes)
        SecureRandom().nextBytes(s)
        return Base64.getEncoder().encodeToString(s)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hash(password: String, saltB64: String): String {
        val salt = Base64.getDecoder().decode(saltB64)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITER, KEY_LEN)
        val skf = SecretKeyFactory.getInstance(ALG)
        val key = skf.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(key)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verify(password: String, saltB64: String, expectedHashB64: String): Boolean {
        return hash(password, saltB64) == expectedHashB64
    }
}

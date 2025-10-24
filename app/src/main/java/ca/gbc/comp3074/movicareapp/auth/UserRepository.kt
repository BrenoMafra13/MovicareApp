package ca.gbc.comp3074.movicareapp.auth

import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()

    suspend fun login(username: String, password: String): Result<Pair<Long, String>> =
        withContext(Dispatchers.IO) {
            try {
                val u = username.trim()
                val p = password

                if (u.isEmpty() || p.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("Ingrese usuario y contraseña"))
                }

                val user = userDao.getByUsername(u)
                    ?: return@withContext Result.failure(Exception("Usuario no encontrado"))

                val ok = PasswordUtils.verify(p, user.salt, user.passwordHash)
                if (!ok) {
                    return@withContext Result.failure(Exception("Credenciales inválidas"))
                }

                Result.success(user.id to user.role)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: String,
        avatarUri: String?
    ): Result<Pair<Long, String>> = withContext(Dispatchers.IO) {
        try {
            val u = username.trim()
            val mail = email.trim()
            if (fullName.isBlank() || u.isBlank() || mail.isBlank() || password.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Complete todos los campos"))
            }

            // ¿usuario ya existe?
            if (userDao.getByUsername(u) != null) {
                return@withContext Result.failure(IllegalStateException("El usuario ya existe"))
            }

            val saltB64 = PasswordUtils.newSaltB64()
            val hashB64 = PasswordUtils.hashB64(password, saltB64)

            val entity = UserEntity(
                // id autogenerado por Room
                fullName = fullName,
                username = u,
                email = mail,
                passwordHash = hashB64,
                salt = saltB64,
                role = role,
                avatarUri = avatarUri
            )

            val id = userDao.insert(entity)
            Result.success(id to role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

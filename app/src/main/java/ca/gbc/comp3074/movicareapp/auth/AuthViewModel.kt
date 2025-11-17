package ca.gbc.comp3074.movicareapp.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UserRepository(AppDatabase.getInstance(app))

    private val _loginUi = MutableStateFlow(LoginUiState())
    val loginUi: StateFlow<LoginUiState> = _loginUi

    private val _signupUi = MutableStateFlow(SignUpUiState())
    val signupUi: StateFlow<SignUpUiState> = _signupUi

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginUi.update { it.copy(loading = true, error = null, successRole = null, userId = null) }
            val result = repo.login(username, password) // Result<Pair<Long, String>>
            result
                .onSuccess { (id, role) ->
                    _loginUi.update { it.copy(loading = false, successRole = role, userId = id) }
                }
                .onFailure { e ->
                    _loginUi.update { it.copy(loading = false, error = e.message ?: "Login failed") }
                }
        }
    }

    fun signUp(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: String,
        avatarUri: String?,
        street: String?,
        unit: String?,
        postalCode: String?
    ) {
        viewModelScope.launch {
            _signupUi.update { it.copy(loading = true, error = null, successRole = null, userId = null) }
            val result = repo.register(fullName, username, email, password, role, avatarUri, street, unit, postalCode)
            result
                .onSuccess { (id, createdRole) ->
                    _signupUi.update { it.copy(loading = false, successRole = createdRole, userId = id) }
                }
                .onFailure { e ->
                    _signupUi.update { it.copy(loading = false, error = e.message ?: "Registration failed") }
                }
        }
    }
}


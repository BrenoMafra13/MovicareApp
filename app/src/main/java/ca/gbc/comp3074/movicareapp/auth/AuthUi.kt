package ca.gbc.comp3074.movicareapp.auth

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val successRole: String? = null,
    val userId: Long? = null
)

data class SignUpUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val successRole: String? = null,
    val userId: Long? = null
)

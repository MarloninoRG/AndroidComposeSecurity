package mx.edu.utng.mrg.security01.models

sealed class AuthState {
    object Idle : AuthState() //Estado inicial, sin acción
    object Loading : AuthState() //Procesando autenciación
    data class Success(val user: User): AuthState() //Login exitoso
    data class Error(val message: String): AuthState() //Ocurrió un error
    object Logout : AuthState() //Usuario cerró sesión
}
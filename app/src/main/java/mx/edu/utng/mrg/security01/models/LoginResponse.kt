package mx.edu.utng.mrg.security01.models

/**
 * Respuesta que recibimos del servidor despues del login
 * @property success Indica si el login fué exitoso
 * @property message Mensaje descriptivo del resultado
 * @property user Datos del usuario (solo si success = true)
 */
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)
package mx.edu.utng.mrg.security01.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.edu.utng.mrg.security01.models.LoginRequest
import mx.edu.utng.mrg.security01.models.User
import mx.edu.utng.mrg.security01.network.MockApiService
import mx.edu.utng.mrg.security01.network.RetrofitClient
import mx.edu.utng.mrg.security01.security.SecureStorage

class AuthRepository(context: Context) {
    private val secureStorage = SecureStorage(context)
    private val apiService = RetrofitClient.apiService

    companion object {
        private const val TAG = "AuthRepository"
        private const val USE_MOCK_API = true //CAMBIAR A FALSE EN PRODUCCIÓN
        //NUNCA hacer Log.d() con tokens
    }

    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                //Válidación básica
                if (email.isBlank() || password.isBlank()) {
                    return@withContext Result.failure(
                        Exception("El email y la contraseña son obligatorios")
                    )
                }

                //Validacion de formato email
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    return@withContext Result.failure(
                        Exception("El formato del email no es válido")
                    )
                }

                Log.d(TAG, "Intentando login para usuario: $email")
                //NUNCA Log.d(TAG, "Password: $password)

                //Creamos peticion
                val loginRequest = LoginRequest(email, password)

                //Hacemos la llamada a la API
                //val response = apiService.login(loginRequest)
                //USAR MOCK PARA PRUEBAS
                val response = if (USE_MOCK_API) {
                    MockApiService.login(loginRequest)
                } else {
                    RetrofitClient.apiService.login(loginRequest)
                }

                //Verificamos la respuesta
                if (response.isSuccessful) {
                    val loginResponse = response.body()

                    if (loginResponse?.success == true && loginResponse.user != null) {
                        //Login exitoso
                        val user = loginResponse.user
                        secureStorage.saveUserSession(user)

                        Log.d(TAG, "Login exitoso para: $email")
                        Result.success(user)
                    } else {
                        Log.w(TAG, "Login fallido: ${loginResponse?.message}")
                        Result.failure(
                            Exception(loginResponse?.message ?: "Error en el login")
                        )
                    }
                } else {
                    //Error HTTP
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        404 -> "Servicio no disponible"
                        500 -> "Error en el servidor"
                        else -> "Error de conexión: ${response.code()}"
                    }
                    Log.e(TAG, "Error HHTP: ${response.code()}")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                //Capturamos cualquier error inesperado
                Log.e(TAG, "Excepcion en login", e)
                Result.failure(
                    Exception("Error de conexión: ${e.localizedMessage}")
                )
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return secureStorage.isLoggedIn()
    }

    fun getCurrentUser(): User? {
        return secureStorage.getUserData()
    }

    suspend fun validateToken(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = secureStorage.getToken()

                if (token == null) {
                    return@withContext Result.success(false)
                }

                //val response = apiService.validateToken("Bearer $token")
                val response = if (USE_MOCK_API) {
                    MockApiService.validateToken("Bearer $token")
                } else {
                    RetrofitClient.apiService.validateToken("Bearer $token")
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    //Token válido, actualizamos timestamp
                    secureStorage.updateSessionTimestamp()
                    Result.success(true)
                } else {
                    //Token invalido
                    logout()
                    Result.success(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error validando token", e)
                Result.failure(e)
            }
        }
    }

    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = secureStorage.getToken()

                //Intentamos cerrar la sesión en el servidor
                if (token != null) {
                    try {
                        //apiService.logout("Bearer $token")
                        val response = if (USE_MOCK_API) {
                            MockApiService.logout("Bearer $token")
                        } else {
                            RetrofitClient.apiService.logout("Bearer $token")
                        }
                        Log.d(TAG, "Sesión cerrada en el servidor")
                    } catch (e: Exception) {
                        //Si fala continuamos cerrando sesión local
                        Log.w(TAG, "No se pudo cerrar la sesión en el servidor", e)
                    }
                }
                //Limpiamos datos locales SIEMPRE
                secureStorage.clearSession()
                Log.d(TAG, "Sesion local limpiada")
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error en logout", e)
                //Aun asi limpiamos el local
                secureStorage.clearSession()
                Result.failure(e)
            }
        }
    }

    fun updateActivity() {
        secureStorage.updateSessionTimestamp()
    }
}
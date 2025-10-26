package mx.edu.utng.mrg.security01.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import mx.edu.utng.mrg.security01.models.User

class SecureStorage(context: Context) {
    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KER_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_SESSION_TIMESTAMP = "session_timestamp"

        //Tiempo de exporiración de sesión: 24 horas
        private const val SESSION_TIMEOUT = 24*60*60*1000L //milisegundos
    }

    //Creamos una llave maestra para encriptar
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) //Algoritmo de encriptación militar
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserSession(user: User) {
        sharedPreferences.edit().apply {
            putString(KEY_TOKEN, user.token)
            putString(KEY_USER_ID, user.id)
            putString(KER_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    fun getToken(): String? {
        return if(isSessionValid()) {
            sharedPreferences.getString(KEY_TOKEN, null)
        } else {
            clearSession() //Sesión expirada, limpiamos todo
            null
        }
    }

    fun getUserData(): User? {
        if(!isSessionValid()) {
            clearSession()
            return null
        }
        val token = sharedPreferences.getString(KEY_TOKEN, null)
        val id = sharedPreferences.getString(KEY_USER_ID, null)
        val email = sharedPreferences.getString(KER_USER_EMAIL, null)
        val name = sharedPreferences.getString(KEY_USER_NAME, null)

        //Si todos los datos existen, creamos el objeto User
        return if(token != null && id != null && email != null && name != null) {
            User(id, email, name, token)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && isSessionValid()
    }

    private fun isSessionValid(): Boolean {
        val sessionTimestamp = sharedPreferences.getLong(KEY_SESSION_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val sessionAge = currentTime - sessionTimestamp

        return sessionAge < SESSION_TIMEOUT
    }

    fun updateSessionTimestamp() {
        sharedPreferences.edit().apply {
            putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis())
            apply()
        }
    }

    fun clearSession() {
        sharedPreferences.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KER_USER_EMAIL)
            remove(KEY_USER_NAME)
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_SESSION_TIMESTAMP)
            apply()
        }
    }
}
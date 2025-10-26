package mx.edu.utng.mrg.security01

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.mrg.security01.navigation.NavigationGraph
import mx.edu.utng.mrg.security01.navigation.Screen
import mx.edu.utng.mrg.security01.ui.theme.SecurityTheme
import mx.edu.utng.mrg.security01.ui.theme.Surface
import mx.edu.utng.mrg.security01.viewmodel.AuthViewModel

/**
 * Activity Principal de la aplicación
 *
 * Explicación:
 * En Compose, la Activity es MUCHO más simple que en Views trdicionales.
 * Solo hace dos cosas principales:
 * 1. Configura el tema
 * 2. Inicia el grafo de navegación
 *
 * Todo lo demás (UI, lógica) está en Composables separados
 * Esto hace el código más limpio y modular
 */
class MainActivity : ComponentActivity() {
    //ViewModel compartido en toda la app
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        //Instalamos el splash screen del sistema
        installSplashScreen()

        super.onCreate(savedInstanceState)
        /**
         * setContent {} es donde "arranca" Compose
         *
         * IMPORTANTE: Todo lo que esté dentro de setContent
         * será UI declarativa con Compose
         */
        setContent {
            SecurityTheme {
                //Surface es el contenedor base
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SecurityApp()
                }
            }
        }
    }
}
/**
 * Composable principal de la aplicación
 *
 * ESTRUCTURA:
 * MainActivity -> SecurityApp -> NavigationGraph -> Screen
 *
 * Es como una pirámide: la base (MainActivity) sostiene todo lo demás
 */
@Composable
fun SecurityApp() {
    val navController = rememberNavController()

    NavigationGraph(
        navController = navController,
        startDestination = Screen.Splash.route
    )
}
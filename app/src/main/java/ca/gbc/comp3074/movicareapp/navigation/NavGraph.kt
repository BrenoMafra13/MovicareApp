package ca.gbc.comp3074.movicareapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ca.gbc.comp3074.movicareapp.WelcomeScreen
import ca.gbc.comp3074.movicareapp.LoginScreen
import ca.gbc.comp3074.movicareapp.SignUpScreen
import ca.gbc.comp3074.movicareapp.HomeScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "welcome") {

        // WELCOME
        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { nav.navigate("login") },
                onSignupClick = { nav.navigate("signup") } // <- nombre correcto
            )
        }

        // LOGIN
        composable("login") {
            LoginScreen(
                onBackClick = { nav.popBackStack() },
                onLoginSuccess = { userId, _ ->
                    nav.navigate("home/$userId") {
                        popUpTo("welcome") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onGoToSignUp = { nav.navigate("signup") }
            )
        }

        // SIGN UP
        composable("signup") {
            SignUpScreen(
                onBackClick = { nav.popBackStack() },
                onRegistrationSuccess = { userId, _ ->
                    nav.navigate("home/$userId") {
                        popUpTo("welcome") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    nav.navigate("login") {
                        popUpTo("welcome") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "home/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType }
            )
        ) { entry ->
            val userId = requireNotNull(entry.arguments?.getLong("userId")) { "userId es requerido" }
            HomeScreen(
                userId = userId,
                onAvatarClick = {  },
                onEditProfile = { },
                onSettings = {  },
                onLogout = {
                    nav.navigate("welcome") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

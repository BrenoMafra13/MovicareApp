package ca.gbc.comp3074.movicareapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ca.gbc.comp3074.movicareapp.*

@Composable
fun AppNavHost() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "welcome") {

        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { nav.navigate("login") },
                onSignupClick = { nav.navigate("signup") }
            )
        }

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

        composable("signup") {
            SignUpScreen(
                onBackClick = { nav.popBackStack() },
                onRegistrationSuccess = { _, _ ->
                    nav.navigate("login") {
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

        // HOME
        composable(
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = requireNotNull(entry.arguments?.getLong("userId")) { "userId is required" }

            HomeScreen(
                userId = userId,
                onAvatarClick = { nav.navigate("profile/$userId") },
                onEditProfile = { nav.navigate("account") },
                onSettings = { nav.navigate("account") },
                onLogout = {
                    nav.navigate("welcome") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        // PROFILE
        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = requireNotNull(entry.arguments?.getLong("userId")) { "userId is required" }
            ProfileScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() },
                onLogoutClick = {
                    nav.navigate("welcome") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onMyHealthClick = { nav.navigate("myHealth") },
                onMedicationsClick = { nav.navigate("medications") },
                onFamilyClick = { nav.navigate("familyMembers/$userId") },
                onAppointmentsClick = { nav.navigate("appointments") },
                onAccountClick = { nav.navigate("account") }
            )
        }

        composable("myHealth") { MyHealthScreen(onBackClick = { nav.popBackStack() }) }

        composable("medications") {
            MedicationsScreen(
                onBackClick = { nav.popBackStack() },
                onAddMedicationClick = { nav.navigate("addMedication") }
            )
        }
        composable("addMedication") { AddMedicationScreen(onBackClick = { nav.popBackStack() }) }

        composable("appointments") {
            AppointmentsScreen(
                onBackClick = { nav.popBackStack() },
                onAddAppointmentClick = { nav.navigate("addAppointment") }
            )
        }
        composable("addAppointment") { AddAppointmentScreen(onBackClick = { nav.popBackStack() }) }

        // FAMILY MEMBERS
        composable(
            route = "familyMembers/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = requireNotNull(entry.arguments?.getLong("userId")) { "userId is required" }

            FamilyMembersScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() }
            )
        }

        composable("account") { AccountScreen(onBackClick = { nav.popBackStack() }) }
    }
}

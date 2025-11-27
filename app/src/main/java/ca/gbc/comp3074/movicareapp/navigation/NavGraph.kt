package ca.gbc.comp3074.movicareapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ca.gbc.comp3074.movicareapp.*
import ca.gbc.comp3074.movicareapp.ui.*
import ca.gbc.comp3074.movicareapp.ui.familymember.*
import ca.gbc.comp3074.movicareapp.ui.familymember.medications.*
import ca.gbc.comp3074.movicareapp.ui.familymember.appointments.*

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
                onLoginSuccess = { userId, role ->
                    // Determine dashboard based on role
                    if (role.equals("family", ignoreCase = true) || role.equals("caregiver", ignoreCase = true)) {
                        nav.navigate("familyDashboard/$userId") {
                            popUpTo("welcome") { inclusive = false }
                            launchSingleTop = true
                        }
                    } else {
                        // Default to Senior/Standard Home
                        nav.navigate("home/$userId") {
                            popUpTo("welcome") { inclusive = false }
                            launchSingleTop = true
                        }
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

        // SENIOR HOME
        composable(
            route = "home/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")

            HomeScreen(
                userId = userId,
                onAvatarClick = { nav.navigate("profile/$userId") },
                onEditProfile = { nav.navigate("account/$userId") },
                onSettings = { nav.navigate("account/$userId") },
                onLogout = {
                    nav.navigate("welcome") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        // FAMILY / CAREGIVER DASHBOARD
        composable(
            route = "familyDashboard/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
            
            FamilyMemberDashboard(
                userId = userId,
                onNavigateToMedications = { seniorId -> nav.navigate("manageMedications/$seniorId") },
                onNavigateToAppointments = { seniorId -> nav.navigate("manageAppointments/$seniorId") },
                onNavigateToNotifications = { nav.navigate("notifications/$userId") },
                onNavigateToInvitations = { nav.navigate("invitations/$userId") },
                onLogout = {
                    nav.navigate("welcome") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                onProfile = { nav.navigate("account/$userId") }
            )
        }
        
        // FAMILY: MANAGE MEDICATIONS
        composable(
            route = "manageMedications/{seniorId}",
            arguments = listOf(navArgument("seniorId") { type = NavType.LongType })
        ) { entry ->
            val seniorId = entry.arguments?.getLong("seniorId") ?: error("seniorId is required")
            MedicationsListScreen(
                seniorId = seniorId,
                onBackClick = { nav.popBackStack() },
                onAddMedication = { nav.navigate("manageMedications/add/$seniorId") },
                onEditMedication = { medId -> nav.navigate("manageMedications/edit/$medId") }
            )
        }
        
        composable(
            route = "manageMedications/add/{seniorId}",
            arguments = listOf(navArgument("seniorId") { type = NavType.LongType })
        ) { entry ->
            val seniorId = entry.arguments?.getLong("seniorId") ?: error("seniorId is required")
            AddMedicationScreenWrapper(
                seniorId = seniorId,
                onBackClick = { nav.popBackStack() }
            )
        }
        
        composable(
            route = "manageMedications/edit/{medicationId}",
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { entry ->
            val medId = entry.arguments?.getLong("medicationId") ?: error("medicationId is required")
            EditMedicationScreen(
                medicationId = medId,
                onBackClick = { nav.popBackStack() }
            )
        }

        // FAMILY: MANAGE APPOINTMENTS
        composable(
            route = "manageAppointments/{seniorId}",
            arguments = listOf(navArgument("seniorId") { type = NavType.LongType })
        ) { entry ->
            val seniorId = entry.arguments?.getLong("seniorId") ?: error("seniorId is required")
            AppointmentsListScreen(
                seniorId = seniorId,
                onBackClick = { nav.popBackStack() },
                onAddAppointment = { nav.navigate("manageAppointments/add/$seniorId") },
                onEditAppointment = { apptId -> nav.navigate("manageAppointments/edit/$apptId") }
            )
        }
        
        composable(
            route = "manageAppointments/add/{seniorId}",
            arguments = listOf(navArgument("seniorId") { type = NavType.LongType })
        ) { entry ->
            val seniorId = entry.arguments?.getLong("seniorId") ?: error("seniorId is required")
            AddAppointmentScreenWrapper(
                seniorId = seniorId,
                onBackClick = { nav.popBackStack() }
            )
        }
        
        composable(
            route = "manageAppointments/edit/{appointmentId}",
            arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
        ) { entry ->
            val apptId = entry.arguments?.getLong("appointmentId") ?: error("appointmentId is required")
            EditAppointmentScreen(
                appointmentId = apptId,
                onBackClick = { nav.popBackStack() }
            )
        }

        // NOTIFICATIONS
        composable(
            route = "notifications/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
            NotificationsScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() }
            )
        }
        
        // INVITATIONS (Probably accessible from Profile or Family Dashboard menu, adding route here)
        composable(
            route = "invitations/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
            InvitationsScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() }
            )
        }

        // PROFILE (Existing routes)
        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
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
                onMedicationsClick = { nav.navigate("medications/$userId") },
                onFamilyClick = { nav.navigate("familyMembers/$userId") },
                onAppointmentsClick = { nav.navigate("appointments/$userId") },
                onAccountClick = { nav.navigate("account/$userId") }
            )
        }

        composable("myHealth") { MyHealthScreen(onBackClick = { nav.popBackStack() }) }

        // SENIOR: MEDICATIONS (Direct link)
        composable(
            route = "medications/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
            MedicationsScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() },
                onAddMedicationClick = { nav.navigate("addMedication/$userId") }
            )
        }
        composable(
            route = "addMedication/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
            AddMedicationScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() }
            )
        }

        // SENIOR: APPOINTMENTS (Direct link)
        composable(
            route = "appointments/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
            AppointmentsScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() },
                onAddAppointmentClick = { nav.navigate("addAppointment/$userId") }
            )
        }
        composable(
            route = "addAppointment/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")
            AddAppointmentScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() }
            )
        }

        // FAMILY MEMBERS (Connections)
        composable(
            route = "familyMembers/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")

            FamilyMembersScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() }
            )
        }

        composable(
            route = "account/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: error("userId is required")

            AccountScreen(
                userId = userId,
                onBackClick = { nav.popBackStack() }
            )
        }
    }
}

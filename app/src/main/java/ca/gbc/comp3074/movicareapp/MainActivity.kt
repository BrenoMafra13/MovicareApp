package ca.gbc.comp3074.movicareapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import ca.gbc.comp3074.movicareapp.ui.theme.MovicareAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovicareAppTheme {
                var showLogin by remember { mutableStateOf(false) }
                var showSignup by remember { mutableStateOf(false) }
                var showHome by remember { mutableStateOf(false) }
                var showProfile by remember { mutableStateOf(false) }
                var showMyHealth by remember { mutableStateOf(false) }
                var showMedications by remember { mutableStateOf(false) }
                var showAddMedication by remember { mutableStateOf(false) }
                var showFamily by remember { mutableStateOf(false) }
                var showAppointments by remember { mutableStateOf(false) }
                var showAddAppointment by remember { mutableStateOf(false) }
                var showAccount by remember { mutableStateOf(false) }

                when {
                    showLogin -> {
                        BackHandler { showLogin = false }
                        LoginScreen(
                            onLoginClick = {
                                showLogin = false
                                showHome = true
                            },
                            onBackClick = { showLogin = false }
                        )
                    }

                    showSignup -> {
                        BackHandler { showSignup = false }
                        SignUpScreen(
                            onBackClick = { showSignup = false },
                            onLoginClick = {
                                showSignup = false
                                showLogin = true
                            }
                        )
                    }

                    showHome -> {
                        BackHandler { showHome = false }
                        HomeScreen(
                            onProfileClick = {
                                showHome = false
                                showProfile = true
                            }
                        )
                    }

                    showProfile -> {
                        BackHandler { showProfile = false }
                        ProfileScreen(
                            onBackClick = {
                                showProfile = false
                                showHome = true
                            },
                            onLogoutClick = {
                                showProfile = false
                                showLogin = true
                            },
                            onMyHealthClick = {
                                showProfile = false
                                showMyHealth = true
                            },
                            onMedicationsClick = {
                                showProfile = false
                                showMedications = true
                            },
                            onFamilyClick = {
                                showProfile = false
                                showFamily = true
                            },
                            onAppointmentsClick = {
                                showProfile = false
                                showAppointments = true
                            },
                            onAccountClick = {
                                showProfile = false
                                showAccount = true
                            }
                        )
                    }

                    showMyHealth -> {
                        BackHandler {
                            showMyHealth = false
                            showProfile = true
                        }
                        MyHealthScreen(
                            onBackClick = {
                                showMyHealth = false
                                showProfile = true
                            }
                        )
                    }

                    showMedications -> {
                        BackHandler {
                            showMedications = false
                            showProfile = true
                        }
                        MedicationsScreen(
                            onBackClick = {
                                showMedications = false
                                showProfile = true
                            },
                            onAddMedicationClick = {
                                showMedications = false
                                showAddMedication = true
                            }
                        )
                    }

                    showAddMedication -> {
                        BackHandler {
                            showAddMedication = false
                            showMedications = true
                        }
                        AddMedicationScreen(
                            onBackClick = {
                                showAddMedication = false
                                showMedications = true
                            }
                        )
                    }

                    showFamily -> {
                        BackHandler {
                            showFamily = false
                            showProfile = true
                        }
                        FamilyMembersScreen(
                            onBackClick = {
                                showFamily = false
                                showProfile = true
                            }
                        )
                    }

                    showAppointments -> {
                        BackHandler {
                            showAppointments = false
                            showProfile = true
                        }
                        AppointmentsScreen(
                            onBackClick = {
                                showAppointments = false
                                showProfile = true
                            },
                            onAddAppointmentClick = {
                                showAppointments = false
                                showAddAppointment = true
                            }
                        )
                    }

                    showAddAppointment -> {
                        BackHandler {
                            showAddAppointment = false
                            showAppointments = true
                        }
                        AddAppointmentScreen(
                            onBackClick = {
                                showAddAppointment = false
                                showAppointments = true
                            }
                        )
                    }

                    showAccount -> {
                        BackHandler {
                            showAccount = false
                            showProfile = true
                        }
                        AccountScreen(
                            onBackClick = {
                                showAccount = false
                                showProfile = true
                            }
                        )
                    }

                    else -> {
                        WelcomeScreen(
                            onLoginClick = { showLogin = true },
                            onSignupClick = { showSignup = true }
                        )
                    }
                }
            }
        }
    }
}

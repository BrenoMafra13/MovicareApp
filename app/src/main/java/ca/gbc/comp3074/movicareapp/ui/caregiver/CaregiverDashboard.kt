package ca.gbc.comp3074.movicareapp.ui.caregiver

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import ca.gbc.comp3074.movicareapp.ui.SeniorDetailsView
import ca.gbc.comp3074.movicareapp.ui.familymember.SeniorAvatarItem

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboard(
    userId: Long,
    onNavigateToNotifications: () -> Unit,
    onNavigateToInvitations: () -> Unit = {},
    onLogout: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    val viewModel = viewModel<CaregiverDashboardViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CaregiverDashboardViewModel(db, userId) as T
            }
        }
    )

    val linkedSeniors by viewModel.linkedSeniors.collectAsStateWithLifecycle()
    val selectedSeniorId by viewModel.selectedSeniorId.collectAsStateWithLifecycle()
    val hasPendingInvitations by viewModel.hasPendingInvitations.collectAsStateWithLifecycle()

    val user by produceState<UserEntity?>(initialValue = null, userId) {
        value = db.userDao().getById(userId)
    }

    var menuExpanded by remember { mutableStateOf(false) }

    val primaryPurple = Color(0xFF6750A4)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Caregiver Dashboard", fontSize = 18.sp)
                        user?.let {
                            Text(
                                "Hi, ${it.fullName.split(" ").firstOrNull() ?: it.username}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (hasPendingInvitations) {
                                Badge { Text("!") }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToInvitations) {
                            Icon(Icons.Default.Mail, contentDescription = "Invitations")
                        }
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                menuExpanded = false
                                onProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pending Invitations") },
                            onClick = {
                                menuExpanded = false
                                onNavigateToInvitations()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                menuExpanded = false
                                onLogout()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (linkedSeniors.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No seniors linked yet.",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "A Senior needs to invite you to view their details.",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Senior Selector
                Text(
                    "Linked Seniors",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(linkedSeniors) { senior ->
                        SeniorAvatarItem(
                            senior = senior,
                            isSelected = senior.id == selectedSeniorId,
                            primaryColor = primaryPurple,
                            onClick = { viewModel.selectSenior(senior.id) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                selectedSeniorId?.let { id ->
                    // READ-ONLY view of Senior Details
                    SeniorDetailsView(
                        seniorId = id
                    )
                }
            }
        }
    }
}

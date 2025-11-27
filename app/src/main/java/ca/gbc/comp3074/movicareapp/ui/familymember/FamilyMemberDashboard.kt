package ca.gbc.comp3074.movicareapp.ui.familymember

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import ca.gbc.comp3074.movicareapp.ui.SeniorDetailsView
import coil.compose.AsyncImage

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMemberDashboard(
    userId: Long,
    onNavigateToMedications: (Long) -> Unit,
    onNavigateToAppointments: (Long) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToInvitations: () -> Unit = {},
    onLogout: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    
    val viewModel = viewModel<FamilyDashboardViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FamilyDashboardViewModel(db, userId) as T
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

    val backgroundColor = Color(0xFFF7F2FF)
    val primaryPurple = Color(0xFF6750A4)

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (user?.avatarUri != null) {
                            AsyncImage(
                                model = user!!.avatarUri,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Family Dashboard", fontSize = 16.sp)
                            user?.let {
                                Text("Hi, ${it.fullName.split(" ").firstOrNull() ?: it.username}", fontSize = 12.sp, fontWeight = FontWeight.Normal)
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToInvitations) {
                        BadgedBox(badge = {
                            if (hasPendingInvitations) {
                                Badge { Text("!") }
                            }
                        }) {
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
                            onClick = { menuExpanded = false; onProfile() }
                        )
                        DropdownMenuItem(
                            text = { Text("Pending Invitations") },
                            onClick = { menuExpanded = false; onNavigateToInvitations() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = { menuExpanded = false; onLogout() }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            if (linkedSeniors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No seniors linked yet.", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Please accept an invitation sent by a Senior.", color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onNavigateToInvitations() }) {
                           Text("View Pending Invitations")
                        }
                    }
                }
            } else {
                // Senior Selector
                Text("Linked Seniors", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(linkedSeniors, key = { it.id }) { senior ->
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
                    // Force recomposition when ID changes
                    key(id) {
                        Column {
                            // Reuse the logic from SeniorDetailsView but inside this column flow
                            SeniorDetailsView(seniorId = id)
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Management Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onNavigateToMedications(id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Manage Meds")
                                }
                                Button(
                                    onClick = { onNavigateToAppointments(id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Manage Appts")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeniorAvatarItem(
    senior: UserEntity,
    isSelected: Boolean,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        val borderColor = if (isSelected) primaryColor else Color.Transparent
        val borderWidth = if (isSelected) 3.dp else 0.dp
        
        Surface(
            shape = CircleShape,
            border = BorderStroke(borderWidth, borderColor),
            modifier = Modifier.size(70.dp),
            color = Color.White
        ) {
            if (!senior.avatarUri.isNullOrBlank()) {
                AsyncImage(
                    model = senior.avatarUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = senior.fullName.split(" ").firstOrNull() ?: senior.username,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) primaryColor else Color.Unspecified
        )
    }
}

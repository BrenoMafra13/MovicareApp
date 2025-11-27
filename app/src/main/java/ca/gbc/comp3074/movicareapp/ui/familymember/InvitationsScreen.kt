package ca.gbc.comp3074.movicareapp.ui.familymember

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.UserRelationshipEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationsScreen(
    userId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val relationshipDao = remember { db.userRelationshipDao() }
    val userDao = remember { db.userDao() }
    val scope = rememberCoroutineScope()

    // Get pending requests (where I am the target)
    val pendingRequests by relationshipDao.getPendingRequests(userId).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitations") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (pendingRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending invitations.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(pendingRequests) { req ->
                        InvitationItem(
                            request = req,
                            userDao = userDao,
                            onAccept = {
                                scope.launch {
                                    relationshipDao.updateRelationship(req.copy(status = "ACCEPTED"))
                                }
                            },
                            onDecline = {
                                scope.launch {
                                    relationshipDao.deleteRelationship(req.requesterId, req.targetId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationItem(
    request: UserRelationshipEntity,
    userDao: ca.gbc.comp3074.movicareapp.data.db.UserDao,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var requesterName by remember { mutableStateOf("Loading...") }
    
    LaunchedEffect(request.requesterId) {
        val user = userDao.getById(request.requesterId)
        requesterName = user?.fullName ?: "Unknown User"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Invitation from", fontSize = 12.sp, color = Color.Gray)
                Text(requesterName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            Row {
                IconButton(onClick = onAccept) {
                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color(0xFF4CAF50))
                }
                IconButton(onClick = onDecline) {
                    Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.Red)
                }
            }
        }
    }
}

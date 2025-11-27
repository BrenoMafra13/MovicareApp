package ca.gbc.comp3074.movicareapp.ui.familymember

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.NotificationEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    userId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val notificationDao = remember { db.notificationDao() }
    val relationshipDao = remember { db.userRelationshipDao() }

    val linkedSeniors by relationshipDao.getAllAcceptedRelationships(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val seniorIds = linkedSeniors.map { if (it.requesterId == userId) it.targetId else it.requesterId }

    // This will automatically re-fetch when seniorIds list changes.
    val notifications by notificationDao.getNotificationsForSeniors(seniorIds).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationEntity) {
    val icon: ImageVector
    val iconColor: Color

    when (notification.type) {
        "MEDICATION" -> {
            icon = Icons.Default.MedicalServices
            iconColor = Color(0xFF1565C0)
        }
        "APPOINTMENT" -> {
            icon = Icons.Default.DateRange
            iconColor = Color(0xFFEF6C00)
        }
        "CHECK_IN" -> {
            icon = Icons.Default.CheckCircle
            iconColor = Color(0xFF4CAF50)
        }
        "PANIC" -> {
            icon = Icons.Default.Report
            iconColor = Color.Red
        }
        else -> {
            icon = Icons.Default.Info
            iconColor = Color.Gray
        }
    }
    
    val formattedDate = remember(notification.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        sdf.format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.message, fontWeight = FontWeight.SemiBold)
                Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

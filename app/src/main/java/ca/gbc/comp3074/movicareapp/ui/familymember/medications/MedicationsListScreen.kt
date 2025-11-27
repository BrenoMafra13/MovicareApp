package ca.gbc.comp3074.movicareapp.ui.familymember.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import ca.gbc.comp3074.movicareapp.data.db.MedicationEntity
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsListScreen(
    seniorId: Long,
    onBackClick: () -> Unit,
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val medicationDao = remember { db.medicationDao() }
    val scope = rememberCoroutineScope()

    val medications by medicationDao.getMedicationsByUserId(seniorId).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedication) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No medications recorded.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                items(medications) { med ->
                    MedicationItem(
                        medication = med,
                        onEdit = { onEditMedication(med.id) },
                        onDelete = {
                            scope.launch {
                                medicationDao.deleteMedication(med)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationItem(
    medication: MedicationEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Determine status
    val now = System.currentTimeMillis()
    val takenDate = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(medication.lastTakenDate), java.time.ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    
    var statusText = "Pending"
    var statusColor = Color.Gray
    
    if (takenDate.isEqual(today)) {
        statusText = "Taken"
        statusColor = Color(0xFF4CAF50)
    } else if (medication.snoozeUntil > now) {
        statusText = "Snoozed"
        statusColor = Color(0xFFFFA000)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(medication.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(statusText, fontWeight = FontWeight.Bold, color = statusColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${medication.dosage} • ${medication.time}", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

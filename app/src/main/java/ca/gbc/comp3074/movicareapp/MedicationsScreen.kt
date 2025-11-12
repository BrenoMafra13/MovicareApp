package ca.gbc.comp3074.movicareapp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.MedicationDao
import ca.gbc.comp3074.movicareapp.data.db.MedicationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationViewModel(val repository: AppDatabase, val userId: Long) : ViewModel() {
    val medications: StateFlow<List<MedicationEntity>> = repository
        .medicationDao()
        .getMedicationsByUserId(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    userId: Long,
    onBackClick: () -> Unit,
    onAddMedicationClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val medicationDao = remember { db.medicationDao() }
    val scope = rememberCoroutineScope()
    val viewModel = remember(userId) { MedicationViewModel(db, userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Medications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
            MedicationList(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                viewModel = viewModel,
                scope = scope,
                medicationDao = medicationDao
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddMedicationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text("Add Medication", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MedicationList(
    modifier: Modifier = Modifier,
    viewModel: MedicationViewModel,
    scope: CoroutineScope,
    medicationDao: MedicationDao
) {
    val data by viewModel.medications.collectAsStateWithLifecycle()

    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No medications yet.", color = Color.Gray, fontSize = 16.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(data, key = { it.id }) { medication ->
                MedicationCard(
                    medication = medication,
                    scope = scope,
                    medicationDao = medicationDao
                )
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: MedicationEntity,
    scope: CoroutineScope,
    medicationDao: MedicationDao
) {
    val scheduleText = when {
        !medication.startDate.isNullOrBlank() && !medication.endDate.isNullOrBlank() ->
            "${medication.startDate} - ${medication.endDate}"
        !medication.startDate.isNullOrBlank() -> medication.startDate
        !medication.endDate.isNullOrBlank() -> medication.endDate
        else -> "No date range set"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.medication),
                    contentDescription = "Medication Icon",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(medication.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(medication.dosage, fontSize = 14.sp, color = Color.Gray)
                    Text("$scheduleText - ${medication.time}", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = {
                    scope.launch { medicationDao.deleteMedication(medication) }
                },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Remove", fontSize = 14.sp)
            }
        }
    }
}

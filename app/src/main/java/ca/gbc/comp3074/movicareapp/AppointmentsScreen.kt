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
import ca.gbc.comp3074.movicareapp.data.db.AppointmentDao
import ca.gbc.comp3074.movicareapp.data.db.AppointmentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppointmentViewModel(val repository: AppDatabase, val userId: Long) : ViewModel() {
    val appointments: StateFlow<List<AppointmentEntity>> = repository
        .appointmentDao()
        .getAppointmentsByUserId(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    userId: Long,
    onBackClick: () -> Unit,
    onAddAppointmentClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val appointmentDao = remember { db.appointmentDao() }
    val scope = rememberCoroutineScope()
    val viewModel = remember(userId) { AppointmentViewModel(db, userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
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
            AppointmentList(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth(),
                viewModel = viewModel,
                scope = scope,
                appointmentDao = appointmentDao
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddAppointmentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3949AB),
                    contentColor = Color.White
                )
            ) {
                Text("Add Appointment", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun AppointmentList(
    modifier: Modifier = Modifier,
    viewModel: AppointmentViewModel,
    scope: CoroutineScope,
    appointmentDao: AppointmentDao
) {
    val data by viewModel.appointments.collectAsStateWithLifecycle()

    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No appointments yet.", color = Color.Gray, fontSize = 16.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(data, key = { it.id }) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    scope = scope,
                    appointmentDao = appointmentDao
                )
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentEntity,
    scope: CoroutineScope,
    appointmentDao: AppointmentDao
) {
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
                    painter = painterResource(id = R.drawable.appointment),
                    contentDescription = "Appointment Icon",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(appointment.type, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${appointment.day} at ${appointment.time}", fontSize = 16.sp, color = Color.Gray)
                }
            }
            Button(
                onClick = {
                    scope.launch { appointmentDao.deleteAppointment(appointment) }
                },
                modifier = Modifier.height(40.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Text("Remove", fontSize = 16.sp)
            }
        }
    }
}

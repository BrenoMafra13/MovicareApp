package ca.gbc.comp3074.movicareapp.ui.familymember.appointments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.AppointmentEntity
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentScreen(
    appointmentId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val appointmentDao = remember { AppDatabase.getInstance(context).appointmentDao() }
    val scope = rememberCoroutineScope()

    var appointment by remember { mutableStateOf<AppointmentEntity?>(null) }

    var appointmentType by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    LaunchedEffect(appointmentId) {
        appointment = appointmentDao.getAppointmentById(appointmentId)
    }

    LaunchedEffect(appointment) {
        appointment?.let {
            appointmentType = it.type
            day = it.day
            time = it.time
        }
    }

    val canSave = appointmentType.isNotBlank() && day.isNotBlank() && time.isNotBlank()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }
    val dateInteraction = remember { MutableInteractionSource() }
    val timeInteraction = remember { MutableInteractionSource() }

    fun openDatePicker() {
        val today = LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                day = picked.format(dateFormatter)
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        ).show()
    }

    fun openTimePicker() {
        val now = LocalTime.now()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val picked = LocalTime.of(hourOfDay, minute)
                time = picked.format(timeFormatter)
            },
            now.hour,
            now.minute,
            false
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Appointment") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = appointmentType,
                onValueChange = { appointmentType = it },
                label = { Text("Appointment type") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = day,
                    onValueChange = { },
                    label = { Text("Day") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.CalendarMonth, "Pick date") }
                )
                Box(modifier = Modifier.matchParentSize().clickable(interactionSource = dateInteraction, indication = null) { openDatePicker() })
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { },
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.AccessTime, "Pick time") }
                )
                Box(modifier = Modifier.matchParentSize().clickable(interactionSource = timeInteraction, indication = null) { openTimePicker() })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(50.dp).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Cancel", fontSize = 16.sp) }

                Button(
                    onClick = {
                        if (canSave) {
                            scope.launch {
                                val updatedAppt = appointment!!.copy(
                                    type = appointmentType.trim(),
                                    day = day.trim(),
                                    time = time.trim()
                                )
                                appointmentDao.updateAppointment(updatedAppt)
                                onBackClick()
                            }
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f).height(50.dp).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0), contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Save Changes", fontSize = 16.sp) }
            }
        }
    }
}

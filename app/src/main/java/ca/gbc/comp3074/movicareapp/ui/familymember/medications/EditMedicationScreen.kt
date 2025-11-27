package ca.gbc.comp3074.movicareapp.ui.familymember.medications

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.movicareapp.R
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.MedicationEntity
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationScreen(
    medicationId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val medicationDao = remember { AppDatabase.getInstance(context).medicationDao() }
    val scope = rememberCoroutineScope()

    var medication by remember { mutableStateOf<MedicationEntity?>(null) }

    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    LaunchedEffect(medicationId) {

    }

    LaunchedEffect(medication) {
        medication?.let {
            medicationName = it.name
            dosage = it.dosage
            time = it.time
            startDate = it.startDate ?: ""
            endDate = it.endDate ?: ""
        }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()) }
    val startDateInteraction = remember { MutableInteractionSource() }
    val endDateInteraction = remember { MutableInteractionSource() }
    val timeInteraction = remember { MutableInteractionSource() }

    val canSave = medicationName.isNotBlank() &&
        dosage.isNotBlank() &&
        time.isNotBlank() &&
        startDate.isNotBlank() &&
        endDate.isNotBlank()

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val today = LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(picked.format(dateFormatter))
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
                title = { Text("Edit Medication") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            OutlinedTextField(
                value = medicationName,
                onValueChange = { medicationName = it },
                label = { Text("Medication Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dosage") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(50.dp).padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White)
                ) { Text("Cancel") }

                Button(
                    onClick = {
                        if (canSave) {
                            scope.launch {
                                val updatedMed = medication!!.copy(
                                    name = medicationName.trim(),
                                    dosage = dosage.trim(),
                                    time = time.trim(),
                                    startDate = startDate.trim(),
                                    endDate = endDate.trim()
                                )
                                medicationDao.updateMedication(updatedMed)
                                onBackClick()
                            }
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f).height(50.dp).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0), contentColor = Color.White)
                ) { Text("Save Changes") }
            }
        }
    }
}

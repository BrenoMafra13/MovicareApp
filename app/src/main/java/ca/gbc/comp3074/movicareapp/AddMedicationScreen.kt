package ca.gbc.comp3074.movicareapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.MedicationEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    userId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val medicationDao = remember { AppDatabase.getInstance(context).medicationDao() }
    val scope = rememberCoroutineScope()

    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    val canSave = medicationName.isNotBlank() && dosage.isNotBlank() && day.isNotBlank() && time.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medication") },
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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.medication),
                    contentDescription = "Medication Image",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Medication", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = medicationName,
                onValueChange = { medicationName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Dosage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Day", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = day,
                onValueChange = { day = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Time", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        if (canSave) {
                            scope.launch {
                                medicationDao.insertMedication(
                                    MedicationEntity(
                                        ownerUserId = userId,
                                        name = medicationName.trim(),
                                        dosage = dosage.trim(),
                                        day = day.trim(),
                                        time = time.trim()
                                    )
                                )
                                medicationName = ""
                                dosage = ""
                                day = ""
                                time = ""
                                onBackClick()
                            }
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Save", fontSize = 16.sp)
                }
            }
        }
    }
}

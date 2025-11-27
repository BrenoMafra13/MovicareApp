package ca.gbc.comp3074.movicareapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gbc.comp3074.movicareapp.R
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.FamilyMemberEntity
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import coil.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    userId: Long,
    onAvatarClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val userDao = remember { db.userDao() }
    val medicationDao = remember { db.medicationDao() }
    val appointmentDao = remember { db.appointmentDao() }
    val familyMemberDao = remember { db.familyMemberDao() }

    var user by remember { mutableStateOf<UserEntity?>(null) }
    
    LaunchedEffect(userId) {
        user = userDao.getById(userId)
    }

    if (user == null) return // Wait for user to load

    // If the user is a Senior, show the Senior Dashboard
    if (user?.role?.equals("senior", ignoreCase = true) == true) {
        SeniorDashboard(
            user = user!!,
            userId = userId,
            onAvatarClick = onAvatarClick
        )
    } else {
        // If Family or Caregiver, show the Connection Dashboard
        FamilyCaregiverDashboard(
            userId = userId,
            onAvatarClick = onAvatarClick,
            onConnectionsClick = { /* Maybe navigate to familyMembers/userId which is now connections */ }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SeniorDashboard(
    user: UserEntity,
    userId: Long,
    onAvatarClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val medicationDao = remember { db.medicationDao() }
    val appointmentDao = remember { db.appointmentDao() }
    val familyMemberDao = remember { db.familyMemberDao() }
    
    // Retrieve all family members to support fallback calling
    val familyMembers by familyMemberDao.getFamilyMembersByUserId(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val primaryContact = familyMembers.firstOrNull()

    val allMedications by medicationDao.getMedicationsByUserId(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    val allAppointments by appointmentDao.getAppointmentsByUserId(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    
    val pendingMedications = remember(allMedications) {
        allMedications.filter { med ->
            val now = System.currentTimeMillis()
            if (med.snoozeUntil > now) return@filter false
            val takenDate = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(med.lastTakenDate), java.time.ZoneId.systemDefault()).toLocalDate()
            val today = LocalDate.now()
            if (takenDate.isEqual(today)) return@filter false
            true
        }.sortedBy { it.time }
    }

    val upcomingAppointments = remember(allAppointments) {
        allAppointments.filter { appt ->
            try {
                val now = LocalDateTime.now()
                val datePart = LocalDate.parse(appt.day, DateTimeFormatter.ISO_DATE) 
                val timePart = LocalTime.parse(appt.time, DateTimeFormatter.ISO_TIME)
                val apptDateTime = LocalDateTime.of(datePart, timePart)
                apptDateTime.isAfter(now)
            } catch (e: Exception) {
                true 
            }
        }.sortedWith(compareBy({ it.day }, { it.time }))
    }

    var showAssistanceDialog by remember { mutableStateOf(false) }
    var isHoldingPanic by remember { mutableStateOf(false) }
    var panicCountdown by remember { mutableStateOf(3) }
    var panicJob by remember { mutableStateOf<Job?>(null) }

    if (showAssistanceDialog) {
        AssistanceDialog(
            userId = userId,
            onDismiss = { showAssistanceDialog = false },
            onContactSelected = { contact ->
                showAssistanceDialog = false
                makePhoneCall(context, contact.phone)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (user.avatarUri.isNullOrBlank()) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(75.dp).clickable { onAvatarClick() }
                )
            } else {
                AsyncImage(
                    model = user.avatarUri,
                    placeholder = painterResource(R.drawable.profile),
                    error = painterResource(R.drawable.profile),
                    contentDescription = "Profile picture",
                    modifier = Modifier.size(75.dp).clickable { onAvatarClick() }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(user.fullName, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text(user.role.uppercase(), fontSize = 20.sp, color = Color.Gray)
            }
        }

        // Panic Button
        val panicBackgroundColor = if (isHoldingPanic) Color.Red else Color(0xFF4CAF50)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(panicBackgroundColor, shape = RoundedCornerShape(20.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHoldingPanic = true
                            panicCountdown = 3
                            
                            panicJob = scope.launch {
                                for (i in 3 downTo 1) {
                                    panicCountdown = i
                                    delay(1000)
                                }
                                isHoldingPanic = false
                                if (familyMembers.isNotEmpty()) {
                                    initiateEmergencyCallSequence(context, familyMembers)
                                } else {
                                    Toast.makeText(context, "No emergency contacts found!", Toast.LENGTH_LONG).show()
                                }
                            }

                            try {
                                tryAwaitRelease()
                            } catch (e: Exception) {
                                false
                            }

                            if (panicJob?.isActive == true) {
                                panicJob?.cancel()
                                primaryContact?.phone?.let { phone ->
                                    sendSms(context, phone, "I'm OK - Just checking in.")
                                    Toast.makeText(context, "Check-in sent to ${primaryContact.name}", Toast.LENGTH_SHORT).show()
                                } ?: run {
                                    Toast.makeText(context, "No contact defined!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            isHoldingPanic = false
                            panicJob = null
                            panicCountdown = 3
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isHoldingPanic) {
                    Text("Panic call in $panicCountdown...", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text("I'm OK", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Notifications, "Alarm", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Panic button\nhold 3 sec", fontSize = 20.sp, color = Color.White, lineHeight = 18.sp)
                    }
                }
            }
        }

        // Medication Section
        if (pendingMedications.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F8F8), shape = RoundedCornerShape(14.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("MEDICATIONS DUE", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                pendingMedications.forEach { med ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(med.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text("${med.dosage} • ${med.time}", fontSize = 16.sp, color = Color.Gray)
                        }
                        Row {
                            Button(
                                onClick = { 
                                    scope.launch {
                                        val updated = med.copy(lastTakenDate = System.currentTimeMillis(), snoozeUntil = 0L)
                                        medicationDao.updateMedication(updated)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("TAKE", fontSize = 12.sp) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { 
                                    scope.launch {
                                        val snoozeTime = System.currentTimeMillis() + 15 * 60 * 1000
                                        val updated = med.copy(snoozeUntil = snoozeTime)
                                        medicationDao.updateMedication(updated)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) { Text("SNOOZE", fontSize = 12.sp) }
                        }
                    }
                    HorizontalDivider(color = Color.LightGray)
                }
            }
        } else {
             Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F8F8), shape = RoundedCornerShape(14.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
                Text("No pending medications", color = Color.Gray, fontSize = 16.sp)
            }
        }

        // Appointments Section
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Appointments", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            if (upcomingAppointments.isNotEmpty()) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(upcomingAppointments) { appt ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Person, "Person", tint = Color(0xFFEF6C00), modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(appt.type, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.DateRange, "Date", tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${appt.day} at ${appt.time}", fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            } else {
                Text("No upcoming appointments", fontSize = 16.sp, color = Color.Gray)
            }
        }

        // Quick Help Request
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(modifier = Modifier.width(12.dp))
            Text("Quick Help Request", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(15.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(130.dp).background(Color.Black, shape = RoundedCornerShape(18.dp)).clickable { openUberOrRideApp(context) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Place, "Ride", tint = Color.White, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Ride", fontSize = 25.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.weight(1f).height(130.dp).background(Color(0xFFE0E0E0), shape = RoundedCornerShape(18.dp)).clickable { showAssistanceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Phone, "Assistance", tint = Color.Black, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Assistance", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FamilyCaregiverDashboard(
    userId: Long,
    onAvatarClick: () -> Unit,
    onConnectionsClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val relationshipDao = remember { db.userRelationshipDao() }
    val userDao = remember { db.userDao() }
    
    // Get accepted seniors
    val linkedSeniors by relationshipDao.getAllAcceptedRelationships(userId).collectAsStateWithLifecycle(initialValue = emptyList())
    
    // Selected senior state
    var selectedSeniorId by remember { mutableStateOf<Long?>(null) }
    
    // Auto-select first senior if available and none selected
    LaunchedEffect(linkedSeniors) {
        if (selectedSeniorId == null && linkedSeniors.isNotEmpty()) {

            val firstRel = linkedSeniors.first()
            val otherId = if (firstRel.requesterId == userId) firstRel.targetId else firstRel.requesterId
            selectedSeniorId = otherId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Seniors", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (linkedSeniors.isEmpty()) {
            Text("No linked seniors found. Please connect with a senior first.", color = Color.Gray)
        } else {
            // Dropdown or List to select Senior
            // For simplicity, a horizontal row of senior avatars/names
            LazyColumn(modifier = Modifier.height(100.dp)) {
                items(linkedSeniors) { rel ->
                    val otherId = if (rel.requesterId == userId) rel.targetId else rel.requesterId
                    SeniorSelectorItem(otherId, userDao, isSelected = otherId == selectedSeniorId) {
                        selectedSeniorId = otherId
                    }
                }
            }
            
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            if (selectedSeniorId != null) {
                SeniorDetailsView(selectedSeniorId!!)
            }
        }
    }
}

@Composable
fun SeniorSelectorItem(
    seniorId: Long, 
    userDao: ca.gbc.comp3074.movicareapp.data.db.UserDao, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    var senior by remember { mutableStateOf<UserEntity?>(null) }
    LaunchedEffect(seniorId) {
        senior = userDao.getById(seniorId)
    }
    
    if (senior != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (senior!!.avatarUri != null) {
                     AsyncImage(
                        model = senior!!.avatarUri,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(senior!!.fullName, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SeniorDetailsView(seniorId: Long) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val medicationDao = remember { db.medicationDao() }
    val appointmentDao = remember { db.appointmentDao() }
    
    val medications by medicationDao.getMedicationsByUserId(seniorId).collectAsStateWithLifecycle(initialValue = emptyList())
    val appointments by appointmentDao.getAppointmentsByUserId(seniorId).collectAsStateWithLifecycle(initialValue = emptyList())

    Column {
        Text("Medications Status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (medications.isEmpty()) {
            Text("No medications scheduled.", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                items(medications) { med ->
                    // Determine status text
                    val now = System.currentTimeMillis()
                    val takenDate = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(med.lastTakenDate), java.time.ZoneId.systemDefault()).toLocalDate()
                    val today = LocalDate.now()
                    
                    var statusText = "Pending"
                    var statusColor = Color.Gray
                    
                    if (takenDate.isEqual(today)) {
                        statusText = "Taken"
                        statusColor = Color(0xFF4CAF50) // Green
                    } else if (med.snoozeUntil > now) {
                        statusText = "Snoozed"
                        statusColor = Color(0xFFFFA000) // Amber
                    } else {
                        try {
                            val schedTime = LocalTime.parse(med.time)
                            if (LocalTime.now().isAfter(schedTime)) {
                                statusText = "Missed / Overdue"
                                statusColor = Color.Red
                            }
                        } catch (e: Exception) {}
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(med.name, fontWeight = FontWeight.Medium)
                            Text("${med.dosage} • ${med.time}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(statusText, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                    HorizontalDivider(color = Color.LightGray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Upcoming Appointments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        
        val futureAppointments = remember(appointments) {
             appointments.filter { appt ->
                try {
                    val now = LocalDateTime.now()
                    val datePart = LocalDate.parse(appt.day, DateTimeFormatter.ISO_DATE) 
                    val timePart = LocalTime.parse(appt.time, DateTimeFormatter.ISO_TIME)
                    val apptDateTime = LocalDateTime.of(datePart, timePart)
                    apptDateTime.isAfter(now)
                } catch (e: Exception) { true }
            }.sortedWith(compareBy({ it.day }, { it.time }))
        }
        
        if (futureAppointments.isEmpty()) {
             Text("No upcoming appointments.", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                items(futureAppointments) { appt ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Icon(Icons.Filled.DateRange, null, tint = Color(0xFF1565C0))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${appt.type} - ${appt.day} at ${appt.time}")
                    }
                }
            }
        }
    }
}

@Composable
fun AssistanceDialog(
    userId: Long,
    onDismiss: () -> Unit,
    onContactSelected: (FamilyMemberEntity) -> Unit
) {
    val context = LocalContext.current
    val familyMemberDao = remember { AppDatabase.getInstance(context).familyMemberDao() }
    val members by familyMemberDao.getFamilyMembersByUserId(userId).collectAsStateWithLifecycle(initialValue = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Contact to Call") },
        text = {
            LazyColumn {
                if (members.isEmpty()) {
                    item { Text("No contacts found.") }
                } else {
                    items(members) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContactSelected(member) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(member.phone ?: "", color = Color.Gray)
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Utility Functions

private fun makePhoneCall(context: Context, phoneNumber: String?) {
    if (phoneNumber.isNullOrBlank()) return
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    } else {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }
}

private fun sendSms(context: Context, phoneNumber: String?, message: String) {
    if (phoneNumber.isNullOrBlank()) return
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } else {
        Toast.makeText(context, "SMS Permission not granted. Cannot send Check-in.", Toast.LENGTH_LONG).show()
    }
}

private fun openUberOrRideApp(context: Context) {
    try {
        val pm = context.packageManager
        try {
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("uber://?action=setPickup&pickup=my_location")
            }
            context.startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException) {
            val url = "https://m.uber.com/ul"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun initiateEmergencyCallSequence(context: Context, contacts: List<FamilyMemberEntity>) {
    if (contacts.isEmpty()) return

    val callManager = EmergencyCallManager.getInstance(context)
    callManager.startSequence(contacts)
}

class EmergencyCallManager private constructor(private val context: Context) {
    
    private var contactsQueue: MutableList<FamilyMemberEntity> = mutableListOf()
    private var currentContactIndex = 0
    private var lastCallStartTime: Long = 0
    private var isSequenceActive = false

    companion object {
        private var INSTANCE: EmergencyCallManager? = null
        fun getInstance(context: Context): EmergencyCallManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EmergencyCallManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun startSequence(contacts: List<FamilyMemberEntity>) {
        if (contacts.isEmpty()) return
        contactsQueue = contacts.toMutableList()
        currentContactIndex = 0
        isSequenceActive = true
        registerPhoneStateListener()
        makeCall(contactsQueue[currentContactIndex])
    }

    private fun makeCall(contact: FamilyMemberEntity) {
        contact.phone?.let { phone ->
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                lastCallStartTime = System.currentTimeMillis()
                val intent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:$phone")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Toast.makeText(context, "Calling ${contact.name}...", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                isSequenceActive = false
            }
        }
    }

    private fun registerPhoneStateListener() {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                if (!isSequenceActive) return

                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        val duration = System.currentTimeMillis() - lastCallStartTime
                        
                        if (duration > 5000 && duration < 45000) { 
                             tryNextContact()
                        } else if (duration <= 5000) {
                             tryNextContact()
                        } else {
                            isSequenceActive = false
                        }
                    }
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private fun tryNextContact() {
        currentContactIndex++
        if (currentContactIndex < contactsQueue.size) {
            makeCall(contactsQueue[currentContactIndex])
        } else {
            isSequenceActive = false
            Toast.makeText(context, "No more contacts to call.", Toast.LENGTH_LONG).show()
        }
    }
}

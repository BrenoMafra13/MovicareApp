package ca.gbc.comp3074.movicareapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
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
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyHealthScreen(
    userId: Long = 1L,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val userDao = remember { db.userDao() }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserEntity?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    // Editable states
    var conditions by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        user = userDao.getById(userId)
        user?.let {
            conditions = it.conditions
            allergies = it.allergies
            height = it.height
            weight = it.weight
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Health") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isEditing) {
                            scope.launch {
                                userDao.updateHealthInfo(userId, conditions, allergies, height, weight)
                                user = userDao.getById(userId)
                                isEditing = false
                            }
                        } else {
                            isEditing = true
                        }
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit"
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Profile Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                if (user?.avatarUri != null) {
                    AsyncImage(
                        model = user?.avatarUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(80.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = user?.fullName ?: "Loading...",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.role ?: "",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            // Medical Conditions Section
            HealthSection(
                title = "Medical Conditions",
                content = conditions,
                isEditing = isEditing,
                onValueChange = { conditions = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Allergies Section
            HealthSection(
                title = "Allergies",
                content = allergies,
                isEditing = isEditing,
                onValueChange = { allergies = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vitals Row (Height & Weight)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthCard(
                    title = "Height",
                    value = height,
                    unit = "cm",
                    isEditing = isEditing,
                    onValueChange = { height = it },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                HealthCard(
                    title = "Weight",
                    value = weight,
                    unit = "kg",
                    isEditing = isEditing,
                    onValueChange = { weight = it },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HealthSection(
    title: String,
    content: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = content,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    placeholder = { Text("Enter $title...") },
                    minLines = 3
                )
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (content.isBlank()) {
                        Text("None listed", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        val items = content.split("\n").filter { it.isNotBlank() }
                        if (items.isEmpty()) {
                             Text(content)
                        } else {
                            items.forEach { item ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("• ", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                    Text(item)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthCard(
    title: String,
    value: String,
    unit: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text(unit) }
                    )
                } else {
                    Text(
                        text = if (value.isBlank()) "-- $unit" else "$value $unit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

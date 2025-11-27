package ca.gbc.comp3074.movicareapp.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.movicareapp.auth.AuthViewModel
import coil.compose.AsyncImage

private val roles = listOf("senior","family","caregiver","volunteer")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onRegistrationSuccess: (userId: Long, role: String) -> Unit,
    onLoginClick: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    val ui by vm.signupUi.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") } // For Invite Linking

    var role by remember { mutableStateOf(roles.first()) }
    var roleMenu by remember { mutableStateOf(false) }

    // Caregiver specific fields
    var specialty by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var idDocumentUri by remember { mutableStateOf<String?>(null) }
    var selfieUri by remember { mutableStateOf<String?>(null) }
    var certificationUri by remember { mutableStateOf<String?>(null) }
    
    var avatarUri by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> avatarUri = uri?.toString() }

    LaunchedEffect(ui.userId, ui.successRole) {
        val id = ui.userId
        val r = ui.successRole
        if (id != null && r != null) onRegistrationSuccess(id, r)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up") },
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
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(32.dp))

            if (avatarUri == null) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Avatar",
                    tint = Color.Gray,
                    modifier = Modifier.size(120.dp)
                )
            } else {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Selected Avatar",
                    modifier = Modifier.size(120.dp)
                )
            }
            TextButton(onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text(if (avatarUri == null) "Choose photo" else "Change photo") }

            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = roleMenu,
                onExpandedChange = { roleMenu = !roleMenu }
            ) {
                OutlinedTextField(
                    value = role.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Role") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = roleMenu,
                    onDismissRequest = { roleMenu = false }
                ) {
                    roles.forEach {
                        DropdownMenuItem(
                            text = { Text(it.replaceFirstChar { c -> c.uppercase() }) },
                            onClick = { role = it; roleMenu = false }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = fullName, onValueChange = { fullName = it },
                label = { Text("Full name") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Username") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phoneNumber, onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            
            // Caregiver Specific Section
            if (role == "caregiver") {
                Text("Caregiver Details", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = specialty, onValueChange = { specialty = it },
                    label = { Text("Specialty (e.g. Nurse)") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = licenseNumber, onValueChange = { licenseNumber = it },
                    label = { Text("Professional License #") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                
                Text("Verification Documents", fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
                Text("Required: ID, Selfie, Certificate", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                

                Button(onClick = { /* Mock ID Picker */ idDocumentUri = "mock_id.jpg" }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (idDocumentUri != null) "ID Uploaded ✓" else "Upload Gov ID")
                }
                Button(onClick = { /* Mock Selfie Picker */ selfieUri = "mock_selfie.jpg" }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selfieUri != null) "Selfie Uploaded ✓" else "Upload Selfie")
                }
                Button(onClick = { /* Mock Cert Picker */ certificationUri = "mock_cert.jpg" }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (certificationUri != null) "Certificate Uploaded ✓" else "Upload Certificate")
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirm, onValueChange = { confirm = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            ui.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (password != confirm) return@Button
                    vm.signUp(
                        fullName.trim(),
                        username.trim(),
                        email.trim(),
                        password,
                        role,
                        avatarUri,
                        street,
                        unit,
                        postalCode,
                        phoneNumber.trim() // Corrected here
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !ui.loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B75BC),
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    if (ui.loading) "Creating…" else "Sign Up",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = "Log In",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

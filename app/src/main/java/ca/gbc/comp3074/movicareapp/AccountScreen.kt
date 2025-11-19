package ca.gbc.comp3074.movicareapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    userId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val userDao = remember { AppDatabase.getInstance(context).userDao() }
    val scope = rememberCoroutineScope()

    var street by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    var user by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(userId) {
        user = withContext(Dispatchers.IO) { userDao.getById(userId) }
    }

    val avatar = user?.avatarUri

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit account", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (!avatar.isNullOrBlank()) {
                AsyncImage(
                    model = avatar,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(170.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(170.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Street:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )

                Text("Unit:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )

                Text("Postal Code:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp)
                        .padding(end = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    )
                ) {
                    Text("BACK", fontSize = 18.sp)
                }

                Button(
                    onClick = {
                        if(street.isNotBlank() || unit.isNotBlank() || postalCode.isNotBlank()) {
                            // update user with new address info
                            scope.launch {
                                userDao.updateUserAddress(
                                    id = userId,
                                    street = street,
                                    unit = unit,
                                    postalCode = postalCode
                                )

                                Toast.makeText(context, "Update successful.", Toast.LENGTH_SHORT).show()

                                street = ""
                                unit = ""
                                postalCode = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp)
                        .padding(start = 8.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0),
                        contentColor = Color.White
                    )
                ) {
                    Text("SAVE", fontSize = 18.sp)
                }
            }
        }
    }
}

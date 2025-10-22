package ca.gbc.comp3074.movicareapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onBackClick: () -> Unit) {
    var street by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

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
            Image(
                painter = painterResource(id = R.drawable.profile),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(170.dp)
            )

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
                    onClick = { },
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

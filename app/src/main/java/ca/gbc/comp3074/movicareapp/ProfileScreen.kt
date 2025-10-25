package ca.gbc.comp3074.movicareapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.UserEntity
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    userId: Long,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMyHealthClick: () -> Unit,
    onMedicationsClick: () -> Unit,
    onFamilyClick: () -> Unit,
    onAppointmentsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val userDao = remember { AppDatabase.getInstance(context).userDao() }
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(userId) {
        user = withContext(Dispatchers.IO) { userDao.getById(userId) }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                withContext(Dispatchers.IO) { userDao.updateAvatar(userId, uri.toString()) }
                user = user?.copy(avatarUri = uri.toString())
                    ?: withContext(Dispatchers.IO) { userDao.getById(userId) }
            }
        }
    }

    val name = user?.fullName ?: "User #$userId"
    val role = user?.role?.uppercase() ?: "SENIOR"
    val avatar = user?.avatarUri

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val onPick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                if (!avatar.isNullOrBlank()) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(130.dp)
                            .clickable { onPick() }
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(130.dp)
                            .clickable { onPick() }
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(name, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text(role, fontSize = 18.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("My Address:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Not set", fontSize = 16.sp, lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            @Composable
            fun MenuItem(title: String, onClick: () -> Unit) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { onClick() },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, fontSize = 18.sp)
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Arrow"
                        )
                    }
                }
            }

            MenuItem("My Health", onMyHealthClick)
            MenuItem("Medications", onMedicationsClick)
            MenuItem("Family Members", onFamilyClick)
            MenuItem("Appointments", onAppointmentsClick)
            MenuItem("Account", onAccountClick)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Back", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
            ) {
                Text("Log out", color = Color(0xFFE3F2FD), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

package ca.gbc.comp3074.movicareapp

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.FamilyMemberDao
import ca.gbc.comp3074.movicareapp.data.db.FamilyMemberEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyMemberViewModel(val repository: AppDatabase, val userId: Long): ViewModel() {
    val familyMembers: StateFlow<List<FamilyMemberEntity>> = repository
        .familyMemberDao()
        .getFamilyMembersByUserId(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    userId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val familyMemberDao = remember { AppDatabase.getInstance(context).familyMemberDao() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var reachedMaxFamilyMembers by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Members", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            FamilyMembersList(
                viewModel = FamilyMemberViewModel(repository = db, userId = userId),
                scope = scope,
                familyMemberDao = familyMemberDao
                )

            if(reachedMaxFamilyMembers) {
                Text(
                   text = "You can have a maximum of 2 family people registered.",
                   fontSize = 17.sp,
                   textAlign = TextAlign.Center,
                   modifier = Modifier
                       .fillMaxWidth()
                       .padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Add a Family Member:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", fontSize = 16.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    val sanitized = input.filter { it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')' }
                    if (sanitized.length <= 20) {
                        phone = sanitized
                    }
                },
                label = { Text("Phone Number", fontSize = 16.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = relation,
                onValueChange = { relation = it },
                label = { Text("Relationship to Family Member", fontSize = 16.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    if(name.isNotBlank() && phone.isNotBlank() && relation.isNotBlank()) {
                        scope.launch {
                            if(familyMemberDao.getFamilyMemberCount(userId).first() == 2) {
                                reachedMaxFamilyMembers = true
                            } else {
                                familyMemberDao.insert(FamilyMemberEntity(
                                    ownerUserId = userId,
                                    name = name,
                                    phone = phone,
                                    relation = relation
                                ))
                                reachedMaxFamilyMembers = false
                            }

                            name = ""
                            phone = ""
                            relation = ""
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                )
            ) {
                Text("Add Family Member", fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun FamilyMembersList(
    viewModel: FamilyMemberViewModel,
    scope: CoroutineScope,
    familyMemberDao: FamilyMemberDao
) {
    val data by viewModel.familyMembers.collectAsStateWithLifecycle()
    LazyColumn {
        items(data) { item ->
            FamilyMember(item, scope, familyMemberDao)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMember(
    familyMember: FamilyMemberEntity,
    scope: CoroutineScope,
    familyMemberDao: FamilyMemberDao
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = familyMember.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            familyMember.relation?.let { Text(text = it, fontSize = 18.sp, color = Color.Gray) }
            familyMember.phone?.let { Text(text = it, fontSize = 16.sp, color = Color.Gray) }
        }
        Button(
            onClick = {
                scope.launch {
                    familyMemberDao.deleteFamilyMember(familyMember)
                }
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

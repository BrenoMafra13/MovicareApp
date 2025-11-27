package ca.gbc.comp3074.movicareapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.gbc.comp3074.movicareapp.data.db.AppDatabase
import ca.gbc.comp3074.movicareapp.data.db.FamilyMemberEntity
import ca.gbc.comp3074.movicareapp.data.db.UserRelationshipEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    userId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val familyMemberDao = remember { db.familyMemberDao() }
    val relationshipDao = remember { db.userRelationshipDao() }
    val userDao = remember { db.userDao() }
    val scope = rememberCoroutineScope()

    // Local list of family members (for panic button & contact list)
    val familyMembers by familyMemberDao.getFamilyMembersByUserId(userId).collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Family") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (familyMembers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No family members added yet.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(familyMembers) { member ->
                        FamilyMemberListItem(member, onDelete = {
                            scope.launch {
                                familyMemberDao.deleteFamilyMember(member)

                            }
                        })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFamilyMemberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, relation, phone ->
                scope.launch {
                    // 1. Add to local list (for Panic Button)
                    familyMemberDao.insert(
                        FamilyMemberEntity(
                            ownerUserId = userId,
                            name = name,
                            relation = relation,
                            phone = phone
                        )
                    )
                    
                    // 2. Invitation: Check if user exists with this phone number
                    val existingUser = userDao.getByPhoneNumber(phone)
                    if (existingUser != null) {
                        // Create Pending Relationship
                        val existingRel = relationshipDao.getRelationship(userId, existingUser.id)
                        if (existingRel == null) {
                            relationshipDao.insertRelationship(
                                UserRelationshipEntity(
                                    requesterId = userId,
                                    targetId = existingUser.id,
                                    status = "PENDING"
                                )
                            )
                            Toast.makeText(context, "Invitation sent to ${existingUser.fullName}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Already linked or pending with ${existingUser.fullName}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Added to contacts. Invite them to join Movicare!", Toast.LENGTH_LONG).show()
                    }
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FamilyMemberListItem(
    member: FamilyMemberEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${member.relation} • ${member.phone}", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AddFamilyMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = relation, onValueChange = { relation = it }, label = { Text("Relationship") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && phone.isNotBlank()) onAdd(name, relation, phone) }) {
                Text("Add & Invite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

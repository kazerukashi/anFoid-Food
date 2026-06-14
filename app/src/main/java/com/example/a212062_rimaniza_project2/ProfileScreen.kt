package com.example.a212062_rimaniza_project2

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a212062_rimaniza_project2.ui.theme.AppPink

@Composable
fun ProfileScreen(
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: FoodViewModel
) {
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val userProfile = viewModel.userProfile

    LaunchedEffect(currentUser) {
        currentUser?.let {
            viewModel.fetchUserProfile(it.uid)
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile?.name ?: "") }
    var editBio by remember { mutableStateOf(userProfile?.bio ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = AppPink)
            }
            
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = AppPink,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = AppPink
            )
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        // Content
        if (currentUser == null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = AppPink.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Login to interact with other users on Community!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { /* Implement real Auth or mock login */ },
                    colors = ButtonDefaults.buttonColors(containerColor = AppPink),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(50.dp)
                ) {
                    Text("Sign in", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = AppPink
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = userProfile?.name ?: "No Name",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = currentUser.email ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Bio", fontWeight = FontWeight.Bold, color = AppPink)
                        Text(userProfile?.bio ?: "No bio yet.")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        editName = userProfile?.name ?: ""
                        editBio = userProfile?.bio ?: ""
                        showEditDialog = true 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppPink)
                ) {
                    Text("Edit Profile")
                }
            }
        }
    }

    if (showEditDialog && currentUser != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") }
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveUserProfile(UserProfile(
                        id = currentUser.uid,
                        name = editName,
                        email = currentUser.email ?: "",
                        bio = editBio
                    ))
                    showEditDialog = false
                }) {
                    Text("Save", color = AppPink)
                }
            }
        )
    }
}

package com.example.a212062_rimaniza_project2

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.a212062_rimaniza_project2.ui.theme.AppPink
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToPostDetail: (Post) -> Unit,
    onNavigateToAddFood: () -> Unit = {},
    viewModel: FoodViewModel,
    targetUserId: String? = null
) {
    val currentUser = viewModel.currentUser
    val isViewingOwnProfile = targetUserId == null || targetUserId == currentUser?.uid
    
    var viewedProfile by remember { mutableStateOf<UserProfile?>(null) }
    val userProfile = if (isViewingOwnProfile) viewModel.userProfile else viewedProfile
    
    val posts by viewModel.posts.collectAsState()
    val allFoodItems by viewModel.allFoodItems.collectAsState()
    
    val displayUserId = targetUserId ?: currentUser?.uid
    val displayPosts = remember(posts, displayUserId) {
        posts.filter { it.userId == displayUserId }
    }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val googleAuthHelper = remember { GoogleAuthHelper(context) }

    LaunchedEffect(displayUserId) {
        displayUserId?.let { id ->
            if (isViewingOwnProfile) {
                viewModel.fetchUserProfile(id)
            } else {
                viewedProfile = viewModel.getUserProfile(id)
            }
            viewModel.fetchPosts()
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editOldPassword by remember { mutableStateOf("") }
    var editNewPassword by remember { mutableStateOf("") }
    var showPasswordFields by remember { mutableStateOf(false) }
    var editProfilePic by remember { mutableStateOf<String?>(null) }
    var editErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showForgetPasswordDialog by remember { mutableStateOf(false) }
    var forgetPasswordEmail by remember { mutableStateOf("") }

    fun createImageUri(): Uri {
        val file = java.io.File(context.cacheDir, "temp_profile_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, tempCameraUri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, tempCameraUri!!)
                ImageDecoder.decodeBitmap(source)
            }
            editProfilePic = viewModel.encodeImage(bitmap)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri()
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(userProfile, showEditDialog) {
        if (showEditDialog && userProfile != null) {
            editName = userProfile.name
            editBio = userProfile.bio
            editEmail = userProfile.email
            editProfilePic = userProfile.profilePicUrl
            editOldPassword = ""
            editNewPassword = ""
            showPasswordFields = false
            editErrorMessage = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            editProfilePic = viewModel.encodeImage(bitmap)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                
                Text(
                    text = if (isViewingOwnProfile) "Profile" else "User Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = AppPink
                )

                if (isViewingOwnProfile && currentUser != null) {
                    var showDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        IconButton(onClick = { showDropdown = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = AppPink)
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Profile") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showDropdown = false
                                    editName = userProfile?.name ?: ""
                                    editBio = userProfile?.bio ?: ""
                                    showEditDialog = true
                                }
                            )
                            if (viewModel.isAdmin) {
                                DropdownMenuItem(
                                    text = { Text("Add Food to Database") },
                                    leadingIcon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                                    onClick = {
                                        showDropdown = false
                                        onNavigateToAddFood()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showDropdown = false
                                    showSignOutDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            if (currentUser == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Log in to view your profile and shared recipes.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToAuth,
                            colors = ButtonDefaults.buttonColors(containerColor = AppPink)
                        ) {
                            Text("Sign In / Sign Up")
                        }
                    }
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val pfpBitmap = remember(userProfile?.profilePicUrl) {
                        userProfile?.profilePicUrl?.let { viewModel.decodeImage(it) }
                    }

                    if (pfpBitmap != null) {
                        Image(
                            bitmap = pfpBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_pfp),
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
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
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = if (isViewingOwnProfile) "My Shared Recipes" else "${userProfile?.name ?: "User"}'s Shared Recipes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppPink,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (displayPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isViewingOwnProfile) "You haven't shared any recipes yet." else "${userProfile?.name ?: "User"} hasn't shared any recipes yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(displayPosts) { post ->
                        val isFav = allFoodItems.find { it.id == post.id }?.isFavourite ?: false
                        PostItem(
                            post = post,
                            onClick = { onNavigateToPostDetail(post) },
                            isFavourite = isFav,
                            onFavouriteToggle = { viewModel.togglePostFavourite(post) },
                            viewModel = viewModel
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        scope.launch {
                            googleAuthHelper.signOut()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog && currentUser != null) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    Button(
                        onClick = { showImageSourceDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Profile Picture")
                    }
                    
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    if (!showPasswordFields) {
                        TextButton(
                            onClick = { showPasswordFields = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Change Password", color = AppPink)
                        }
                    } else {
                        Text("Change Password", fontWeight = FontWeight.Bold, color = AppPink)
                        
                        OutlinedTextField(
                            value = editOldPassword,
                            onValueChange = { editOldPassword = it },
                            label = { Text("Old Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = editNewPassword,
                            onValueChange = { editNewPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true
                        )

                        TextButton(
                            onClick = { showForgetPasswordDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Forget Password?", style = MaterialTheme.typography.bodySmall, color = AppPink)
                        }
                    }

                    if (editErrorMessage != null) {
                        Text(
                            text = editErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isSaving = true
                        editErrorMessage = null
                        
                        scope.launch {
                            try {
                                // 1. Handle Email/Password change first as they might require re-auth
                                val user = currentUser
                                
                                if (showPasswordFields && editNewPassword.isNotBlank()) {
                                    if (editOldPassword.isBlank()) {
                                        editErrorMessage = "Please enter old password to change password"
                                        isSaving = false
                                        return@launch
                                    }
                                    
                                    // Re-authenticate
                                    val credential = EmailAuthProvider.getCredential(user.email!!, editOldPassword)
                                    user.reauthenticate(credential).await()
                                    
                                    // Update Password
                                    user.updatePassword(editNewPassword).await()
                                }

                                if (editEmail != user.email) {
                                    // If we didn't just re-auth for password, might need it for email
                                    if (!showPasswordFields || editOldPassword.isBlank()) {
                                        editErrorMessage = "Please enter your password to change email"
                                        showPasswordFields = true
                                        isSaving = false
                                        return@launch
                                    }
                                    
                                    // Re-authenticate if not already done
                                    val credential = EmailAuthProvider.getCredential(user.email!!, editOldPassword)
                                    user.reauthenticate(credential).await()
                                    
                                    user.verifyBeforeUpdateEmail(editEmail).await()
                                }

                                // 2. Update Firestore Profile
                                viewModel.saveUserProfile(UserProfile(
                                    id = user.uid,
                                    name = editName,
                                    email = editEmail,
                                    bio = editBio,
                                    profilePicUrl = editProfilePic,
                                    role = userProfile?.role ?: "user"
                                ))
                                
                                showEditDialog = false
                            } catch (e: Exception) {
                                editErrorMessage = e.localizedMessage ?: "Failed to update profile"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Text("Save", color = AppPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !isSaving) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showForgetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgetPasswordDialog = false },
            title = { Text("Forget Password?") },
            text = {
                Column {
                    Text("Enter your email or username to receive a password reset link.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forgetPasswordEmail,
                        onValueChange = { forgetPasswordEmail = it },
                        label = { Text("Email or Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendPasswordReset(forgetPasswordEmail, 
                            onSuccess = {
                                showForgetPasswordDialog = false
                                editErrorMessage = "Reset link sent to your email"
                            },
                            onFailure = { error ->
                                editErrorMessage = error
                            }
                        )
                    }
                ) {
                    Text("Send Reset Link", color = AppPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgetPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Select Image Source") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Camera") },
                        leadingContent = { Icon(Icons.Filled.CameraAlt, null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val uri = createImageUri()
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Gallery") },
                        leadingContent = { Icon(Icons.Filled.PhotoLibrary, null) },
                        modifier = Modifier.clickable {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            },
            confirmButton = {}
        )
    }
}

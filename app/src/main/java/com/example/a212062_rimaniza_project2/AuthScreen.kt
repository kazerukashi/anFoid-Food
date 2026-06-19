package com.example.a212062_rimaniza_project2

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a212062_rimaniza_project2.ui.theme.AppPink
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: FoodViewModel,
    isFirstLaunch: Boolean = false,
    onSkip: (() -> Unit)? = null
) {
    var isLogin by remember { mutableStateOf(!isFirstLaunch) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgetPasswordDialog by remember { mutableStateOf(false) }
    var forgetPasswordEmailOrUsername by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        capturedBitmap = null
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        capturedBitmap = bitmap
        selectedImageUri = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(if (isFirstLaunch) 48.dp else 24.dp))

            if (isFirstLaunch) {
                Text(
                    text = "Welcome to anFoid Food",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppPink
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = if (isLogin) "Login" else "Sign Up",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AppPink
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isLogin) {
                // Profile Picture Selector
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        capturedBitmap != null -> {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        selectedImageUri != null -> {
                            val bitmap = remember(selectedImageUri) {
                                if (Build.VERSION.SDK_INT < 28) {
                                    @Suppress("DEPRECATION")
                                    MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
                                } else {
                                    val source = ImageDecoder.createSource(context.contentResolver, selectedImageUri!!)
                                    ImageDecoder.decodeBitmap(source)
                                }
                            }
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.default_pfp),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "Add Photo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text(
                    "Profile Picture (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AppPink) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(if (isLogin) "Email or Username" else "Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AppPink) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AppPink) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = AppPink
                        )
                    }
                }
            )

            if (isLogin) {
                TextButton(
                    onClick = { showForgetPasswordDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forget Password?", color = AppPink, style = MaterialTheme.typography.bodySmall)
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (!viewModel.isOnline()) {
                        errorMessage = "Not Connected to Internet. Please check your connection."
                        return@Button
                    }

                    if (email.isBlank() || password.isBlank() || (!isLogin && username.isBlank())) {
                        errorMessage = "Please fill in all mandatory fields."
                        return@Button
                    }
                    
                    if (!isLogin) {
                        val regex = "^[a-zA-Z0-9]*$".toRegex()
                        if (!regex.matches(username)) {
                            errorMessage = "Username cannot contain special characters or whitespace."
                            return@Button
                        }
                    }

                    isLoading = true
                    errorMessage = null
                    
                    scope.launch {
                        if (isLogin) {
                            var loginEmail = email
                            if (!email.contains("@")) {
                                // User entered a username, try to find email
                                loginEmail = viewModel.getEmailByUsername(email) ?: ""
                                if (loginEmail.isEmpty()) {
                                    isLoading = false
                                    errorMessage = "Username or password is incorrect. Please try again."
                                    return@launch
                                }
                            }

                            auth.signInWithEmailAndPassword(loginEmail, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) onAuthSuccess()
                                    else errorMessage = "Email or password is incorrect. Please try again."
                                }
                        } else {
                            if (viewModel.isUsernameTaken(username)) {
                                isLoading = false
                                errorMessage = "Username is already taken. Please choose another one."
                                return@launch
                            }
                            // We'll let Firebase Auth handle the email uniqueness check
                            // as it's more accurate if the user manually deleted Auth accounts.

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = task.result?.user
                                        if (user != null) {
                                            val profilePic = when {
                                                capturedBitmap != null -> viewModel.encodeImage(capturedBitmap!!)
                                                selectedImageUri != null -> {
                                                    val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                                        @Suppress("DEPRECATION")
                                                        MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
                                                    } else {
                                                        val source = ImageDecoder.createSource(context.contentResolver, selectedImageUri!!)
                                                        ImageDecoder.decodeBitmap(source)
                                                    }
                                                    viewModel.encodeImage(bitmap)
                                                }
                                                else -> null
                                            }
                                            viewModel.saveUserProfile(UserProfile(
                                                id = user.uid,
                                                name = username,
                                                email = email,
                                                bio = "Hello, I am $username",
                                                profilePicUrl = profilePic,
                                                role = "user"
                                            ))
                                        }
                                        isLoading = false
                                        onAuthSuccess()
                                    } else {
                                        isLoading = false
                                        val error = task.exception?.localizedMessage ?: "Sign up failed"
                                        errorMessage = if (error.contains("already in use", ignoreCase = true)) {
                                            "Email is already in use. Please use another one or login."
                                        } else {
                                            error
                                        }
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPink),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text(if (isLogin) "Login" else "Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { isLogin = !isLogin }) {
                Text(
                    if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login",
                    color = AppPink
                )
            }
        }

        // Image Source Dialog
        if (showImageSourceDialog) {
            AlertDialog(
                onDismissRequest = { showImageSourceDialog = false },
                title = { Text("Choose Profile Picture") },
                text = {
                    Column {
                        ListItem(
                            headlineContent = { Text("Gallery") },
                            leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                            modifier = Modifier.clickable {
                                galleryLauncher.launch("image/*")
                                showImageSourceDialog = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Camera") },
                            leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                            modifier = Modifier.clickable {
                                cameraLauncher.launch(null)
                                showImageSourceDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showImageSourceDialog = false }) {
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
                            value = forgetPasswordEmailOrUsername,
                            onValueChange = { forgetPasswordEmailOrUsername = it },
                            label = { Text("Email or Username") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.sendPasswordReset(forgetPasswordEmailOrUsername,
                                onSuccess = {
                                    showForgetPasswordDialog = false
                                    errorMessage = "Password reset link sent to your email."
                                },
                                onFailure = { error ->
                                    errorMessage = error
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

        // Skip Button for First Launch
        if (isFirstLaunch && onSkip != null) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp)
            ) {
                Text("Skip", color = AppPink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else if (!isFirstLaunch) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppPink)
            }
        }
    }
}

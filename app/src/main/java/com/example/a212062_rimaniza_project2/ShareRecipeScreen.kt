package com.example.a212062_rimaniza_project2

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.a212062_rimaniza_project2.ui.theme.AppPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRecipeScreen(
    onBackClick: () -> Unit,
    onPostSuccess: () -> Unit,
    viewModel: FoodViewModel,
    editPostId: String? = null,
    isDatabaseItem: Boolean = false,
    editFoodId: String? = null
) {
    val posts by viewModel.posts.collectAsState()
    val allFoodItems by viewModel.allFoodItems.collectAsState()

    val editPost = remember(editPostId, posts) {
        posts.find { it.id == editPostId }
    }
    
    val editFood = remember(editFoodId, allFoodItems) {
        allFoodItems.find { it.id == editFoodId }
    }

    var foodName by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("Malay") }
    var components by remember { mutableStateOf(listOf(RecipeSection("", emptyList(), emptyList()))) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun createImageUri(): Uri {
        val file = java.io.File(context.cacheDir, "temp_post_${System.currentTimeMillis()}.jpg")
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
            selectedImageUri = tempCameraUri
            existingImageUrl = null
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

    var ingredientsTexts by remember { mutableStateOf(listOf("")) }
    var instructionsTexts by remember { mutableStateOf(listOf("")) }

    LaunchedEffect(editPost, editFood) {
        if (isDatabaseItem && editFood != null) {
            foodName = editFood.name
            origin = editFood.origin
            components = editFood.sections
            existingImageUrl = editFood.imageUrl
            
            ingredientsTexts = editFood.sections.map { c -> c.ingredients.joinToString("\n") }
            instructionsTexts = editFood.sections.map { c -> c.instructions.joinToString("\n") }
        } else if (!isDatabaseItem && editPost != null) {
            foodName = editPost.foodName
            components = editPost.components
            existingImageUrl = editPost.imageUrl
            
            ingredientsTexts = editPost.components.map { c -> c.ingredients.joinToString("\n") }
            instructionsTexts = editPost.components.map { c -> c.instructions.joinToString("\n") }
        }
    }

    val userProfile = viewModel.userProfile

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        existingImageUrl = null // Clear existing if new one selected
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isDatabaseItem) {
                            if (editFoodId == null) "Add Food to Database" else "Edit Database Food"
                        } else {
                            if (editPostId == null) "Share a Recipe!" else "Edit Recipe"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppPink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = AppPink,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Name (e.g. Nasi Lemak)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppPink)
            )

            if (isDatabaseItem) {
                Spacer(modifier = Modifier.height(16.dp))
                
                var expanded by remember { mutableStateOf(false) }
                val origins = remember(allFoodItems) {
                    val list = allFoodItems.map { it.origin }.filter { it.isNotBlank() }.distinct().toMutableList()
                    val defaults = listOf("Malay", "Chinese", "Indian", "Italian")
                    defaults.forEach { if (!list.contains(it)) list.add(it) }
                    list.sorted()
                }

                var showAddOriginDialog by remember { mutableStateOf(false) }
                var showManageOriginsDialog by remember { mutableStateOf(false) }
                var newOriginText by remember { mutableStateOf("") }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = origin,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Origin") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppPink)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            origins.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        origin = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Add New Origin...", color = AppPink, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    expanded = false
                                    showAddOriginDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = AppPink) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = { showManageOriginsDialog = true },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Manage Origins", tint = AppPink)
                    }
                }

                if (showManageOriginsDialog) {
                    var editingOrigin by remember { mutableStateOf<String?>(null) }
                    var editOriginValue by remember { mutableStateOf("") }
                    var originToDelete by remember { mutableStateOf<String?>(null) }

                    AlertDialog(
                        onDismissRequest = { showManageOriginsDialog = false },
                        title = { Text("Manage Origins") },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                origins.forEach { o ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (editingOrigin == o) {
                                                OutlinedTextField(
                                                    value = editOriginValue,
                                                    onValueChange = { editOriginValue = it },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true
                                                )
                                                IconButton(onClick = {
                                                    if (editOriginValue.isNotBlank() && editOriginValue != o) {
                                                        viewModel.renameOrigin(o, editOriginValue) {
                                                            if (origin == o) origin = editOriginValue
                                                            editingOrigin = null
                                                        }
                                                    } else {
                                                        editingOrigin = null
                                                    }
                                                }) {
                                                    Icon(Icons.Default.Check, null, tint = Color.Green)
                                                }
                                            } else {
                                                Text(o, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                                IconButton(onClick = { 
                                                    editingOrigin = o
                                                    editOriginValue = o
                                                }) {
                                                    Icon(Icons.Default.Edit, null, tint = AppPink, modifier = Modifier.size(20.dp))
                                                }
                                                IconButton(onClick = { originToDelete = o }) {
                                                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showManageOriginsDialog = false }) {
                                Text("Done", color = AppPink)
                            }
                        }
                    )

                    if (originToDelete != null) {
                        AlertDialog(
                            onDismissRequest = { originToDelete = null },
                            title = { Text("Delete Origin") },
                            text = { Text("Are you sure you want to delete '$originToDelete'? This will clear the origin for all associated recipes.") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        val toDel = originToDelete!!
                                        viewModel.deleteOrigin(toDel) {
                                            if (origin == toDel) origin = ""
                                            originToDelete = null
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                ) {
                                    Text("Delete")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { originToDelete = null }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }

                if (showAddOriginDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddOriginDialog = false },
                        title = { Text("Add New Origin") },
                        text = {
                            OutlinedTextField(
                                value = newOriginText,
                                onValueChange = { newOriginText = it },
                                label = { Text("Origin Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (newOriginText.isNotBlank()) {
                                        origin = newOriginText.trim()
                                        showAddOriginDialog = false
                                        newOriginText = ""
                                    }
                                }
                            ) {
                                Text("Add", color = AppPink)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddOriginDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Food Image Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showImageSourceDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedImageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (existingImageUrl != null) {
                        val bitmap = remember(existingImageUrl) {
                            if (existingImageUrl!!.startsWith("data:image")) {
                                viewModel.decodeImage(existingImageUrl!!.substringAfter("base64,"))
                            } else null
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Existing Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            coil.compose.AsyncImage(
                                model = existingImageUrl,
                                contentDescription = "Existing Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = AppPink, modifier = Modifier.size(48.dp))
                            Text("Add Food Picture", color = AppPink)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            components.forEachIndexed { index, component ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Component ${index + 1}", fontWeight = FontWeight.Bold, color = AppPink)
                            if (components.size > 1) {
                                IconButton(onClick = {
                                    components = components.toMutableList().apply { removeAt(index) }
                                }) {
                                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = component.sectionName,
                            onValueChange = { newValue ->
                                components = components.toMutableList().apply {
                                    this[index] = component.copy(sectionName = newValue)
                                }
                            },
                            label = { Text("Component Name (e.g. Sambal)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val ingredientsText = ingredientsTexts.getOrElse(index) { "" }
                        OutlinedTextField(
                            value = ingredientsText,
                            onValueChange = { newValue ->
                                ingredientsTexts = ingredientsTexts.toMutableList().apply {
                                    if (index < size) this[index] = newValue else add(newValue)
                                }
                                components = components.toMutableList().apply {
                                    this[index] = component.copy(ingredients = newValue.split("\n").filter { it.isNotBlank() })
                                }
                            },
                            label = { Text("Ingredients (one per line)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )
                        
                        Text(
                            text = "Example:\n100 gram rice\n2 cups butter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val instructionsText = instructionsTexts.getOrElse(index) { "" }
                        OutlinedTextField(
                            value = instructionsText,
                            onValueChange = { newValue ->
                                instructionsTexts = instructionsTexts.toMutableList().apply {
                                    if (index < size) this[index] = newValue else add(newValue)
                                }
                                components = components.toMutableList().apply {
                                    this[index] = component.copy(instructions = newValue.split("\n").filter { it.isNotBlank() })
                                }
                            },
                            label = { Text("Recipe/Instructions (one per line)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )
                    }
                }
            }

            Button(
                onClick = {
                    components = components + RecipeSection("", emptyList(), emptyList())
                    ingredientsTexts = ingredientsTexts + ""
                    instructionsTexts = instructionsTexts + ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Another Component")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = {
                    if (!viewModel.isOnline()) {
                        errorMessage = "Not Connected to Internet. Please check your connection."
                        return@Button
                    }
                    if (foodName.isBlank() || components.any { it.sectionName.isBlank() || it.ingredients.isEmpty() || it.instructions.isEmpty() }) {
                        errorMessage = "Please fill in all fields for all components."
                        return@Button
                    }
                    isLoading = true
                    
                    var finalImageUrl = existingImageUrl
                    if (selectedImageUri != null) {
                        try {
                            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
                            } else {
                                val source = ImageDecoder.createSource(context.contentResolver, selectedImageUri!!)
                                ImageDecoder.decodeBitmap(source)
                            }
                            // Scale down to avoid Firestore 1MB limit
                            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 400 * bitmap.height / bitmap.width, true)
                            finalImageUrl = "data:image/jpeg;base64," + viewModel.encodeImage(scaledBitmap)
                        } catch (e: Exception) {
                            errorMessage = "Error processing image: ${e.localizedMessage}"
                            isLoading = false
                            return@Button
                        }
                    }

                    if (isDatabaseItem) {
                        if (editFoodId != null) {
                            viewModel.updateFoodInDatabase(
                                foodId = editFoodId,
                                foodName = foodName,
                                origin = origin,
                                components = components,
                                imageUrl = finalImageUrl,
                                onSuccess = {
                                    isLoading = false
                                    onPostSuccess()
                                },
                                onFailure = {
                                    isLoading = false
                                    errorMessage = it
                                }
                            )
                        } else {
                            viewModel.addFoodToDatabase(
                                foodName = foodName,
                                origin = origin,
                                components = components,
                                imageUrl = finalImageUrl,
                                onSuccess = {
                                    isLoading = false
                                    onPostSuccess()
                                },
                                onFailure = {
                                    isLoading = false
                                    errorMessage = it
                                }
                            )
                        }
                    } else if (editPostId != null && editPost != null) {
                        viewModel.updatePost(
                            editPost.copy(
                                foodName = foodName,
                                components = components,
                                imageUrl = finalImageUrl
                            )
                        )
                        isLoading = false
                        onPostSuccess()
                    } else {
                        viewModel.shareRecipe(
                            foodName = foodName,
                            components = components,
                            imageUrl = finalImageUrl,
                            onSuccess = {
                                isLoading = false
                                onPostSuccess()
                            },
                            onFailure = {
                                isLoading = false
                                errorMessage = it
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppPink),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else {
                    val btnText = if (isDatabaseItem) {
                        if (editFoodId == null) "Add Food" else "Update Food"
                    } else {
                        if (editPostId == null) "Post Recipe" else "Update Recipe"
                    }
                    Text(btnText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
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

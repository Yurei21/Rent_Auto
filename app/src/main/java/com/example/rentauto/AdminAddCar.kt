package com.example.rentauto

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.rentauto.network.RetrofitClient
import com.example.rentauto.ui.theme.RentAutoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(navController: NavController) {
    val context = LocalContext.current

    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var rentPrice by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Available") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Car") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") })
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") })
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(
                    value = rentPrice,
                    onValueChange = { rentPrice = it },
                    label = { Text("Rent Price") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                val availabilityOptions = listOf("Available", "Rented", "Under Maintenance")
                var expanded by remember { mutableStateOf(false) }

                Box {
                    OutlinedTextField(
                        value = availability,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Availability Status") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availabilityOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    availability = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }


                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Pick Image")
                }

                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(model = it),
                        contentDescription = null,
                        modifier = Modifier.size(150.dp)
                    )
                }

                Button(onClick = {
                    if (imageUri != null && brand.isNotBlank() && model.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val inputStream = context.contentResolver.openInputStream(imageUri!!)
                                val file = File(context.cacheDir, "upload.jpg")
                                val outputStream = FileOutputStream(file)
                                inputStream?.copyTo(outputStream)
                                inputStream?.close()
                                outputStream.close()

                                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                val body = MultipartBody.Part.createFormData("car_image", file.name, requestFile)

                                val map = mapOf(
                                    "brand" to brand.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    "model" to model.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    "year" to year.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    "rent_price" to rentPrice.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    "availability_status" to availability.toRequestBody("text/plain".toMediaTypeOrNull())
                                )

                                val response = RetrofitClient.api.addCar(body, map)
                                withContext(Dispatchers.Main) {
                                    message = if (response.success) "Car added!" else "Error: ${response.message}"
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    message = "Upload error: ${e.localizedMessage}"
                                }
                            }
                        }
                    } else {
                        message = "Please fill all fields and select an image."
                    }
                }) {
                    Text("Submit")
                }

                message?.let { Text(text = it) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddCarPreview() {
    val context = LocalContext.current
    val navController = TestNavHostController(context)
    navController.setLifecycleOwner(LocalLifecycleOwner.current)

    RentAutoTheme {
        AddCarScreen(navController)
    }
}

package com.example.rentauto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import com.example.rentauto.network.RetrofitClient
import com.example.rentauto.network.Vehicle
import com.example.rentauto.ui.theme.RentAutoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyCarScreen(navController: NavController, vehicleId: Int?) {
    val context = LocalContext.current
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var rentPrice by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            try {
                val response = RetrofitClient.api.getCarById(vehicleId)
                if (response.success) {
                    response.vehicle?.let { vehicle ->
                        brand = vehicle.brand
                        model = vehicle.model
                        year = vehicle.year.toString()
                        rentPrice = vehicle.rentPrice.toString()
                        imageUrl = vehicle.carUrl
                        availability = vehicle.availabilityStatus
                    }
                } else {
                    message = "Failed to load vehicle."
                }
            } catch (e: Exception) {
                message = "Error: ${e.localizedMessage}"
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Modify Car") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("adminDashboard") {
                            popUpTo("modifyCar") { inclusive = true }
                        }
                    }) {
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
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand") })
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Model") })
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = rentPrice, onValueChange = { rentPrice = it }, label = { Text("Rent Price") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = availability, onValueChange = { availability = it }, label = { Text("Availability Status") })

                Button(onClick = {
                    if (vehicleId != null) {
                        val updatedVehicle = Vehicle(
                            vehicleId = vehicleId,
                            brand = brand,
                            model = model,
                            year = year.toIntOrNull() ?: 0,
                            rentPrice = rentPrice.toDoubleOrNull() ?: 0.0,
                            carUrl = imageUrl,
                            availabilityStatus = availability
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val result = RetrofitClient.api.updateCar(updatedVehicle)
                                withContext(Dispatchers.Main) {
                                    message = if (result.success) {
                                        "Vehicle updated successfully!"
                                    } else {
                                        "Update failed: ${result.message}"
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    message = "Error: ${e.localizedMessage}"
                                }
                            }
                        }
                    }
                }) {
                    Text("Update Car")
                }
                message?.let {
                    Text(text = it)
                }

                Button(
                    onClick = {
                        if (vehicleId != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val result = RetrofitClient.api.deleteCar(mapOf("vehicle_id" to vehicleId))
                                    withContext(Dispatchers.Main) {
                                        message = if (result.success) {
                                            navController.navigate("AdminDashboard") {
                                                popUpTo("modifyCar") { inclusive = true }
                                            }
                                            "Vehicle deleted successfully!"
                                        } else {
                                            "Delete failed: ${result.message}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        message = "Error: ${e.localizedMessage}"
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Delete Car")
                }

            }
        }
    }
}



@Preview (showBackground = true)
@Composable
fun ModifyPreview() {
    val context = LocalContext.current
    val navController = TestNavHostController(context)
    navController.setLifecycleOwner(LocalLifecycleOwner.current)
    val id = 0

    RentAutoTheme {
        ModifyCarScreen(navController, id)
    }
}
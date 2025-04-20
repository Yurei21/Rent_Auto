package com.example.rentauto

import android.icu.text.CaseMap.Title
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.testing.TestNavHostController
import com.example.rentauto.network.RetrofitClient
import com.example.rentauto.network.Vehicle
import com.example.rentauto.ui.theme.RentAutoTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.example.rentauto.network.PaymentRecord
import com.example.rentauto.network.RentalRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AdminDashboardScreen(navController: NavHostController) {
    val adminNavController = rememberNavController()
    val items = listOf(
        AdminNavItem.Home,
        AdminNavItem.Rentals,
        AdminNavItem.Payments,
        AdminNavItem.Return
    )

    val navBackStackEntry by adminNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                adminNavController.navigate(item.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = adminNavController,
                startDestination = AdminNavItem.Home.route
            ) {
                composable(AdminNavItem.Home.route) { ViewCarsScreen(navController = navController) }
                composable(AdminNavItem.Rentals.route) { RentalRecordsScreen() }
                composable(AdminNavItem.Payments.route) { PaymentRecordsScreen() }
                composable(AdminNavItem.Return.route) { ReturnCarScreen() }
                composable("modifyCar/{vehicleId}") { backStackEntry ->
                    val vehicleId = backStackEntry.arguments?.getString("vehicleId")?.toIntOrNull()
                    vehicleId?.let { ModifyCarScreen(navController, vehicleId = it) }
                }
                composable("AddCar" ) { AddCarScreen(navController) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewCarsScreen(viewModel: VehicleViewModel = viewModel(), navController: NavController) {
    val vehicles = viewModel.vehicleList
    val isLoading = viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add or Modify Car") },
                actions = {
                    IconButton(onClick = { navController.navigate("AddCar") }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Car")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(vehicles) { vehicle ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .height(320.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                            ) {
                                AsyncImage(
                                    model = vehicle.carUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(20.dp))
                                Column (
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium)
                                    Text("Year: ${vehicle.year}")
                                    Text("₱${vehicle.rentPrice} /day", fontWeight = FontWeight.Bold)
                                    Text("Status: ${vehicle.availabilityStatus}")
                                }
                                Button(
                                    onClick = { navController.navigate("modifyCar/${vehicle.vehicleId}") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) { Text("Modify") }
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalRecordsScreen(viewModel : RentalRecordsViewModel = viewModel()) {
    val rentalRecords = viewModel.rentalRecords
    val isLoading = viewModel.isLoading

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Rental Records") }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(rentalRecords) { record ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(Modifier.padding(8.dp)) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text("User: ${record.username}")
                                    Text("Car: ${record.model} ${record.brand}")
                                    Text("Rental Start: ${record.rentalStart}")
                                    Text("Rental End: ${record.rentalEnd}")
                                    Text("Total Cost: ₱${record.totalCost}")
                                    Text("Payment Status: ${record.paymentStatus}")
                                    Text("Car Status: ${record.carStatus}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentRecordsScreen(viewModel: PaymentRecordsViewModel = viewModel()) {
    val isLoading = viewModel.isLoading
    val payments = viewModel.payments

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Records") }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(payments) { payment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("User: ${payment.username}", fontWeight = FontWeight.Bold)
                                Text("Car: ${payment.vehicleBrand} ${payment.vehicleModel}")
                                Text("Rental ID: ${payment.rentalId}")
                                Text("Amount Paid: ₱${payment.amountPaid}")
                                Text("Payment Method: ${payment.paymentMethod}")
                                Text("Date: ${payment.paymentDate}")
                                Text("Status: ${payment.payStatus}")
                                if (payment.additionalOrLateFee > 0.0) {
                                    Text("Late Fee: ₱${payment.additionalOrLateFee}", color = MaterialTheme.colorScheme.error)
                                }
                                Text("Total Cost: ₱${payment.totalCost}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnCarScreen() {
    var barcode by remember { mutableStateOf("") }
    var additionalFee by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val fee = additionalFee.toDoubleOrNull() ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Return the Car") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode Number") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = barcode.isEmpty() && resultMessage != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = additionalFee,
                    onValueChange = { additionalFee = it },
                    label = { Text("Additional Fee") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (barcode.isNotEmpty()) {
                            isProcessing = true
                            handleBarcodeReturn(barcode, additionalFee, onSuccess = { msg ->
                                resultMessage = msg
                                isProcessing = false
                            }, onError = { err ->
                                resultMessage = err
                                isProcessing = false
                            })
                        } else {
                            resultMessage = "Please enter a valid barcode."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return Car")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isProcessing || !resultMessage.isNullOrBlank()) {
                    Text(
                        text = if (isProcessing) "Processing..." else resultMessage ?: "",
                        fontWeight = FontWeight.Bold,
                        color = when {
                            resultMessage?.contains("success", ignoreCase = true) == true -> Color.Green
                            resultMessage?.contains("failed", ignoreCase = true) == true ||
                                    resultMessage?.contains("error", ignoreCase = true) == true -> Color.Red
                            else -> Color.Black
                        }
                    )
                }

            }

        }
    }
}



sealed class AdminNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : AdminNavItem("admin_home", "Home", Icons.Filled.DirectionsCar)
    object Rentals : AdminNavItem("rental_records", "Rentals", Icons.AutoMirrored.Filled.List)
    object Payments : AdminNavItem("payment_records", "Payments", Icons.Filled.AttachMoney)
    object Return : AdminNavItem("return_car", "Return", Icons.AutoMirrored.Filled.KeyboardReturn)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AdminDashboardPreview () {
    val context = LocalContext.current
    val navController = TestNavHostController(context)
    navController.setLifecycleOwner(LocalLifecycleOwner.current)

    RentAutoTheme {
        AdminDashboardScreen(navController)
    }
}

class VehicleViewModel : ViewModel() {
    var vehicleList by mutableStateOf<List<Vehicle>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        fetchVehicles()
    }

    private fun fetchVehicles() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.viewCars()
                if (response.success) {
                    vehicleList = response.vehicles
                } else {
                    Log.e("VehicleVM", "API failed")
                }
            } catch (e: Exception) {
                Log.e("VehicleVM", "Failed to load vehicles", e)
            }
            isLoading = false
        }
    }
}

class RentalRecordsViewModel : ViewModel() {
    var rentalRecords by mutableStateOf<List<RentalRecord>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        fetchRentalRecords()
    }

    private fun fetchRentalRecords() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.viewRentals()
                if (response.success) {
                    rentalRecords = response.records ?: emptyList()
                } else {
                    Log.e("RentalRecordsVM", "API failed")
                }
            } catch (e: Exception) {
                Log.e("RentalRecordsVM", "Failed to load rental records", e)
            }
            isLoading = false
        }
    }
}

class PaymentRecordsViewModel : ViewModel() {
    var payments by mutableStateOf<List<PaymentRecord>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        fetchPayments()
    }

    private fun fetchPayments() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.viewPayments()
                if (response.success) {
                    payments = response.payments ?: emptyList()
                } else {
                    Log.e("PaymentVM", "Failed to fetch payments")
                }
            } catch (e: Exception) {
                Log.e("PaymentVM", "Error fetching payments", e)
            }
            isLoading = false
        }
    }
}

fun handleBarcodeReturn(barcode: String, addFee: String, onSuccess:(String) -> Unit, onError: (String) -> Unit) {
    val scope = CoroutineScope(Dispatchers.IO)
    scope.launch {
        try {
            val response = RetrofitClient.api.returnCar(barcode, addFee)
            withContext(Dispatchers.Main) {
                if (response.success) {
                    onSuccess("Return successful.")
                } else {
                    onError("Return failed.")
                }
            }
        } catch (e: Exception) {
            Log.e("ReturnBarcode", "API Error", e)
            withContext(Dispatchers.Main) {
                onError("Network Error: ${e.localizedMessage}")
            }
        }
    }
}
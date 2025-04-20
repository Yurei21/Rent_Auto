package com.example.rentauto

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.rentauto.ui.theme.RentAutoTheme
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.rentauto.network.PaymentRentalRecord
import com.example.rentauto.network.RetrofitClient
import com.example.rentauto.network.UserDocument
import com.example.rentauto.network.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val userId by userPrefs.userId.collectAsState(initial = null)
    Log.d("DashboardScreen", "Collected userId: $userId")

    val userNavController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val items = listOf(
        UserNavItem.Home,
        UserNavItem.Records,
        UserNavItem.Profile
    )

    val navBackStackEntry by userNavController.currentBackStackEntryAsState()
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
                                userNavController.navigate(item.route)
                            }
                        }
                    )
                }
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.KeyboardBackspace, contentDescription = "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            userPrefs.logout()
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = userNavController,
                startDestination = UserNavItem.Home.route
            ) {
                composable(UserNavItem.Home.route) { UserViewCarsScreen(navController = navController) }
                composable(UserNavItem.Records.route) { userId?.let { it1 -> UserRecordsScreen(it1) } }
                composable(UserNavItem.Profile.route) { userId?.let { it1 -> ProfileScreen(it1) } }
                composable("rentCar/{vehicleId}") { backStackEntry ->
                    val vehicleId = backStackEntry.arguments?.getString("vehicleId")?.toIntOrNull()
                    vehicleId?.let { RentScreen(navController, vehicleId = it) }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserViewCarsScreen(viewModel: VehicleViewModel = viewModel(), navController: NavController) {
    val vehicles = viewModel.vehicleList
    val isLoading = viewModel.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rent Car") }
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
                                if (vehicle.availabilityStatus == "Available") {
                                    Button(
                                        onClick = { navController.navigate("rentCar/${vehicle.vehicleId}") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Text("Rent Car")
                                    }
                                } else {
                                    Text(
                                        text = when (vehicle.availabilityStatus) {
                                            "Rented" -> "Currently Rented"
                                            "Under Maintenance" -> "Unavailable for Maintenance"
                                            else -> "Unavailable"
                                        },
                                        color = Color.Red,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
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
fun UserRecordsScreen(userId: Int, viewModel: UserRecordsViewModel = viewModel()) {
    val rentalRecords by viewModel.records.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(userId) {
        viewModel.fetchUserRecords(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rental Records") }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()) {

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
                            Column(Modifier.padding(8.dp)) {
                                Text("Car: ${record.vehicleBrand} ${record.vehicleModel}", fontWeight = FontWeight.Bold)
                                Text("Rental Start: ${record.rentalStartDate}")
                                Text("Rental End: ${record.rentalEndDate}")
                                Text("Total Cost: ₱${record.totalCost}")
                                Text("Amount Paid: ₱${record.amountPaid}")
                                Text("Payment Method: ${record.paymentMethod}")
                                Text("Additional/Late Fee: ₱${record.additionalOrLateFee}")
                                Text("Status: ${record.rentalStatus} | ${record.carStatus}")
                                Text("Payment Status: ${record.payStatus}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(userId: Int, viewModel: UserProfileViewModel = viewModel()) {
    val profileState by viewModel.profileState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.fetchUserProfile(userId)
    }

    when (val state = profileState) {
        is UserProfileState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UserProfileState.Success -> {
            val data = state.data

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        "User Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    ProfileInfoCard(data)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Uploaded Documents",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (!data.documents.isNullOrEmpty()) {
                    items(data.documents) { doc ->
                        DocumentCard(doc)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    item {
                        Text("No document images available.")
                    }
                }

            }
        }

        is UserProfileState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


sealed class UserNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : UserNavItem("home", "Home", Icons.Filled.DirectionsCar)
    object Records : UserNavItem("rental_records", "Records", Icons.AutoMirrored.Filled.List)
    object Profile : UserNavItem("profile", "Profile", Icons.Filled.SupervisedUserCircle)
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val context = LocalContext.current
    val navController = TestNavHostController(context)
    navController.setLifecycleOwner(LocalLifecycleOwner.current)

    RentAutoTheme {
        DashboardScreen(navController)
    }
}

sealed class UserProfileState {
    object Loading: UserProfileState()
    data class Success(val data: UserProfile) : UserProfileState()
    data class Error (val message: String) : UserProfileState()
}

class UserProfileViewModel : ViewModel() {
    private val _profileState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val profileState: StateFlow<UserProfileState> get() = _profileState

    fun fetchUserProfile(userId: Int) {
        _profileState.value = UserProfileState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getProfile(userId)
                if (response.success) {
                    response.data?.let {
                        _profileState.value = UserProfileState.Success(it)
                    } ?: run {
                        _profileState.value = UserProfileState.Error("Data is Null")
                    }
                } else {
                    _profileState.value = UserProfileState.Error(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {

            }
        }
    }
}

class UserRecordsViewModel : ViewModel() {

    private val _records = MutableStateFlow<List<PaymentRentalRecord>>(emptyList())
    val records: StateFlow<List<PaymentRentalRecord>> = _records

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchUserRecords(userId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.api.getPaymentAndRentalRecords(userId)
                if (response.success && response.data != null) {
                    _records.value = response.data
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to fetch records"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
            } finally {
                _loading.value = false
            }
        }
    }
}

@Composable
fun ProfileInfoCard(data: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${data.name}", style = MaterialTheme.typography.titleMedium)
            Text("Email: ${data.email}")
            Text("Phone: ${data.phone}")
            Text("Address: ${data.address ?: "N/A"}")
            Text("Status: ${data.status}")
        }
    }
}

@Composable
fun DocumentCard(doc: UserDocument) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Type: ${doc.documentType}", style = MaterialTheme.typography.bodyLarge)

            doc.localImagePath?.let { path ->
                AsyncImage(
                    model = path,
                    contentDescription = "Document Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(top = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


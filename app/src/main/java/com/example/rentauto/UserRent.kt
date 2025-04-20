package com.example.rentauto

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.rentauto.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentScreen(navController: NavController, vehicleId: Int?) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var rentPrice by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val context = LocalContext.current

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    val paymentOptions = listOf("Cash", "Online Payment", "Credit Card")
    var expanded by remember { mutableStateOf(false) }

    val userPrefs = remember { UserPreferences(context) }
    var userId by remember { mutableStateOf<Int?>(null) }

    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var confirmedStartDate by remember { mutableStateOf("") }
    var confirmedEndDate by remember { mutableStateOf("") }
    var confirmedCost by remember { mutableStateOf(0.0) }
    var confirmedPaymentMethod by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var showRentConfirmDialog by remember { mutableStateOf(false) }

    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            startDate = dateFormatter.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val selectedEndDate = dateFormatter.format(calendar.time)

            val startCalendar = Calendar.getInstance().apply {
                dateFormatter.parse(startDate)?.let { time = it }
            }
            startCalendar.add(Calendar.DAY_OF_YEAR, 1)

            if (dateFormatter.format(startCalendar.time) == selectedEndDate) {
                endDate = selectedEndDate
            } else {
                message = "End date must be exactly one day after the start date."
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(vehicleId) {
        vehicleId?.let {
            try {
                val response = RetrofitClient.api.getCarById(it)
                if (response.success) {
                    response.vehicle?.let { vehicle ->
                        brand = vehicle.brand
                        model = vehicle.model
                        year = vehicle.year.toString()
                        rentPrice = vehicle.rentPrice.toString()
                        imageUrl = vehicle.carUrl
                    }
                } else {
                    message = "Failed to load vehicle."
                }
            } catch (e: Exception) {
                message = "Error: ${e.localizedMessage}"
            }
        }
    }

    LaunchedEffect(true) {
        userId = userPrefs.getUserId()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rent Vehicle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (message != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text(text = message ?: "", color = Color.Red)
            }
        } else {
            if (!showConfirmation) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Vehicle Details", style = MaterialTheme.typography.headlineSmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Vehicle Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(bottom = 16.dp)
                        )
                    }

                    InfoRow("Brand", brand)
                    InfoRow("Model", model)
                    InfoRow("Year", year)
                    InfoRow("Rent Price", "₱$rentPrice / day")

                    Spacer(modifier = Modifier.height(32.dp))

                    DateInputField("Start Rent Date", startDate) { showStartPicker = true }
                    Spacer(modifier = Modifier.height(12.dp))
                    DateInputField("End Rent Date", endDate) { showEndPicker = true }
                    Spacer(modifier = Modifier.height(16.dp))

                    PaymentMethodField(
                        selectedMethod = selectedPaymentMethod,
                        options = paymentOptions,
                        expanded = expanded,
                        onClick = { expanded = true },
                        onDismiss = { expanded = false },
                        onSelect = { selectedPaymentMethod = it }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (userId == null || vehicleId == null || startDate.isEmpty() || endDate.isEmpty()) {
                                message = "Missing required data."
                                return@Button
                            }
                            showRentConfirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Proceed to Rent")
                    }

                    if (showRentConfirmDialog) {
                        AlertDialog(
                            onDismissRequest = { showRentConfirmDialog = false },
                            title = { Text("Confirm Rental") },
                            text = {
                                Text("Are you sure you want to rent this vehicle from $startDate to $endDate?")
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showRentConfirmDialog = false

                                    val totalDays = calculateDaysBetween(startDate, endDate)
                                    val pricePerDay = rentPrice.toDoubleOrNull() ?: 0.0
                                    val totalCost = pricePerDay * totalDays

                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val response = RetrofitClient.api.rentCar(
                                                userId = userId!!,
                                                vehicleId = vehicleId!!,
                                                startDate = startDate,
                                                endDate = endDate,
                                                totalCost = totalCost,
                                                paymentMethod = selectedPaymentMethod
                                            )

                                            withContext(Dispatchers.Main) {
                                                if (response.success) {
                                                    confirmedStartDate = startDate
                                                    confirmedEndDate = endDate
                                                    confirmedCost = totalCost
                                                    confirmedPaymentMethod = selectedPaymentMethod
                                                    barcodeBitmap = generateBarcodeBitmap(response.barcode.toString())
                                                    startDate = ""
                                                    endDate = ""
                                                    selectedPaymentMethod = "Cash"
                                                    showConfirmation = true
                                                } else {
                                                    message = "Failed: ${response.message}"
                                                }
                                            }

                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                message = "Error: ${e.localizedMessage}"
                                            }
                                        }
                                    }

                                }) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showRentConfirmDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                }

                if (showStartPicker) {
                    startDatePicker.show()
                    showStartPicker = false
                }

                if (showEndPicker) {
                    endDatePicker.show()
                    showEndPicker = false
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Rental Confirmation", style = MaterialTheme.typography.headlineSmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow("Vehicle", "$brand $model")
                    InfoRow("Start Date", confirmedStartDate)
                    InfoRow("End Date", confirmedEndDate)
                    InfoRow("Total Cost", "₱%.2f".format(confirmedCost))
                    InfoRow("Payment Method", confirmedPaymentMethod)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Receipt Barcode", style = MaterialTheme.typography.bodyMedium)

                    barcodeBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Barcode",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun PaymentMethodField(
    selectedMethod: String,
    options: List<String>,
    expanded: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp)
        ) {
            Text("Payment Method", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = selectedMethod,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Divider(color = Color.Gray, thickness = 1.dp)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun DateInputField(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(vertical = 8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(
            text = if (value.isNotEmpty()) value else "Select date",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(vertical = 4.dp)
        )
        Divider(color = Color.Gray, thickness = 1.dp)
    }
}

fun calculateDaysBetween(start: String, end: String): Int {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDate = format.parse(start)
    val endDate = format.parse(end)
    val diffMillis = endDate.time - startDate.time
    return (diffMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
}

fun generateBarcodeBitmap(barcodeData: String, width: Int = 600, height: Int = 300): Bitmap {
    val bitMatrix: BitMatrix = MultiFormatWriter().encode(barcodeData, BarcodeFormat.CODE_128, width, height)
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

    for (x in 0 until width) {
        for (y in 0 until height) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }

    return bmp
}
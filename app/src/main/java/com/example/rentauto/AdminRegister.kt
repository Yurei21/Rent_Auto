package com.example.rentauto

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import com.example.rentauto.network.AdminRegisterRequest
import com.example.rentauto.network.RegisterRequest
import com.example.rentauto.network.RetrofitClient
import com.example.rentauto.ui.theme.RentAutoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AdminRegisterScreen(navController : NavController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val imageRes = if (isDark) R.drawable.iconlight else R.drawable.icondark

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Image (
            painter = painterResource(id = imageRes),
            contentDescription = "App Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "Register",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email" ) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.OutlinedTextField(
                value = confPass,
                onValueChange = { confPass = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if ( username.trim().isEmpty() ||
                        email.trim().isEmpty() ||
                        password.trim().isEmpty() ||
                        confPass.trim().isEmpty()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (password != confPass) {
                        Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val request = AdminRegisterRequest (
                                username, email, password
                            )
                            val response = RetrofitClient.api.registerAdmin(request)
                            withContext(Dispatchers.Main) {
                                if(response.success) {
                                    Toast.makeText(context, response.message ?: "Registered Successfully", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login")
                                } else
                                    Toast.makeText(context, response.message ?: "Registration Failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main){
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text("Register")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminRegisterPreview() {
    val context = LocalContext.current
    val navController = TestNavHostController(context)
    navController.setLifecycleOwner(LocalLifecycleOwner.current)

    RentAutoTheme {
        AdminRegisterScreen(navController)
    }
}
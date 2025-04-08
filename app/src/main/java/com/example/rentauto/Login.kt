package com.example.rentauto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.FlowRowScopeInstance.align
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rentauto.ui.theme.RentAutoTheme
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import android.widget.Toast
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.rentauto.network.RetrofitClient

@Composable
fun LoginScreen(navController: NavController) {
    // Add state variables for email and password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = "App Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .height(300.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "LOGIN",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            androidx.compose.material3.OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Checkbox(
                    checked = false,
                    onCheckedChange = {}
                )
                Text("Remember Me")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.api.loginUser(email, password)
                            withContext(Dispatchers.Main) {
                                if (response.success) {
                                    Toast.makeText(
                                        context,
                                        "Welcome ${response.name}!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("dashboard") // replace with your real route
                                } else {
                                    val errorMessage = response.message ?: "Login failed"
                                    // Handle specific error message for incorrect credentials
                                    if (errorMessage.contains("Incorrect password", true)) {
                                        Toast.makeText(
                                            context,
                                            "Incorrect password. Please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            errorMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("LOGIN")
            }

            RegisterLink(navController)
        }
    }
}

@Composable
fun RegisterLink(navController: NavController) {
    val annotatedText = buildAnnotatedString {
        append("Don't have an account? ")

        pushStringAnnotation(tag = "SIGNUP", annotation = "register")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Sign up")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "SIGNUP", start = offset, end = offset)
                .firstOrNull()?.let {
                    navController.navigate("register")
                }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    val context = LocalContext.current
    val navController = TestNavHostController(context)
    navController.setLifecycleOwner(LocalLifecycleOwner.current)

    RentAutoTheme {
        LoginScreen(navController)
    }
}
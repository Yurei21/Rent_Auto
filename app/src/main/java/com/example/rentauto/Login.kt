package com.example.rentauto


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.rentauto.network.RetrofitClient

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var clickCount by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val imageRes = if (isDark) R.drawable.iconlight else R.drawable.icondark

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "App Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable (
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ){
                    clickCount++
                    if (clickCount >= 5) {
                        Toast.makeText(context, "Accessing Admin Login...", Toast.LENGTH_SHORT).show()
                        clickCount = 0 // Reset counter
                        navController.navigate("AdminLogin")
                    }
            }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "Login",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide Password" else "Show Password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image as ImageVector, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val userPrefs = UserPreferences(context)
                    var userid: Int?

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.api.loginUser(email, pass)
                            userid = response.userId
                            userPrefs.saveUser(userid)
                            if (response.success) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Welcome ${response.name}!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    val errorMessage = response.message ?: "Login failed"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            RegisterLink(navController)
        }
    }
}

@Composable
fun RegisterLink(navController: NavController) {
    val isDark = isSystemInDarkTheme()

    val mainTextColor = if (isDark) {
        Color.White
    } else {
        Color.Black
    }

    val textColor = if (isDark) {
        Color.Blue
    } else {
        MaterialTheme.colorScheme.primary
    }

    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = mainTextColor,
                fontSize = 16.sp
            )
        ) {
            append("Don't have an account? ")
        }

        pushStringAnnotation(tag = "SIGNUP", annotation = "register")
        withStyle(
            style = SpanStyle(
                color = textColor,
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
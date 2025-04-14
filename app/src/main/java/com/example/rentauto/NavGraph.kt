package com.example.rentauto

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(navController : NavHostController) {
    NavHost(navController = navController, startDestination = "landing") {
        composable("landing") { Landing(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("AdminLogin") { AdminLoginScreen(navController) }
        composable("AdminRegister") { AdminRegisterScreen(navController) }
    }
}
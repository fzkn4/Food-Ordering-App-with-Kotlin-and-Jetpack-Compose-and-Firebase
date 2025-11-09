package com.example.foodorderingapp.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.foodorderingapp.ui.screens.LoginScreen
import com.example.foodorderingapp.ui.screens.SignUpScreen
import com.example.foodorderingapp.ui.screens.MainScreen
import com.example.foodorderingapp.ui.screens.PreviousOrdersScreen


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("main") { MainScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable(Screen.PreviousOrders.route) { PreviousOrdersScreen(navController) }
    }
}

package com.example.foodorderingapp.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object PreviousOrders : Screen("previousOrders")
}


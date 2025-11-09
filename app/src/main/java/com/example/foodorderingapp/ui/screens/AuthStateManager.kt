package com.example.foodorderingapp.ui.screens

import androidx.compose.runtime.*
// Firebase imports
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun AuthStateManager(
    onUserLoggedIn: () -> Unit,
    onUserLoggedOut: () -> Unit
) {
    var user by remember { mutableStateOf<FirebaseUser?>(null) }
    
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
            if (user != null) {
                onUserLoggedIn()
            } else {
                onUserLoggedOut()
            }
        }
    }
}

fun isUserLoggedIn(): Boolean {
    return FirebaseAuth.getInstance().currentUser != null
}

fun getCurrentUser(): FirebaseUser? {
    return FirebaseAuth.getInstance().currentUser
}

package com.example.foodorderingapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
// Firebase imports
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.text.input.VisualTransformation
import com.example.foodorderingapp.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    // Firebase instances
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    fun validateInputs(): Boolean {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || username.isBlank()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password.length < 6) {
            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (username.length < 3) {
            Toast.makeText(context, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    fun registerUser() {
        if (!validateInputs()) return
        
        isLoading = true
        
        // Create user account with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        // Create user data object
                        val userData = User(
                            uid = firebaseUser.uid,
                            email = email,
                            username = username,
                            createdAt = System.currentTimeMillis()
                        )
                        
                        // Store user data in Firebase Realtime Database
                        database.reference.child("users").child(firebaseUser.uid)
                            .setValue(userData)
                            .addOnCompleteListener { dbTask ->
                                isLoading = false
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_LONG).show()
                                    // Navigate to main screen after successful registration
                                    navController.navigate("main") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                } else {
                                    // Handle database save failure
                                    Toast.makeText(context, "Failed to save user data: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } ?: run {
                        // Handle null user case
                        isLoading = false
                        Toast.makeText(context, "Failed to create user account", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Handle authentication failure
                    isLoading = false
                    val errorMessage = when (task.exception?.message) {
                        "The email address is already in use by another account." -> "Email already registered"
                        "The given password is invalid. [ Password should be at least 6 characters ]" -> "Password too short"
                        "The password provided is too weak using the connection and credential of a user that was signed in previously." -> "Password is too weak"
                        "The operation is not allowed. Error code: OPERATION_NOT_ALLOWED" -> "Registration is disabled"
                        else -> "Registration failed: ${task.exception?.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF18172C),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF18172C))
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Welcome Text
                Text(
                    text = "Join Food Ordering App",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color(0xFF838393)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFE862B),
                        unfocusedBorderColor = Color(0xFF838393),
                        focusedLabelColor = Color(0xFFFE862B),
                        unfocusedLabelColor = Color(0xFF838393),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color(0xFF838393)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFE862B),
                        unfocusedBorderColor = Color(0xFF838393),
                        focusedLabelColor = Color(0xFFFE862B),
                        unfocusedLabelColor = Color(0xFF838393),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color(0xFF838393)) },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFE862B),
                        unfocusedBorderColor = Color(0xFF838393),
                        focusedLabelColor = Color(0xFFFE862B),
                        unfocusedLabelColor = Color(0xFF838393),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password",
                                tint = Color(0xFF838393)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = Color(0xFF838393)) },
                    singleLine = true,
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFE862B),
                        unfocusedBorderColor = Color(0xFF838393),
                        focusedLabelColor = Color(0xFFFE862B),
                        unfocusedLabelColor = Color(0xFF838393),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPassword) "Hide password" else "Show password",
                                tint = Color(0xFF838393)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Register Button
                Button(
                    onClick = { registerUser() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFE862B),
                        disabledContainerColor = Color(0xFF838393)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Create Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Back to Login button
                TextButton(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Already have an account? Sign In",
                        color = Color(0xFFFE862B),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

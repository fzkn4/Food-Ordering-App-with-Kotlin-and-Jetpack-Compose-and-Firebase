package com.example.foodorderingapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousOrdersScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val database = remember { FirebaseDatabase.getInstance() }

    var isLoading by remember { mutableStateOf(true) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            isLoading = false
            orders = emptyList()
            return@LaunchedEffect
        }

        database.reference.child("orders")
            .get()
            .addOnSuccessListener { snapshot ->
                val fetched = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                    .filter { it.userId == userId }
                    .sortedByDescending { it.orderDate }
                orders = fetched
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    androidx.compose.material3.Surface(color = Color(0xFF18172C)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
            title = { Text("Previous Orders", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18172C)),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFE862B))
                }
            } else if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No previous orders found", color = Color(0xFF838393))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        OrderSummaryCard(order)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(order: Order) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1E31)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatOrderDate(order.orderDate),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₱" + String.format("%.2f", order.total),
                    color = Color(0xFFFE862B),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Status: ${order.status}",
                color = Color(0xFF838393),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2A2940))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item.foodName, color = Color.White)
                    Text(text = "x${item.quantity}", color = Color(0xFF838393))
                }
            }
        }
    }
}

private fun formatOrderDate(timestamp: Long): String {
    if (timestamp == 0L) return "Unknown date"
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault())
    return formatter.format(date)
}



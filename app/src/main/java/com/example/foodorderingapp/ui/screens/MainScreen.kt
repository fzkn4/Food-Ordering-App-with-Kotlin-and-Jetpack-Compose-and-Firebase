package com.example.foodorderingapp.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.foodorderingapp.R
import kotlinx.coroutines.launch
// Firebase Auth import
import com.google.firebase.auth.FirebaseAuth
// Import User class from data package
import com.example.foodorderingapp.data.User

data class NavigationItems(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)

@Immutable
data class FoodItem(
    val id: String,
    val imageRes: Int,
    val price: Int,
    val foodType: String
)

data class SelectedFoodItem(
    val foodItem: FoodItem,
    var quantity: Int = 1
)

// Order data class for Firebase Database
data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val promoCode: String? = null,
    val orderDate: Long = System.currentTimeMillis(),
    val status: String = "pending"
)

data class OrderItem(
    val foodId: String = "",
    val foodName: String = "",
    val quantity: Int = 0,
    val price: Int = 0
)

// User data class is defined in SignUpScreen.kt

private val foodItems = listOf(
    FoodItem("Hawaiian Pizza", R.drawable.hawaiian_pizza, 250, "Pizza"),
    FoodItem("Chicken Burger", R.drawable.chicken_burger, 135, "Burger"),
    FoodItem("Chicken Pizza", R.drawable.chicken_pizza, 220, "Pizza"),
    FoodItem("Beef Burger", R.drawable.beef_burger, 145, "Burger"),
    FoodItem("Cheese Pizza", R.drawable.cheese_pizza, 280, "Pizza"),
    FoodItem("Cheese Burger", R.drawable.cheese_burger, 120, "Burger"),
    FoodItem("Spaghetti", R.drawable.spaghetti, 135, "Pasta"),
    FoodItem("Carbonara", R.drawable.carbonara, 145, "Pasta"),
)

private val foodTypes = foodItems.map { it.foodType }.distinct()

@Composable
fun MainScreen(navController: NavHostController) {
    val selectedOrderItems = remember { mutableStateListOf<SelectedFoodItem>() }
    AppContent(navController, selectedOrderItems)
}

@Composable
fun AppContent(navController: NavHostController, selectedOrderItems: SnapshotStateList<SelectedFoodItem>) {
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    
    // Create navigation items with dynamic badge count
    val items = remember(selectedOrderItems.size) {
        listOf(
            NavigationItems(
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                hasNews = false,
            ),
            NavigationItems(
                selectedIcon = Icons.Filled.ShoppingCart,
                unselectedIcon = Icons.Outlined.ShoppingCart,
                hasNews = false,
                badgeCount = selectedOrderItems.size
            ),
            NavigationItems(
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                hasNews = true
            )
        )
    }

    Scaffold(
        containerColor = Color(0xFF18172C),
        bottomBar = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFF18172C))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xfffe862b),
                                unselectedIconColor = Color(0xff838393),
                                selectedTextColor = Color.Transparent,
                                indicatorColor = Color.Transparent
                            ),
                            icon = {
                                BadgedBox(
                                    badge = {
                                        when {
                                            item.badgeCount != null && item.badgeCount > 0 -> {
                                                Badge(
                                                    containerColor = Color(0xfffe862b),
                                                    contentColor = Color(0xff18172c)
                                                ) {
                                                    Text(item.badgeCount.toString())
                                                }
                                            }
                                            item.badgeCount == null && item.hasNews -> {
                                                Badge(containerColor = Color(0xfffe862b))
                                            }
                                            else -> {}
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedItemIndex == index) {
                                            item.selectedIcon
                                        } else item.unselectedIcon,
                                        contentDescription = null
                                    )
                                }
                            },
                            selected = selectedItemIndex == index,
                            onClick = { selectedItemIndex = index }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedItemIndex) {
                0 -> HomeScreen(selectedOrderItems)
                1 -> OrdersScreen(selectedOrderItems)
                2 -> SettingsScreen(navController)
            }
        }
    }
}

@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    isSelected: Boolean,
    onSelectionChanged: (Boolean, FoodItem) -> Unit
) {
    val context = LocalContext.current
    val image = painterResource(id = foodItem.imageRes)

    Card(
        onClick = {
            val newSelectedState = !isSelected
            onSelectionChanged(newSelectedState, foodItem)
            Toast.makeText(
                context,
                if (newSelectedState) "Selected: ${foodItem.id}" else "Deselected: ${foodItem.id}",
                Toast.LENGTH_SHORT
            ).show()
        },
        modifier = Modifier
            .size(180.dp)
            .padding(4.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xfffe862b) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff1f1e31))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xff1f1e31))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = "${foodItem.price} ₱",
                    color = Color(0xfffe862b),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp),
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add",
                    tint = Color(0xfffe862b)
                )
            }
            Image(
                painter = image,
                contentDescription = "Food image: ${foodItem.id}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .align(Alignment.Center)
            )
            Text(
                text = foodItem.id.replaceFirstChar { it.uppercase() },
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun HeaderWithBadge() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Hello, User!",
            color = Color.LightGray
        )
        NotificationIconWithBadge(badgeCount = 3)
    }
}

@Composable
fun NotificationIconWithBadge(badgeCount: Int) {
    val context = LocalContext.current
    BadgedBox(
        badge = {
            if (badgeCount > 0) {
                Badge { Text(text = badgeCount.toString()) }
            }
        }
    ) {
        IconButton(onClick = {
            Toast.makeText(context, "Notifications clicked!", Toast.LENGTH_SHORT).show()
        }) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF838393)
            )
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(selectedOrderItems: SnapshotStateList<SelectedFoodItem>) {
    val recommendedListState = rememberLazyListState()
    val popularListState = rememberLazyListState()
    val scrollState = rememberScrollState()
    var selectedFoodType by remember { mutableStateOf<String?>(null) }
    var searchText by remember { mutableStateOf("") }

    val filteredFoodItems by derivedStateOf {
        foodItems.filter { item ->
            (selectedFoodType == null || item.foodType.equals(selectedFoodType, ignoreCase = true)) &&
                    (searchText.isBlank() || item.id.contains(searchText, ignoreCase = true))
        }
    }

    Surface(color = Color(0xff18172c)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                HeaderWithBadge()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "What would you like to order today?",
                        color = Color.White,
                        fontSize = 24.sp,
                        maxLines = 2,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row {
                    TextField(
                        singleLine = false,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1F1E31),
                            focusedContainerColor = Color(0xFF1F1E31),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        value = searchText,
                        onValueChange = { searchText = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF838393)
                            )
                        },
                        placeholder = {
                            Text(
                                text = "Search food...",
                                color = Color(0xFF838393)
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
                Row(Modifier.padding(top = 30.dp)) {
                    Text(text = "Recommended", fontSize = 16.sp, color = Color.White)
                }
            }
            Column(Modifier.wrapContentHeight()) {
                Row(Modifier.fillMaxWidth()) {
                    FoodTypesItemsNavigation(
                        foodTypes = foodTypes,
                        selectedFoodType = selectedFoodType,
                        onFoodTypeSelected = { type ->
                            selectedFoodType = if (selectedFoodType == type) null else type
                        }
                    )
                }
                Row(
                    Modifier
                        .padding(vertical = 20.dp)
                        .wrapContentHeight()
                ) {
                    LazyRow(state = recommendedListState) {
                        items(filteredFoodItems) { item ->
                            FoodItemCard(
                                foodItem = item,
                                isSelected = selectedOrderItems.any { it.foodItem.id == item.id },
                                onSelectionChanged = { isSelected, foodItem ->
                                    if (isSelected) {
                                        selectedOrderItems.add(SelectedFoodItem(foodItem))
                                    } else {
                                        selectedOrderItems.removeAll { it.foodItem.id == item.id }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            Column(Modifier.wrapContentHeight()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(text = "Most Popular", fontSize = 16.sp, color = Color.White)
                }
                Row(
                    Modifier
                        .padding(top = 20.dp)
                        .wrapContentHeight()
                ) {
                    LazyRow(state = popularListState) {
                        items(foodItems) { item ->
                            FoodItemCard(
                                foodItem = item,
                                isSelected = selectedOrderItems.any { it.foodItem.id == item.id },
                                onSelectionChanged = { isSelected, foodItem ->
                                    if (isSelected) {
                                        selectedOrderItems.add(SelectedFoodItem(foodItem))
                                    } else {
                                        selectedOrderItems.removeAll { it.foodItem.id == item.id }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodTypesItemsNavigation(
    foodTypes: List<String>,
    selectedFoodType: String?,
    onFoodTypeSelected: (String) -> Unit
) {
    val foodTypeListState = rememberLazyListState()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        state = foodTypeListState
    ) {
        items(foodTypes) { food ->
            val isSelected = food == selectedFoodType
            FilledTonalButton(
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isSelected) Color(0xfffe862b) else Color(0xff18172c),
                    contentColor = if (isSelected) Color.White else Color(0xFF838393)
                ),
                onClick = { onFoodTypeSelected(food) },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(food)
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val duration = Toast.LENGTH_SHORT
    // Firebase Auth
    val auth = FirebaseAuth.getInstance()
    
    // Get current user
    val currentUser = auth.currentUser
    
    // State for user data from database
    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Fetch user data from Firebase Database
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
            database.reference.child("users").child(currentUser.uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    userData = snapshot.getValue(User::class.java)
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    fun logout() {
        // Firebase sign out
        auth.signOut()
        Toast.makeText(context, "Signed Out", duration).show()
        // Navigate to login screen
        navController.navigate("login") {
            popUpTo("main") { inclusive = true }
        }
    }

    Surface(color = Color(0xff18172c)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.man),
                    contentDescription = "user",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column(Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFE862B),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                // Display user information
                                currentUser?.let { user ->
                                    Text(
                                        text = userData?.username?.ifBlank { "User" } ?: "User",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = user.email ?: "No email",
                                        color = Color(0xff838393),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                    Text(
                                        text = "Member since: ${formatDate(userData?.createdAt ?: 0)}",
                                        color = Color(0xff838393),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                        textAlign = TextAlign.Start
                                    )
                                } ?: run {
                                    Text(
                                        text = "Guest User",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Not signed in",
                                        color = Color(0xff838393),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                }
                            }
                        }
                        
                        // Refresh button
                        if (currentUser != null) {
                            IconButton(
                                onClick = {
                                    // Refresh user data
                                    isLoading = true
                                    val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                                    database.reference.child("users").child(currentUser.uid)
                                        .get()
                                        .addOnSuccessListener { snapshot ->
                                            userData = snapshot.getValue(User::class.java)
                                            isLoading = false
                                            Toast.makeText(context, "Profile refreshed!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(context, "Failed to refresh profile", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh Profile",
                                    tint = Color(0xFFFE862B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ClickableUserCard(
                    onClick = {
                        navController.navigate(com.example.foodorderingapp.navigation.Screen.PreviousOrders.route)
                    },
                    icon = Icons.Filled.CardGiftcard,
                    title = "Previous Orders"
                )
                ClickableUserCard(
                    onClick = {
                        Toast.makeText(context, "My Vouchers", duration).show()
                    },
                    icon = Icons.Filled.CardMembership,
                    title = "My Vouchers"
                )
                ClickableUserCard(
                    onClick = {
                        // Show user profile details
                        val profileInfo = buildString {
                            append("Username: ${userData?.username ?: "Not set"}\n")
                            append("Email: ${currentUser?.email ?: "Not set"}\n")
                            append("Phone: ${userData?.phoneNumber?.ifBlank { "Not set" } ?: "Not set"}\n")
                            append("Address: ${userData?.address?.ifBlank { "Not set" } ?: "Not set"}\n")
                            append("Member since: ${formatDate(userData?.createdAt ?: 0)}")
                        }
                        Toast.makeText(context, profileInfo, Toast.LENGTH_LONG).show()
                    },
                    icon = Icons.Filled.AccountCircle,
                    title = "My Profile"
                )
                ClickableUserCard(
                    onClick = { logout() },
                    icon = Icons.Filled.Logout,
                    title = "Sign Out"
                )
            }
        }
    }
}

@Composable
fun ClickableUserCard(onClick: () -> Unit, icon: ImageVector, title: String?) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
            Text(
                text = title.toString(),
                color = Color(0xff838393),
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 20.dp)
            )
        }
    }
}

@Composable
fun OrdersScreen(selectedOrderItems: SnapshotStateList<SelectedFoodItem>) {
    val foodOrdersListState = rememberLazyListState()
    val context = LocalContext.current
    val promoCodes = remember { setOf("EAu9099", "ASDWW001", "POkl8890") }
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    
    // State for checkout confirmation dialog
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var isProcessingOrder by remember { mutableStateOf(false) }
    
    // Firebase instances
    val auth = FirebaseAuth.getInstance()
    val database = com.google.firebase.database.FirebaseDatabase.getInstance()

    Surface(color = Color(0xff18172c)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    state = foodOrdersListState
                ) {
                    items(selectedOrderItems) { selectedItem ->
                        OrderCardItem(
                            foodItem = selectedItem.foodItem,
                            quantity = selectedItem.quantity,
                            onQuantityChange = { newQty ->
                                selectedItem.quantity = newQty.coerceAtLeast(1)
                            }
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .fillMaxWidth()
                    ) {
                        TextField(
                            singleLine = false,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFF1F1E31),
                                focusedContainerColor = Color(0xFF1F1E31),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            value = text,
                            onValueChange = { text = it },
                            placeholder = {
                                Text(
                                    text = "Promo Code",
                                    color = Color(0xFF838393)
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (text.trim() in promoCodes) {
                                        Toast.makeText(context, "Promo code applied successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Invalid promo code.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = text.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xfffe862b),
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (text.isBlank()) Color.Gray else Color.Transparent
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .height(IntrinsicSize.Min)
                        ) {
                            Text("Apply", fontSize = 16.sp)
                        }
                    }
                    val subtotal = selectedOrderItems.sumOf { it.foodItem.price * it.quantity }
                    val tax = subtotal * 0.12
                    val total = tax + subtotal

                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Subtotal",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "%.2f ₱".format(subtotal.toFloat()),
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Tax",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "%.2f ₱".format(tax.toFloat()),
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Total",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "%.2f ₱".format(total.toFloat()),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(
                        onClick = { 
                            if (selectedOrderItems.isNotEmpty()) {
                                showCheckoutDialog = true
                            } else {
                                Toast.makeText(context, "Please add items to your cart first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffe862b)),
                        shape = RoundedCornerShape(18.dp),
                        enabled = selectedOrderItems.isNotEmpty() && !isProcessingOrder
                    ) {
                        if (isProcessingOrder) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                "Checkout",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Checkout confirmation dialog
    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isProcessingOrder) {
                    showCheckoutDialog = false 
                }
            },
            title = {
                Text(
                    "Confirm Order",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Order Summary:",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    selectedOrderItems.forEach { item ->
                        Text(
                            "• ${item.foodItem.id} x${item.quantity} - ₱${item.foodItem.price * item.quantity}",
                            color = Color(0xFF838393),
                            fontSize = 14.sp
                        )
                    }
                    
                    Divider(
                        color = Color(0xFF838393),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    val subtotal = selectedOrderItems.sumOf { it.foodItem.price * it.quantity }
                    val tax = subtotal * 0.12
                    val total = subtotal + tax
                    
                    Text(
                        "Subtotal: ₱${subtotal}",
                        color = Color(0xFF838393),
                        fontSize = 14.sp
                    )
                    Text(
                        "Tax: ₱${tax}",
                        color = Color(0xFF838393),
                        fontSize = 14.sp
                    )
                    Text(
                        "Total: ₱${total}",
                        color = Color(0xFFFE862B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        processOrder(
                            selectedOrderItems = selectedOrderItems,
                            promoCode = text.trim().takeIf { it.isNotBlank() },
                            auth = auth,
                            database = database,
                            onSuccess = {
                                showCheckoutDialog = false
                                selectedOrderItems.clear()
                                text = ""
                                Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, "Failed to place order: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isProcessingOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE862B))
                ) {
                    if (isProcessingOrder) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text("Confirm Order")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        if (!isProcessingOrder) {
                            showCheckoutDialog = false 
                        }
                    },
                    enabled = !isProcessingOrder
                ) {
                    Text("Cancel", color = Color(0xFF838393))
                }
            },
            containerColor = Color(0xFF1F1E31),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

@Composable
fun OrderCardItem(
    foodItem: FoodItem,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xff1f1e31)),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = foodItem.imageRes),
                contentDescription = "Food image: ${foodItem.id}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodItem.id.replaceFirstChar { it.uppercase() },
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "${foodItem.price} ₱",
                    color = Color(0xfffe862b),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xfffe862b),
                    modifier = Modifier.size(34.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Increase quantity",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = quantity.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xfffe862b),
                    modifier = Modifier.size(34.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Decrease quantity",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format date
private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Unknown"
    
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}

// Function to process and store the order
private fun processOrder(
    selectedOrderItems: SnapshotStateList<SelectedFoodItem>,
    promoCode: String?,
    auth: FirebaseAuth,
    database: com.google.firebase.database.FirebaseDatabase,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val currentUser = auth.currentUser
    if (currentUser == null) {
        onError("User not authenticated")
        return
    }
    
    if (selectedOrderItems.isEmpty()) {
        onError("No items in cart")
        return
    }
    
    // Calculate order totals
    val subtotal = selectedOrderItems.sumOf { it.foodItem.price * it.quantity }
    val tax = subtotal * 0.12
    val total = subtotal + tax
    
    // Create order items
    val orderItems = selectedOrderItems.map { selectedItem ->
        OrderItem(
            foodId = selectedItem.foodItem.id,
            foodName = selectedItem.foodItem.id,
            quantity = selectedItem.quantity,
            price = selectedItem.foodItem.price
        )
    }
    
    // Create order object
    val order = Order(
        orderId = database.reference.child("orders").push().key ?: "",
        userId = currentUser.uid,
        items = orderItems,
        subtotal = subtotal.toDouble(),
        tax = tax.toDouble(),
        total = total.toDouble(),
        promoCode = promoCode,
        orderDate = System.currentTimeMillis(),
        status = "pending"
    )
    
    // Store order in Firebase Database
    database.reference.child("orders").child(order.orderId).setValue(order)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { exception ->
            onError(exception.message ?: "Unknown error")
        }
}
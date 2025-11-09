package com.example.foodorderingapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.foodorderingapp.R
import com.example.foodorderingapp.data.User
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

data class NavigationItems(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null,
    val label: String
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

data class NearbyShop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val distanceMeters: Float,
    val rating: Double?,
    val userRatingsTotal: Int?
)

private data class GeocodedLocation(
    val address: String?,
    val city: String?
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
                label = "Home"
            ),
            NavigationItems(
                selectedIcon = Icons.Filled.Store,
                unselectedIcon = Icons.Outlined.Store,
                hasNews = false,
                label = "Shops"
            ),
            NavigationItems(
                selectedIcon = Icons.Filled.ShoppingCart,
                unselectedIcon = Icons.Outlined.ShoppingCart,
                hasNews = false,
                badgeCount = selectedOrderItems.size,
                label = "Cart"
            ),
            NavigationItems(
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                hasNews = true,
                label = "Settings"
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
                                        contentDescription = item.label
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
                1 -> ShopsScreen()
                2 -> OrdersScreen(selectedOrderItems)
                3 -> SettingsScreen(navController)
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

@SuppressLint("MissingPermission")
@Composable
fun ShopsScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    val googleMapsApiKey = remember { getGoogleMapsApiKey(context) }

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasRequestedPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf<String?>(null) }
    var cityName by remember { mutableStateOf<String?>(null) }

    var isFetchingShops by remember { mutableStateOf(false) }
    var shopsError by remember { mutableStateOf<String?>(null) }
    val nearbyShops = remember { mutableStateListOf<NearbyShop>() }

    fun fetchLocation() {
        if (!permissionGranted) return
        loadLocation(
            fusedLocationClient = fusedLocationClient,
            onStart = {
                isLocating = true
                locationError = null
            },
            onSuccess = { location ->
                isLocating = false
                locationError = null
                currentLocation = location
            },
            onFallback = {
                isLocating = false
                locationError = "Unable to determine your location right now."
            },
            onFailure = { throwable ->
                isLocating = false
                locationError = throwable.localizedMessage ?: "Failed to fetch your location."
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            fetchLocation()
        } else {
            locationError = "Location access is needed to show the food spots near you."
        }
    }

    val shouldShowRationale = remember(permissionGranted, hasRequestedPermission) {
        !permissionGranted && hasRequestedPermission && activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == true
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted && currentLocation == null) {
            fetchLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionGranted && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    suspend fun updateNearbyShops(location: Location) {
        if (googleMapsApiKey.isBlank()) {
            shopsError = "Google Maps API key missing. Add it to the .env file and sync the project."
            nearbyShops.clear()
            return
        }
        shopsError = null
        isFetchingShops = true
        val result = runCatching {
            fetchFastFoodSpots(
                latitude = location.latitude,
                longitude = location.longitude,
                radiusMeters = 7000,
                cityName = cityName,
                apiKey = googleMapsApiKey
            )
        }
        result.onSuccess { shops ->
            nearbyShops.clear()
            nearbyShops.addAll(shops)
        }.onFailure { throwable ->
            shopsError = throwable.localizedMessage ?: "Failed to load nearby shops."
            nearbyShops.clear()
        }
        isFetchingShops = false
    }

    LaunchedEffect(currentLocation, googleMapsApiKey) {
        val location = currentLocation ?: return@LaunchedEffect
        val geocoded = geocodeLocation(context, location.latitude, location.longitude)
        address = geocoded?.address
        cityName = geocoded?.city
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(location.latitude, location.longitude),
            14f
        )
        updateNearbyShops(location)
    }

    Surface(color = Color(0xff18172c)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Text(
                text = "Nearby Shops",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Explore the fast food spots around you.",
                color = Color(0xFF838393),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                googleMapsApiKey.isBlank() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Google Maps API key not found.",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Add GOOGLE_MAPS_API_KEY to the .env file and sync the project.",
                            color = Color(0xFF838393),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                !permissionGranted -> {
                    LocationPermissionState(
                        shouldShowRationale = shouldShowRationale,
                        onRequestPermission = {
                            hasRequestedPermission = true
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    )
                }

                isLocating -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xfffe862b))
                    }
                }

                locationError != null -> {
                    LocationErrorState(
                        message = locationError.orEmpty(),
                        onRetry = {
                            locationError = null
                            fetchLocation()
                        }
                    )
                }

                currentLocation == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xfffe862b))
                    }
                }

                else -> {
                    val location = currentLocation!!
                    val userLatLng = LatLng(location.latitude, location.longitude)
    val mapStyleOptions = remember {
        runCatching {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_food_only)
        }.getOrNull()
    }

    val mapProperties = MapProperties(
        isMyLocationEnabled = permissionGranted,
        mapStyleOptions = mapStyleOptions
    )
                    val mapUiSettings = MapUiSettings(
                        myLocationButtonEnabled = true,
                        zoomControlsEnabled = false
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        GoogleMap(
                            modifier = Modifier.matchParentSize(),
                            cameraPositionState = cameraPositionState,
                            properties = mapProperties,
                            uiSettings = mapUiSettings
                        ) {
                            Marker(
                                state = rememberMarkerState(position = userLatLng),
                                title = cityName ?: "You are here",
                                snippet = address ?: "Current location"
                            )

                            nearbyShops.forEach { shop ->
                                val markerLatLng = LatLng(shop.latitude, shop.longitude)
                                val snippet = buildString {
                                    shop.address?.let {
                                        append(it)
                                    }
                                    append("\n")
                                    append("Distance: ${formatDistance(shop.distanceMeters)}")
                                    shop.rating?.let { rating ->
                                        append("\nRating: ${"%.1f".format(rating)}")
                                        shop.userRatingsTotal?.let { total ->
                                            append(" ($total reviews)")
                                        }
                                    }
                                }
                                Marker(
                                    state = rememberMarkerState(position = markerLatLng),
                                    title = shop.name,
                                    snippet = snippet
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                color = Color(0xCC1F1E31),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                ) {
                                    cityName?.let { city ->
                                        Text(
                                            text = city,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    address?.let { currentAddress ->
                                        Text(
                                            text = currentAddress,
                                            color = Color(0xFF838393),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = { coroutineScope.launch { updateNearbyShops(location) } },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(40.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0xCC1F1E31),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh map",
                                tint = Color(0xfffe862b)
                            )
                        }

                        when {
                            shopsError != null -> {
                                Surface(
                                    color = Color(0xCC18172C),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = shopsError.orEmpty(),
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                shopsError = null
                                                coroutineScope.launch { updateNearbyShops(location) }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffe862b))
                                        ) {
                                            Text("Try Again")
                                        }
                                    }
                                }
                            }

                            isFetchingShops -> {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color(0x6618172C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }

                            nearbyShops.isEmpty() -> {
                                Surface(
                                    color = Color(0xCC18172C),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(24.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "No fast food spots found yet.",
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                coroutineScope.launch { updateNearbyShops(location) }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffe862b))
                                        ) {
                                            Text("Refresh Map")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun loadLocation(
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onStart: () -> Unit,
    onSuccess: (Location) -> Unit,
    onFallback: () -> Unit,
    onFailure: (Throwable) -> Unit
) {
    onStart()
    val cancellationTokenSource = CancellationTokenSource()
    fusedLocationClient
        .getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token
        )
        .addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(location)
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            onSuccess(lastLocation)
                        } else {
                            onFallback()
                        }
                    }
                    .addOnFailureListener { throwable -> onFailure(throwable) }
            }
        }
        .addOnFailureListener { throwable -> onFailure(throwable) }
}

@Composable
private fun LocationPermissionState(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (shouldShowRationale) {
                "We need your location to show nearby food spots. Please grant access in settings."
            } else {
                "We need your location to show nearby food spots."
            },
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffe862b)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Grant Location Access")
        }
    }
}

@Composable
private fun LocationErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffe862b)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Try Again")
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

private fun calculateDistanceMeters(
    originLatitude: Double,
    originLongitude: Double,
    targetLatitude: Double,
    targetLongitude: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        originLatitude,
        originLongitude,
        targetLatitude,
        targetLongitude,
        results
    )
    return results[0]
}

private fun formatDistance(distanceMeters: Float): String {
    return if (distanceMeters >= 1000f) {
        "%.1f km".format(distanceMeters / 1000f)
    } else {
        "%.0f m".format(distanceMeters)
    }
}

private fun getGoogleMapsApiKey(context: Context): String {
    return try {
        val packageManager = context.packageManager
        val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        }
        applicationInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
    } catch (exception: Exception) {
        ""
    }
}

private suspend fun geocodeLocation(
    context: Context,
    latitude: Double,
    longitude: Double
): GeocodedLocation? = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1)
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)
        }

        val first = results?.firstOrNull() ?: return@withContext null
        val addressLine = first.getAddressLine(0)
        val cityName = first.locality
            ?.takeIf { it.isNotBlank() }
            ?: first.subAdminArea
            ?: first.adminArea

        GeocodedLocation(address = addressLine, city = cityName)
    } catch (_: IOException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}

private suspend fun fetchFastFoodSpots(
    latitude: Double,
    longitude: Double,
    radiusMeters: Int,
    cityName: String?,
    apiKey: String
): List<NearbyShop> = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) {
        throw IllegalStateException("Google Maps API key is missing.")
    }

    val encodedKeyword = URLEncoder.encode("fast food", StandardCharsets.UTF_8.name())
    val encodedCityQuery = cityName
        ?.takeIf { it.isNotBlank() }
        ?.let { URLEncoder.encode("fast food in $it", StandardCharsets.UTF_8.name()) }

    val shopsById = linkedMapOf<String, NearbyShop>()

    suspend fun collectFromUrlBuilder(builder: (String?) -> String) {
        var nextPageToken: String? = null
        var pageIndex = 0
        do {
            val url = builder(nextPageToken)
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 20000
                readTimeout = 20000
            }

            try {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(response)
                val status = root.optString("status")
                if (status !in listOf("OK", "ZERO_RESULTS")) {
                    val errorMessage = root.optString("error_message")
                    throw IOException(
                        buildString {
                            append("Places API error: ")
                            append(status)
                            if (errorMessage.isNotBlank()) {
                                append(" - ")
                                append(errorMessage)
                            }
                        }
                    )
                }

                val resultsArray = root.optJSONArray("results") ?: JSONArray()
                for (index in 0 until resultsArray.length()) {
                    val item = resultsArray.optJSONObject(index) ?: continue
                    val placeId = item.optString("place_id")
                        .takeIf { it.isNotBlank() }
                        ?: item.optString("id").takeIf { it.isNotBlank() }
                        ?: continue

                    val geometry = item.optJSONObject("geometry")
                        ?.optJSONObject("location")
                        ?: continue
                    val shopLatitude = geometry.optDouble("lat", Double.NaN)
                    val shopLongitude = geometry.optDouble("lng", Double.NaN)
                    if (!shopLatitude.isFinite() || !shopLongitude.isFinite()) continue

                    val distance = calculateDistanceMeters(
                        originLatitude = latitude,
                        originLongitude = longitude,
                        targetLatitude = shopLatitude,
                        targetLongitude = shopLongitude
                    )

                    val ratingValue = item.optDouble("rating", Double.NaN)
                    val rating = when {
                        ratingValue.isNaN() || ratingValue < 0 -> null
                        else -> ratingValue
                    }
                    val totalRatings = item.optInt("user_ratings_total", -1).takeIf { it >= 0 }

                    val shop = NearbyShop(
                        id = placeId,
                        name = item.optString("name").ifBlank { "Fast food spot" },
                        latitude = shopLatitude,
                        longitude = shopLongitude,
                        address = item.optString("formatted_address", item.optString("vicinity", null)),
                        distanceMeters = distance,
                        rating = rating,
                        userRatingsTotal = totalRatings
                    )

                    shopsById[placeId] = shop
                }

                val token = root.optString("next_page_token", "").takeIf { it.isNotBlank() }
                nextPageToken = if (token != null && pageIndex < 2) token else null
                pageIndex++

                if (nextPageToken != null) {
                    delay(2000)
                }

                if (status == "ZERO_RESULTS") {
                    nextPageToken = null
                }
            } finally {
                connection.disconnect()
            }
        } while (nextPageToken != null && pageIndex < 3)
    }

    if (encodedCityQuery != null) {
        collectFromUrlBuilder { pageToken ->
            if (pageToken == null) {
                "https://maps.googleapis.com/maps/api/place/textsearch/json?query=$encodedCityQuery&key=$apiKey"
            } else {
                "https://maps.googleapis.com/maps/api/place/textsearch/json?pagetoken=$pageToken&key=$apiKey"
            }
        }
    }

    if (shopsById.isEmpty()) {
        val clampedRadius = radiusMeters.coerceIn(1000, 50000)
        collectFromUrlBuilder { pageToken ->
            if (pageToken == null) {
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&radius=$clampedRadius&type=restaurant&keyword=$encodedKeyword&key=$apiKey"
            } else {
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=$pageToken&key=$apiKey"
            }
        }
    }

    shopsById.values.sortedBy { it.distanceMeters }
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
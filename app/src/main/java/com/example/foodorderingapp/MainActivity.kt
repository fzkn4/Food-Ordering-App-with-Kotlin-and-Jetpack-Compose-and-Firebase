package com.example.foodorderingapp

import android.graphics.Paint
import android.os.Bundle
import android.provider.CalendarContract
import android.service.autofill.OnClickAction
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.foodorderingapp.ui.theme.FoodOrderingAppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign

data class NavigationItems(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)
data class FoodItem(val id: String, val imageRes: Int, val price: Int, val foodType: String)

data class SelectedFoodItem(
    val foodItem: FoodItem,
    var quantity: Int = 1
)

val selectedOrderItems = mutableStateListOf<String>()
val foodItems = listOf(
    FoodItem("Hawaiian Pizza", R.drawable.hawaiian_pizza, 250, "Pizza"),
    FoodItem("Chicken Burger", R.drawable.chicken_burger, 135, "Burger"),
    FoodItem("Chicken Pizza", R.drawable.chicken_pizza, 220, "Pizza"),
    FoodItem("Beef Burger", R.drawable.beef_burger, 145, "Burger"),
    FoodItem("Cheese Pizza", R.drawable.cheese_pizza, 280, "Pizza"),
    FoodItem("Cheese Burger", R.drawable.cheese_burger, 120, "Burger"),
    FoodItem("Spaghetti", R.drawable.spaghetti, 135, "Pasta"),
    FoodItem("Carbonara", R.drawable.carbonara, 145, "Pasta"),
)

class MainActivity : ComponentActivity() {
    val foodTypes : MutableList<String> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodOrderingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF18172C))
                    {
                        AppContent()
                    }
            }
        }
    }
}

@Composable
fun AppContent() {
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    var selectedOrder by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavigationItems(
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            hasNews = false,
        ),
        NavigationItems(
            selectedIcon = Icons.Filled.ShoppingCart,
            unselectedIcon = Icons.Outlined.ShoppingCart,
            hasNews = false,
            badgeCount = selectedOrder
        ),
        NavigationItems(
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            hasNews = true
        )
    )

    Scaffold(
        containerColor = Color(0xFF18172C),
        bottomBar = {
            // Wrap NavigationBar in a Box with rounded corners
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFF18172C)
                    )
            ) {
                NavigationBar(
                    containerColor = Color.Transparent, // Make the actual bar transparent
                    modifier = Modifier
                        .padding(top = 8.dp) // Add some space above the items
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
                                        if (item.badgeCount != null) {
                                            Badge(
                                                containerColor = Color(0xfffe862b),
                                                contentColor = Color(0xff18172c)
                                            ) {
                                                Text(item.badgeCount.toString())
                                            }
                                        } else if (item.hasNews) {
                                            Badge(
                                                containerColor = Color(0xfffe862b)
                                            )
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
                0 -> HomeScreen()
                1 -> OrdersScreen()
                2 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun FoodItemCard(
    foodItem: FoodItem,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Card(
        onClick = {
            val newSelectedState = !isSelected
            onSelectionChanged(newSelectedState)
            Toast.makeText(
                context,
                if (newSelectedState) "Selected: ${foodItem.id}"
                else "Deselected: ${foodItem.id}",
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xff1f1e31)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background color
            Box(modifier = Modifier.fillMaxSize().background(Color(0xff1f1e31)))

            // Price and Add icon
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
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

            // Food image
            Image(
                painter = painterResource(id = foodItem.imageRes),
                contentDescription = "Food image: ${foodItem.id}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .align(Alignment.Center)
            )

            // Food name
            Text(
                text = foodItem.id.replaceFirstChar { it.uppercase() },
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 16.sp,
            )
        }
    }
}


@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    val selectedItems = selectedOrderItems
    var selectedFoodType by remember { mutableStateOf<String?>(null) } // Track selected food type
    val foodTypes = listOf(
        "Pizza",
        "Burger",
        "Sushi",
        "Pasta",
        "Dessert"
    )

    val foodItems = listOf(
        FoodItem("Hawaiian Pizza", R.drawable.hawaiian_pizza, 250, "Pizza"),
        FoodItem("Chicken Burger", R.drawable.chicken_burger, 135, "Burger"),
        FoodItem("Chicken Pizza", R.drawable.chicken_pizza, 220, "Pizza"),
        FoodItem("Beef Burger", R.drawable.beef_burger, 145, "Burger"),
        FoodItem("Cheese Pizza", R.drawable.cheese_pizza, 280, "Pizza"),
        FoodItem("Cheese Burger", R.drawable.cheese_burger, 120, "Burger"),
        FoodItem("Spaghetti", R.drawable.spaghetti, 135, "Pasta"),
        FoodItem("Carbonara", R.drawable.carbonara, 145, "Pasta"),
    )

    // Filter food items based on selected food type
    val filteredFoodItems = remember(selectedFoodType) {
        if (selectedFoodType == null) {
            foodItems
        } else {
            foodItems.filter { it.foodType.equals(selectedFoodType, ignoreCase = true) }
        }
    }

    var text by remember { mutableStateOf("") }

    Surface(color = Color(0xff18172c)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Your home screen content
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.restaurant),
                            contentDescription = "Restaurant",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Grab 'n Eat",
                            color = Color.White,
                            modifier = Modifier.padding(start = 5.dp))
                    }
                }
                Column (
                    Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 20.dp),
                ){
                    Text(color = Color.LightGray, text = "Hello, User!")
                    Text(text = "What would you like to eat",
                        color = Color.White,
                        fontSize = 24.sp,
                        maxLines = 2,
                        fontWeight = FontWeight.ExtraBold)
                }
                Row {
                    TextField(
                        singleLine = false,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFF1F1E31),
                            focusedContainerColor = Color(0xFF1F1E31),
                            focusedIndicatorColor = Color.Transparent, // Remove focused line
                            unfocusedIndicatorColor = Color.Transparent, // Remove unfocused line
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        value = text,
                        onValueChange = { text = it },
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
                                color = Color(0xFF838393)) // Add placeholder color
                        },
                        shape = RoundedCornerShape(16.dp) // Explicit shape for consistency
                    )

                }
                Row(Modifier.padding(top = 30.dp)){
                    Text(text = "Recommended", fontSize = 16.sp, color = Color.White)
                }
            }
            Column(Modifier.wrapContentHeight()) {
                // Food types items - modified to update selectedFoodType
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
                    Modifier.padding(vertical = 20.dp)
                        .wrapContentHeight()
                ) {
                    LazyRow {
                        items(filteredFoodItems) { item ->
                            FoodItemCard(
                                foodItem = item,
                                isSelected = selectedItems.contains(item.id),
                                onSelectionChanged = { isSelected ->
                                    if (isSelected) {
                                        selectedItems.add(item.id)
                                    } else {
                                        selectedItems.remove(item.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Column (
                Modifier.wrapContentHeight()
            ){
                Row (
                    Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Start
                    ){
                    Text(text = "Most Popular", fontSize = 16.sp, color = Color.White)
                }

                Row(
                    Modifier.padding(top= 20.dp)
                        .wrapContentHeight()
                ){
                    LazyRow {
                        items(foodItems) { item ->
                            FoodItemCard(
                                foodItem = item,
                                isSelected = selectedItems.contains(item.id),
                                onSelectionChanged = { isSelected ->
                                    if (isSelected) {
                                        selectedItems.add(item.id)
                                    } else {
                                        selectedItems.remove(item.id)
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(foodTypes) { food ->
            val isSelected = food == selectedFoodType

            FilledTonalButton(
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isSelected) Color(0xfffe862b)
                    else Color(0xff18172c),
                    contentColor = if (isSelected) Color.White
                    else Color(0xFF838393)
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
fun SettingsScreen() {
    Surface(
        color = Color(0xff18172c)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

            ) {
            // Your home screen content
            Text(text = "Settings screen", color = Color.White)
        }
    }
}

@Composable
fun OrdersScreen() {
    Surface(
        color = Color(0xff18172c)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scrollable content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                items(foodItems) { item ->
                    if (item.id in selectedOrderItems){
                        OrderCardItem(item)
                    }
                }
            }

            // Fixed bottom content (Total and Button)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Total",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "0.00 ₱",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { /* Button click action */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xfffe862b)
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
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


@Composable
fun OrderCardItem(foodItem: FoodItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xff1f1e31),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Fixed height for consistency
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image on the left
            Image(
                painter = painterResource(id = foodItem.imageRes),
                contentDescription = "Food image: ${foodItem.id}",
                contentScale = ContentScale.Crop, // Changed to Crop
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Content on the right
            // Food Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
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

            // Quantity Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing
            ) {
                // Quantity/status indicator
                Surface(
                    shape = RoundedCornerShape(8.dp), // Adjust this value for desired corner radius
                    color = Color(0xfffe862b), // Your button color
                    modifier = Modifier.size(34.dp)
                ) {
                    IconButton(
                        onClick = { /* Increase quantity */ },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent // Make button itself transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = "1x",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium, // Smaller text
                    modifier = Modifier.padding(vertical = 4.dp) // Reduced padding
                )
                Surface(
                    shape = RoundedCornerShape(8.dp), // Adjust this value for desired corner radius
                    color = Color(0xfffe862b), // Your button color
                    modifier = Modifier.size(34.dp)
                ) {
                    IconButton(
                        onClick = { /* Increase quantity */ },
                        modifier = Modifier.size(28.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Transparent // Make button itself transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FoodOrderingAppTheme {
        AppContent()
    }
}

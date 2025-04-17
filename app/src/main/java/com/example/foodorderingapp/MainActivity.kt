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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale

data class NavigationItems(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val badgeCount: Int? = null
)
data class FoodItem(val id: String, val imageRes: Int, val price: Int)

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
            badgeCount = 10
        ),
        NavigationItems(
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            hasNews = true
        )
    )
    Scaffold(
        bottomBar = {
            NavigationBar (
                containerColor = Color(0xFF18172C)
            ){

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
                                        Badge (
                                            containerColor = Color(0xfffe862b), // Background color
                                            contentColor = Color(0xff18172c)
                                        ){
                                            Text(item.badgeCount.toString()) }
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
                                    } else {
                                        item.unselectedIcon
                                    },
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
fun FoodItemCard(foodId: String, imageResId: Int, price: Int) {
    val context = LocalContext.current

    Card(
        onClick = {
            Toast.makeText(
                context,
                "Selected: ${foodId.replaceFirstChar { it.uppercase() }}",
                Toast.LENGTH_SHORT
            ).show()
        },
        modifier = Modifier
            .size(180.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xff1f1e31)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Background color behind the image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xff1f1e31)) // Change this to your desired background color
            )

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)){
                Text(text = "$price ₱",
                    color = Color(0xfffe862b),
                    modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 10.dp),
                    fontWeight = FontWeight.Bold)
                Icon(
                    modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp),
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add",
                    tint = Color(0xfffe862b),

                )

            }

            // Slightly smaller image
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Food image: $foodId",
                contentScale = ContentScale.Fit, // Use Fit to avoid stretching
                modifier = Modifier
                    .fillMaxSize(0.7f) // Image takes 90% of the available size
                    .align(Alignment.Center)
            )

            // Text overlay
            Text(
                text = foodId.replaceFirstChar { it.uppercase() },
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
            )
        }
    }
}


@Composable
fun HomeScreen() {
    val foodTypes = listOf(
        "Pizza",
        "Burger",
        "Sushi",
        "Pasta",
        "Dessert")

    val foodItems = listOf(
        FoodItem("Hawaiian Pizza", R.drawable.hawaiian_pizza, 250),
        FoodItem("Chicken Pizza", R.drawable.chicken_pizza, 220),
        FoodItem("Cheese Pizza", R.drawable.cheese_pizza, 280),

    )

    var text by remember{
        mutableStateOf("")
    }
    Surface(
    color = Color(0xff18172c)
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    Text(color = Color.LightGray, text = "Welcome back! User")
                    Text(text = "What are your cravings today?", color = Color.White, fontSize = 24.sp, maxLines = 2)
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
                    Text(text = "Available For You", fontSize = 16.sp, color = Color.White)
                }
            }
            Column (){
                // Food types items
                FoodTypesItemsNavigation(foodTypes = foodTypes)

                Row(
                    Modifier.padding(vertical = 20.dp)
                ){
                    LazyRow {
                        items(foodItems) { item ->
                            FoodItemCard(
                                foodId = item.id,
                                imageResId = item.imageRes,
                                price = item.price
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodTypesItemsNavigation(foodTypes: List<String>) {
    val context = LocalContext.current
    val duration = Toast.LENGTH_SHORT
    val text = "Selected: "
    var selectedItem by remember { mutableStateOf<String?>(foodTypes.firstOrNull()) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(foodTypes) { food ->
            if (food == selectedItem) {
                FilledTonalButton(
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor  = Color(0xfffe862b),
                    ),
                    onClick = {
                        selectedItem = null
                        Toast.makeText(context, "$text$food", duration).show()
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(food)
                }
            } else {
                FilledTonalButton(
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor  = Color(0xff18172c),
                        contentColor = Color(0xFF838393)
                    ),
                    onClick = {
                        selectedItem = food
                        Toast.makeText(context, "$text$food", duration).show()
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(food)
                }
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
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            // Your home screen content
            Text(text = "Orders screen", color = Color.White)
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

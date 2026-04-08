package com.example.a212062_rimaniza_lab1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a212062_rimaniza_lab1.ui.theme.A212062_Rimaniza_Lab1Theme

// Data class for easier item management
data class FoodItemData(val imageRes: Int, val name: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A212062_Rimaniza_Lab1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // Scrollable section
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.size(24.dp)) // Extra padding for the top bar
                            Search()
                            Category()
                            FoodCategory()
                        }
                        
                        // Fixed section
                        NavBar(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun Search(
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF242424), shape = RoundedCornerShape(25.dp))
            .padding(horizontal = 4.dp)
    ) {
        IconButton(onClick = { /* TODO: Open menu */ }) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color(0xFFFFC0CB))
        }
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Search", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        IconButton(onClick = { /* TODO: Perform search */ }) {
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFFFFC0CB))
        }
    }
}

@Composable
fun Category(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryItem(Icons.Filled.Favorite, "Favourite", Modifier.weight(1f))
            CategoryItem(Icons.Filled.AccessTime, "Recent", Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Origin with a slightly different background as requested
            CategoryItem(Icons.Filled.Public, "Origin", Modifier.weight(1f), true)
            CategoryItem(Icons.Filled.Flatware, "Type", Modifier.weight(1f))
        }
    }
}

@Composable
fun CategoryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isSelected) Color(0xFFFFC0CB) else Color(0xFF242424)
    val iconTint = if (isSelected) Color(0xFF242424) else Color(0xFFFFC0CB)
    val fontColor = if (isSelected) Color(0xFF242424) else Color.White


    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = iconTint)
        Text(
            text = label,
            fontSize = 18.sp,
            textAlign = TextAlign.Left,
            color = fontColor,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun FoodCategory(
    modifier: Modifier = Modifier
) {
    // 1. Create lists of data for easier management
    val malayFood = listOf(
        FoodItemData(R.drawable.nasmak, "Nasi Lemak"),
        FoodItemData(R.drawable.beefrendang, "Beef Rendang"),
        FoodItemData(R.drawable.satay, "Satay"), // Added 3rd item to test scroll
    )
    val chineseFood = listOf(
        FoodItemData(R.drawable.pekingduck, "Peking Duck"),
        FoodItemData(R.drawable.xiaolongbao, "Xiaolongbao"),
        FoodItemData(R.drawable.chowmein, "Chow Mein"),
    )
    val indianFood = listOf(
        FoodItemData(R.drawable.butterchicken, "Butter Chicken"),
        FoodItemData(R.drawable.biryani, "Biryani"),
        FoodItemData(R.drawable.samosa, "Samosa"),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FoodSectionHeader("Malay")
        FoodRow(malayFood)

        FoodSectionHeader("Chinese")
        FoodRow(chineseFood)

        FoodSectionHeader("Indian")
        FoodRow(indianFood)
    }
}

@Composable
fun FoodSectionHeader(title: String, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            color = Color.White
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "More", tint = Color(0xFFFFC0CB))
    }
}

@Composable
fun FoodRow(items: List<FoodItemData>) {
    // 2. Row handles the scroll. We remove weight(1f) from children so they can expand past screen width.
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            FoodItem(item.imageRes, item.name)
        }
    }
}

@Composable
fun FoodItem(imageRes: Int, name: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = name,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NavBar(
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Recipe", "Shopping", "Planner", "Community")
    val icons = listOf(Icons.AutoMirrored.Filled.MenuBook, Icons.Filled.ShoppingCart, Icons.Filled.CalendarMonth, Icons.Filled.Groups)

    NavigationBar(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        containerColor = Color(0xFF242424),
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItem == index
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item, fontSize = 12.sp) },
                selected = isSelected,
                onClick = { selectedItem = index },
                modifier = if (isSelected) {
                    Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFC0CB))
                } else Modifier,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF242424),
                    selectedTextColor = Color(0xFF242424),
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodAppPreview() {
    A212062_Rimaniza_Lab1Theme {
        Surface(color = Color.Black) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Search()
                    Category()
                    FoodCategory()
                }
                NavBar(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

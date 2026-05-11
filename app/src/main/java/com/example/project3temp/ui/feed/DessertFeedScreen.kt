package com.example.project3temp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project3temp.data.Categories
import com.example.project3temp.data.Dessert
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.DessertRepo
import com.example.project3temp.data.Districts
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.DistrictChipBg
import com.example.project3temp.ui.theme.Project3tempTheme
import com.example.project3temp.ui.theme.SoldOutRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DessertFeedScreen(
    onAddClick: () -> Unit = {},
    onCardClick: (Dessert) -> Unit = {},
) {
    var selectedCategoryId by remember { mutableStateOf(Categories.ALL_ID) }
    var selectedRegion by remember { mutableStateOf("서울") }
    var selectedDistrict by remember { mutableStateOf(Districts.ALL_LABEL) }

    val filtered = DessertRepo.items.filter { dessert ->
        val byCategory = selectedCategoryId == Categories.ALL_ID ||
            dessert.menuItems.any { it.categoryId == selectedCategoryId }
        val byDistrict =
            selectedDistrict == Districts.ALL_LABEL || dessert.district == selectedDistrict
        byCategory && byDistrict
    }

    Scaffold(
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "오늘의 디저트",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = "DESSERTS TODAY",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground),
            )
        },
        floatingActionButton = {
            AddFab(onClick = onAddClick)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CategoryRow(
                categories = Categories.list,
                selectedId = selectedCategoryId,
                onSelect = { selectedCategoryId = it },
            )
            FilterRow(
                selectedRegion = selectedRegion,
                onRegionChange = { selectedRegion = it },
                selectedDistrict = selectedDistrict,
                onDistrictChange = { selectedDistrict = it },
            )
            FeedHeader(updatedCount = filtered.size)
            DessertGrid(items = filtered, onCardClick = onCardClick)
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<DessertCategory>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(categories, key = { it.id }) { category ->
            CategoryItem(
                category = category,
                selected = category.id == selectedId,
                onClick = { onSelect(category.id) },
            )
        }
    }
}

@Composable
private fun CategoryItem(
    category: DessertCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) BrandOrange else BrandOrangeSoft
    val labelColor = if (selected) BrandOrange else Color.DarkGray
    val labelWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category.emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = category.label,
            fontSize = 13.sp,
            fontWeight = labelWeight,
            color = labelColor,
        )
    }
}

@Composable
private fun FilterRow(
    selectedRegion: String,
    onRegionChange: (String) -> Unit,
    selectedDistrict: String,
    onDistrictChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChipDropdown(
            label = selectedRegion,
            options = listOf("서울"),
            onSelect = onRegionChange,
        )
        FilterChipDropdown(
            label = selectedDistrict,
            options = Districts.seoul,
            onSelect = onDistrictChange,
        )
    }
}

@Composable
private fun FilterChipDropdown(
    label: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DistrictChipBg)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun FeedHeader(updatedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "오늘 ${updatedCount}곳 업데이트",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "지점별 보기",
            fontSize = 12.sp,
            color = Color.Gray,
        )
    }
}

@Composable
private fun DessertGrid(
    items: List<Dessert>,
    onCardClick: (Dessert) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { dessert ->
            DessertCard(dessert = dessert, onClick = { onCardClick(dessert) })
        }
    }
}

@Composable
private fun DessertCard(dessert: Dessert, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column {
            Box {
                AsyncImage(
                    model = dessert.imageUrl,
                    contentDescription = dessert.storeName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
                if (dessert.soldOutCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .align(Alignment.TopEnd),
                    ) {
                        Text(
                            text = "${dessert.soldOutCount}품절",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = dessert.storeName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dessert.areaLabel,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "오늘 ${dessert.stockToday}개",
                    fontSize = 13.sp,
                    color = SoldOutRed,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AddFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(BrandOrange)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "재고 등록",
            tint = Color.White,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun DessertFeedScreenPreview() {
    Project3tempTheme {
        DessertFeedScreen()
    }
}

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project3temp.data.Categories
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.Districts
import com.example.project3temp.data.ListingTypes
import com.example.project3temp.data.network.CafeListItem
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.formatHours
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.DistrictChipBg
import com.example.project3temp.ui.theme.SoldOutRed

private const val FIXED_CITY = "서울"

private sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Content(val items: List<CafeListItem>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DessertFeedScreen(
    onAddClick: () -> Unit = {},
    onCardClick: (Int, String) -> Unit = { _, _ -> },
) {
    var selectedCategoryId by remember { mutableStateOf(Categories.ALL_ID) }
    var selectedDistrict by remember { mutableStateOf(Districts.ALL_LABEL) }
    var selectedListingType by remember { mutableStateOf(ListingTypes.BASIC) }
    var uiState by remember { mutableStateOf<FeedUiState>(FeedUiState.Loading) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedCategoryId, selectedDistrict, selectedListingType, reloadKey) {
        uiState = FeedUiState.Loading
        uiState = runCatching {
            NetworkModule.cafeApi.listCafes(
                categoryId = Categories.byId(selectedCategoryId).typeId,
                addressCity = FIXED_CITY,
                addressDistrict = selectedDistrict,
                listingType = selectedListingType,
            )
        }.fold(
            onSuccess = { FeedUiState.Content(it) },
            onFailure = { FeedUiState.Error(it.toUserMessage()) },
        )
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
                selectedDistrict = selectedDistrict,
                onDistrictChange = { selectedDistrict = it },
                selectedListingType = selectedListingType,
                onListingTypeChange = { selectedListingType = it },
            )
            FeedHeader(
                count = (uiState as? FeedUiState.Content)?.items?.size ?: 0,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    FeedUiState.Loading -> LoadingView()
                    is FeedUiState.Error -> ErrorView(
                        message = state.message,
                        onRetry = { reloadKey++ },
                    )
                    is FeedUiState.Content -> if (state.items.isEmpty()) {
                        EmptyView()
                    } else {
                        DessertGrid(items = state.items, onCardClick = onCardClick)
                    }
                }
            }
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
    selectedDistrict: String,
    onDistrictChange: (String) -> Unit,
    selectedListingType: String,
    onListingTypeChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StaticChip(label = FIXED_CITY)
        FilterChipDropdown(
            label = selectedDistrict,
            options = Districts.seoul,
            onSelect = onDistrictChange,
        )
        Spacer(Modifier.weight(1f))
        FilterChipDropdown(
            label = ListingTypes.labelOf(selectedListingType),
            options = ListingTypes.options.map { it.label },
            onSelect = { selected ->
                ListingTypes.options.firstOrNull { it.label == selected }
                    ?.let { onListingTypeChange(it.value) }
            },
        )
    }
}

@Composable
private fun StaticChip(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DistrictChipBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
private fun FeedHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "오늘 ${count}곳 업데이트",
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
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandOrange)
    }
}

@Composable
private fun EmptyView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "조건에 맞는 카페가 없어요",
            fontSize = 14.sp,
            color = Color.Gray,
        )
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "불러오기 실패", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("다시 시도", color = Color.White)
        }
    }
}

@Composable
private fun DessertGrid(
    items: List<CafeListItem>,
    onCardClick: (Int, String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { cafe ->
            CafeCard(cafe = cafe, onClick = { onCardClick(cafe.id, cafe.cafeName) })
        }
    }
}

@Composable
private fun CafeCard(cafe: CafeListItem, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column {
            AsyncImage(
                model = cafe.imageUrl,
                contentDescription = cafe.cafeName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF1ECE6)),
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = cafe.cafeName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                formatHours(cafe.open, cafe.close)?.let { hours ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = hours,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(cafe.addressCity, cafe.addressDistrict)
                        .filter { it.isNotBlank() }
                        .joinToString(" "),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "오늘 ${cafe.totalCount}개",
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

package com.example.project3temp.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project3temp.data.Categories
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.MenuItem
import com.example.project3temp.data.network.CafeMenusResponse
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.formatHours
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.SoldOutRed
import java.text.NumberFormat
import java.util.Locale

// 카페 재고 상세 정보 페이지의 3가지 상태
private sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(val cafe: CafeMenusResponse) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DessertDetailScreen(
    cafeId: Int,
    cafeName: String,
    onClose: () -> Unit,
) {
    var uiState by remember { mutableStateOf<DetailUiState>(DetailUiState.Loading) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(cafeId, reloadKey) {
        uiState = DetailUiState.Loading
        uiState = runCatching { NetworkModule.cafeApi.getMenus(cafeId) }
            .fold(
                onSuccess = { DetailUiState.Content(it) },
                onFailure = { DetailUiState.Error(it.toUserMessage()) },
            )
    }

    Scaffold(
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = {
                    val cafe = (uiState as? DetailUiState.Content)?.cafe
                    val areaLabel = cafe?.let {
                        listOfNotNull(it.addressCity, it.addressDistrict)
                            .filter { part -> part.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { null }
                    }
                    DetailTitle(
                        storeName = cafeName,
                        areaLabel = areaLabel,
                        addressDetail = cafe?.addressDetail,
                    )
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                DetailUiState.Loading -> LoadingView()
                is DetailUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { reloadKey++ },
                )
                is DetailUiState.Content -> CafeContent(cafe = state.cafe)
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandOrange)
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
        Text(
            text = "불러오기 실패",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
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
private fun CafeContent(cafe: CafeMenusResponse) {
    val menuItems: List<MenuItem> = remember(cafe) {
        cafe.menu.mapIndexed { idx, dto ->
            MenuItem(
                id = "${cafe.cafeId}-$idx",
                typeId = dto.typeId,
                itemName = dto.itemName,
                cost = dto.cost,
                stock = dto.stock,
            )
        }
    }
    val groupedByCategory = menuItems.groupBy { it.typeId }.toList()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            AsyncImage(
                model = cafe.imageUrl,
                contentDescription = cafe.cafeName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF1ECE6)),
            )
        }

        formatHours(cafe.open, cafe.close)?.let { hours ->
            item {
                Text(
                    text = hours,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        item {
            val hasDescription = !cafe.description.isNullOrBlank()
            Text(
                text = if (hasDescription) cafe.description!! else "No Description",
                fontSize = 14.sp,
                color = if (hasDescription) Color.DarkGray else Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            )
        }

        groupedByCategory.forEach { (typeId, items) ->
            val category = Categories.byTypeId(typeId)
            item(key = "header-$typeId") {
                CategoryHeader(category = category)
            }
            items(items, key = { "menu-${it.id}" }) { item ->
                MenuRow(category = category, item = item)
            }
        }

        item { Spacer(Modifier.height(28.dp)) }
    }
}

// 메인 카드 클릭 - 상세 페이지 - 타이틀 바
@Composable
private fun DetailTitle(storeName: String, areaLabel: String?, addressDetail: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandOrangeSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🍰", fontSize = 20.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text( // 카페 이름
                text = storeName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            val hasArea = !areaLabel.isNullOrBlank()
            val hasDetail = !addressDetail.isNullOrBlank()
            if (hasArea || hasDetail) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn, // 지도 핀 아이콘
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    if (hasArea) {
                        Text( // 시 + 구
                            text = areaLabel,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                        )
                    }
                    if (hasArea && hasDetail) Spacer(Modifier.width(6.dp))
                    if (hasDetail) {
                        Text( // 상세 주소
                            text = addressDetail,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }
    }
}

// 메뉴 대분류
@Composable
private fun CategoryHeader(category: DessertCategory) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text = "■", fontSize = 12.sp, color = Color.DarkGray)
        Spacer(Modifier.width(6.dp))
        Text(text = category.label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

// 하나의 메뉴 - 행
@Composable
private fun MenuRow(category: DessertCategory, item: MenuItem) {
    val isSoldOut = item.stock == 0 // 솔드 아웃 표시
    val nameColor = if (isSoldOut) Color.Gray else Color.Black
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BrandOrangeSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category.emoji, fontSize = 16.sp) // 대분류에 해당하는 이모지
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = item.itemName, // 메뉴 이름
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = nameColor,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatCost(item.cost), // 가격 (nullable)
            fontSize = 14.sp,
            color = if (item.cost == null) Color.Gray else Color.DarkGray,
            textAlign = TextAlign.End,
            modifier = Modifier.width(96.dp),
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            StockLabel(stock = item.stock) // 재고 (nullable)
        }
    }
}

// 재고 표시 방법
@Composable
private fun StockLabel(stock: Int?) {
    when {
        stock == null -> Unit
        stock == 0 -> Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(SoldOutRed.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) { // stock 이 0이면
            Text(
                text = "SOLD OUT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SoldOutRed,
            )
        } // stock 이 0이 아니면
        else -> Text(
            text = "${stock}개",
            fontSize = 14.sp,
            color = Color.DarkGray,
        )
    }
}

// 가격 포맷 - 1000단위로 , 표기
private fun formatCost(cost: Int?): String =
    if (cost == null) "정보 없음"
    else "₩" + NumberFormat.getNumberInstance(Locale.KOREA).format(cost)

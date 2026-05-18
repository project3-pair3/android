package com.example.project3temp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project3temp.data.network.CafeListItem
import com.example.project3temp.data.network.formatHours

@Composable
internal fun DessertGrid(
    items: List<CafeListItem>, // 전체 카드
    onCardClick: (Int, String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 가로 2개
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

// 하나의 개별 카드
@Composable
private fun CafeCard(cafe: CafeListItem, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
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
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = cafe.cafeName, // 카페 이름
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                // 시 - 구 위치 표현
                val area = listOfNotNull(cafe.addressCity, cafe.addressDistrict)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                if (area.isNotBlank()) {
                    Text(
                        text = area,
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA),
                        maxLines = 1,
                        lineHeight = 16.sp,
                    )
                }
                // 운영 시간
                formatHours(cafe.open, cafe.close)?.let { hours ->
                    Text(
                        text = hours,
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA),
                        maxLines = 1,
                        lineHeight = 16.sp,
                    )
                }
                // 사장님 한줄평
                cafe.mention?.takeIf { it.isNotBlank() }?.let { mention ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = mention,
                        fontSize = 11.sp,
                        color = Color(0xFF555555),
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        lineHeight = 15.sp,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

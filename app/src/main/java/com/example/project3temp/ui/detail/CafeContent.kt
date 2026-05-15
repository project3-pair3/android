package com.example.project3temp.ui.detail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.MenuItem
import com.example.project3temp.data.network.CafeMenusResponse
import com.example.project3temp.data.network.formatHours
import com.example.project3temp.ui.theme.BrandOrangeSoft

@Composable
internal fun CafeContent(cafe: CafeMenusResponse) {
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

    // typeId (메뉴 대분류 아이디) 로 그룹 바이
    // 형태 : List<Pair<Int, List<MenuItem>>>
    /* ex)

    listOf(
      1 to listOf(
          MenuItem("1-0", 1, "오레오",   3000, 20),
          MenuItem("1-1", 1, "군옥수수", 3500, 25),
      ),
      2 to listOf(
          MenuItem("1-2", 2, "솔티카라멜", 2500, 10),
          MenuItem("1-3", 2, "무화과",     3000, 15),
      ),
  )   */


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

        // HH:MM ~ HH:MM
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

        cafe.createdAt?.let { createdAt ->
            item {
                Text(
                    text = "등록일: $createdAt",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                )
            }
        }

        cafe.updatedAt?.let { updatedAt ->
            item {
                Text(
                    text = "수정일: $updatedAt",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                )
            }
        }

        // description 없으면 "No Description" 출력
        item {
            val hasDescription = !cafe.description.isNullOrBlank()
            Text(
                text = if (hasDescription) cafe.description!! else "No Description",
                fontSize = 14.sp,
                color = if (hasDescription) Color.DarkGray else Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            )
        }

        // group by 아이템들(메뉴) 순회, 출력
        groupedByCategory.forEach { (typeId, items) ->
            val category = DessertCategory.byTypeId(typeId)
            if (category == null) {
                // 백엔드가 알 수 없는 typeId를 내려준 경우 - 에러 메시지로 노출
                item(key = "unknown-$typeId") {
                    UnknownCategoryBlock(typeId = typeId, count = items.size)
                }
            } else {
                item(key = "header-$typeId") {
                    CategoryHeader(category = category)
                }
                items(items, key = { "menu-${it.id}" }) { item ->
                    MenuRow(category = category, item = item)
                }
            }
        }

        item { Spacer(Modifier.height(28.dp)) }
    }
}

// 상세 페이지 - 타이틀 바
@Composable
internal fun DetailTitle(storeName: String, areaLabel: String?, addressDetail: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandOrangeSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🍰", fontSize = 20.sp) // 기본 이모지
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

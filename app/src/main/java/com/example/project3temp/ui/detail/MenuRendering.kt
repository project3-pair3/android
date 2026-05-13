package com.example.project3temp.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.MenuItem
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.SoldOutRed
import java.text.NumberFormat
import java.util.Locale

// 알 수 없는 typeId가 백엔드에서 내려왔을 때 보여주는 에러 블록
@Composable
internal fun UnknownCategoryBlock(typeId: Int, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text = "⚠️", fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = "알 수 없는 카테고리 (typeId=$typeId · 메뉴 ${count}개 숨김)",
            fontSize = 13.sp,
            color = SoldOutRed,
            fontWeight = FontWeight.Medium,
        )
    }
}

// 메뉴 대분류
@Composable
internal fun CategoryHeader(category: DessertCategory) {
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
internal fun MenuRow(category: DessertCategory, item: MenuItem) {
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
        stock == null -> Text( // null이면 "정보 없음"
            text = "정보 없음",
            fontSize = 12.sp,
            color = Color.Gray,
        )
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

// 가격 포맷 - 1000단위로 , 표기. null이면 "정보 없음" (0은 "₩0"으로 표시)
private fun formatCost(cost: Int?): String =
    if (cost == null) "정보 없음"
    else "₩" + NumberFormat.getNumberInstance(Locale.KOREA).format(cost)

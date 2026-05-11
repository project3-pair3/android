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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project3temp.data.Categories
import com.example.project3temp.data.Dessert
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.DummyData
import com.example.project3temp.data.MenuItem
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.Project3tempTheme
import com.example.project3temp.ui.theme.SoldOutRed
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DessertDetailScreen(
    dessert: Dessert,
    onClose: () -> Unit,
) {
    val groupedByCategory = dessert.menuItems
        .groupBy { it.categoryId }
        .toList()

    Scaffold(
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = { DetailTitle(dessert = dessert) },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item {
                AsyncImage(
                    model = dessert.imageUrl,
                    contentDescription = dessert.storeName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color(0xFFF1ECE6)),
                )
            }

            if (!dessert.intro.isNullOrBlank()) {
                item {
                    Text(
                        text = dessert.intro,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 16.dp,
                        ),
                    )
                }
            } else {
                item { Spacer(Modifier.height(8.dp)) }
            }

            groupedByCategory.forEach { (categoryId, items) ->
                val category = Categories.byId(categoryId)
                item(key = "header-$categoryId") {
                    CategoryHeader(category = category)
                }
                items(items, key = { "menu-${it.id}" }) { item ->
                    MenuRow(category = category, item = item)
                }
            }

            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun DetailTitle(dessert: Dessert) {
    val category = Categories.byId(dessert.categoryId)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BrandOrangeSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = category.emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = dessert.storeName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = dessert.areaLabel,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: DessertCategory) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = "■",
            fontSize = 12.sp,
            color = Color.DarkGray,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = category.label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MenuRow(category: DessertCategory, item: MenuItem) {
    val isSoldOut = item.stock == 0
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
            Text(text = category.emoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = item.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = nameColor,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatPrice(item.price),
            fontSize = 14.sp,
            color = if (item.price == null) Color.Gray else Color.DarkGray,
            textAlign = TextAlign.End,
            modifier = Modifier.width(96.dp),
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            StockLabel(stock = item.stock)
        }
    }
}

@Composable
private fun StockLabel(stock: Int?) {
    when {
        stock == null -> Unit
        stock == 0 -> Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(SoldOutRed.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = "SOLD OUT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SoldOutRed,
            )
        }
        else -> Text(
            text = "${stock}개",
            fontSize = 14.sp,
            color = Color.DarkGray,
        )
    }
}

private fun formatPrice(price: Int?): String =
    if (price == null) "정보 없음"
    else "₩" + NumberFormat.getNumberInstance(Locale.KOREA).format(price)

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun DessertDetailScreenPreview() {
    Project3tempTheme {
        DessertDetailScreen(
            dessert = DummyData.desserts.first(),
            onClose = {},
        )
    }
}

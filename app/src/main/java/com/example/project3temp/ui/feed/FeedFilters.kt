package com.example.project3temp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.Categories
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.Districts
import com.example.project3temp.data.ListingTypes
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.DistrictChipBg

// 메뉴 대분류 필터링 - "전체" 칩 + enum 카테고리 칩들
@Composable
internal fun CategoryRow(
    selected: DessertCategory?, // null = 전체
    onSelect: (DessertCategory?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "all") { // "전체" 의사 카테고리
            CategoryItem(
                emoji = Categories.ALL_EMOJI,
                label = Categories.ALL_LABEL,
                selected = selected == null,
                onClick = { onSelect(null) },
            )
        }
        items(DessertCategory.entries, key = { it.name }) { category ->
            CategoryItem(
                emoji = category.emoji,
                label = category.label,
                selected = category == selected,
                onClick = { onSelect(category) },
            )
        }
    }
}

// 메뉴 대분류 필터링 1개 아이콘
@Composable
private fun CategoryItem(
    emoji: String,
    label: String,
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
            Text(text = emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = labelWeight,
            color = labelColor,
        )
    }
}

// "시" / "구" 필터링과 정렬 방식 선택
@Composable
internal fun FilterRow(
    selectedCity: String,
    onCityChange: (String) -> Unit,
    selectedDistrict: String,
    onDistrictChange: (String) -> Unit,
    selectedListingType: String,
    onListingTypeChange: (String) -> Unit,
    cityOptions: List<String>,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChipDropdown( // "시" 필터링 (현재 서울시만)
            label = selectedCity,
            options = cityOptions,
            onSelect = onCityChange,
        )
        FilterChipDropdown( // "구" 필터링
            label = selectedDistrict,
            options = Districts.seoul,
            onSelect = onDistrictChange,
        )
        Spacer(Modifier.weight(1f))
        FilterChipDropdown( // 정렬 방식
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
private fun FilterChipDropdown(
    label: String,
    options: List<String>, // 서초구 ~ 서대문구 또는 ListingType 2개
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

package com.example.project3temp.ui.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.DistrictChipBg

// 카페 메뉴 기본 정보
internal data class MenuItemDraft(
    val id: String,
    val category: DessertCategory, // 대분류
    val name: String = "",
    val price: String = "", // nullable
    val stock: String = "", // nullable
)

// 하나의 메뉴 아이템 카드
@Composable
internal fun MenuItemCard(
    index: Int,
    draft: MenuItemDraft,
    canDelete: Boolean,
    onChange: (MenuItemDraft) -> Unit,
    onDelete: () -> Unit,
) {
    val category = draft.category

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFEEE6DD),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandOrangeSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$index",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange,
                    )
                }
                Spacer(Modifier.width(8.dp))
                CategoryDropdown( // 메뉴 대분류
                    selected = category,
                    onSelect = { onChange(draft.copy(category = it)) },
                )
                Spacer(Modifier.weight(1f))
                if (canDelete) { // 메뉴 종류 2개 이상 존재할 때부터 삭제 가능
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "메뉴 삭제",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandOrangeSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = category.emoji, fontSize = 18.sp) // 대분류에 대한 이모지
                }
                Spacer(Modifier.width(10.dp))
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onChange(draft.copy(name = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("메뉴 이름 *", fontSize = 13.sp, color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = brandFieldColors(),
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = draft.price, // 가격 입력
                    onValueChange = { input ->
                        onChange(draft.copy(price = input.filter { it.isDigit() }))
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("₩ 가격", fontSize = 13.sp, color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = brandFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = draft.stock, // 재고 입력
                    onValueChange = { input ->
                        onChange(draft.copy(stock = input.filter { it.isDigit() }))
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("재고", fontSize = 13.sp, color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = brandFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        }
    }
}

@Composable
private fun CategoryDropdown(
    selected: DessertCategory,
    onSelect: (DessertCategory) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DistrictChipBg)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(text = selected.emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = selected.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
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
            DessertCategory.entries.forEach { cat ->
                DropdownMenuItem(
                    text = { Text("${cat.emoji}  ${cat.label}") },
                    onClick = {
                        onSelect(cat)
                        expanded = false
                    },
                )
            }
        }
    }
}

// 메뉴 아이템 추가 버튼
@Composable
internal fun AddMenuButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                border = BorderStroke(1.dp, BrandOrange),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = BrandOrange,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "메뉴 추가",
                color = BrandOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

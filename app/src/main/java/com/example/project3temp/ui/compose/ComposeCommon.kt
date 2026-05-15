package com.example.project3temp.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import coil.compose.AsyncImage
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.network.CreateCafeRequest
import com.example.project3temp.data.network.MenuItemDto
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft

// 카페 등록 화면에서 재사용하는 공통 UI 부품과 헬퍼

// 드롭다운
@Composable
internal fun ChipDropdown(
    label: String,
    options: List<String>, // 선택지
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2D9CF), RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
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
internal fun PhotoUploadBox(
    imageUri: String?,
    isLoading: Boolean = false,
    onPick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF1ECE6))
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "업로드 사진",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "사진 변경",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BrandOrangeSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "사진 1장 업로드",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "오늘 진열 사진을 올려주세요",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }

        // presigned URL 발급 중 로딩 오버레이
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            }
        }
    }
}

// 포커스 되면 오렌지 색 테두리
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun brandFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandOrange,
    unfocusedBorderColor = Color(0xFFE2D9CF),
    cursorColor = BrandOrange,
)

// 밀리초를 활용한 MenuDraftId 표현
internal fun newDraftId(): String =
    System.currentTimeMillis().toString() + "-" + (0..9999).random()

// "HH:mm" 문자열을 TimeInput으로 파싱. 형식이 이상하거나 null이면 빈 TimeInput 반환
internal fun parseTimeOrEmpty(value: String?): TimeInput {
    if (value.isNullOrBlank()) return TimeInput()
    val parts = value.split(":")
    if (parts.size < 2) return TimeInput()
    val hour = parts[0].toIntOrNull()
    val minute = parts[1].toIntOrNull()
    if (hour == null || minute == null) return TimeInput()
    return TimeInput(hour = hour, minute = minute)
}

// 서버 응답 MenuItemDto → 화면에서 편집하는 MenuItemDraft 로 변환
internal fun MenuItemDto.toDraft(): MenuItemDraft {
    val category = DessertCategory.byTypeId(typeId) ?: DessertCategory.entries.first()
    return MenuItemDraft(
        id = newDraftId(),
        category = category,
        name = itemName,
        // 백엔드에서 0이나 null로 오면 입력란을 빈 상태로 보여줌
        price = cost?.takeIf { it > 0 }?.toString() ?: "",
        stock = stock?.takeIf { it > 0 }?.toString() ?: "",
    )
}

// 카페 정보 업로드용 request
internal fun buildCreateCafeRequest(
    cafeName: String,
    addressCity: String,
    addressDistrict: String,
    addressDetail: String,
    intro: String,
    mention: String,
    openTime: String,
    closeTime: String,
    imageUrl: String,
    drafts: List<MenuItemDraft>,
): CreateCafeRequest = CreateCafeRequest(
    cafeName = cafeName.trim(),
    addressCity = addressCity,
    addressDistrict = addressDistrict,
    addressDetail = addressDetail.trim(),
    description = intro.trim().ifBlank { null },
    mention = mention.trim().ifBlank { null },
    open = openTime,
    close = closeTime,
    imageUrl = imageUrl,
    menu = drafts.map { d ->
        MenuItemDto(
            itemName = d.name.trim(),
            typeId = d.category.typeId,
            // 빈칸이면 null로 전송 (0과 구분 - 0은 솔드아웃, null은 미입력)
            cost = d.price.toIntOrNull(),
            stock = d.stock.toIntOrNull(),
        )
    },
)

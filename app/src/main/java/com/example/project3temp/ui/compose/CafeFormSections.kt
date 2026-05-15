package com.example.project3temp.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.Districts
import com.example.project3temp.ui.theme.BrandOrange

// 카페 등록 화면의 입력 섹션들

// addressCity 드롭다운에 쓸 실제 시 목록 (기본 서울시)
internal const val CITY_SEOUL = "서울시"

// addressDistrict 드롭다운에 쓸 실제 구 목록 ("전체" 제외)
private val districtOptions: List<String> = Districts.seoul.filter { it != Districts.ALL_LABEL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CafeNameSection(
    value: String,
    onChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionLabel(label = "카페 이름", required = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("예) DAILY DESSERT CO.", fontSize = 13.sp, color = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = brandFieldColors(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddressSection(
    city: String, // 시
    onCityChange: (String) -> Unit,
    district: String?, // 구
    onDistrictChange: (String) -> Unit,
    detail: String, // 세부
    onDetailChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionLabel(label = "주소", required = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ChipDropdown( // 현재 서울시만 선택 가능
                label = city,
                options = listOf(CITY_SEOUL),
                onSelect = onCityChange,
                modifier = Modifier.weight(1f),
            )
            ChipDropdown(
                label = district ?: "구 선택",
                options = districtOptions, // "구" 리스트
                onSelect = onDistrictChange,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = detail,
            onValueChange = onDetailChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("상세 주소 (예: 강남대로 889)", fontSize = 13.sp, color = Color.Gray)
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = brandFieldColors(),
        )
    }
}

@Composable
internal fun HoursSection(
    open: TimeInput,
    onOpenChange: (TimeInput) -> Unit,
    close: TimeInput,
    onCloseChange: (TimeInput) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionLabel(label = "영업 시간", required = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeField( // open
                label = "오픈",
                value = open,
                onChange = onOpenChange,
                modifier = Modifier.weight(1f),
            )
            TimeField( // close
                label = "마감",
                value = close,
                onChange = onCloseChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// description 입력
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IntroSection(
    value: String,
    onChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionLabel(label = "한 줄 소개", required = false)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "예) 11시 2차 베이킹 완료. 소금빵 통밀버전 한정 6개",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = brandFieldColors(),
            maxLines = 3,
        )
    }
}

// mention 입력 (피드에 표시할 짧은 문구) - nullable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MentionSection(
    value: String,
    onChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionLabel(label = "오늘의 한마디", required = false)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "피드에 표시할 짧은 문구 넣어주세요!",
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = brandFieldColors(),
        )
    }
}

@Composable
internal fun MenuItemsSection(
    items: List<MenuItemDraft>, // MenuItemDraft : 메뉴 기본 정보
    onUpdate: (Int, MenuItemDraft) -> Unit,
    onDelete: (Int) -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "메뉴 항목",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "· 메뉴 이름 필수",
                fontSize = 12.sp,
                color = Color.Gray,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${items.size}개", // 메뉴의 종류 수
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }

        Spacer(Modifier.height(12.dp))

        items.forEachIndexed { idx, draft ->
            MenuItemCard(
                index = idx + 1,
                draft = draft,
                canDelete = items.size > 1,
                onChange = { onUpdate(idx, it) },
                onDelete = { onDelete(idx) },
            )
            Spacer(Modifier.height(12.dp))
        }

        AddMenuButton(onClick = onAdd)
    }
}

// 입력 세션 이름과 필수 여부
@Composable
private fun SectionLabel(label: String, required: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (required) "· 필수" else "· 선택",
            fontSize = 12.sp,
            color = if (required) BrandOrange else Color.Gray,
        )
    }
}

package com.example.project3temp.ui.compose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.project3temp.data.Categories
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.Districts
import com.example.project3temp.data.network.CreateCafeRequest
import com.example.project3temp.data.network.MenuItemDto
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.DistrictChipBg
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

// addressCity 드롭다운에 쓸 실제 시 목록 (기본 서울)
private const val CITY_SEOUL = "서울"

// addressDistrict 드롭다운에 쓸 실제 구 목록 ("전체" 제외)
private val districtOptions: List<String> = Districts.seoul.filter { it != Districts.ALL_LABEL }

// 카페 메뉴 기본 정보
private data class MenuItemDraft(
    val id: String,
    val categoryId: String, // 대분류
    val name: String = "",
    val price: String = "", // nullable
    val stock: String = "", // nullable
)

// 날짜와 시간을 따로 보관 (DatePicker → TimePicker 순서로 입력 받음)
// todo 시간만 입력받도록 수정
private data class DateTimeInput(
    val dateMillis: Long? = null, // DatePickerState가 주는 UTC 자정 millis
    val hour: Int? = null,
    val minute: Int? = null,
) {
    fun isComplete(): Boolean =
        dateMillis != null && hour != null && minute != null

    fun toIsoString(): String? {
        if (!isComplete()) return null
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = dateMillis!!
        }
        return "%04d-%02d-%02dT%02d:%02d:00".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
        )
    }

    fun displayString(): String {
        if (!isComplete()) return ""
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = dateMillis!!
        }
        return "%04d-%02d-%02d %02d:%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    onClose: () -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    // 입력 상태
    var cafeName by remember { mutableStateOf("") }
    var addressCity by remember { mutableStateOf(CITY_SEOUL) }
    var addressDistrict by remember { mutableStateOf<String?>(null) }
    var addressDetail by remember { mutableStateOf("") }
    var openInput by remember { mutableStateOf(DateTimeInput()) }
    var closeInput by remember { mutableStateOf(DateTimeInput()) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var intro by remember { mutableStateOf("") }
    var menuItems by remember {
        mutableStateOf(
            listOf(
                MenuItemDraft(
                    id = newDraftId(),
                    categoryId = Categories.selectable.first().id,
                ),
            ),
        )
    }
    var isSubmitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) imageUri = uri.toString()
    }

    // not null 필드 입력 여부 확인
    val canSubmit = !isSubmitting &&
        cafeName.isNotBlank() &&
        addressDistrict != null &&
        addressDetail.isNotBlank() &&
        openInput.isComplete() &&
        closeInput.isComplete() &&
        imageUri != null &&
        menuItems.isNotEmpty() && menuItems.all { it.name.isNotBlank() }

    Scaffold(
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "오늘의 재고 등록",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // 카페 입력 - 필수
            CafeNameSection(value = cafeName, onChange = { cafeName = it })

            HorizontalDivider(color = Color(0xFFEEE6DD))

            // 사진 업로드 1장 - 필수
            PhotoUploadBox(
                imageUri = imageUri,
                onPick = {
                    launcher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                        ),
                    )
                },
            )

            HorizontalDivider(color = Color(0xFFEEE6DD))

            // 주소 - 시, 구, 상세 주소 - 필수
            AddressSection(
                city = addressCity,
                onCityChange = { addressCity = it },
                district = addressDistrict,
                onDistrictChange = { addressDistrict = it },
                detail = addressDetail,
                onDetailChange = { addressDetail = it },
            )

            HorizontalDivider(color = Color(0xFFEEE6DD))

            // 영업 시간 - 필수
            // todo 시간만 받도록 추 후 수정
            HoursSection(
                open = openInput,
                onOpenChange = { openInput = it },
                close = closeInput,
                onCloseChange = { closeInput = it },
            )

            HorizontalDivider(color = Color(0xFFEEE6DD))

            // 한 줄 소개 (description) - nullable
            IntroSection(value = intro, onChange = { intro = it })

            HorizontalDivider(color = Color(0xFFEEE6DD))

            // 메뉴 정보 기입 - 적어도 1개 입력
            MenuItemsSection(
                items = menuItems,
                onUpdate = { idx, updated ->
                    menuItems = menuItems.toMutableList().apply { set(idx, updated) }
                },
                onDelete = { idx ->
                    if (menuItems.size > 1) {
                        menuItems = menuItems.toMutableList().apply { removeAt(idx) }
                    }
                },
                onAdd = {
                    menuItems = menuItems + MenuItemDraft(
                        id = newDraftId(),
                        categoryId = Categories.selectable.first().id,
                    )
                },
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val request = buildCreateCafeRequest(
                        cafeName = cafeName,
                        addressCity = addressCity,
                        addressDistrict = addressDistrict ?: return@Button,
                        addressDetail = addressDetail,
                        intro = intro,
                        openIso = openInput.toIsoString() ?: return@Button,
                        closeIso = closeInput.toIsoString() ?: return@Button,
                        imageUrl = imageUri ?: return@Button,
                        drafts = menuItems,
                    )
                    isSubmitting = true
                    scope.launch {
                        val result = runCatching {
                            NetworkModule.cafeApi.createCafeWithMenu(request) // /cafes POST API
                        }
                        isSubmitting = false
                        result.fold(
                            onSuccess = { onSubmitSuccess() },
                            onFailure = { e ->
                                snackbarHostState.showSnackbar(
                                    "업로드 실패: ${e.toUserMessage()}",
                                )
                            },
                        )
                    }
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandOrange,
                    disabledContainerColor = Color(0xFFD8D8D8),
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(
                        text = "카페 정보 업로드",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

// ────────── 입력 섹션들 ──────────

@Composable
private fun CafeNameSection(
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

@Composable
private fun AddressSection(
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
            ChipDropdown( // 현재 서울만 선택 가능
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
private fun HoursSection(
    open: DateTimeInput,
    onOpenChange: (DateTimeInput) -> Unit,
    close: DateTimeInput,
    onCloseChange: (DateTimeInput) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionLabel(label = "영업 시간", required = true)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DateTimeField( // open
                label = "오픈",
                value = open,
                onChange = onOpenChange,
                modifier = Modifier.weight(1f),
            )
            DateTimeField( // close
                label = "마감",
                value = close,
                onChange = onCloseChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// description 입력
@Composable
private fun IntroSection(
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

@Composable
private fun MenuItemsSection(
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

// ────────── 공통 컴포넌트 ──────────

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

// 드롭다운
@Composable
private fun ChipDropdown(
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

// 날짜+시간 입력 필드. 탭하면 DatePicker → TimePicker 순서로 다이얼로그가 뜸
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeField(
    label: String,
    value: DateTimeInput,
    onChange: (DateTimeInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    // DatePicker에서 고른 날짜를 TimePicker로 넘기기 위한 임시 보관
    var pendingDateMillis by remember { mutableStateOf<Long?>(null) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2D9CF), RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { showDatePicker = true }
            .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.Gray,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value.displayString().ifBlank { "YYYY-MM-DD HH:mm" },
                fontSize = 14.sp,
                color = if (value.isComplete()) Color.Black else Color.Gray,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value.dateMillis ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDateMillis = state.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("다음") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = value.hour ?: 9,
            initialMinute = value.minute ?: 0,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("$label 시간 선택") },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = state)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange(
                        DateTimeInput(
                            dateMillis = pendingDateMillis ?: value.dateMillis,
                            hour = state.hour,
                            minute = state.minute,
                        ),
                    )
                    showTimePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("취소") }
            },
        )
    }
}

// ────────── 기존 컴포넌트 (사진/메뉴/카테고리 드롭다운) ──────────

@Composable
private fun PhotoUploadBox(
    imageUri: String?,
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
    }
}

// 하나의 메뉴 아이템 카드
@Composable
private fun MenuItemCard(
    index: Int,
    draft: MenuItemDraft,
    canDelete: Boolean,
    onChange: (MenuItemDraft) -> Unit,
    onDelete: () -> Unit,
) {
    val category = Categories.byId(draft.categoryId)

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
                    onSelect = { onChange(draft.copy(categoryId = it.id)) },
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
            Categories.selectable.forEach { cat ->
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
private fun AddMenuButton(onClick: () -> Unit) {
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

// 포커스 되면 오렌지 색 테두리
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun brandFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandOrange,
    unfocusedBorderColor = Color(0xFFE2D9CF),
    cursorColor = BrandOrange,
)

// 밀리초를 활용한 MenuDraftId 표현
private fun newDraftId(): String =
    System.currentTimeMillis().toString() + "-" + (0..9999).random()

// 카페 정보 업로드용 request
private fun buildCreateCafeRequest(
    cafeName: String,
    addressCity: String,
    addressDistrict: String,
    addressDetail: String,
    intro: String,
    openIso: String,
    closeIso: String,
    imageUrl: String,
    drafts: List<MenuItemDraft>,
): CreateCafeRequest = CreateCafeRequest(
    cafeName = cafeName.trim(),
    addressCity = addressCity,
    addressDistrict = addressDistrict,
    addressDetail = addressDetail.trim(),
    description = intro.trim().ifBlank { null },
    open = openIso,
    close = closeIso,
    imageUrl = imageUrl,
    menu = drafts.map { d ->
        MenuItemDto(
            itemName = d.name.trim(),
            typeId = Categories.byId(d.categoryId).typeId,
            cost = d.price.toIntOrNull() ?: 0,
            stock = d.stock.toIntOrNull() ?: 0,
        )
    },
)

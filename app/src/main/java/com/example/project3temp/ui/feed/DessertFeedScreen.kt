package com.example.project3temp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.project3temp.data.Districts
import com.example.project3temp.data.ListingTypes
import com.example.project3temp.data.UserSession
import com.example.project3temp.data.network.CafeListItem
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.formatHours
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.DistrictChipBg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 현재 "시"는 "서울시"만 사용 - 추 후 확장 가능
private const val DEFAULT_CITY = "서울시"
private val cityOptions: List<String> = listOf(DEFAULT_CITY)

// 피드 화면이 가질 수 있는 상태 3가지 - 로딩, 성공, 에러
private sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Content(val items: List<CafeListItem>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DessertFeedScreen(
    snackbarMessage: String? = null, // 카페 정보 추가 성공 후 메시지
    onSnackbarShown: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onCardClick: (Int, String) -> Unit = { _, _ -> },
    onLoginClick: () -> Unit = {}, // 우측 상단 "로그인" 버튼
) {
    var selectedCategory by remember { mutableStateOf<DessertCategory?>(null) } // 메뉴 대분류 필터링 (null = 전체)
    var selectedCity by remember { mutableStateOf(DEFAULT_CITY) } // 지역 "시" 필터링
    var selectedDistrict by remember { mutableStateOf(Districts.ALL_LABEL) } // 지역 "구" 필터링
    var selectedListingType by remember { mutableStateOf(ListingTypes.BASIC) } // 정렬 기준
    var uiState by remember { mutableStateOf<FeedUiState>(FeedUiState.Loading) } // 로딩중
    var reloadKey by remember { mutableIntStateOf(0) } // 에러 화면에서 LaunchEffect 재실행을 위한 더미 데이터
    val snackbarHostState = remember { SnackbarHostState() } // 카페 정보 등록 성공 메시지

    // 외부에서 메시지가 들어오면 3초간 보여주고 자동으로 숨김
    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        val job = launch {
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Indefinite)
        }
        try {
            delay(3000) // snackbar 3초
        } finally {
            job.cancel()
            onSnackbarShown()
        }
    }

    LaunchedEffect(selectedCategory, selectedCity, selectedDistrict, selectedListingType, reloadKey) {
        uiState = FeedUiState.Loading
        uiState = runCatching {
            NetworkModule.cafeApi.listCafes( // /cafes GET
                categoryId = selectedCategory?.typeId ?: Categories.ALL_TYPE_ID,
                addressCity = selectedCity,
                addressDistrict = selectedDistrict,
                listingType = selectedListingType,
            )
        }.fold(
            onSuccess = { FeedUiState.Content(it) },
            onFailure = { FeedUiState.Error(it.toUserMessage()) },
        )
    }

    Scaffold(
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "오늘의 디저트",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = "DESSERTS TODAY",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                actions = { // 로그인 상태에 따라 [로그인] 또는 [닉네임 + 로그아웃]
                    val user = UserSession.current
                    if (user == null) {
                        TextButton(onClick = onLoginClick) {
                            Text(
                                text = "로그인",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandOrange,
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp, top = 4.dp),
                        ) {
                            Text(
                                text = user.nickname,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandOrange,
                            )
                            TextButton(
                                onClick = { UserSession.logout() },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    text = "로그아웃",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground),
            )
        },
        floatingActionButton = {
            // 로그인한 사용자에게만 재고 등록 버튼 노출
            if (UserSession.current != null) {
                AddFab(onClick = onAddClick)
            }
        },
        bottomBar = {
            // 비로그인 상태에서 카페 등록 안내 배너
            if (UserSession.current == null) {
                LoginPromptBanner(onLoginClick = onLoginClick)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CategoryRow( // 대분류 카테고리 필터링
                selected = selectedCategory, // 선택중인 카테고리 (null = 전체)
                onSelect = { selectedCategory = it }, // 상태 저장
            )
            FilterRow( // 지역 필터링과 정렬 // todo 서울시 이외의 "시" 추가
                selectedCity = selectedCity, // "시" 필터링
                onCityChange = { selectedCity = it },
                selectedDistrict = selectedDistrict, // "구" 필터링
                onDistrictChange = { selectedDistrict = it },
                selectedListingType = selectedListingType, // 정렬 방식
                onListingTypeChange = { selectedListingType = it },
            )
            FeedHeader( // 오늘 업데이트 된 카페 수
                count = (uiState as? FeedUiState.Content)?.items?.size ?: 0,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    FeedUiState.Loading -> LoadingView()
                    is FeedUiState.Error -> ErrorView(
                        message = state.message,
                        onRetry = { reloadKey++ }, // 다시 시도 버튼 -> 화면 재 렌더링을 위한 변수
                    )
                    is FeedUiState.Content -> if (state.items.isEmpty()) {
                        EmptyView()
                    } else {
                        DessertGrid(items = state.items, onCardClick = onCardClick)
                    }
                }
            }
        }
    }
}

// 메뉴 대분류 필터링 - "전체" 칩 + enum 카테고리 칩들
@Composable
private fun CategoryRow(
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
private fun FilterRow(
    selectedCity: String,
    onCityChange: (String) -> Unit,
    selectedDistrict: String,
    onDistrictChange: (String) -> Unit,
    selectedListingType: String,
    onListingTypeChange: (String) -> Unit,
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


// 업데이트 가게 수
@Composable
private fun FeedHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "오늘 ${count}곳 업데이트",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// 로딩 화면
@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandOrange)
    }
}

@Composable
private fun EmptyView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "조건에 맞는 카페가 없어요",
            fontSize = 14.sp,
            color = Color.Gray,
        )
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
        Text(text = "불러오기 실패", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Text("다시 시도", color = Color.White) // 누르면 LaunchedEffect 실행
        }
    }
}

@Composable
private fun DessertGrid(
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

// 재고 등록 버튼 (로그인 상태일 때만 호출됨)
@Composable
private fun AddFab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(BrandOrange)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "재고 등록",
            tint = Color.White,
        )
    }
}

// 비로그인 상태 하단 안내 배너
@Composable
private fun LoginPromptBanner(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF8F3)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandOrangeSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column {
                    Text(
                        text = "카페 사장님이신가요?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                    )
                    Text(
                        text = "로그인하고 카페 정보를 등록해보세요",
                        fontSize = 12.sp,
                        color = Color(0xFF888888),
                    )
                }
            }
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "로그인",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
        }
    }
}

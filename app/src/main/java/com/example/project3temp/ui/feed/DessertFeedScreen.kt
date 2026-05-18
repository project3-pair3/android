package com.example.project3temp.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.Categories
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.Districts
import com.example.project3temp.data.ListingTypes
import com.example.project3temp.data.UserSession
import com.example.project3temp.data.network.CafeListItem
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
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
                cityOptions = cityOptions,
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

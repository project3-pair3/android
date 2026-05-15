package com.example.project3temp.ui.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.network.CafeInfoResponse
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 화면 상태 3가지: 로딩 / 에러 / 정보 수신 완료
private sealed interface InfoLoadState {
    data object Loading : InfoLoadState
    data class Error(val message: String) : InfoLoadState
    data class Loaded(val info: CafeInfoResponse) : InfoLoadState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    userId: Int, // user의 pk
    onClose: () -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    var loadState by remember { mutableStateOf<InfoLoadState>(InfoLoadState.Loading) }
    var reloadKey by remember { mutableIntStateOf(0) }

    // 진입 시 + 재시도 시 카페 정보 조회
    LaunchedEffect(userId, reloadKey) {
        loadState = InfoLoadState.Loading
        loadState = runCatching {
            NetworkModule.cafeApi.getCafeInfo(userId)
        }.fold(
            onSuccess = { InfoLoadState.Loaded(it) },
            onFailure = { InfoLoadState.Error(it.toUserMessage()) },
        )
    }

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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = loadState) {
                InfoLoadState.Loading -> LoadingView()
                is InfoLoadState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { reloadKey++ },
                )
                is InfoLoadState.Loaded -> CafeForm(
                    userId = userId,
                    info = state.info,
                    onSubmitSuccess = onSubmitSuccess,
                )
            }
        }
    }
}

// 정보 조회가 끝난 후 보여주는 본 폼. statusCode에 따라 prefill / 버튼 텍스트가 달라짐
@Composable
private fun CafeForm(
    userId: Int,
    info: CafeInfoResponse,
    onSubmitSuccess: () -> Unit,
) {
    val context = LocalContext.current

    // 응답을 폼 초기값으로 변환
    // statusCode == 1 이면 응답 필드가 어차피 빈 값이라 빈 폼이 됨
    var cafeName by remember { mutableStateOf(info.cafeName) }
    var addressCity by remember {
        mutableStateOf(info.addressCity.ifBlank { CITY_SEOUL })
    }
    var addressDistrict by remember {
        mutableStateOf<String?>(info.addressDistrict.ifBlank { null })
    }
    var addressDetail by remember { mutableStateOf(info.addressDetail) }
    var openInput by remember { mutableStateOf(parseTimeOrEmpty(info.open)) }
    var closeInput by remember { mutableStateOf(parseTimeOrEmpty(info.close)) }
    // 화면에 표시할 이미지 (로컬 URI 문자열 또는 기존 서버 URL)
    var displayImageUri by remember { mutableStateOf<String?>(info.imageUrl?.ifBlank { null }) }
    // 새로 선택한 로컬 이미지 URI (null이면 이미지 변경 없음)
    var pendingLocalUri by remember { mutableStateOf<Uri?>(null) }
    // GET /s3/presigned-url 응답으로 받은 presigned URL과 최종 image URL
    var presignedUrl by remember { mutableStateOf<String?>(null) }
    var s3ImageUrl by remember { mutableStateOf<String?>(null) }
    // 이미지 선택 후 presigned URL 발급 + S3 업로드가 완료될 때까지 true
    var isUploadingImage by remember { mutableStateOf(false) }
    var intro by remember { mutableStateOf(info.description ?: "") }
    var mention by remember { mutableStateOf(info.mention ?: "") }
    var menuItems by remember {
        mutableStateOf(
            if (info.menu.isEmpty()) {
                // 빈 폼에서도 메뉴는 최소 1개 카드가 보이도록 기본 카드 1개
                listOf(
                    MenuItemDraft(
                        id = newDraftId(),
                        category = DessertCategory.entries.first(),
                    ),
                )
            } else {
                info.menu.map { it.toDraft() }
            },
        )
    }

    var isSubmitting by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            displayImageUri = uri.toString()
            pendingLocalUri = uri
            presignedUrl = null
            s3ImageUrl = null
            isUploadingImage = true
            // 이미지 선택 직후 presigned URL 발급 → 바로 S3에 업로드
            scope.launch {
                val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                    null, null, null,
                )?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                } ?: uri.lastPathSegment ?: "image.jpg"

                // 1. presigned URL + imageUrl(temp 포함) 발급
                val presignedRes = runCatching {
                    NetworkModule.s3Api.getPresignedUrl(fileName, contentType)
                }.getOrElse {
                    snackbarHostState.showSnackbar("이미지 URL 준비 실패: ${it.toUserMessage()}")
                    isUploadingImage = false
                    return@launch
                }

                // 2. 이미지 바이트 읽기
                val imageBytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                if (imageBytes == null) {
                    snackbarHostState.showSnackbar("이미지를 읽을 수 없습니다.")
                    isUploadingImage = false
                    return@launch
                }

                // 3. S3에 바로 업로드 (temp 경로)
                runCatching {
                    NetworkModule.uploadToS3(presignedRes.presignedUrl, imageBytes, contentType)
                }.fold(
                    onSuccess = {
                        presignedUrl = presignedRes.presignedUrl
                        s3ImageUrl = presignedRes.imageUrl
                    },
                    onFailure = {
                        snackbarHostState.showSnackbar("이미지 업로드 실패: ${it.toUserMessage()}")
                        displayImageUri = null
                        pendingLocalUri = null
                    },
                )
                isUploadingImage = false
            }
        }
    }

    // not null 필드 입력 여부 확인. 이미지 업로드 중에는 제출 불가
    val canSubmit = !isSubmitting &&
        !isUploadingImage &&
        cafeName.isNotBlank() &&
        addressDistrict != null &&
        addressDetail.isNotBlank() &&
        openInput.isComplete() &&
        closeInput.isComplete() &&
        displayImageUri != null &&
        menuItems.isNotEmpty() && menuItems.all { it.name.isNotBlank() }

    // statusCode == 3 이면 "수정", 1/2 이면 "업로드"
    val submitButtonText = if (info.statusCode == 3) "카페 정보 수정" else "카페 정보 업로드"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // 카페 입력 - 필수
            CafeNameSection(value = cafeName, onChange = { cafeName = it })

            HorizontalDivider(color = Color(0xFFEEE6DD))

            // 사진 업로드 1장 - 필수
            PhotoUploadBox(
                imageUri = displayImageUri,
                isLoading = isUploadingImage,
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

            // 영업 시간 (시:분) - 필수
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

            // 오늘의 한마디 (mention) - nullable, 피드 카드에 표시
            MentionSection(value = mention, onChange = { mention = it })

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
                        category = DessertCategory.entries.first(),
                    )
                },
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val district = addressDistrict ?: return@Button
                    val openTime = openInput.toApiString() ?: return@Button
                    val closeTime = closeInput.toApiString() ?: return@Button
                    // 새 이미지가 있으면 이미지 선택 시점에 이미 S3(temp 경로)에 업로드된 s3ImageUrl 사용
                    val cafeImageUrl = if (pendingLocalUri != null) (s3ImageUrl ?: "") else (displayImageUri ?: "")
                    val request = buildCreateCafeRequest(
                        cafeName = cafeName,
                        addressCity = addressCity,
                        addressDistrict = district,
                        addressDetail = addressDetail,
                        intro = intro,
                        mention = mention,
                        openTime = openTime,
                        closeTime = closeTime,
                        imageUrl = cafeImageUrl,
                        drafts = menuItems,
                    )
                    isSubmitting = true
                    scope.launch {
                        // PUT /cafes/update/{userId} - 1/2/3 모두 동일하게 호출
                        // imageUrl에 temp 경로가 포함되어 있으면 백엔드가 confirm으로 복사 후 DB 업데이트
                        val cafeResult = runCatching {
                            NetworkModule.cafeApi.updateCafe(userId, request)
                        }
                        if (cafeResult.isFailure) {
                            isSubmitting = false
                            snackbarHostState.showSnackbar(
                                "업로드 실패: ${cafeResult.exceptionOrNull()?.toUserMessage()}",
                            )
                            return@launch
                        }

                        isSubmitting = false
                        onSubmitSuccess()
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
                        text = submitButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }

        // 화면 하단 스낵바
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrandOrange)
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
            Text("다시 시도", color = Color.White)
        }
    }
}

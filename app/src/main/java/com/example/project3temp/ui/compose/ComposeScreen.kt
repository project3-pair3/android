package com.example.project3temp.ui.compose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.DessertCategory
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import kotlinx.coroutines.launch

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
    var openInput by remember { mutableStateOf(TimeInput()) }
    var closeInput by remember { mutableStateOf(TimeInput()) }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var intro by remember { mutableStateOf("") }
    var menuItems by remember {
        mutableStateOf(
            listOf(
                MenuItemDraft(
                    id = newDraftId(),
                    category = DessertCategory.entries.first(),
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
                    val request = buildCreateCafeRequest(
                        cafeName = cafeName,
                        addressCity = addressCity,
                        addressDistrict = addressDistrict ?: return@Button,
                        addressDetail = addressDetail,
                        intro = intro,
                        openTime = openInput.toApiString() ?: return@Button,
                        closeTime = closeInput.toApiString() ?: return@Button,
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

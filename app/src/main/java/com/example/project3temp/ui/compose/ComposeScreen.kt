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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.project3temp.data.network.CreateMenusRequest
import com.example.project3temp.data.network.MenuItemDto
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange
import com.example.project3temp.ui.theme.BrandOrangeSoft
import com.example.project3temp.ui.theme.DistrictChipBg
import kotlinx.coroutines.launch

private data class MenuItemDraft(
    val id: String,
    val categoryId: String,
    val name: String = "",
    val price: String = "",
    val stock: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    cafeId: Int,
    onClose: () -> Unit,
) {
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

    val canSubmit = !isSubmitting &&
        imageUri != null && menuItems.isNotEmpty() && menuItems.all { it.name.isNotBlank() }

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

            IntroSection(value = intro, onChange = { intro = it })

            HorizontalDivider(color = Color(0xFFEEE6DD))

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
                    val image = imageUri ?: return@Button
                    val request = buildCreateMenusRequest(
                        imageUrl = image,
                        intro = intro,
                        drafts = menuItems,
                    )
                    isSubmitting = true
                    scope.launch {
                        val result = runCatching {
                            NetworkModule.cafeApi.createMenus(cafeId, request)
                        }
                        isSubmitting = false
                        result.fold(
                            onSuccess = { onClose() },
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "한 줄 소개",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "· 선택",
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
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
    items: List<MenuItemDraft>,
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
                text = "${items.size}개",
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
                CategoryDropdown(
                    selected = category,
                    onSelect = { onChange(draft.copy(categoryId = it.id)) },
                )
                Spacer(Modifier.weight(1f))
                if (canDelete) {
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
                    Text(text = category.emoji, fontSize = 18.sp)
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
                    value = draft.price,
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
                    value = draft.stock,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun brandFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandOrange,
    unfocusedBorderColor = Color(0xFFE2D9CF),
    cursorColor = BrandOrange,
)

private fun newDraftId(): String =
    System.currentTimeMillis().toString() + "-" + (0..9999).random()

private fun buildCreateMenusRequest(
    imageUrl: String,
    intro: String,
    drafts: List<MenuItemDraft>,
): CreateMenusRequest = CreateMenusRequest(
    imageUrl = imageUrl,
    description = intro.trim(),
    menu = drafts.map { d ->
        MenuItemDto(
            itemName = d.name.trim(),
            typeId = Categories.byId(d.categoryId).typeId,
            cost = d.price.toIntOrNull() ?: 0,
            stock = d.stock.toIntOrNull() ?: 0,
        )
    },
)


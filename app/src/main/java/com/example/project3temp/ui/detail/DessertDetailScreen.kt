package com.example.project3temp.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.data.network.CafeMenusResponse
import com.example.project3temp.data.network.NetworkModule
import com.example.project3temp.data.network.toUserMessage
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange

// 카페 재고 상세 정보 페이지의 3가지 상태
private sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(val cafe: CafeMenusResponse) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DessertDetailScreen(
    cafeId: Int,
    cafeName: String,
    onClose: () -> Unit,
) {
    var uiState by remember { mutableStateOf<DetailUiState>(DetailUiState.Loading) }
    var reloadKey by remember { mutableIntStateOf(0) } // error handling - LaunchedEffect

    LaunchedEffect(cafeId, reloadKey) {
        uiState = DetailUiState.Loading
        uiState = runCatching { NetworkModule.cafeApi.getMenus(cafeId) } // /cafes/{id}/menus GET API
            .fold(
                onSuccess = { DetailUiState.Content(it) },
                onFailure = { DetailUiState.Error(it.toUserMessage()) },
            )
    }

    Scaffold(
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = {
                    val cafe = (uiState as? DetailUiState.Content)?.cafe // 카페 이름
                    val areaLabel = cafe?.let {
                        listOfNotNull(it.addressCity, it.addressDistrict)
                            .filter { part -> part.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { null }
                    }
                    DetailTitle( // "시", "구" 제외 상세 주소
                        storeName = cafeName,
                        areaLabel = areaLabel,
                        addressDetail = cafe?.addressDetail,
                    )
                },
                actions = { // 닫기
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
            when (val state = uiState) {
                DetailUiState.Loading -> LoadingView()
                is DetailUiState.Error -> ErrorView(
                    message = state.message,
                    onRetry = { reloadKey++ }, // "다시 시도" 버튼 누르고 LaunchedEffect 실행을 위한 더미 데이터
                )
                is DetailUiState.Content -> CafeContent(cafe = state.cafe)
            }
        }
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
        Text(
            text = "불러오기 실패",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
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
            Text("다시 시도", color = Color.White) // 리컴포저블 발생
        }
    }
}

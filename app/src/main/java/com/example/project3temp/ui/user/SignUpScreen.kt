package com.example.project3temp.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project3temp.ui.theme.BrandBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
) {
    // TODO: 아이디/비밀번호/비밀번호 확인 등 입력 상태
    // TODO: 가입 진행중 상태 (isSubmitting)
    // TODO: 검증 결과 (이미 존재하는 아이디, 비밀번호 불일치 등) 표시용 상태

    Scaffold(
        containerColor = BrandBackground,
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBackground),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            // TODO: 아이디 입력 OutlinedTextField
            // TODO: 비밀번호 입력 OutlinedTextField
            // TODO: 비밀번호 확인 OutlinedTextField
            // TODO: (선택) 그 외 필수 항목들
            // TODO: 회원가입 버튼 - 성공시 onSignUpSuccess() 호출 (MainActivity가 로그인 화면으로 돌려보냄)
        }
    }
}

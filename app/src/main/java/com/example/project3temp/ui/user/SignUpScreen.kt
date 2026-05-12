package com.example.project3temp.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project3temp.ui.theme.BrandBackground
import com.example.project3temp.ui.theme.BrandOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
) {
    // 아이디/비밀번호/비밀번호 확인 등 입력 상태
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

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
            // 아이디 입력
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("아이디") },
                placeholder = { Text("아이디를 입력하세요") },
                singleLine = true,
            )

            // 비밀번호 입력
            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("비밀번호")},
                placeholder = { Text("비밀번호를 입력하세요")},
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,

                )

            // 비밀번호 확인
            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = {passwordConfirm = it},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("비밀번호 확인")},
                placeholder = { Text("비밀번호를 입력하세요")},
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                )

            // 닉네임
            OutlinedTextField(
                value = nickname,
                onValueChange = {nickname = it},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("닉네임")},
                placeholder = { Text("닉네임을 입력하세요")},
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )

            Spacer(Modifier.height(8.dp))

            // 회원가입 버튼 - 세 필드가 모두 비어있지 않고 비밀번호가 확인과 일치할 때 활성화
            Button(
                onClick = {
                    // TODO: 입력 검증 + 회원가입 API 호출. 성공시 onSignUpSuccess() 호출
                    onSignUpSuccess()
                },
                enabled = userId.isNotBlank() && password.isNotBlank() && passwordConfirm.isNotBlank() && nickname.isNotBlank()
                        && (password == passwordConfirm),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandOrange,
                    disabledContainerColor = Color(0xFFD8D8D8),
                ),
            ) {
                Text(
                    text = "회원가입",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }


            // TODO: (선택) 그 외 필수 항목들
            // TODO: 회원가입 버튼 - 성공시 onSignUpSuccess() 호출 (MainActivity가 로그인 화면으로 돌려보냄)
        }
    }
}

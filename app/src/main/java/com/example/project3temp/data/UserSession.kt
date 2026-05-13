package com.example.project3temp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// 로그인된 사용자의 정보 (앱 프로세스 메모리 안에서만 유지)
data class CurrentUser(
    val id: Int,        // pk
    val userId: String,
    val nickname: String,
)

// Compose가 자동 재구성되도록 mutableStateOf 사용
object UserSession {
    var current: CurrentUser? by mutableStateOf(null)
        private set

    fun login(user: CurrentUser) {
        current = user
    }

    fun logout() {
        current = null
    }
}

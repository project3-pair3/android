package com.example.project3temp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.project3temp.data.UserSession
import com.example.project3temp.ui.compose.ComposeScreen
import com.example.project3temp.ui.detail.DessertDetailScreen
import com.example.project3temp.ui.feed.DessertFeedScreen
import com.example.project3temp.ui.theme.Project3tempTheme
import com.example.project3temp.ui.user.LoginScreen
import com.example.project3temp.ui.user.SignUpScreen

private sealed interface Screen {
    data object Feed : Screen
    data class Compose(val userId: Int) : Screen // userId = 로그인한 user의 pk
    data class Detail(val cafeId: Int, val cafeName: String) : Screen
    data object Login : Screen
    data object SignUp : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project3tempTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Feed) }
                // Feed에 띄울 스낵바 메시지 (예: 카페 등록 성공 후)
                var feedMessage by remember { mutableStateOf<String?>(null) }
                // Login에 띄울 스낵바 메시지 (예: 회원가입 성공 후)
                var loginMessage by remember { mutableStateOf<String?>(null) }

                when (val current = screen) {
                    is Screen.Feed -> DessertFeedScreen(
                        snackbarMessage = feedMessage, // 카페 작성 완료 후 메시지
                        onSnackbarShown = { feedMessage = null },
                        onAddClick = {
                            // FAB는 로그인 상태일 때만 보이므로 current는 not null이지만, 안전하게 한 번 더 체크
                            val user = UserSession.current ?: return@DessertFeedScreen
                            screen = Screen.Compose(user.id)
                        },
                        onCardClick = { cafeId, cafeName -> // 상세 페이지
                            screen = Screen.Detail(cafeId, cafeName)
                        },
                        onLoginClick = { screen = Screen.Login }, // 로그인 스크린
                    )

                    is Screen.Compose -> ComposeScreen(
                        userId = current.userId,
                        onClose = { screen = Screen.Feed },
                        onSubmitSuccess = {
                            feedMessage = "카페가 등록되었어요"
                            screen = Screen.Feed
                        },
                    )

                    is Screen.Detail -> DessertDetailScreen(
                        cafeId = current.cafeId,
                        cafeName = current.cafeName,
                        onClose = { screen = Screen.Feed },
                    )

                    Screen.Login -> LoginScreen(
                        snackbarMessage = loginMessage,
                        onSnackbarShown = { loginMessage = null },
                        onClose = { screen = Screen.Feed }, // 메인 페이지로
                        onSignUpClick = { screen = Screen.SignUp }, // 회원가입 페이지로
                        onLoginSuccess = { screen = Screen.Feed }, // TODO: 로그인 성공 후 동작 사용자가 결정
                    )

                    Screen.SignUp -> SignUpScreen(
                        onBack = { screen = Screen.Login }, // 로그인 페이지로
                        onSignUpSuccess = {
                            loginMessage = "회원가입이 완료되었습니다"
                            screen = Screen.Login
                        },
                    )
                }
            }
        }
    }
}

package com.example.project3temp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.project3temp.ui.compose.ComposeScreen
import com.example.project3temp.ui.detail.DessertDetailScreen
import com.example.project3temp.ui.feed.DessertFeedScreen
import com.example.project3temp.ui.theme.Project3tempTheme

private sealed interface Screen {
    data object Feed : Screen
    data object Compose : Screen
    data class Detail(val cafeId: Int, val cafeName: String) : Screen
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

                when (val current = screen) {
                    is Screen.Feed -> DessertFeedScreen(
                        snackbarMessage = feedMessage,
                        onSnackbarShown = { feedMessage = null },
                        onAddClick = { screen = Screen.Compose },
                        onCardClick = { cafeId, cafeName ->
                            screen = Screen.Detail(cafeId, cafeName)
                        },
                    )

                    is Screen.Compose -> ComposeScreen(
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
                }
            }
        }
    }
}

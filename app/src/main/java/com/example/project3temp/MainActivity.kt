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

private const val MY_CAFE_ID = 1

private sealed interface Screen {
    data object Feed : Screen
    data object Compose : Screen
    data class Detail(val cafeId: Int) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project3tempTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.Feed) }

                when (val current = screen) {
                    is Screen.Feed -> DessertFeedScreen(
                        onAddClick = { screen = Screen.Compose },
                        onCardClick = { cafeId -> screen = Screen.Detail(cafeId) },
                    )

                    is Screen.Compose -> ComposeScreen(
                        cafeId = MY_CAFE_ID,
                        onClose = { screen = Screen.Feed },
                    )

                    is Screen.Detail -> DessertDetailScreen(
                        cafeId = current.cafeId,
                        onClose = { screen = Screen.Feed },
                    )
                }
            }
        }
    }
}

package com.example.project3temp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.project3temp.data.Dessert
import com.example.project3temp.data.DessertRepo
import com.example.project3temp.ui.compose.ComposeScreen
import com.example.project3temp.ui.detail.DessertDetailScreen
import com.example.project3temp.ui.feed.DessertFeedScreen
import com.example.project3temp.ui.theme.Project3tempTheme

private sealed interface Screen {
    data object Feed : Screen
    data object Compose : Screen
    data class Detail(val dessert: Dessert) : Screen
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
                        onCardClick = { screen = Screen.Detail(it) },
                    )

                    is Screen.Compose -> ComposeScreen(
                        onClose = { screen = Screen.Feed },
                        onSubmit = { dessert ->
                            DessertRepo.addAtTop(dessert)
                            screen = Screen.Feed
                        },
                    )

                    is Screen.Detail -> DessertDetailScreen(
                        dessert = current.dessert,
                        onClose = { screen = Screen.Feed },
                    )
                }
            }
        }
    }
}

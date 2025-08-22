package com.example.minesweeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.minesweeper.ui.theme.GameScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 建立 NavController 用於管理導航
            val navController = rememberNavController()

            // NavHost 負責畫面導航，startDestination 為開始畫面
            NavHost(navController = navController, startDestination = "start") {

                // 開始畫面
                composable("start") {
                    StartScreen(
                        onStartGame = { length, width, mines ->
                            // 按下 Start Game 時導航到遊戲畫面，並傳入參數
                            navController.navigate("game/$length/$width/$mines")
                        }
                    )
                }

                // 遊戲畫面
                composable(
                    route = "game/{length}/{width}/{mines}",
                    arguments = listOf(
                        navArgument("length") { type = NavType.IntType },
                        navArgument("width") { type = NavType.IntType },
                        navArgument("mines") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    // 從導航參數取得長、寬、地雷數量
                    val length = backStackEntry.arguments?.getInt("length") ?: 0
                    val width = backStackEntry.arguments?.getInt("width") ?: 0
                    val mines = backStackEntry.arguments?.getInt("mines") ?: 0

                    // 顯示遊戲畫面，onBack 返回開始畫面
                    GameScreen(
                        rows = length,
                        cols = width,
                        mines = mines,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

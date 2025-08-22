package com.example.minesweeper

import android.R.attr.fontWeight
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

// 開始畫面 Composable
@Composable
fun StartScreen(onStartGame: (length: Int, width: Int, mines: Int) -> Unit) {
    //最大限制數量，因為手機沒辦法容納這麼多的格子
    val maxCols = 12
    val maxRows = 16
    val maxMines = 99


    Box(modifier = Modifier.fillMaxSize()) {

        // 背景圖片
        Image(
            painter = painterResource(id = R.drawable.main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(55.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 標題
            Text(
                text = "MineSweeper\n踩地雷小遊戲",
                fontSize = 45.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(100.dp))

            // 三個輸入框：長、寬、地雷數
            var lengthInput by remember { mutableStateOf("") }
            var widthInput by remember { mutableStateOf("") }
            var minesInput by remember { mutableStateOf("") }

            TextField(
                value = widthInput,
                onValueChange = { widthInput = it },
                label = { Text("介面寬度(Max: 20)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))

            TextField(
                value = lengthInput,
                onValueChange = { lengthInput = it },
                label = { Text("介面長度(Max: 20)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))

            TextField(
                value = minesInput,
                onValueChange = { minesInput = it },
                label = { Text("地雷數量(Max: 99)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(110.dp))

            // Start Game 按鈕
            Button(
                onClick = {
                    val length = lengthInput.toIntOrNull() ?: 0
                    val width = widthInput.toIntOrNull() ?: 0
                    val mines = minesInput.toIntOrNull() ?: 0
                    onStartGame(length, width, mines) // 將數據傳給 MainActivity
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 100.dp)
            ) {
                Text(
                    "Start Game!",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

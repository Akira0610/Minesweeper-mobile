package com.example.minesweeper

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
    val maxCols = 30
    val maxRows = 30
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
                .wrapContentHeight()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 標題
            Text(
                text = "MineSweeper\n踩地雷小遊戲",
                fontSize = 45.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(80.dp))

            // 三個輸入框：長、寬、地雷數
            var lengthInput by remember { mutableStateOf("") }
            var widthInput by remember { mutableStateOf("") }
            var minesInput by remember { mutableStateOf("") }

            TextField(
                value = widthInput,
                onValueChange = { input ->
                    // 只允許數字輸入並限制最大值
                    val value = input.filter { it.isDigit() }.take(2)
                    if (value.isEmpty() || value.toIntOrNull()?.let { it <= maxCols } == true) {
                        widthInput = value
                    }
                },
                label = { Text("介面寬度(Max: $maxCols)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = lengthInput,
                onValueChange = { input ->
                    // 只允許數字輸入並限制最大值
                    val value = input.filter { it.isDigit() }.take(2)
                    if (value.isEmpty() || value.toIntOrNull()?.let { it <= maxRows } == true) {
                        lengthInput = value
                    }
                },
                label = { Text("介面長度(Max: $maxRows)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = minesInput,
                onValueChange = { input ->
                    // 只允許數字輸入並限制最大值
                    val value = input.filter { it.isDigit() }.take(2)
                    if (value.isEmpty() || value.toIntOrNull()?.let { it <= maxMines } == true) {
                        minesInput = value
                    }
                },
                label = { Text("地雷數量(Max: $maxMines)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(60.dp))

            // Start Game 按鈕
            Button(
                onClick = {
                    val length = lengthInput.toIntOrNull() ?: 0
                    val width = widthInput.toIntOrNull() ?: 0
                    val mines = minesInput.toIntOrNull() ?: 0

                    // 驗證輸入值
                    if (length > 0 && width > 0 && mines > 0 &&
                        length <= maxRows && width <= maxCols &&
                        mines <= maxMines && mines < length * width) {
                        onStartGame(length, width, mines)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                enabled = run {
                    val length = lengthInput.toIntOrNull() ?: 0
                    val width = widthInput.toIntOrNull() ?: 0
                    val mines = minesInput.toIntOrNull() ?: 0
                    length > 0 && width > 0 && mines > 0 &&
                            length <= maxRows && width <= maxCols &&
                            mines <= maxMines && mines < length * width
                }
            ) {
                Text(
                    "Start Game!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// GameScreen.kt - 改善版本
package com.example.minesweeper.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minesweeper.model.Cell
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Composable
fun GameScreen(
    rows: Int,
    cols: Int,
    mines: Int,
    onBack: () -> Unit
) {
    // 獲取螢幕配置信息
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // 動態計算 cell 大小和初始縮放
    val availableWidth = screenWidth - 32 // 減去 padding
    val availableHeight = screenHeight - 280 // 減去狀態欄、按鈕等預留空間，調整為更精確

    val maxCellSize = 40.dp // 最大 cell 尺寸
    val minCellSize = 8.dp // 減少最小 cell 尺寸，讓大網格有更多空間

    // 根據網格大小計算適合的 cell 尺寸
    val calculatedCellSize = min(
        maxCellSize.value,
        max(
            minCellSize.value,
            min(
                availableWidth / cols.toFloat(),
                availableHeight / rows.toFloat()
            )
        )
    ).dp

    // 動態計算初始縮放比例 - 讓30x30網格更好地填滿螢幕
    val calculatedInitialScale = when {
        rows <= 10 && cols <= 10 -> 1.0f
        rows <= 15 && cols <= 15 -> 0.9f
        rows <= 20 && cols <= 20 -> 0.8f
        rows <= 25 && cols <= 25 -> 0.7f
        rows <= 30 && cols <= 30 -> 0.85f // 30x30 特別優化
        else -> 0.6f
    }

    // 遊戲狀態
    var grid by remember { mutableStateOf(generateEmptyGrid(rows, cols)) }
    var gameOver by remember { mutableStateOf(false) }
    var victory by remember { mutableStateOf(false) }
    var gameStarted by remember { mutableStateOf(false) }
    var firstClick by remember { mutableStateOf(true) }
    var minesRemaining by remember { mutableIntStateOf(mines) }

    // 計時器狀態
    var gameTime by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // 縮放和移動狀態 - 使用動態計算的初始縮放
    var scale by remember { mutableFloatStateOf(calculatedInitialScale) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current

    // 計時器
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            gameTime++
        }
    }

    // 重新開始遊戲函數
    fun resetGame() {
        grid = generateEmptyGrid(rows, cols)
        gameOver = false
        victory = false
        gameStarted = false
        firstClick = true
        minesRemaining = mines
        gameTime = 0
        isTimerRunning = false
        scale = calculatedInitialScale
        offsetX = 0f
        offsetY = 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // 頂部狀態欄
        TopStatusBar(
            minesRemaining = minesRemaining,
            gameTime = gameTime,
            onReset = { resetGame() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 遊戲狀態顯示
        GameStatusDisplay(gameOver, victory)

        // 遊戲區域 (可縮放和移動) - 修復拖曳衝突問題
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            // 透明的手勢覆蓋層，處理縮放和拖拽
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.3f, 4.0f)
                            offsetX += pan.x
                            offsetY += pan.y

                            // 計算網格的實際尺寸
                            val gridWidthPx = cols * calculatedCellSize.toPx() * scale
                            val gridHeightPx = rows * calculatedCellSize.toPx() * scale

                            // 更寬鬆的邊界限制
                            val boundaryBuffer = 50f
                            val maxOffsetX = max(boundaryBuffer, (gridWidthPx - size.width) / 2 + boundaryBuffer)
                            val maxOffsetY = max(boundaryBuffer, (gridHeightPx - size.height) / 2 + boundaryBuffer)

                            offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                        }
                    }
            )

            // 遊戲網格容器
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // 遊戲網格 - 使用動態計算的 cell 尺寸
                Column(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    grid.forEach { row ->
                        Row {
                            row.forEach { cell ->
                                CellView(
                                    cell = cell,
                                    cellSize = calculatedCellSize
                                ) {
                                    if (!gameOver && !victory) {
                                        handleCellClick(
                                            cell = cell,
                                            grid = grid,
                                            rows = rows,
                                            cols = cols,
                                            mines = mines,
                                            firstClick = firstClick,
                                            onGridUpdate = { newGrid -> grid = newGrid },
                                            onGameStateUpdate = { newGameOver, newVictory, newMinesRemaining ->
                                                gameOver = newGameOver
                                                victory = newVictory
                                                minesRemaining = newMinesRemaining
                                            },
                                            onFirstClickHandled = {
                                                firstClick = false
                                                gameStarted = true
                                                isTimerRunning = true
                                            },
                                            onTimerStop = { isTimerRunning = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 控制說明和返回按鈕
        Column {
            // 控制提示 (針對大網格) - 簡化提示以節省空間
            if (rows > 15 || cols > 15) {
                Text(
                    text = "💡 雙指縮放和拖拽操作",
                    modifier = Modifier.padding(bottom = 4.dp),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // 返回按鈕
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp), // 稍微減少按鈕高度以節省空間
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "🏠 返回主選單",
                        fontSize = 16.sp, // 稍微減少字體大小
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TopStatusBar(
    minesRemaining: Int,
    gameTime: Int,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 地雷剩餘數量
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "💣",
                    fontSize = 20.sp
                )
                Text(
                    text = minesRemaining.toString().padStart(3, '0'),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            // 重新開始按鈕
            Button(
                onClick = onReset,
                modifier = Modifier.size(60.dp)
            ) {
                Text(
                    text = "🔄",
                    fontSize = 24.sp
                )
            }

            // 計時器
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "⏱️",
                    fontSize = 20.sp
                )
                Text(
                    text = formatTime(gameTime),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )
            }
        }
    }
}

@Composable
fun GameStatusDisplay(gameOver: Boolean, victory: Boolean) {
    if (gameOver || victory) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (victory) Color.Green.copy(alpha = 0.2f)
                else Color.Red.copy(alpha = 0.2f)
            )
        ) {
            Text(
                text = if (victory) "🎉 恭喜獲勝！ 🎉" else "💥 遊戲結束 💥",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (victory) Color.Green else Color.Red,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CellView(cell: Cell, cellSize: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    // 根據 cell 大小動態調整字體大小 - 針對大網格優化
    val fontSize = when {
        cellSize >= 30.dp -> 14.sp
        cellSize >= 20.dp -> 12.sp
        cellSize >= 15.dp -> 10.sp
        cellSize >= 10.dp -> 8.sp
        else -> 6.sp
    }

    val numberFontSize = when {
        cellSize >= 30.dp -> 12.sp
        cellSize >= 20.dp -> 10.sp
        cellSize >= 15.dp -> 8.sp
        cellSize >= 10.dp -> 6.sp
        else -> 5.sp
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(0.2.dp)
            .background(
                color = when {
                    cell.isRevealed && cell.isMine -> Color.Red
                    cell.isRevealed -> Color.White
                    cell.isFlagged -> Color.Blue
                    else -> Color.Gray
                },
                shape = RoundedCornerShape(1.dp)
            )
            .pointerInput(cell) { // 使用 pointerInput 而不是 clickable 以避免手勢衝突
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { },
                    onDrag = { _, _ -> }
                )
            }
            .pointerInput(cell) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isFlagged -> Text("🚩", fontSize = fontSize)
            cell.isRevealed && cell.isMine -> Text("💣", fontSize = fontSize)
            cell.isRevealed && cell.adjacentMines > 0 -> Text(
                text = cell.adjacentMines.toString(),
                fontSize = numberFontSize,
                fontWeight = FontWeight.Bold,
                color = getNumberColor(cell.adjacentMines)
            )
        }
    }
}

// 其他函數保持不變...
fun handleCellClick(
    cell: Cell,
    grid: List<List<Cell>>,
    rows: Int,
    cols: Int,
    mines: Int,
    firstClick: Boolean,
    onGridUpdate: (List<List<Cell>>) -> Unit,
    onGameStateUpdate: (Boolean, Boolean, Int) -> Unit,
    onFirstClickHandled: () -> Unit,
    onTimerStop: () -> Unit
) {
    val newGrid = if (firstClick) {
        val gridWithMines = generateGridWithMines(grid, rows, cols, mines, cell)
        onFirstClickHandled()
        gridWithMines
    } else {
        grid
    }

    val updatedGrid = revealCell(newGrid, cell)
    onGridUpdate(updatedGrid)

    val gameOver = updatedGrid.any { row -> row.any { it.isRevealed && it.isMine } }
    val victory = checkVictory(updatedGrid, mines)
    val minesRemaining = mines - updatedGrid.sumOf { row -> row.count { it.isFlagged } }

    onGameStateUpdate(gameOver, victory, minesRemaining)

    if (gameOver || victory) {
        onTimerStop()
    }
}

fun generateEmptyGrid(rows: Int, cols: Int): List<List<Cell>> {
    return List(rows) { r ->
        List(cols) { c -> Cell(r, c) }
    }.map { it.toMutableList() }
}

fun generateGridWithMines(
    grid: List<List<Cell>>,
    rows: Int,
    cols: Int,
    mines: Int,
    avoidCell: Cell
): List<List<Cell>> {
    val newGrid = grid.map { row -> row.map { it.copy() }.toMutableList() }.toMutableList()

    var placed = 0
    while (placed < mines) {
        val r = Random.nextInt(rows)
        val c = Random.nextInt(cols)
        val targetCell = newGrid[r][c]

        if (!targetCell.isMine && !isAdjacentTo(targetCell, avoidCell)) {
            targetCell.isMine = true
            placed++
        }
    }

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val cell = newGrid[r][c]
            cell.adjacentMines = getAdjacentCells(newGrid, r, c).count { it.isMine }
        }
    }

    return newGrid
}

fun isAdjacentTo(cell1: Cell, cell2: Cell): Boolean {
    return kotlin.math.abs(cell1.row - cell2.row) <= 1 &&
            kotlin.math.abs(cell1.col - cell2.col) <= 1
}

fun checkVictory(grid: List<List<Cell>>, mines: Int): Boolean {
    val totalCells = grid.size * grid[0].size
    val revealed = grid.sumOf { row -> row.count { it.isRevealed } }
    return revealed == totalCells - mines
}

fun getAdjacentCells(grid: List<List<Cell>>, row: Int, col: Int): List<Cell> {
    val rows = grid.size
    val cols = grid[0].size
    val neighbors = mutableListOf<Cell>()
    for (r in row - 1..row + 1) {
        for (c in col - 1..col + 1) {
            if (r in 0 until rows && c in 0 until cols && (r != row || c != col)) {
                neighbors.add(grid[r][c])
            }
        }
    }
    return neighbors
}

fun revealCell(grid: List<List<Cell>>, cell: Cell): List<List<Cell>> {
    val newGrid = grid.map { it.map { it.copy() }.toMutableList() }.toMutableList()

    fun reveal(r: Int, c: Int) {
        if (r !in 0 until newGrid.size || c !in 0 until newGrid[0].size) return
        val current = newGrid[r][c]
        if (current.isRevealed || current.isFlagged) return

        current.isRevealed = true
        if (current.adjacentMines == 0 && !current.isMine) {
            getAdjacentCells(newGrid, r, c).forEach {
                reveal(it.row, it.col)
            }
        }
    }

    reveal(cell.row, cell.col)
    return newGrid
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

fun getNumberColor(number: Int): Color {
    return when (number) {
        1 -> Color.Blue
        2 -> Color.Green
        3 -> Color.Red
        4 -> Color.Magenta
        5 -> Color(0xFF8B4513) // Brown
        6 -> Color.Cyan
        7 -> Color.Black
        8 -> Color.Gray
        else -> Color.Black
    }
}
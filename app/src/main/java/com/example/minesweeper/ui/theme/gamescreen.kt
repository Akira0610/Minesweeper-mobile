// GameScreen.kt (增強版)
package com.example.minesweeper.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minesweeper.model.Cell
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen(
    rows: Int,
    cols: Int,
    mines: Int,
    onBack: () -> Unit
) {
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

    // 縮放和移動狀態 - 初始縮放調整為適合螢幕
    var scale by remember { mutableFloatStateOf(0.8f) }
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
        scale = 0.8f
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

        // 遊戲區域 (可縮放和移動) - 改善佈局以防止切割
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.3f, 2.5f)
                        offsetX += pan.x
                        offsetY += pan.y

                        // 限制移動範圍，防止網格移出可見區域
                        val maxOffset = size.width * (scale - 1) / 2
                        offsetX = offsetX.coerceIn(-maxOffset, maxOffset)
                        val maxOffsetY = size.height * (scale - 1) / 2
                        offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                    }
                }
        ) {
            // 滾動容器包裹遊戲網格
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // 遊戲網格
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
                                CellView(cell = cell) {
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

        // 返回按鈕 - 改善設計，更容易使用
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "🏠 返回主選單",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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
                    text = "return",
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
fun CellView(cell: Cell, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp) // 減小格子大小以適應更多格子
            .padding(0.5.dp)
            .background(
                color = when {
                    cell.isRevealed && cell.isMine -> Color.Red
                    cell.isRevealed -> Color.White
                    cell.isFlagged -> Color.Blue
                    else -> Color.Gray
                },
                shape = RoundedCornerShape(3.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isFlagged -> Text("🚩", fontSize = 14.sp)
            cell.isRevealed && cell.isMine -> Text("💣", fontSize = 14.sp)
            cell.isRevealed && cell.adjacentMines > 0 -> Text(
                text = cell.adjacentMines.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = getNumberColor(cell.adjacentMines)
            )
        }
    }
}

// 處理格子點擊
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
        // 第一次點擊時生成地雷，避開點擊的格子
        val gridWithMines = generateGridWithMines(grid, rows, cols, mines, cell)
        onFirstClickHandled()
        gridWithMines
    } else {
        grid
    }

    val updatedGrid = revealCell(newGrid, cell)
    onGridUpdate(updatedGrid)

    // 檢查遊戲狀態
    val gameOver = updatedGrid.any { row -> row.any { it.isRevealed && it.isMine } }
    val victory = checkVictory(updatedGrid, mines)
    val minesRemaining = mines - updatedGrid.sumOf { row -> row.count { it.isFlagged } }

    onGameStateUpdate(gameOver, victory, minesRemaining)

    if (gameOver || victory) {
        onTimerStop()
    }
}

// 生成空網格
fun generateEmptyGrid(rows: Int, cols: Int): List<List<Cell>> {
    return List(rows) { r ->
        List(cols) { c -> Cell(r, c) }
    }.map { it.toMutableList() }
}

// 在第一次點擊後生成地雷
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

        // 避開第一次點擊的格子和其周圍
        if (!targetCell.isMine && !isAdjacentTo(targetCell, avoidCell)) {
            targetCell.isMine = true
            placed++
        }
    }

    // 計算每個格子周圍地雷數
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val cell = newGrid[r][c]
            cell.adjacentMines = getAdjacentCells(newGrid, r, c).count { it.isMine }
        }
    }

    return newGrid
}

// 檢查兩個格子是否相鄰
fun isAdjacentTo(cell1: Cell, cell2: Cell): Boolean {
    return kotlin.math.abs(cell1.row - cell2.row) <= 1 &&
            kotlin.math.abs(cell1.col - cell2.col) <= 1
}

// 檢查勝利條件
fun checkVictory(grid: List<List<Cell>>, mines: Int): Boolean {
    val totalCells = grid.size * grid[0].size
    val revealed = grid.sumOf { row -> row.count { it.isRevealed } }
    return revealed == totalCells - mines
}

// 取得相鄰格子
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

// 翻開格子 (遞迴展開空白格子)
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

// 格式化時間顯示
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

// 根據數字獲取顏色
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
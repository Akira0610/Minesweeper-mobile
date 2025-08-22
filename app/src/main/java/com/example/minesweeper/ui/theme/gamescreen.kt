package com.example.minesweeper.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.minesweeper.model.Cell
import java.nio.file.WatchEvent
import kotlin.random.Random

@Composable
fun GameScreen(
    rows: Int,
    cols: Int,
    mines: Int,
    onBack: () -> Unit
) {
    var grid by remember { mutableStateOf(generateGrid(rows, cols, mines)) }
    var gameOver by remember { mutableStateOf(false) }
    var victory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        if (gameOver) {
            Text("Game Over 💥", color = Color.Red)
        } else if (victory) {
            Text("You Win 🎉", color = Color.Green)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 遊戲格子
        Column {
            grid.forEach { row ->
                Row {
                    row.forEach { cell ->
                        CellView(cell = cell) {
                            if (!gameOver && !victory) {
                                val newGrid = revealCell(grid, cell)
                                grid = newGrid
                                gameOver = newGrid.any { r -> r.any { it.isRevealed && it.isMine } }
                                victory = checkVictory(newGrid, mines)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
            // 返回按鈕
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun CellView(cell: Cell, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .padding(2.dp)
            .background(
                color = if (cell.isRevealed) Color.LightGray else Color.Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when {
            cell.isRevealed && cell.isMine -> Text("💣")
            cell.isRevealed && cell.adjacentMines > 0 -> Text(cell.adjacentMines.toString())
        }
    }
}

// 檢查勝利：所有非地雷格子都被翻開
fun checkVictory(grid: List<List<Cell>>, mines: Int): Boolean {
    val totalCells = grid.size * grid[0].size
    val revealed = grid.sumOf { row -> row.count { it.isRevealed } }
    return revealed == totalCells - mines
}

// 產生格子並隨機放置地雷
fun generateGrid(rows: Int, cols: Int, mines: Int): List<List<Cell>> {
    val grid = List(rows) { r ->
        List(cols) { c -> Cell(r, c) }
    }.map { it.toMutableList() }.toMutableList()

    var placed = 0
    while (placed < mines) {
        val r = Random.nextInt(rows)
        val c = Random.nextInt(cols)
        if (!grid[r][c].isMine) {
            grid[r][c].isMine = true
            placed++
        }
    }

    // 計算每個格子周圍地雷數
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val cell = grid[r][c]
            cell.adjacentMines = getAdjacentCells(grid, r, c).count { it.isMine }
        }
    }

    return grid
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
    val newGrid = grid.map { it.map { it.copy() } } // 深拷貝
    fun reveal(r: Int, c: Int) {
        val current = newGrid[r][c]
        if (current.isRevealed) return
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

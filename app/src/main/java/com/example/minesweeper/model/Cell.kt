package com.example.minesweeper.model

// defined data structure about Cell
// Bomb yes or not, display or not, flag do or undo

data class Cell(
    val row: Int,
    val col: Int,
    var isMine: Boolean = false,
    var isRevealed: Boolean = false,
    var isFlagged: Boolean = false,
    var adjacentMines: Int = 0
)
package com.example.minesweeper.game

import com.example.minesweeper.model.Cell

/**
 * 產生一個 rows x cols 的踩地雷盤面，隨機放置 mines 顆地雷，
 * 並計算每個非地雷格的鄰近地雷數（八方向）。
 *
 * @param rows 盤面列數（高度）
 * @param cols 盤面行數（寬度）
 * @param mines 地雷數量
 * @return 二維 List，內含 Cell，代表整個盤面
 */
fun generateGrid(rows: Int, cols: Int, mines: Int): List<List<Cell>> {
    // 建立二維可變列表 grid：大小為 rows x cols
    // 其中每一格以對應的 (row, col) 初始化一個 Cell
    val grid = MutableList(rows) { r ->
        MutableList(cols) { c ->
            Cell(row = r, col = c) // 預設為非地雷，鄰近地雷數通常為 0（視 Cell 預設值而定）
        }
    }

    // placed 用來計數已成功放置的地雷數
    var placed = 0
    // 當前已放地雷數小於目標 mines 時持續嘗試
    while (placed < mines) {
        // 隨機挑一個列索引 r 與行索引 c
        val r = (0 until rows).random()
        val c = (0 until cols).random()
        // 若該格尚未是地雷才可放置，避免重複放在同一格
        if (!grid[r][c].isMine) {
            // 透過資料類別的 copy()，只更新 isMine = true，其餘欄位維持不變
            grid[r][c] = grid[r][c].copy(isMine = true)
            // 成功放置一顆地雷，計數 +1
            placed++
        }
        // 若該格已是地雷則不變更，迴圈會再嘗試新的位置
    }

    // 走訪盤面每一格，為非地雷格計算鄰近地雷數
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            // 地雷格不需計算鄰近數，略過
            if (!grid[r][c].isMine) {
                // 建立該格(r,c)的所有候選鄰居座標：
                // dr, dc 取自 {-1, 0, 1} 的笛卡兒積，共 9 個（含自己）
                val neighbors = listOf(-1, 0, 1).flatMap { dr ->
                    listOf(-1, 0, 1).map { dc -> r + dr to c + dc }
                }.filter { (nr, nc) ->
                    // 篩掉越界位置，以及本身 (r,c)
                    nr in 0 until rows && nc in 0 until cols && !(nr == r && nc == c)
                }

                // 統計鄰居中有幾格為地雷
                val count = neighbors.count { (nr, nc) -> grid[nr][nc].isMine }

                // 用 copy() 更新當前格子的 adjacentMines 值
                grid[r][c] = grid[r][c].copy(adjacentMines = count)
            }
        }
    }

    // 回傳整個盤面（以不可變 List 介面暴露）
    return grid
}

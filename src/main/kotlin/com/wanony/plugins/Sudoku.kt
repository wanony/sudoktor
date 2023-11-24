package com.wanony.plugins

import com.wanony.LayoutTemplate
import com.wanony.PageType
import com.wanony.dao.getBoard
import com.wanony.dao.getDifficulty
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.html.FlowContent
import io.ktor.http.Parameters

fun Application.sudokuPlugin() {
    routing {
        get("/game") {
            call.respondHtmlTemplate(LayoutTemplate(call, PageType.PLAY)) {
                content {
                    insertGame(call.request.queryParameters, call.request.path())
                }
            }
        }
    }
}

fun FlowContent.insertGame(params: Parameters, path: String) {

}


// Make the difficulty based on the clues
enum class Difficulty(val clues: Int) {
    VERY_HARD(17),
    HARD(28),
    MEDIUM(32),
    EASY(36),
    VERY_EASY(46)
}

class Sudoku(private val boardId: Int) {
    private var solved: Boolean = false
    private var board: Array<Array<Int?>> = getBoard(boardId)

    init {
        val difficulty = getDifficulty(boardId)
        generatePuzzle(difficulty)
    }

    private fun generatePuzzle(difficulty: Difficulty) {
        // start with a solved sudoku
        solve()

        // remove numbers and leave the correct number of clues
        val numToRemove = 81 - difficulty.clues
        for (i in 0 until numToRemove) {
            val row = (0 until 9).random()
            val col = (0 until 9).random()

            // Save the value before removing it
            val value = board[row][col]

            // Check if the puzzle still has a unique solution after removing the clue
            if (!hasUniqueSolution()) {
                // If not, revert the removal
                board[row][col] = value
            }
        }
    }

    private fun hasUniqueSolution(): Boolean {
        val originalBoard = getBoard(boardId)
        solve()
        val uniqueSolution = solved
        // Restore the original board
        board = originalBoard
        solved = false
        return uniqueSolution
    }

    private fun solve(): Boolean {

        fun isValidInsertion(row: Int, col: Int, num: Int): Boolean {
            for (i in 0..9) {
                if (board[i][col] == num ||
                    board[row][i] == num ||
                    board[row / 3 * 3 + i % 3][col / 3 * 3 + i % 3] == num) {
                    return false
                }
            }
            return true
        }

        for (row in 0..9) {
            for (col in 0..9) {
                if (board[row][col] == null) {
                    for (n in 1..9) {
                        if (isValidInsertion(row, col, n)) {
                            board[row][col] = n

                            if (solve()) {
                                return true
                            }

                            board[row][col] = null
                        }
                    }
                    return false
                }
            }

        }
        return true
    }

    private fun isValid(): Boolean {
        val rows: MutableMap<Int, MutableSet<Int?>> = mutableMapOf()
        val cols: MutableMap<Int, MutableSet<Int?>> = mutableMapOf()
        val squares: MutableMap<Pair<Int, Int>, MutableSet<Int?>> = mutableMapOf()

        for (row in 0..9) {
            for (col in 0..9) {
                // if the current board item is a number, we check
                if (board[row][col] != null) {
                    rows.getOrPut(row) { HashSet() }
                    cols.getOrPut(col) { HashSet() }
                    squares.getOrPut(row / 3 to col / 3) { HashSet() }

                    // if we have already seen this number in a row, col or square, then it's not valid
                    if (rows[row]?.contains(board[row][col]) == true ||
                        cols[col]?.contains(board[row][col]) == true ||
                        squares[row / 3 to col / 3]?.contains(board[row][col]) == true) {
                            return false
                    }

                    // otherwise we add the seen number to our hashsets
                    rows[row]?.add(board[row][col])
                    cols[col]?.add(board[row][col])
                    squares[row / 3 to col / 3]?.add(board[row][col])
                }
            }
        }
        // if we see every number and there are no collisions, we know it's valid
        return true
    }

}
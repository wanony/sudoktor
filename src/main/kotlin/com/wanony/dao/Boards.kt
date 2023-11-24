package com.wanony.dao

import com.wanony.plugins.Difficulty
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction


object Boards : IntIdTable() {
    val difficulty = enumerationByName("difficulty", 20, Difficulty::class)
}

class Board(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Board>(Boards)
    var difficulty by Boards.difficulty
}

// Define a table for Sudoku cells
object Cells : IntIdTable() {
    val boardId = reference("board_id", Boards)
    val row = integer("row")
    val col = integer("col")
    val value = integer("value").nullable()
}

class Cell(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Cell>(Cells)
    var row by Cells.row
    var col by Cells.col
    var value by Cells.value
}

// data classes for DB purposes
data class SudokuBoardData(val difficulty: Difficulty, val cells: List<SudokuCellData>)

data class SudokuCellData(val row: Int, val col: Int, val value: Int?)

fun saveSudokuBoard(board: Array<Array<Int?>>, difficulty: Difficulty): Int {
    val cellsData = mutableListOf<SudokuCellData>()
    for (row in 0..9) {
        for (col in 0..9) {
            cellsData.add(SudokuCellData(row, col, board[row][col]))
        }
    }
    val boardData = SudokuBoardData(
        difficulty = difficulty,
        cells = cellsData
    )

    return insertSudokuBoard(boardData)
}

fun insertSudokuBoard(boardData: SudokuBoardData): Int {
    return transaction {
        val boardId = Boards.insertAndGetId {
            it[difficulty] = boardData.difficulty
        }.value

        boardData.cells.forEach { cellData ->
            Cells.insert {
                it[Cells.boardId] = EntityID(boardId, Boards)
                it[Cells.row] = cellData.row
                it[Cells.col] = cellData.col
                it[Cells.value] = cellData.value
            }
        }

        boardId
    }
}


fun getDifficulty(boardId: Int): Difficulty {
    return transaction {
        Boards.select { Boards.id eq boardId }.single().let { it[Boards.difficulty] }
    }
}

fun getBoard(boardId: Int): Array<Array<Int?>> {
    return transaction {
        Cells
            .select { Cells.boardId eq boardId }
            .orderBy(Cells.row).orderBy(Cells.col)
            .map {
                it[Cells.row] to it[Cells.col] to it[Cells.value]
            }
            .groupBy { it.first }
            .mapValues { entry ->
                entry.value.map { it.second }
            }
            .map { (_, cells) ->
                cells
                    .sortedBy { it?.let { 0 } ?: 1 } // put nulls at the end
                    .toTypedArray()
            }
            .toTypedArray()
    }
}
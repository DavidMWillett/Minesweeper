package minesweeper

import java.util.Scanner
import kotlin.random.Random

/**
 * Initializes minefield and runs game loop, calling functions of MineField object according to user input.
 */
fun main() {
    val scanner = Scanner(System.`in`)
    print("How many mines do you want on the field? ")
    MineField.placeMines(scanner.nextInt())
    do {
        MineField.print()
        do {
            print("Set/unset mines marks or claim a cell as free: ")
            val x = scanner.nextInt()
            val y = scanner.nextInt()
            val isValidCommand = when (scanner.next()) {
                "mine" -> MineField.setMark(y - 1, x - 1)
                "free" -> { MineField.explore(y - 1, x - 1); true }
                else -> false
            }
        } while (!isValidCommand)
    } while (!MineField.completed && MineField.survived)
    MineField.print()
    println(if (MineField.survived)
        "Congratulations! You found all mines!"
        else "You stepped on a mine and failed!")
}

/**
 * Maintains state of minefield and provides functions to modify its state.
 */
object MineField {

    private const val FIELD_SIZE = 9
    private const val TOTAL_CELLS = FIELD_SIZE * FIELD_SIZE

    private val field = Array(FIELD_SIZE) { Array(FIELD_SIZE) {
        Cell(false, 0, marked = false, explored = false) } }

    // Member variables keeps track of mine clearing progress so as to be able to determine when the game is
    // completed. It's a more efficient alternative to repeatedly scanning the minefield.
    private var numberOfMines = 0
    private var exploredCells = 0
    private var correctlyMarkedCells = 0
    private var incorrectlyMarkedCells = 0

    // These are the criteria for completing the game, implemented as a property.
    val completed: Boolean
        get() = correctlyMarkedCells == numberOfMines && incorrectlyMarkedCells == 0 ||
                exploredCells == TOTAL_CELLS - numberOfMines

    var survived = true

    /**
     * Places the number of mines indicated by [mines]. Will fail if [mines] > TOTAL_CELLS.
     */
    fun placeMines(mines: Int) {
        while (numberOfMines < mines) {
            placeMine()
        }
    }

    /**
     * Places a single mine at a random empty cell on the board. Guaranteed to place a mine so long as at least
     * one free cell remains.
     */
    private fun placeMine() {
        var row: Int
        var col: Int
        // Look for an empty cell.
        do {
            row = Random.nextInt(FIELD_SIZE)
            col = Random.nextInt(FIELD_SIZE)
        } while (field[row][col].mined)
        // Place mine and update nearby mine count of adjacent cells.
        field[row][col].mined = true
        for (r in maxOf(row - 1, 0)..minOf(row + 1, 8)) {
            for (c in maxOf(col - 1, 0)..minOf(col + 1, 8)) {
                field[r][c].nearbyMines++
            }
        }
        numberOfMines++
    }

    /**
     * Removes a mine at the specified cell. There must be a mine present, the assert will fail if not.
     */
    private fun clearMine(row: Int, col: Int) {
        assert(field[row][col].mined)
        field[row][col].mined = false
        for (r in maxOf(row - 1, 0)..minOf(row + 1, 8)) {
            for (c in maxOf(col - 1, 0)..minOf(col + 1, 8)) {
                field[r][c].nearbyMines--
            }
        }
        numberOfMines--
    }

    /**
     * Sets a mark at the specified cell, so long as the cell has not already been explored. Return value is true
     * if the mark was successfully placed, otherwise false.
     */
    fun setMark(row: Int, col: Int): Boolean {
        val cell = field[row][col]
        if (cell.explored) {
            if (cell.hasNumber) println("There is a number here!")
            return false
        }
        cell.marked = !cell.marked
        if (cell.mined) {
            correctlyMarkedCells += if (cell.marked) 1 else -1
        } else {
            incorrectlyMarkedCells += if (cell.marked) 1 else -1
        }
        return true
    }

    /**
     * Explores the specified cell, checking first to see if it is mined. If not, auto-explores from this cell.
     * Note that mine is cleared and repositioned if no cells have been explored yet.
     */
    fun explore(row: Int, col: Int) {
        if (field[row][col].mined) {
            if (exploredCells > 0) {    // Cell is mined and not first move, so BANG!
                revealMines()
                survived = false
                return
            }
            do {                        // First move, so clear and replace mine. Keep doing so until
                clearMine(row, col)     // mine has been placed elsewhere. Will fail if no free cells.
                placeMine()
            } while (field[row][col].mined)
        }
        autoExplore(row, col)           // Automatically explore from here.
    }

    /**
     * Shows where mines are. Called when a mined cell is explored.
     */
    private fun revealMines() {
        for (row in field) {
            for (cell in row) {
                if (cell.mined) cell.explored = true
            }
        }
    }

    /**
     * Recursive function to automatically explore area of contiguous cells bounded by those adjacent to at least
     * one mine.
     */
    private fun autoExplore(row: Int, col: Int) {
        val cell = field[row][col]
        if (cell.explored) return
        cell.explored = true
        exploredCells++
        if (cell.nearbyMines > 0) return
        for (r in maxOf(row - 1, 0)..minOf(row + 1, 8)) {
            for (c in maxOf(col - 1, 0)..minOf(col + 1, 8)) {
                autoExplore(r, c)
            }
        }
    }

    /**
     * Prints the minefield.
     */
    fun print() {
        println()
        println(" |123456789|")
        println("—|—————————|")
        for (index in field.indices) {
            print("${index + 1}|")
            for (cell in field[index]) {
                print(cell.appearance)
            }
            println("|")
        }
        println("—|—————————|")
    }

    /**
     * Represents a cell on the minefield. Maintains state of cell and provides properties to help read its state
     * and to indicate how current state should be displayed.
     */
    private class Cell(var mined: Boolean, var nearbyMines: Int, var marked: Boolean, var explored: Boolean) {
        val appearance: String
            get() = when {
                explored && mined -> "X"
                explored && nearbyMines == 0 -> "/"
                explored && nearbyMines > 0 -> nearbyMines.toString()
                marked -> "*"
                else -> "."
            }
        val hasNumber: Boolean
            get() = !mined && nearbyMines > 0
    }
}

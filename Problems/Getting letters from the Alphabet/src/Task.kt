import java.util.Scanner

fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    // put your code here
    val letter = input.next()
    printLettersPreceding(letter.first())
}

fun printLettersPreceding(letter: Char) {
    for (c in 'a'..'z') {
        if (c == letter) return
        print(c)
    }
}

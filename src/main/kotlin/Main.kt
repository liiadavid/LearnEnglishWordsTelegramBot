import java.io.File

fun main() {
    val wordsFile: File = File("words.txt")

    wordsFile.createNewFile()

    wordsFile.writeText("hello привет\n")
    wordsFile.appendText("dog собака\n")
    wordsFile.appendText("cat кошка\n")

    wordsFile.forEachLine {
        println(it)
    }
}
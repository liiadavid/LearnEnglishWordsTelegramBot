import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int? = 0,
)

fun main() {
    val wordsFile: File = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.createNewFile()

    wordsFile.writeText("hello|привет|1\n")
    wordsFile.appendText("dog|собака|0\n")
    wordsFile.appendText("cat|кошка|0\n")

    wordsFile.forEachLine { line ->
        val word = line.split("|")
        dictionary.add(Word(word[0], word[1], word[2].toIntOrNull() ?: 0))
    }

    dictionary.forEach {
        println(it)
    }
}
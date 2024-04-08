import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

fun main() {
    val wordsFile: File = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.forEachLine { line ->
        val word = line.split("|")
        dictionary.add(Word(word[0], word[1], word[2].toIntOrNull() ?: 0))
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        when (readln().toInt()) {
            1 -> {
                val questionWordsList = dictionary.filter { it.correctAnswersCount < NUMBER_OF_CORRECT_ANSWERS }

                if (questionWordsList.isEmpty()) return println("Вы выучили все слова!")

                for (word in questionWordsList.shuffled()) {
                    println(questionWordsList.random().original)

                    val answerWordsList = if (questionWordsList.size < NUMBER_OF_ANSWERS) {
                        val learnedWordsList =
                            dictionary.filter { it.correctAnswersCount >= NUMBER_OF_CORRECT_ANSWERS }.shuffled()
                        questionWordsList.shuffled().take(NUMBER_OF_ANSWERS) +
                                learnedWordsList.take(NUMBER_OF_ANSWERS - questionWordsList.size)
                    } else {
                        questionWordsList.shuffled().take(NUMBER_OF_ANSWERS)
                    }.shuffled()

                    answerWordsList.shuffled().forEachIndexed { index, word ->
                        println("${index + 1}. ${word.translate}")
                    }

                    print("Вариант ответа: ")
                    readln()
                }
            }

            2 -> {
                val numberOfLearnedWords =
                    dictionary.filter { it.correctAnswersCount >= NUMBER_OF_CORRECT_ANSWERS }.size
                val numberOfWordsInDictionary = dictionary.size
                val percentageOfLearnedWords =
                    (numberOfLearnedWords.toDouble() / numberOfWordsInDictionary * 100).toInt()
                println("Выучено $numberOfLearnedWords из $numberOfWordsInDictionary слов | $percentageOfLearnedWords%")
            }

            0 -> break
            else -> println("Для управления вводите только 1, 2 или 0")
        }
    }
}

const val NUMBER_OF_CORRECT_ANSWERS = 3
const val NUMBER_OF_ANSWERS = 4
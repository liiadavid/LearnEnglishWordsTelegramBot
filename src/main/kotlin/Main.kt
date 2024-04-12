import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {
    val wordsFile = File("words.txt")
    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.forEachLine { line ->
        val word = line.split("|")
        dictionary.add(Word(word[0], word[1], word[2].toIntOrNull() ?: 0))
    }

    fun saveDictionary(dictionary: List<Word>) {
        wordsFile.writeText("")
        dictionary.forEach { word ->
            wordsFile.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")

        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val unlearnedWordsList =
                        dictionary.filter { it.correctAnswersCount < NUMBER_OF_CORRECT_ANSWERS }

                    if (unlearnedWordsList.isEmpty()) {
                        println("Вы выучили все слова!")
                        break
                    }

                    val questionWordsList =
                        unlearnedWordsList.shuffled().take(NUMBER_OF_ANSWERS).toMutableList()
                    val questionWord = questionWordsList.random()

                    if (questionWordsList.size < NUMBER_OF_ANSWERS) {
                        val learnedWordsList =
                            dictionary.filter { it.correctAnswersCount >= NUMBER_OF_CORRECT_ANSWERS }.shuffled()

                        questionWordsList +=
                            learnedWordsList.take(NUMBER_OF_ANSWERS - questionWordsList.size).shuffled()
                    }

                    val questionWordId = questionWordsList.indexOf(questionWord) + 1
                    println()

                    println(questionWord.original)

                    questionWordsList.forEachIndexed { index, answerWord ->
                        println("${index + 1}. ${answerWord.translate}")
                    }

                    println("0. вернуться в меню")
                    print("Вариант ответа: ")
                    val userAnswer = readln().toIntOrNull()

                    when (userAnswer) {
                        questionWordId -> {
                            println("Верно!")
                            questionWord.correctAnswersCount++
                            saveDictionary(dictionary)
                        }

                        0 -> break
                        else -> println("Ответ неверный.")
                    }
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
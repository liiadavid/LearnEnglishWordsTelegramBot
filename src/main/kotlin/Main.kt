fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word -> " ${index + 1} - ${word.translate}" }
        .joinToString("\n")
    return this.correctAnswer.original + "\n" + variants + "\n 0 - выйти в меню"
}

fun main() {
    val trainer = try {
        LearnWordsTrainer(learnedAnswerCount = 3, countOfQuestionWords = 4)
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        when (readln().toIntOrNull()) {
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()

                    if (question == null) {
                        println("Вы выучили все слова!")
                        break
                    } else {
                        println(question.asConsoleString())
                    }
                    print("Вариант ответа: ")
                    val userAnswer = readln().toIntOrNull()
                    if (userAnswer == 0) break

                    if (trainer.checkAnswer(userAnswer?.minus(1))) {
                        println("Правильно!\n")
                    } else {
                        println(
                            "Неправильно! ${question.correctAnswer.original} - это ${question.correctAnswer.translate}\n"
                        )
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println(
                    "Выучено ${statistics.numberOfLearnedWords} из ${statistics.numberOfWordsInDictionary} слов |" +
                            " ${statistics.percentageOfLearnedWords}%"
                )
            }

            0 -> break
            else -> println("Введите 1, 2 или 0")
        }
    }
}
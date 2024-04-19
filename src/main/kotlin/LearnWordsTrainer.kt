import java.io.File

data class Statistics(
    val numberOfLearnedWords: Int,
    val numberOfWordsInDictionary: Int,
    val percentageOfLearnedWords: Int,
)

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4
) {
    var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistics(): Statistics {
        val numberOfLearnedWords = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.size
        val numberOfWordsInDictionary = dictionary.size
        val percentageOfLearnedWords = numberOfLearnedWords * 100 / numberOfWordsInDictionary
        return Statistics(numberOfLearnedWords, numberOfWordsInDictionary, percentageOfLearnedWords)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWordsList =
            dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
        if (unlearnedWordsList.isEmpty()) return null
        val questionWordsList = if (unlearnedWordsList.size < countOfQuestionWords) {
            val learnedWordsList = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.shuffled()
            unlearnedWordsList.shuffled()
                .take(countOfQuestionWords) + learnedWordsList.take(countOfQuestionWords - unlearnedWordsList.size)
        } else {
            unlearnedWordsList.shuffled().take(countOfQuestionWords)
        }.shuffled()
        val correctAnswer = questionWordsList.random()
        question = Question(
            variants = questionWordsList,
            correctAnswer = correctAnswer,
        )
        return question
    }

    fun checkAnswer(userAnswer: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.variants.indexOf(it.correctAnswer)
            if (correctAnswerId == userAnswer) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }

    private fun loadDictionary(): List<Word> {
        try {
            val dictionary = mutableListOf<Word>()
            val wordsFile = File("words.txt")
            wordsFile.forEachLine { line ->
                val word = line.split("|")
                dictionary.add(Word(word[0], word[1], word[2].toIntOrNull() ?: 0))
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Некорректный файл")
        }
    }

    private fun saveDictionary(dictionary: List<Word>) {
        val wordsFile = File("words.txt")
        wordsFile.writeText("")
        dictionary.forEach { word ->
            wordsFile.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }
}
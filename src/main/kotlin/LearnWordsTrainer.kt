import kotlinx.serialization.Serializable
import java.io.File

data class Statistics(
    val numberOfLearnedWords: Int,
    val numberOfWordsInDictionary: Int,
    val percentageOfLearnedWords: Int,
)

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
    val photo: String,
    var photoId: String?,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
    private val learnedAnswerCount: Int = 3,
    private val countOfQuestionWords: Int = 4,
    var lastMessageId: Long? = null,
) {
    var question: Question? = null
    private val photoId: String? = null
    private val dictionary = loadDictionary(photoId)

    fun getStatistics(): Statistics {
        val numberOfLearnedWords = dictionary.filter { it.correctAnswersCount >= learnedAnswerCount }.size
        val numberOfWordsInDictionary = dictionary.size
        val percentageOfLearnedWords = numberOfLearnedWords * 100 / numberOfWordsInDictionary
        return Statistics(numberOfLearnedWords, numberOfWordsInDictionary, percentageOfLearnedWords)
    }

    fun resetProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
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
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun loadDictionary(photoId: String?): List<Word> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val dictionary = mutableListOf<Word>()
            wordsFile.forEachLine { line ->
                val word = line.split("|")
                dictionary.add(
                    Word(
                        original = word[0],
                        translate = word[1],
                        correctAnswersCount = word[2].toIntOrNull() ?: 0,
                        photo = "wordspictures/${word[0]}.jpg",
                        photoId = photoId
                    )
                )
            }
            return dictionary
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Некорректный файл")
        }
    }

    fun saveDictionary() {
        val wordsFile = File(fileName)
        wordsFile.writeText("")
        dictionary.forEach { word ->
            wordsFile.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}|${word.photo}|${word.photoId ?: null}\n")
        }
    }
}
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

fun main(args: Array<String>) {
    val json = Json {
        ignoreUnknownKeys = true
    }
    val botToken = args[0]
    val bot = TelegramBotService(botToken, json)
    val trainers = HashMap<Long, LearnWordsTrainer>()
    var trainer = LearnWordsTrainer()
    var responseString: String
    var response: Response
    var sortedUpdates: List<Update>
    var lastUpdateId = 0L
    var chatId: Long
    var message: String?
    var data: String?
    var statistics: Statistics
    var question: Question?

    fun checkNextQuestionAndSend(chatId: Long) {
        question = trainer.getNextQuestion()
        if (question == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
        else bot.sendQuestion(chatId, trainer.getNextQuestion())
    }

    fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>) {
        chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        message = update.message?.text
        data = update.callbackQuery?.data
        statistics = trainer.getStatistics()

        trainer = trainers.getOrPut(chatId) {LearnWordsTrainer("$chatId.txt")}

        if (message?.lowercase() == "/start")
            bot.sendMenu(chatId)
        if (data?.lowercase() == DATA_STATISTICS)
            bot.sendMessage(
                chatId, "Выучено ${statistics.numberOfLearnedWords} из " +
                        "${statistics.numberOfWordsInDictionary} слов | ${statistics.percentageOfLearnedWords}%"
            )
        if (data?.lowercase() ==  DATA_RESET_STATISTICS) {
            trainer.resetProgress()
            bot.sendMessage(chatId, "Прогресс обнулён")
        }
        if (data?.lowercase() == DATA_LEARN_WORDS) {
            question = trainer.getNextQuestion()
            if (question == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
            else bot.sendQuestion(chatId, question)
        }
        if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            if (trainer.checkAnswer(data?.substringAfter(CALLBACK_DATA_ANSWER_PREFIX)?.toInt()))
                bot.sendMessage(chatId, "Правильно!")
            else bot.sendMessage(
                chatId,
                "Не правильно: ${trainer.question?.correctAnswer?.original} - " +
                        "${trainer.question?.correctAnswer?.translate}"
            )
            checkNextQuestionAndSend(chatId)
        }
    }

    while (true) {
        Thread.sleep(MILLIS)
        responseString = bot.getUpdates(lastUpdateId)
        println(responseString)
        response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

const val MILLIS: Long = 2000
const val DATA_STATISTICS = "statistics_clicked"
const val DATA_LEARN_WORDS = "learn_words_clicked"
const val DATA_RESET_STATISTICS = "reset_statistics_clicked"
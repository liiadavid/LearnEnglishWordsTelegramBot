import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val botToken = args[0]
    val bot = TelegramBotService(botToken)
    val trainers = HashMap<Long, LearnWordsTrainer>()

    var lastUpdateId = 0L

    while (true) {
        Thread.sleep(MILLIS)
        val response: Response = bot.getUpdates(lastUpdateId) ?: continue
        println(response)
        if (response.result.isEmpty()) continue
        val sortedUpdates: List<Update> = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(bot, it, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun checkNextQuestionAndSend(
    bot: TelegramBotService,
    trainer: LearnWordsTrainer,
    chatId: Long,
) {
    val question: Question? = trainer.getNextQuestion()
    if (question == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
    else bot.sendQuestion(chatId, question)
}

fun handleUpdate(
    bot: TelegramBotService,
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
) {
    val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val message: String? = update.message?.text
    val data: String? = update.callbackQuery?.data
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (message?.lowercase() == DATA_MENU)
        bot.sendMenu(chatId)
    if (data?.lowercase() == DATA_STATISTICS) {
        val statistics: Statistics = trainer.getStatistics()
        bot.sendMessage(
            chatId, "Выучено ${statistics.numberOfLearnedWords} из " +
                    "${statistics.numberOfWordsInDictionary} слов | ${statistics.percentageOfLearnedWords}%"
        )
    }
    if (data?.lowercase() == DATA_RESET_STATISTICS) {
        trainer.resetProgress()
        bot.sendMessage(chatId, "Прогресс обнулён")
    }
    if (data?.lowercase() == DATA_LEARN_WORDS) {
        val question: Question? = trainer.getNextQuestion()
        if (question == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
        else bot.sendQuestion(chatId, question)
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        if (trainer.checkAnswer(data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()))
            bot.sendMessage(chatId, "Правильно!")
        else bot.sendMessage(
            chatId,
            "Не правильно: ${trainer.question?.correctAnswer?.original} - " +
                    "${trainer.question?.correctAnswer?.translate}"
        )
        checkNextQuestionAndSend(bot, trainer, chatId)
    }
    if (data?.lowercase() == DATA_MENU)
        bot.sendMenu(chatId)
}

const val MILLIS: Long = 2000
const val DATA_STATISTICS = "statistics_clicked"
const val DATA_LEARN_WORDS = "learn_words_clicked"
const val DATA_RESET_STATISTICS = "reset_statistics_clicked"
const val DATA_MENU = "/start"
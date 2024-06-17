import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

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
    @SerialName("message_id")
    val id: Long,
    @SerialName("text")
    val text: String? = null,
    @SerialName("chat")
    val chat: Chat,
    @SerialName("photo")
    val photo: Array<Photo>? = null,
)

@Serializable
data class Photo(
    @SerialName("file_id")
    val fileId: String = "",
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
    messageId: Long,
) {
    val question: Question? = trainer.getNextQuestion()
    if (question == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
    else bot.sendQuestionWithPhoto(chatId, true, question)
    //bot.sendQuestion(chatId, messageId, question)
}

fun handleUpdate(
    bot: TelegramBotService,
    update: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
) {
    val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val messageId: Long = update.callbackQuery?.message?.id ?: 0
    val message: String? = update.message?.text
    val data: String? = update.callbackQuery?.data
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (message?.lowercase()?.startsWith(DATA_MENU) == true ||
        data?.lowercase()?.startsWith(DATA_MENU) == true
    ) {
        bot.sendMenu(chatId)
    }
    if (data?.lowercase() == DATA_STATISTICS) {
        val statistics: Statistics = trainer.getStatistics()
        bot.sendStatistics(
            chatId, messageId, "Выучено ${statistics.numberOfLearnedWords} из " +
                    "${statistics.numberOfWordsInDictionary} слов | ${statistics.percentageOfLearnedWords}%"
        )
    }
    if (data?.lowercase() == DATA_RESET_STATISTICS) {
        trainer.resetProgress()
        bot.sendResetMessage(chatId, messageId, "Прогресс обнулён")
    }
    if (data?.lowercase() == DATA_LEARN_WORDS) {
        val question: Question? = trainer.getNextQuestion()
        if (question == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
        else bot.sendQuestionWithPhoto(chatId, true, question)
        //bot.sendQuestion(chatId, messageId, question)
    }
    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        if (trainer.checkAnswer(data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()))
            bot.sendPhoto(
                File(trainer.question?.correctAnswer?.photo),
                chatId,
                "Правильно! ${trainer.question?.correctAnswer?.original} - ${trainer.question?.correctAnswer?.translate}"
            )
        else bot.sendPhoto(
            File(trainer.question?.correctAnswer?.photo),
            chatId,
            "Не правильно: ${trainer.question?.correctAnswer?.original} - " +
                    "${trainer.question?.correctAnswer?.translate}"
        )
        Thread.sleep(MILLIS)
        checkNextQuestionAndSend(bot, trainer, chatId, messageId)
    }
}

const val MILLIS: Long = 2000
const val DATA_STATISTICS = "statistics_clicked"
const val DATA_LEARN_WORDS = "learn_words_clicked"
const val DATA_RESET_STATISTICS = "reset_statistics_clicked"
const val DATA_MENU = "/start"
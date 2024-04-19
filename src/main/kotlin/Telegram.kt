fun main(args: Array<String>) {
    val botToken = args[0]
    val bot = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()
    var updateId = 0
    var chatId: String?
    var updates: String
    val updateIdRegex = "\"update_id\":(\\d+),".toRegex()
    val chatIdRegex = "\"chat\":\\{\"id\":(\\d+),".toRegex()
    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
    val dataRegex = "\"data\":\"(.+?)\"".toRegex()
    var messageText: String?
    var data: String?
    var statistics: Statistics
    var question: Question?

    fun checkNextQuestionAndSend(chatId: String?) {
        if (trainer.getNextQuestion() == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
        else bot.sendQuestion(chatId, trainer.getNextQuestion())
    }

    while (true) {
        Thread.sleep(MILLIS)
        updates = bot.getUpdates(updateId)
        println(updates)
        updateId = (updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()?.plus(1)) ?: continue
        chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        messageText = messageTextRegex.find(updates)?.groups?.get(1)?.value
        data = dataRegex.find(updates)?.groups?.get(1)?.value
        statistics = trainer.getStatistics()

        if (messageText?.lowercase() == "/start")
            bot.sendMenu(chatId)
        if (data?.lowercase() == DATA_STATISTICS)
            bot.sendMessage(
                chatId, "Выучено ${statistics.numberOfLearnedWords} из " +
                        "${statistics.numberOfWordsInDictionary} слов | ${statistics.percentageOfLearnedWords}%"
            )
        if (data?.lowercase() == DATA_LEARN_WORDS) {
            question = trainer.getNextQuestion()
            if (question == null) bot.sendMessage(chatId, "Вы выучили все слова в базе")
            else bot.sendQuestion(chatId, question)
        }
        if (data != null && data.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            if (trainer.checkAnswer(data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()))
                bot.sendMessage(chatId, "Правильно!")
            else bot.sendMessage(
                chatId,
                "Не правильно: ${trainer.question?.correctAnswer?.original} - " +
                        "${trainer.question?.correctAnswer?.translate}"
            )
            checkNextQuestionAndSend(chatId)
        }

    }
}

const val MILLIS: Long = 2000
const val DATA_STATISTICS = "statistics_clicked"
const val DATA_LEARN_WORDS = "learn_words_clicked"
fun main(args: Array<String>) {
    val botToken = args[0]
    val bot = TelegramBotService(botToken)
    var updateId = 0
    var updates: String
    var updateIdRegex: String?
    var chatId: String?
    var messageTextRegex: String?
    var text = ""
    var message: String

    while (true) {
        Thread.sleep(MILLIS)
        updates = bot.getUpdates(updateId)
        println(updates)
        updateIdRegex = "\"update_id\":(.+?),".toRegex().find(updates)?.groups?.get(1)?.value
        updateId = (updateIdRegex?.toInt()?.plus(1)) ?: continue

        chatId = "\"id\":(.+?),".toRegex().find(updates)?.groups?.get(1)?.value
        messageTextRegex = "\"text\":\"(.+?)\"".toRegex().find(updates)?.groups?.get(1)?.value
        if (messageTextRegex.equals("Hello")) text = "Hello"
        message = bot.sendMessage(chatId, text)
        println(message)
    }
}

const val MILLIS: Long = 2000
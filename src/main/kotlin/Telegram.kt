fun main(args: Array<String>) {
    val botToken = args[0]
    val bot = TelegramBotService(botToken)
    var updateId = 0
    var chatId: String?
    var updates: String
    val updateIdRegex = "\"update_id\":(.+?),".toRegex()
    val chatIdRegex = "\"id\":(.+?),".toRegex()
    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()
    var text = ""
    var message: String

    while (true) {
        Thread.sleep(MILLIS)
        updates = bot.getUpdates(updateId)
        println(updates)
        updateId = (updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()?.plus(1)) ?: continue
        chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value

        if (messageTextRegex.find(updates)?.groups?.get(1)?.value.equals("Hello")) text = "Hello"
        message = bot.sendMessage(chatId, text)
        println(message)
    }
}

const val MILLIS: Long = 2000
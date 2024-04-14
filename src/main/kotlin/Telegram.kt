fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val bot = TelegramBotService()

    while (true) {
        Thread.sleep(2000)
        val updates: String = bot.getUpdates(botToken, updateId)
        println(updates)
        val updateIdRegex = "\"update_id\":(.+?),".toRegex().find(updates)?.groups?.get(1)?.value
        updateId = (updateIdRegex?.toInt()?.plus(1)) ?: continue

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val matchResult: MatchResult? = messageTextRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        val chatId = "\"id\":(.+?),".toRegex().find(updates)?.groups?.get(1)?.value
        val message = bot.sendMessage(botToken, chatId, text)
        println(message)
    }
}
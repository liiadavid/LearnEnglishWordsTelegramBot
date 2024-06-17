import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("message_id")
    val messageId: Long = 0L,
    @SerialName("text")
    val text: String?,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyBoard>?>,
)

@Serializable
data class InlineKeyBoard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class GetFileRequest(
    @SerialName("file_id")
    val fileId: String?,
)

class TelegramBotService(
    private val botToken: String,
) {
    private val json: Json = Json { ignoreUnknownKeys = true }
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): Response? {
        val urlGetUpdates = "$API_BOT$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()

        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (responseResult.isSuccess)
            responseResult.getOrNull()?.body()?.let { json.decodeFromString(it) }
        else
            null
    }

    fun editMessage(chatId: Long, messageId: Long, message: String?): String? {
        val urlSendMessage = "$API_BOT$botToken/editMessageText"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }

        return if (responseResult.isSuccess)
            responseResult.getOrNull()?.body()
        else
            null
    }

    fun sendMessage(chatId: Long, message: String?): String? {
        val urlSendMessage = "$API_BOT$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }

        return if (responseResult.isSuccess)
            responseResult.getOrNull()?.body()
        else
            null
    }

    fun sendMenu(chatId: Long): String? {
        val urlSendMessage = "$API_BOT$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyBoard(
                            text = "Изучать слова",
                            callbackData = DATA_LEARN_WORDS
                        )
                    ),
                    listOf(
                        InlineKeyBoard(
                            text = "Статистика",
                            callbackData = DATA_STATISTICS
                        )
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (responseResult.isSuccess)
            responseResult.getOrNull()?.body()
        else
            null
    }

    fun sendQuestion(chatId: Long, messageId: Long, question: Question): String? {
        val urlSendMessage = "$API_BOT$botToken/editMessageText"
        val variantsOfAnswer = question.variants.mapIndexed { index, word ->
            listOf(
                InlineKeyBoard(
                    text = word.translate,
                    callbackData = CALLBACK_DATA_ANSWER_PREFIX + index
                )
            )
        }.toMutableList()
        val variantMenu = listOf(
            InlineKeyBoard(
                text = "в меню",
                callbackData = DATA_MENU
            )
        )
        variantsOfAnswer.add(variantMenu)
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
            text = question.correctAnswer.original,
            replyMarkup = ReplyMarkup(
                variantsOfAnswer
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (responseResult.isSuccess)
            responseResult.getOrNull()?.body()
        else
            null
    }

    fun sendStatistics(chatId: Long, messageId: Long, message: String): String? {
        val urlSendMessage = "$API_BOT$botToken/editMessageText"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
            text = message,
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyBoard(
                            text = "Сбросить прогресс",
                            callbackData = DATA_RESET_STATISTICS
                        )
                    ),
                    listOf(
                        InlineKeyBoard(
                            text = "Основное меню",
                            callbackData = DATA_MENU
                        )
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (responseResult.isSuccess)
            responseResult.getOrNull()?.body()
        else
            null
    }

    fun sendResetMessage(chatId: Long, messageId: Long, message: String): String? {
        val urlSendMessage = "$API_BOT$botToken/editMessageText"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
            text = message,
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyBoard(
                            text = "Изучать слова",
                            callbackData = DATA_LEARN_WORDS
                        )
                    ),
                    listOf(
                        InlineKeyBoard(
                            text = "Основное меню",
                            callbackData = DATA_MENU
                        )
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (responseResult.isSuccess)
            responseResult.getOrNull()?.body()
        else
            null
    }

    fun getFile(fileId: String, json: Json): String {
        val urlGetFile = "$API_BOT/getFile"
        println(urlGetFile)
        val requestBody = GetFileRequest(fileId = fileId)
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlGetFile))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        val response: HttpResponse<String> = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        )
        return response.body()
//        val responseResult: Result<HttpResponse<String>> =
//            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
//        return if (responseResult.isSuccess)
//            responseResult.getOrNull()?.let { json.decodeFromString(it.body()) }
//        else
//            null
    }

    fun downloadFile(filePath: String, fileName: String) {
        val urlGetFile = "$API_BOT/$filePath"
        println(urlGetFile)
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(urlGetFile))
            .GET()
            .build()
        val response: HttpResponse<InputStream> = HttpClient
            .newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofInputStream())
        println("status code: " + response.statusCode())
        val body: InputStream = response.body()
        body.copyTo(File(fileName).outputStream(), 16 * 1024)
    }
}

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val API_BOT = "https://api.telegram.org/bot"
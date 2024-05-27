import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
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
                        ),
                        InlineKeyBoard(
                            text = "Статистика",
                            callbackData = DATA_STATISTICS
                        )
                    ),
                    listOf(
                        InlineKeyBoard(
                            text = "Сбросить прогресс",
                            callbackData = DATA_RESET_STATISTICS
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

    fun sendQuestion(chatId: Long, question: Question): String? {
        val urlSendMessage = "$API_BOT$botToken/sendMessage"
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

}

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val API_BOT = "https://api.telegram.org/bot"
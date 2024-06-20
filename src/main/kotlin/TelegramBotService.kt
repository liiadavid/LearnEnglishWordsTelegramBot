import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.math.BigInteger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("message_id")
    val messageId: Long = 0L,
    @SerialName("photo")
    val photo: String? = "",
    @SerialName("text")
    val text: String? = null,
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
data class ResponseItem<T>(
    @SerialName("result")
    val result: T
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

    fun sendQuestionWithPhoto(chatId: Long, hasSpoiler: Boolean = false, question: Question): Message? {
        val urlSendMessage = "$API_BOT$botToken/sendPhoto"
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
        val data: MutableMap<String, Any> = LinkedHashMap()
        data["chat_id"] = chatId.toString()
        data["photo"] = question.correctAnswer.photoId ?: File(question.correctAnswer.photo)
        data["caption"] = question.correctAnswer.original
        data["has_spoiler"] = hasSpoiler
        data["reply_markup"] = json.encodeToString(ReplyMarkup(variantsOfAnswer))
        val boundary: String = BigInteger(35, Random()).toString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlSendMessage))
            .postMultipartFormData(boundary, data)
            .build()
        val client: HttpClient = HttpClient.newBuilder().build()
        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (responseResult.isSuccess) {
            responseResult.getOrNull()
                ?.body()
                ?.let {
                    val result = runCatching { json.decodeFromString<ResponseItem<Message>>(it) }
                    result.exceptionOrNull()?.let {
                        println(it.printStackTrace())
                    }
                    question.correctAnswer.photoId = result.getOrNull()?.result?.photo?.get(1)?.fileId.toString()
                    result.getOrNull()?.result
                }
        } else {
            null
        }
    }

    fun sendPhoto(fileId: String?, file: String?, chatId: Long, caption: String, hasSpoiler: Boolean = false): Message? {
        val data: MutableMap<String, Any> = LinkedHashMap()
        data["chat_id"] = chatId.toString()
        data["photo"] = fileId ?: File(file)
        data["caption"] = caption
        data["has_spoiler"] = hasSpoiler
        val boundary: String = BigInteger(35, Random()).toString()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$API_BOT$botToken/sendPhoto"))
            .postMultipartFormData(boundary, data)
            .build()
        val client: HttpClient = HttpClient.newBuilder().build()
        val responseResult: Result<HttpResponse<String>> =
            runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
        return if (responseResult.isSuccess) {
            responseResult.getOrNull()
                ?.body()
                ?.let {
                    val result = runCatching { json.decodeFromString<ResponseItem<Message>>(it) }
                    result.exceptionOrNull()?.let {
                        println(it.printStackTrace())
                    }
                    result.getOrNull()?.result
                }
        } else {
            null
        }
    }

    private fun HttpRequest.Builder.postMultipartFormData(
        boundary: String,
        data: Map<String, Any>
    ): HttpRequest.Builder {
        val byteArrays = ArrayList<ByteArray>()
        val separator = "--$boundary\r\nContent-Disposition: form-data; name=".toByteArray(StandardCharsets.UTF_8)

        for (entry in data.entries) {
            byteArrays.add(separator)
            when (entry.value) {
                is File -> {
                    val file = entry.value as File
                    val path = Path.of(file.toURI())
                    val mimeType = Files.probeContentType(path)
                    byteArrays.add(
                        "\"${entry.key}\"; filename=\"${path.fileName}\"\r\nContent-Type: $mimeType\r\n\r\n".toByteArray(
                            StandardCharsets.UTF_8
                        )
                    )
                    byteArrays.add(Files.readAllBytes(path))
                    byteArrays.add("\r\n".toByteArray(StandardCharsets.UTF_8))
                }

                else -> byteArrays.add("\"${entry.key}\"\r\n\r\n${entry.value}\r\n".toByteArray(StandardCharsets.UTF_8))
            }
        }
        byteArrays.add("--$boundary--".toByteArray(StandardCharsets.UTF_8))

        this.header("Content-Type", "multipart/form-data;boundary=$boundary")
            .POST(HttpRequest.BodyPublishers.ofByteArrays(byteArrays))
        return this
    }

    fun deleteMessage(chatId: Long, messageId: Long): String? {
        val urlSendMessage = "$API_BOT$botToken/deleteMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            messageId = messageId,
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
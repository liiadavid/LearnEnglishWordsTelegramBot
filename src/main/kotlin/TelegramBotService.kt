import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: String?, message: String?): String {
        val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8)
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMenu(chatId: String?): String {
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "learn_words_clicked"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "statistics_clicked"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendQuestion(chatId: String?, question: Question?): String {
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
        val questionVariants: MutableList<String> = mutableListOf()
        question?.variants?.forEachIndexed { index, _ ->
            questionVariants.add(
                "{\n\t\"text\": \"${question.variants[index].translate}\"," +
                        "\n\t\"callback_data\": \"$CALLBACK_DATA_ANSWER_PREFIX$index\"\n}"
            )
        }
        val sendQuestionBody = """
            {
                "chat_id": $chatId,
                "text": "${question?.correctAnswer?.original}",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            ${questionVariants.joinToString(",\n")}
                        ]
                    ]
                }
            }
        """.trimIndent()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: String?) {
        if (trainer.getNextQuestion() == null) sendMessage(chatId, "Вы выучили все слова в базе")
        else sendQuestion(chatId, trainer.getNextQuestion())
    }
}

const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
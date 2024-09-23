    import org.example.Question
    import java.net.URI
    import java.net.URLEncoder
    import java.net.http.HttpClient
    import java.net.http.HttpRequest
    import java.net.http.HttpResponse
    import java.nio.charset.StandardCharsets

    const val TELEGRAM_API_URL = "https://api.telegram.org"
    const val LEARN_WORDS_CLICKED = "learn_words_clicked"
    const val STATISTICS_CLICKED = "statistics_clicked"
    const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

    class TelegramBotService(private val botToken: String) {

        fun getUpdates(updateId: Int): String {
            val urlGetUpdates = "$TELEGRAM_API_URL/bot$botToken/getUpdates?offset=$updateId"
            val client: HttpClient = HttpClient.newBuilder().build()
            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        }

        fun sendMessage(chatId: String, message: String): String {
            val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8)
            val urlSendMessage = "$TELEGRAM_API_URL/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
            val client: HttpClient = HttpClient.newBuilder().build()
            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        }

        fun sendMenu(chatId: String): String {
            val urlSendMenu = "$TELEGRAM_API_URL/bot$botToken/sendMessage"
            val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                 "inline_keyboard": [
                    [
                        {
                            "text": "Изучить слова",
                            "callback_data": "$LEARN_WORDS_CLICKED"
                        },
                        {
                            "text": "Статистика",
                            "callback_data": "$STATISTICS_CLICKED"
                        }
                    ]
                 ]
                }
            }
            """.trimIndent()

            val client: HttpClient = HttpClient.newBuilder().build()
            val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
                .build()

            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        }

        fun sendQuestion(chatId: String, question: Question): String {
            val optionsJson = question.variants.mapIndexed { index, word ->
                """
                {
                    "text": "${word.original}",
                    "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX$index"
                }
                """
            }.joinToString(",")

            val sendQuestionBody = """
            {
                "chat_id": "$chatId",
                "text": "Translate: ${question.correctAnswer.translate}",
                "reply_markup": {
                    "inline_keyboard": [
                        [$optionsJson]
                    ]
                }
            }
            """.trimIndent()

            val client: HttpClient = HttpClient.newBuilder().build()
            val request: HttpRequest = HttpRequest.newBuilder()
                .uri(URI.create("$TELEGRAM_API_URL/bot$botToken/sendMessage"))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
                .build()

            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        }
    }
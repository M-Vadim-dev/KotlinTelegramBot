import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.Question
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_URL = "https://api.telegram.org"
const val LEARN_WORDS_CLICKED = "learn_words_clicked"
const val STATISTICS_CLICKED = "statistics_clicked"
const val RESET_CLICKED = "reset_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val MAIN_MENU = "main_menu"
const val LEARN_WORDS_MENU = "learn_words_menu"

class TelegramBotService(private val botToken: String, private val json: Json) {

    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$TELEGRAM_API_URL/bot$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        while (true) {
            val result = runCatching { client.send(request, HttpResponse.BodyHandlers.ofString()) }
            if (result.isSuccess) {
                return result.getOrNull()?.body()
                    ?: "{\"status\":\"error\",\"message\":\"Обновление отсутствует\"}"
            } else {
                println("Ошибка при получении обновлений: ${result.exceptionOrNull()?.message}")
                Thread.sleep(5000)
            }
        }
    }

    fun sendMessage(chatId: Long, message: String): String {
        val urlSenMessage = "$TELEGRAM_API_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSenMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return runCatching {
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        }.getOrElse { e ->
            println("Ошибка при отправке сообщения: ${e.message}")
            "{\"status\":\"error\",\"message\":\"Ошибка при отправке сообщения\"}"
        }
    }

    fun sendMenu(chatId: Long): String {
        val urlSendMenu = "$TELEGRAM_API_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = """Режим изучения слов.
                |
                |Выбирайте подходящее значение из списка.
                |Если вы несколько раз правильно выберите слово,то автоматически слово пометится как изученное и не будет больше показываться.
            """.trimMargin(),
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "✍ Перейти к изучению", callbackData = LEARN_WORDS_CLICKED),
                    ),
                    listOf(
                        InlineKeyboard(text = "ℹ Статистика", callbackData = STATISTICS_CLICKED),
                        InlineKeyboard(text = "⬅ В главное меню", callbackData = MAIN_MENU),
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return runCatching {
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        }.getOrElse { e ->
            println("Ошибка при отправке меню: ${e.message}")
            "{\"status\":\"error\",\"message\":\"Ошибка при отправке меню\"}"
        }
    }

    fun sendMainMenu(chatId: Long): String {
        val urlSendMenu = "$TELEGRAM_API_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = """Привет!
                |Легкий английский ждет тебя!
                |Бот поможет тебе в обучении английского языка.
            """.trimMargin(),
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(InlineKeyboard(text = "\uD83D\uDCD6 Изучать слова", callbackData = LEARN_WORDS_MENU)),
                    listOf(InlineKeyboard(text = "🔄️ Сбросить прогресс", callbackData = RESET_CLICKED)),
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return runCatching {
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        }.getOrElse { e ->
            println("Ошибка при отправке главного меню: ${e.message}")
            "{\"status\":\"error\",\"message\":\"Ошибка при отправке главного меню\"}"
        }
    }

    fun sendQuestion(chatId: Long, question: Question): String {
        val urlSendQuestion = "$TELEGRAM_API_URL/bot$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = "Выбери правильный перевод: ${question.correctAnswer.original}",
            replyMarkup = ReplyMarkup(
                listOf(
                    question.variants.mapIndexed { index, word ->
                        InlineKeyboard(text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index")
                    },
                    listOf(InlineKeyboard(text = "⬅ Назад", callbackData = LEARN_WORDS_MENU)),
                )
            )
        )

        val requestBodyString = json.encodeToString(requestBody)
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(urlSendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()
        return runCatching {
            val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
            response.body()
        }.getOrElse { e ->
            println("Ошибка при отправке вопроса: ${e.message}")
            "{\"status\":\"error\",\"message\":\"Ошибка при отправке вопроса\"}"
        }
    }
}
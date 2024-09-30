import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.LearnWordsTrainer
import java.io.File

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)

    val wordsFile = File("words.txt")
    val trainer = LearnWordsTrainer(wordsFile, 3, 3)

    var lastUpdateId = 0L

    val json = Json {
        ignoreUnknownKeys = true
    }


    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (chatId != null && message?.lowercase() == "/start") {
            telegramBotService.sendMenu(json, chatId)
        }

        if (chatId != null && data?.lowercase() == LEARN_WORDS_CLICKED) {
            checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
        }

        if (chatId != null && data?.lowercase() == STATISTICS_CLICKED) {
            val statistics = trainer.getStatistics()
            val statisticsMessage =
                "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percentage}%"
            telegramBotService.sendMessage(json, chatId, statisticsMessage)
        }

        if (chatId != null && data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

            if (trainer.currentQuestion != null) {
                if (trainer.checkAnswer(answerIndex + 1)) {
                    telegramBotService.sendMessage(json, chatId, "Правильно!")
                } else {
                    val correctAnswer = trainer.currentQuestion?.correctAnswer?.translate
                    telegramBotService.sendMessage(json, chatId, "Неправильно. Правильный ответ: $correctAnswer")
                }
            } else telegramBotService.sendMessage(json, chatId, "Не найден текущий вопрос.")
            checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
        }
    }
}

fun checkNextQuestionAndSend(
    json: Json,
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long,
) {
    val unlearnedWords = trainer.dictionary.filter { it.correctAnswersCount < trainer.learnedAnswerCount }

    if (unlearnedWords.isNotEmpty()) {
        trainer.getQuestion(unlearnedWords, trainer.dictionary)
        val question = trainer.currentQuestion
        question?.let { telegramBotService.sendQuestion(json, chatId, it) }
    } else telegramBotService.sendMessage(json, chatId, "Вы выучили все слова в базе.")

}
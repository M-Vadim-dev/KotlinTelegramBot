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
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val telegramBotService = TelegramBotService(botToken, json)

    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, telegramBotService, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1

    }
}

fun handleUpdate(update: Update, telegramBotService: TelegramBotService, trainers: HashMap<Long, LearnWordsTrainer>) {
    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainerFile = File("${chatId}.txt")
    if (!trainerFile.exists()) {
        val wordsFile = File("words.txt")
        if (wordsFile.exists()) wordsFile.copyTo(trainerFile)
        else {
            telegramBotService.sendMessage(chatId, "Словарь слов не найден.")
            return
        }
    }
    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer(trainerFile, 3, 3, telegramBotService) }

    if (message?.lowercase() == "/start") {
        telegramBotService.sendMenu(chatId)
    }

    if (data?.lowercase() == LEARN_WORDS_CLICKED) {
        trainer.checkNextQuestionAndSend(chatId)
    }

    if (data?.lowercase() == STATISTICS_CLICKED) {
        val statistics = trainer.getStatistics()
        val statisticsMessage =
            "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percentage}%"
        telegramBotService.sendMessage(chatId, statisticsMessage)
    }

    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()

        if (trainer.currentQuestion != null) {
            if (trainer.checkAnswer(answerIndex + 1)) {
                telegramBotService.sendMessage(chatId, "Правильно!")
            } else {
                val correctAnswer = trainer.currentQuestion?.correctAnswer?.translate
                telegramBotService.sendMessage(chatId, "Неправильно. Правильный ответ: $correctAnswer")
            }
        } else telegramBotService.sendMessage(chatId, "Не найден текущий вопрос.")
        trainer.checkNextQuestionAndSend(chatId)
    }

    if (data?.lowercase() == RESET_CLICKED) {
        trainer.resetProgress()
        telegramBotService.sendMessage(chatId, "Прогресс сброшен")
    }
}
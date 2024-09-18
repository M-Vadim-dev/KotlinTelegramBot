import org.example.LearnWordsTrainer
import java.io.File

fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)

    val wordsFile = File("words.txt")
    val trainer = LearnWordsTrainer(wordsFile, 3, 3)

    var updateId = 0

    val updateIdRegex = "\"update_id\":\\s*(\\d+)".toRegex()
    val chatIdRegex = "\"chat\":\\s*\\{[^}]*\"id\":\\s*(\\d+)".toRegex()
    val messageRegex = "\"text\":\\s*\"(.*?)\"".toRegex()
    val dataRegex = "\"data\":\\s*\"(.*?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        updateId = updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt()?.plus(1) ?: continue

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value
        val message = messageRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value

        if (chatId != null && message?.lowercase() == "/start") {
            telegramBotService.sendMenu(chatId)
        }

        if (chatId != null && data?.lowercase() == LEARN_WORDS_CLICKED) {
            telegramBotService.sendMessage(chatId, "Начнём!")
        }

        if (chatId != null && data?.lowercase() == STATISTICS_CLICKED) {
            val statistics = trainer.getStatistics()
            val statisticsMessage =
                "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percentage}%"
            telegramBotService.sendMessage(chatId, statisticsMessage)
        }

    }

}

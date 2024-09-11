fun main(args: Array<String>) {
    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = telegramBotService.getUpdates(updateId)
        println(updates)

        val updateRegex = "\"update_id\":\\s*(\\d+)".toRegex()
        val matchResult = updateRegex.find(updates)

        if (matchResult != null) {
            updateId = matchResult.groups[1]?.value?.toInt()?.plus(1) ?: updateId
        }

        val chatIdRegex = "\"chat\":\\s*\\{[^}]*\"id\":\\s*(\\d+)".toRegex()
        val messageRegex = "\"text\":\\s*\"(.*?)\"".toRegex()

        val chatIdMatchResult = chatIdRegex.find(updates)
        val messageMatchResult = messageRegex.find(updates)

        if (chatIdMatchResult != null && messageMatchResult != null) {
            val chatId = chatIdMatchResult.groups[1]?.value
            val message = messageMatchResult.groups[1]?.value

            if (chatId != null && !message.isNullOrEmpty()) {
                telegramBotService.sendMessage(chatId, message)
            }
        }
    }
}
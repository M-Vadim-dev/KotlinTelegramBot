package org.example

import java.io.File

const val PERCENTAGE = 100

fun main() {
    val wordsFile = File("words.txt")

    val dictionary = mutableListOf<DataWord>()

    for (line in wordsFile.readLines()) {
        val parts = line.split("|")
        if (parts.size == 3) {
            val correctAnswersCount = parts[2].toIntOrNull() ?: 0
            val word = DataWord(
                original = parts[0],
                translate = parts[1],
                correctAnswersCount = correctAnswersCount,
            )
            dictionary.add(word)
        }
    }

    while (true) {
        println("Меню:")
        println("1 – Учить слова")
        println("2 – Статистика")
        println("0 – Выход")

        print("Введите номер меню: ")
        val userInput = readln()

        when (userInput) {
            "1" -> learningWord(dictionary, wordsFile)
            "2" -> statisticsShow(dictionary)
            "0" -> return

            else -> println("Неверный ввод. Введите 1, 2 или 0")
        }
    }
}

fun learningWord(dictionary: MutableList<DataWord>, wordsFile: File) {
    while (true) {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount < 3 }

        if (unlearnedWords.isEmpty()) {
            println("Вы выучили все слова.")
            return
        }

        val currentWord = unlearnedWords.random()
        val currentWordAndUnlearnedWords =
            (unlearnedWords.filter { it != currentWord }.shuffled().take(3) + currentWord).shuffled()

        println("Слово: ${currentWord.original}")
        currentWordAndUnlearnedWords.forEachIndexed { index, word ->
            println("${index + 1} – ${word.translate}")
        }

        val userInput = readln()
        if (userInput == "0") return

        val answerIndex = userInput.toIntOrNull()?.minus(1)
        if (answerIndex != null && answerIndex in currentWordAndUnlearnedWords.indices) {
            if (currentWordAndUnlearnedWords[answerIndex].translate == currentWord.translate) {
                println("Правильно!")
                currentWord.correctAnswersCount++
                saveDictionary(dictionary, wordsFile)
            } else println("Неправильно! Правильный ответ: ${currentWord.translate}.")
        } else println("Неверный ввод. Введите номер ответа или 0 для возврата в меню.")
    }
}

fun saveDictionary(dictionary: MutableList<DataWord>, wordsFile: File) {
    val updatedLines = dictionary.joinToString("\n") {
        "${it.original}|${it.translate}|${it.correctAnswersCount}"
    }
    wordsFile.writeText(updatedLines)
}

fun statisticsShow(dictionary: List<DataWord>) {
    val totalWords = dictionary.size
    val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }.size
    val percentage = if (totalWords > 0) (learnedWords * PERCENTAGE) / totalWords else 0

    println("Выучено $learnedWords из $totalWords слов | $percentage%")
}

data class DataWord(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)
package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")

    val dictionary = mutableListOf<Word>()

    for (line in wordsFile.readLines()) {
        val parts = line.split("|")
        if (parts.size == 3) {
            val correctAnswersCount = parts[2].toIntOrNull() ?: 0
            val word = Word(
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
            "1" -> learningWords(dictionary)
            "2" -> showStatistics(dictionary)
            "0" -> return

            else -> println("Неверный ввод. Введите 1, 2 или 0")
        }
    }
}

fun learningWords(dictionary: MutableList<Word>) {
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
        println("Варианты ответов: ${currentWordAndUnlearnedWords.joinToString(", ") { it.translate }}")

        val userAnswer = readln().lowercase()

        if (userAnswer == currentWord.translate) println("Правильно!")
        else println("Неправильно! Правильный ответ: ${currentWord.translate}.")
    }
}

fun showStatistics(dictionary: List<Word>) {
    val totalWords = dictionary.size
    val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }.size
    val percentage = if (totalWords > 0) (learnedWords * PERCENTAGE_BASE) / totalWords else 0

    println("Выучено $learnedWords из $totalWords слов | $percentage%")
}
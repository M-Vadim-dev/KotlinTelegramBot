package org.example

import java.io.File

const val PERCENTAGE_BASE = 100

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

    val totalWords = dictionary.size
    val learnedWords = dictionary.filter { it.correctAnswersCount >= 3 }.size
    val percentage = if (totalWords > 0) (learnedWords * PERCENTAGE_BASE) / totalWords else 0

    while (true) {
        println("Меню:")
        println("1 – Учить слова")
        println("2 – Статистика")
        println("0 – Выход")

        print("Введите номер меню: ")
        val userInput = readln()

        when (userInput) {
            "1" -> println("Вы выбрали 'Учить слова'")
            "2" -> println("Выучено $learnedWords из $totalWords слов | $percentage%")
            "0" -> return

            else -> println("Неверный ввод. Введите 1, 2 или 0")
        }
    }
}
package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val trainer = LearnWordsTrainer(wordsFile)

    while (true) {
        println("Меню:\n1 – Учить слова\n2 – Статистика\n0 – Выход")
        print("Введите номер меню: ")
        val userInput = readln()

        when (userInput) {
            "1" -> trainer.learnWords()
            "2" -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percentage}%")
            }

            "0" -> return
            else -> println("Неверный ввод. Введите 1, 2 или 0")
        }
    }
}
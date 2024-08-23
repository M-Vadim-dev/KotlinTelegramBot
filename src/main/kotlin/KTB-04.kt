package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.csv")
    wordsFile.createNewFile()
    wordsFile.writeText("hello,привет|3\n")
    wordsFile.appendText("dog,собака|1\n")
    wordsFile.appendText("cat,кошка|2\n")

    val dictionary = mutableListOf<Word>()

    for (line in wordsFile.readLines()) {
        val parts = line.split("|")
        if (parts.size == 2) {
            val wordParts = parts[0].split(",")
            if (wordParts.size == 2) {
                val correctAnswersCount = parts[1].toIntOrNull() ?: 0
                val word = Word(
                    original = wordParts[0],
                    translate = wordParts[1],
                    correctAnswersCount = correctAnswersCount,
                )
                dictionary.add(word)
            }
        }
    }

    File("learnedWordsCount.txt").writeText("Количество выученных слов: ${dictionary.size}")

    dictionary.forEach { word ->
        println("Слово: ${word.original}, Перевод: ${word.translate}, Количество правильных ответов: ${word.correctAnswersCount}")
    }
}

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

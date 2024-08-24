package org.example

import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.writeText("hello|привет|3\n")
    wordsFile.appendText("dog|собака|1\n")
    wordsFile.appendText("cat|кошка|2\n")

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

    dictionary.forEach { word ->
        println("Слово: ${word.original}, Перевод: ${word.translate}, Количество правильных ответов: ${word.correctAnswersCount}")
    }
}

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)
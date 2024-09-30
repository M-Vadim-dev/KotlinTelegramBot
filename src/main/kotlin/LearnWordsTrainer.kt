package org.example

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

data class Statistics(
    val totalWords: Int,
    val learnedWords: Int,
    val percentage: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

const val PERCENTAGE_BASE = 100

class LearnWordsTrainer(
    private val wordsFile: File,
    val learnedAnswerCount: Int = 3,
    private val countOfIncorrectQuestionWords: Int = 3,
) {
    val dictionary: MutableList<Word> = loadDictionary(wordsFile)
    var currentQuestion: Question? = null

    private fun loadDictionary(file: File): MutableList<Word> {
        return file.readLines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size == 3) {
                val correctAnswersCount = parts[2].toIntOrNull() ?: 0
                Word(original = parts[0], translate = parts[1], correctAnswersCount = correctAnswersCount)
            } else null
        }.toMutableList()
    }

    private fun saveDictionary() {
        wordsFile.writeText(dictionary.joinToString("\n") {
            "${it.original}|${it.translate}|${it.correctAnswersCount}"
        })
    }

    fun getStatistics(): Statistics {
        val totalWords = dictionary.size
        val learnedWords = dictionary.count { it.correctAnswersCount >= 3 }
        val percentage = if (totalWords > 0) (learnedWords * PERCENTAGE_BASE) / totalWords else 0
        return Statistics(totalWords, learnedWords, percentage)
    }

    fun getQuestion(unlearnedWords: List<Word>, learnedWords: List<Word>) {
        val currentWord = unlearnedWords.random()
        val availableWords = unlearnedWords.filter { it != currentWord }

        val requiredCount = countOfIncorrectQuestionWords - availableWords.size
        val variants = if (availableWords.size < countOfIncorrectQuestionWords) {
            (availableWords + learnedWords.shuffled().take(maxOf(requiredCount, 0))).shuffled()
        } else availableWords.shuffled().take(countOfIncorrectQuestionWords)

        currentQuestion = Question(variants = (variants + currentWord).shuffled(), correctAnswer = currentWord)
    }

    fun learnWords() {
        while (true) {
            val unlearnedWords = dictionary.filter { it.correctAnswersCount < learnedAnswerCount }
            if (unlearnedWords.isEmpty()) {
                println("Вы выучили все слова.")
                return
            }

            getQuestion(unlearnedWords, dictionary)
            currentQuestion?.let { presentQuestion(it) }

            val userInput = readln().toIntOrNull()
            if (userInput == 0) return

            if (userInput != null) {
                checkAnswer(userInput)
            } else {
                println("Неверный ввод. Пожалуйста, введите номер ответа.")
            }
        }
    }

    private fun presentQuestion(question: Question) {
        println("Слово: ${question.correctAnswer.original}")
        question.variants.forEachIndexed { index, word ->
            println("${index + 1} – ${word.translate}")
        }
    }

    fun checkAnswer(userInput: Int): Boolean {
        val question = currentQuestion ?: return false
        val answerIndex = userInput - 1

        return if (answerIndex in question.variants.indices) {
            if (question.variants[answerIndex].translate == question.correctAnswer.translate) {
                println("Правильно!")
                question.correctAnswer.correctAnswersCount++
                saveDictionary()
                true
            } else {
                println("Неправильно! Правильный ответ: ${question.correctAnswer.translate}.")
                false
            }
        } else {
            println("Неверный ввод. Введите номер ответа или 0 для возврата в меню.")
            false
        }
    }
}
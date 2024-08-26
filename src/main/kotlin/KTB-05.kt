package org.example

fun main() {
    while (true) {
        println("Меню:")
        println("1 – Учить слова")
        println("2 – Статистика")
        println("0 – Выход")

        print("Введите номер меню: ")
        val userInput = readln()

        when (userInput) {
            "1" -> println("Вы выбрали 'Учить слова'")
            "2" -> println("Вы выбрали 'Статистика'")
            "0" -> return

            else -> println("Неверный ввод. Введите 1, 2 или 0")
        }
    }
}
package com.example.coffeeshop.utils

import java.text.Normalizer

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex() // dùng Normalizer + regex để bỏ dấu. [web:973][web:975]

fun String.unaccentLower(): String {
    val nfd = Normalizer.normalize(this, Normalizer.Form.NFD)
    val noAccent = REGEX_UNACCENT.replace(nfd, "")
    return noAccent.lowercase()
}

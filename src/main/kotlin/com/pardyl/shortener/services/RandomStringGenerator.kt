package com.pardyl.shortener.services

import java.lang.StringBuilder
import java.util.concurrent.ThreadLocalRandom
import org.springframework.stereotype.Service

@Service
class RandomStringGenerator {
    companion object {
        private const val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val lower = upper.toLowerCase()
        private const val digits = "1234567890"
        private val alphabet = upper + lower + digits
    }

    fun randomAlphanumeric(length: Int): String {
        val builder = StringBuilder(length)
        ThreadLocalRandom.current().ints(0, alphabet.length).limit(length.toLong())
            .forEach { builder.append(alphabet[it]) }
        return builder.toString()
    }
}

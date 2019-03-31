package com.pardyl.shortener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ShortenerApplication

fun main(args: Array<String>) {
    runApplication<ShortenerApplication>(*args)
}

package com.bestbidder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BestBidderApplication

fun main(args: Array<String>) {
    runApplication<BestBidderApplication>(*args)
}
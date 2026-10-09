package com.availelabs.tabla

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TablaApplication

fun main(args: Array<String>) {
    runApplication<TablaApplication>(*args)
}

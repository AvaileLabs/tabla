package com.availelabs.tabla

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TablaServerApplication

fun main(args: Array<String>) {
	runApplication<TablaServerApplication>(*args)
}

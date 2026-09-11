package com.availelabs.tabla

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class SearchQuery(
    val ticker: String
)

data class EarningsResponse(
    val ticker: String
)

@RestController
class SearchController {

    @GetMapping("/api/earnings")
    fun search(
        @RequestParam ticker: String,
    ): EarningsResponse {
        TODO()
    }
}
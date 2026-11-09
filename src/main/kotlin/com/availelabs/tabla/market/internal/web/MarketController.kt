package com.availelabs.tabla.market.internal.web

import com.availelabs.tabla.market.internal.FundResult
import com.availelabs.tabla.market.internal.ListingResult
import com.availelabs.tabla.market.internal.MarketQueries
import kotlinx.collections.immutable.ImmutableList
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/market")
class MarketController(private val queries: MarketQueries) {
    @GetMapping("/listings")
    fun listings(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) etf: Boolean?
    ): ImmutableList<ListingResult> = queries.listings(query, limit, etf)

    @GetMapping("/fund-share-classes")
    fun funds(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "20") limit: Int
    ): ImmutableList<FundResult> = queries.funds(query, limit)

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidQuery(exception: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message ?: "Invalid market query")
}

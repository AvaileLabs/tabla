package com.availelabs.tabla.market.internal.web

import com.availelabs.tabla.market.internal.FundResult
import com.availelabs.tabla.market.internal.ListingResult
import com.availelabs.tabla.market.internal.MarketQueries
import kotlinx.collections.immutable.ImmutableList
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/market", version = "1")
class MarketController(private val queries: MarketQueries) {

    @GetMapping("/listings")
    @McpTool(
        description = "Search Nasdaq directory listings by symbol or name using case-insensitive literal text. " +
                "Results are ordered by Nasdaq symbol and exclude prices.",
        generateOutputSchema = true,
        annotations = McpTool.McpAnnotations(
            readOnlyHint = true,
            openWorldHint = false
        )
    )
    fun searchListings(
        @RequestParam
        @McpToolParam(description = "Search text, at most 200 characters; omit to browse", required = false)
        query: String?,

        @RequestParam
        @McpToolParam(description = "Maximum results (1–100); defaults to 20", required = false)
        limit: Int?,

        @RequestParam
        @McpToolParam(description = "True for ETFs, false for other securities; omit for both", required = false)
        etf: Boolean?
    ): ImmutableList<ListingResult> =
        queries.listings(query, limit ?: 20, etf)

    @GetMapping("/fund-share-classes")
    @McpTool(
        description = "Search SEC fund share classes by ticker, series ID or class ID using case-insensitive literal text. " +
                "Includes mutual funds and ETFs without distinguishing between them. " +
                "Results are ordered by class ID and exclude fund names and prices.",
        generateOutputSchema = true,
        annotations = McpTool.McpAnnotations(
            readOnlyHint = true,
            openWorldHint = false
        )
    )
    fun searchFundShareClasses(
        @RequestParam
        @McpToolParam(description = "Search text, at most 200 characters; omit to browse", required = false)
        query: String?,

        @RequestParam
        @McpToolParam(description = "Maximum results (1–100); defaults to 20", required = false)
        limit: Int?
    ): ImmutableList<FundResult> =
        queries.funds(query, limit ?: 20)

    @ExceptionHandler(IllegalArgumentException::class)
    fun invalidQuery(exception: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            exception.message ?: "Invalid market query"
        )
}

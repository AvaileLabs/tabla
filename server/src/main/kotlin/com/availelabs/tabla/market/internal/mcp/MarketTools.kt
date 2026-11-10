package com.availelabs.tabla.market.internal.mcp

import com.availelabs.tabla.market.internal.FundResult
import com.availelabs.tabla.market.internal.ListingResult
import com.availelabs.tabla.market.internal.MarketQueries
import kotlinx.collections.immutable.ImmutableList
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component

@Component
class MarketTools(private val queries: MarketQueries) {
    @McpTool(
        name = "search_listings",
        generateOutputSchema = true,
        description = "Search Nasdaq directory listings by symbol or name using case-insensitive literal text. " +
                "Use etf=true for ETFs or etf=false for non-ETF listings (stocks and other securities). " +
                "Returns the first matches ordered by Nasdaq symbol. No prices.",
        annotations = McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false)
    )
    fun searchListings(
        @McpToolParam(
            description = "Search text, at most 200 characters; omit to browse",
            required = false
        ) query: String?,
        @McpToolParam(description = "Maximum results, 1-100; default 20", required = false) limit: Int?,
        @McpToolParam(description = "true for ETFs, false for non-ETFs; omit for both", required = false) etf: Boolean?
    ): ImmutableList<ListingResult> = queries.listings(query, limit ?: 20, etf)

    @McpTool(
        name = "search_fund_share_classes",
        generateOutputSchema = true,
        description = "Search SEC fund share classes by ticker, series ID or class ID using case-insensitive literal text. " +
                "Includes mutual funds and ETFs; the source does not provide fund names or distinguish the two. " +
                "Returns the first matches ordered by class ID. No prices.",
        annotations = McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, openWorldHint = false)
    )
    fun searchFunds(
        @McpToolParam(
            description = "Search text, at most 200 characters; omit to browse",
            required = false
        ) query: String?,
        @McpToolParam(description = "Maximum results, 1-100; default 20", required = false) limit: Int?
    ): ImmutableList<FundResult> = queries.funds(query, limit ?: 20)
}

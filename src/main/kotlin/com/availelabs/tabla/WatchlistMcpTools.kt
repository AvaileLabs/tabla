package com.availelabs.tabla

import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.stereotype.Service

@Service
class WatchlistMcpTools(
    private val watchlistService: WatchlistService
) {
    @McpTool(
        name = "add_stock_to_watchlist",
        description = "Adds a stock, ADR, or ETF to the user's watchlist."
    )
    fun addStock(stock: Stock): StockWatchlist.AddStockResult {
        return watchlistService.addStock(stock)
    }

    @McpTool(
        name = "remove_stock_from_watchlist",
        description = "Removes a stock, ADR, or ETF from the user's watchlist."
    )
    fun removeStock(stock: Stock): StockWatchlist.RemoveStockResult {
        return watchlistService.removeStock(stock)
    }
}
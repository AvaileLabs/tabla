package com.availelabs.tabla.watchlist

import com.availelabs.tabla.Stock
import com.availelabs.tabla.StockId

class StockWatchlist {
    enum class AddStockResult {
        ADDED,
        INVALID_TICKER,
        ALREADY_EXISTS
    }

    enum class RemoveStockResult {
        REMOVED,
        NOT_FOUND
    }

    val stocks: List<Stock>
        field = mutableListOf()

    private fun isTickerValid(ticker: String): Boolean {
        TODO()
    }

    fun add(stock: Stock): AddStockResult {
        val normalizedStock = stock.normalize()

        if (stocks.any { it.id == normalizedStock.id }) return AddStockResult.ALREADY_EXISTS

        if (!isTickerValid(normalizedStock.profile.ticker)) return AddStockResult.INVALID_TICKER
        stocks.add(normalizedStock)
        return AddStockResult.ADDED
    }

    fun remove(id: StockId): RemoveStockResult {
        val removed = stocks.removeIf { it.id == id }
        return if (removed) RemoveStockResult.REMOVED else RemoveStockResult.NOT_FOUND
    }
}
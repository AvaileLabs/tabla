package com.availelabs.tabla

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

        if (stocks.contains(normalizedStock)) return AddStockResult.ALREADY_EXISTS

        if (!isTickerValid(normalizedStock.ticker)) return AddStockResult.INVALID_TICKER
        stocks.add(normalizedStock)
        return AddStockResult.ADDED
    }

    fun remove(stock: Stock): RemoveStockResult {
        if (!stocks.contains(stock)) return RemoveStockResult.NOT_FOUND
        stocks.remove(stock)
        return RemoveStockResult.REMOVED
    }
}
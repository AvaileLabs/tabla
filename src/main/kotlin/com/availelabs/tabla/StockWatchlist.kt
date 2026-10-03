package com.availelabs.tabla

data class Stock(
    val ticker: String,
    val name: String
)

class StockWatchlist {
    val stocks: List<Stock>
        field = mutableListOf()

    private fun isTickerValid(ticker: String): Boolean {
        TODO()
    }

    fun add(stock: Stock) {
        val normalizedStock = stock.copy(
            ticker = stock.ticker.trim().uppercase(),
            name = stock.name.trim()
        )

        if (!isTickerValid(normalizedStock.ticker)) return
        stocks.add(normalizedStock)
    }

    fun remove(stock: Stock) {
        stocks.remove(stock)
    }
}
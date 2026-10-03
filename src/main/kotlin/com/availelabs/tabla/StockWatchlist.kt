package com.availelabs.tabla

enum class InstrumentType { COMMON_STOCK, ADR, ETF }

data class StockId(val ticker: String, val listingExchange: String)

data class Stock(
    val ticker: String,
    val name: String,
    val listingExchange: String,
    val instrumentType: InstrumentType
) {
    val id: StockId get() = StockId(ticker, listingExchange)
}

class StockWatchlist {
    val stocks: List<Stock>
        field = mutableListOf()

    private fun isTickerValid(ticker: String): Boolean {
        TODO()
    }

    fun add(stock: Stock) {
        val normalizedStock = stock.copy(
            ticker = stock.ticker.trim().uppercase(),
            name = stock.name.trim(),
            listingExchange = stock.listingExchange
        )

        if (!isTickerValid(normalizedStock.ticker)) return
        stocks.add(normalizedStock)
    }

    fun remove(stock: Stock) {
        stocks.remove(stock)
    }
}
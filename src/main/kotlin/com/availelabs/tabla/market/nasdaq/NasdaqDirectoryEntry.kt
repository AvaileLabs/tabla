package com.availelabs.tabla.market.nasdaq

value class NasdaqDirectoryEntry private constructor(
    val symbols: NasdaqSymbols,
    val name: String,
    val exchange: ListingExchange,
    val nasdaqTraded: Boolean,
    val etf: Boolean,
    val roundLotSize: Int,
    val marketCategory: Char?,
    val financialStatus: Char?,
    val nextShares: Boolean
) {
    init {
        require(name.isNotBlank() && name == name.trim()) {
            "Name must be non-empty and trimmed"
        }
        require(roundLotSize > 0) { "Round lot size must be positive" }
    }

    companion {
        fun of(
            symbols: NasdaqSymbols,
            name: String,
            exchange: ListingExchange,
            nasdaqTraded: Boolean,
            etf: Boolean,
            roundLotSize: Int,
            marketCategory: Char?,
            financialStatus: Char?,
            nextShares: Boolean
        ): NasdaqDirectoryEntry =
            NasdaqDirectoryEntry(
                symbols = symbols,
                name = name.trim(),
                exchange = exchange,
                nasdaqTraded = nasdaqTraded,
                etf = etf,
                roundLotSize = roundLotSize,
                marketCategory = marketCategory?.takeUnless { it.isWhitespace() },
                financialStatus = financialStatus?.takeUnless { it.isWhitespace() },
                nextShares = nextShares
            )
    }
}

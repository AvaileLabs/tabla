package com.availelabs.tabla.market

value class SecurityListing private constructor(
    val cik: Int,
    val symbols: SecuritySymbols,
    val name: String,
    val exchange: Exchange
) {
    init {
        require(cik > 0) { "CIK must be a positive integer" }
        require(name.isNotBlank() && name == name.trim()) {
            "Name must be non-empty and trimmed"
        }
    }

    companion {
        fun of(
            cik: Int,
            symbols: SecuritySymbols,
            name: String,
            exchange: Exchange
        ): SecurityListing =
            SecurityListing(
                cik = cik,
                symbols = symbols,
                name = name.trim(),
                exchange = exchange
            )
    }
}

value class SecuritySymbols private constructor(
    val symbol: String,
    val nasdaqSymbol: String,
    val cqsSymbol: String?,
    val edgarSymbol: String
) {
    init {
        require(symbol.isNormalizedSymbol()) {
            "Symbol must be non-empty and normalized"
        }
        require(nasdaqSymbol.isNormalizedSymbol()) {
            "NASDAQ symbol must be non-empty and normalized"
        }
        require(cqsSymbol == null || cqsSymbol.isNormalizedSymbol()) {
            "CQS symbol must be normalized when present"
        }
        require(edgarSymbol.isNormalizedSymbol()) {
            "EDGAR symbol must be non-empty and normalized"
        }
    }

    companion {
        fun of(
            symbol: String,
            nasdaqSymbol: String,
            cqsSymbol: String?,
            edgarSymbol: String
        ): SecuritySymbols =
            SecuritySymbols(
                symbol = symbol.trim().uppercase(),
                nasdaqSymbol = nasdaqSymbol.trim().uppercase(),
                cqsSymbol = cqsSymbol
                    ?.trim()
                    ?.uppercase()
                    ?.takeIf { it.isNotEmpty() },
                edgarSymbol = edgarSymbol.trim().uppercase()
            )
    }

    private fun String.isNormalizedSymbol(): Boolean =
        isNotEmpty() && this == trim().uppercase()
}

enum class Exchange {
    NASDAQ,
    NYSE_AMERICAN,
    NYSE,
    NYSE_ARCA,
    CBOE_BZX,
    IEX,
    TXSE,
    NYSE_TEXAS
}

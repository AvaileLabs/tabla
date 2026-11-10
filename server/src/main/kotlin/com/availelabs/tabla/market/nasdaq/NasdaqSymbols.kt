package com.availelabs.tabla.market.nasdaq

value class NasdaqSymbols private constructor(
    val symbol: String,
    val nasdaqSymbol: String,
    val cqsSymbol: String?
) {
    init {
        require(symbol.isNormalizedSymbol()) {
            "Symbol must be non-empty and normalized"
        }
        require(nasdaqSymbol.isNormalizedSymbol()) {
            "NASDAQ symbol must be non-empty and normalized"
        }
        require(cqsSymbol == null || (cqsSymbol.isNotBlank() && cqsSymbol == cqsSymbol.trim())) {
            "CQS symbol must be non-empty and trimmed when present"
        }
    }

    companion {
        fun of(
            symbol: String,
            nasdaqSymbol: String,
            cqsSymbol: String?
        ): NasdaqSymbols =
            NasdaqSymbols(
                symbol = symbol.trim().uppercase(),
                nasdaqSymbol = nasdaqSymbol.trim().uppercase(),
                cqsSymbol = cqsSymbol
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
            )
    }

    private fun String.isNormalizedSymbol(): Boolean =
        isNotEmpty() && this == trim().uppercase()
}

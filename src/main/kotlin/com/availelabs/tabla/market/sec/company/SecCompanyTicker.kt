package com.availelabs.tabla.market.sec.company

value class SecCompanyTicker private constructor(
    val cik: Int,
    val name: String,
    val symbol: String,
    val exchange: String?
) {
    init {
        require(cik > 0) { "CIK must be a positive integer" }
        require(name.isNotBlank() && name == name.trim()) {
            "Name must be non-empty and trimmed"
        }
        require(symbol.isNotEmpty() && symbol == symbol.trim().uppercase()) {
            "Symbol must be non-empty and normalized"
        }
        require(exchange == null || (exchange.isNotBlank() && exchange == exchange.trim())) {
            "Exchange must be non-empty and trimmed when present"
        }
    }

    companion {
        fun of(cik: Int, name: String, symbol: String, exchange: String?): SecCompanyTicker =
            SecCompanyTicker(
                cik = cik,
                name = name.trim(),
                symbol = symbol.trim().uppercase(),
                exchange = exchange?.trim()?.takeIf { it.isNotEmpty() }
            )
    }
}

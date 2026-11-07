package com.availelabs.tabla.market

value class FundShareClass private constructor(
    val cik: Int,
    val seriesId: String,
    val classId: String,
    val symbol: String
) {
    init {
        require(cik > 0) {
            "CIK must be a positive integer"
        }
        require(SERIES_ID.matches(seriesId)) {
            "Invalid fund series ID"
        }
        require(CLASS_ID.matches(classId)) {
            "Invalid fund class ID"
        }
        require(
            symbol.isNotEmpty() &&
                    symbol == symbol.trim().uppercase()
        ) {
            "Symbol must be non-empty and normalized"
        }
    }

    companion {
        private val SERIES_ID = Regex("S[0-9]{9}")
        private val CLASS_ID = Regex("C[0-9]{9}")

        fun of(
            cik: Int,
            seriesId: String,
            classId: String,
            symbol: String
        ): FundShareClass =
            FundShareClass(
                cik = cik,
                seriesId = seriesId.trim().uppercase(),
                classId = classId.trim().uppercase(),
                symbol = symbol.trim().uppercase()
            )
    }
}

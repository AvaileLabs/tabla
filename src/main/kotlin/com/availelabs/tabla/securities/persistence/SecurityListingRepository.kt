package com.availelabs.tabla.securities.persistence

import com.availelabs.tabla.Etf
import com.availelabs.tabla.securities.persistence.dto.SecurityListingInput
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class SecurityListingRepository(
    private val sqlClient: KSqlClient
) {

    @Transactional
    fun insertAll(etfs: List<Etf>) {
        val inputs = etfs.map { etf ->
            val symbols = etf.listing.value.symbols

            SecurityListingInput(
                id = etf.instrumentId.value,
                symbol = symbols.symbol,
                nasdaqSymbol = symbols.nasdaqSymbol,
                cqsSymbol = symbols.cqsSymbol
            )
        }

        sqlClient.saveInputs(inputs, SaveMode.INSERT_ONLY)
    }
}
package com.availelabs.tabla.market.sec.fund.internal

import com.availelabs.tabla.market.sec.fund.FundShareClass
import com.availelabs.tabla.market.sec.fund.FundShareClassWriter
import com.availelabs.tabla.market.sec.fund.internal.dto.FundShareClassInput
import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class FundShareClassRepository(private val sqlClient: KSqlClient) : FundShareClassWriter {
    @Transactional
    override fun replaceAll(snapshot: Sourced<ImmutableList<FundShareClass>>) {
        val funds = snapshot.value
        require(funds.isNotEmpty()) { "Fund share classes must not be empty" }
        require(funds.distinctBy { it.classId }.size == funds.size) {
            "Fund share classes must have distinct class IDs"
        }

        val source = snapshot.sourceInfo
        val inputs = funds.map { fund ->
            FundShareClassInput(
                classId = fund.classId,
                cik = fund.cik,
                seriesId = fund.seriesId,
                symbol = fund.symbol,
                sourceUri = source.uri,
                retrievedAt = source.retrievedAt
            )
        }

        sqlClient.createDelete(FundShareClassEntity::class) {}.execute()
        sqlClient.saveInputs(inputs, SaveMode.INSERT_ONLY)
    }
}

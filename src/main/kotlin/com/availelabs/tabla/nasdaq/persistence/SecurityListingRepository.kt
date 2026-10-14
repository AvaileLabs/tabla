package com.availelabs.tabla.nasdaq.persistence

import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Instant

@Repository
class SecurityListingRepository(
    private val sqlClient: KSqlClient
) {
    @Transactional
    fun replaceNasdaqDirectory(directory: NasdaqDirectoryEntity) {
        sqlClient.save(directory.copy { id = 1 }) {
            setMode(SaveMode.UPSERT)
            setAssociatedMode(NasdaqDirectoryEntity::entries, AssociatedSaveMode.VIOLENTLY_REPLACE)
            setDissociateAction(NasdaqDirectoryEntryEntity::directory, DissociateAction.DELETE)
        }
    }

    // Returns null if the Nasdaq directory has never been persisted.
    fun getRetrievalTimeOfNasdaqDirectoryOrNull(): Instant? =
        sqlClient.createQuery(NasdaqDirectoryEntity::class) {
            where(table.id eq 1)
            select(table.retrievedAt)
        }.fetchOneOrNull()
}

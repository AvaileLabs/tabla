package com.availelabs.tabla.securities.persistence

import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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
}

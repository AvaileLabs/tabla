package com.availelabs.tabla.persistence.snapshot

import com.availelabs.tabla.Sourced
import com.availelabs.tabla.persistence.snapshot.dto.SourceSnapshotInput
import com.availelabs.tabla.sourcedBy
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.modulith.NamedInterface
import org.springframework.stereotype.Repository
import java.net.URI
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Repository
@NamedInterface
class SourceSnapshotRepository(
    private val sqlClient: KSqlClient
) {

    @OptIn(ExperimentalUuidApi::class)
    fun save(snapshot: Sourced<ByteArray>) =
        sqlClient.save(
            SourceSnapshotInput(
                id = Uuid.generateV7(),
                sourceUri = snapshot.sourceInfo.uri,
                retrievedAt = snapshot.sourceInfo.retrievedAt,
                content = snapshot.value
            )
        ) { setMode(SaveMode.INSERT_ONLY) }

    fun latestRetrievalTimeOf(sourceUri: URI): Instant? =
        sqlClient.createQuery(SourceSnapshotEntity::class) {
            where(table.sourceUri eq sourceUri)
            orderBy(table.retrievedAt.desc())
            select(table.retrievedAt)
        }.limit(1).execute().firstOrNull()

    fun latestOf(sourceUri: URI): Sourced<ByteArray>? =
        sqlClient.createQuery(SourceSnapshotEntity::class) {
            where(table.sourceUri eq sourceUri)
            orderBy(table.retrievedAt.desc())
            select(table)
        }.limit(1).execute().firstOrNull()
            ?.let {
                it.content.sourcedBy(uri = it.sourceUri, retrievedAt = it.retrievedAt)
            }
}
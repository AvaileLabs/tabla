package com.availelabs.tabla.persistence.snapshot

import com.availelabs.tabla.SnapshotSource
import com.availelabs.tabla.SourceFile
import com.availelabs.tabla.Sourced
import com.availelabs.tabla.persistence.snapshot.dto.SourceSnapshotInput
import com.availelabs.tabla.sourcedBy
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.rowCount
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
    fun <T : SourceFile> save(source: SnapshotSource<T>, snapshot: Sourced<T>) {
        require(snapshot.sourceInfo.uri == source.uri) {
            "Snapshot URI ${snapshot.sourceInfo.uri} does not match source URI ${source.uri}"
        }

        sqlClient.save(
            SourceSnapshotInput(
                id = Uuid.generateV7(),
                sourceUri = source.uri,
                retrievedAt = snapshot.sourceInfo.retrievedAt,
                content = snapshot.value.bytes
            )
        ) { setMode(SaveMode.INSERT_ONLY) }
    }

    fun latestRetrievalTimeOf(source: SnapshotSource<*>): Instant? =
        sqlClient.createQuery(SourceSnapshotEntity::class) {
            where(table.sourceUri eq source.uri)
            orderBy(table.retrievedAt.desc())
            select(table.retrievedAt)
        }.limit(1).execute().firstOrNull()

    fun <T : SourceFile> latestOf(source: SnapshotSource<T>): Sourced<T>? =
        sqlClient.createQuery(SourceSnapshotEntity::class) {
            where(table.sourceUri eq source.uri)
            orderBy(table.retrievedAt.desc())
            select(table)
        }.limit(1).execute().firstOrNull()
            ?.let {
                it.content.sourcedBy(source = source, retrievedAt = it.retrievedAt)
            }

    fun countOf(sourceUri: URI): Long =
        sqlClient.createQuery(SourceSnapshotEntity::class) {
            where(table.sourceUri eq sourceUri)
            select(rowCount())
        }.execute().single()

    fun sourceUris(): List<URI> =
        sqlClient.createQuery(SourceSnapshotEntity::class) {
            select(table.sourceUri)
        }.distinct().execute()

    fun retainLatestOf(sourceUri: URI, keep: Int) {
        require(keep >= 1)

        val excess = countOf(sourceUri) - keep
        if (excess <= 0) return

        val oldestIds = sqlClient.createQuery(SourceSnapshotEntity::class) {
            where(table.sourceUri eq sourceUri)
            orderBy(table.retrievedAt.asc(), table.id.asc())
            select(table.id)
        }.limit(excess.toInt()).execute()

        sqlClient.deleteByIds(SourceSnapshotEntity::class, oldestIds)
    }
}
package com.availelabs.tabla.ingestion.snapshot

import com.availelabs.tabla.ingestion.APP_TIME_ZONE
import com.availelabs.tabla.ingestion.snapshot.dto.SourceSnapshotInput
import com.availelabs.tabla.ingestion.source.SnapshotSource
import com.availelabs.tabla.ingestion.source.SourceFile
import com.availelabs.tabla.ingestion.source.sourcedBy
import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.asc
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.rowCount
import org.springframework.stereotype.Repository
import java.net.URI
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Repository
class SourceSnapshotRepository(
    private val sqlClient: KSqlClient
) {

    fun <T : SourceFile> save(source: SnapshotSource<T>, snapshot: Sourced<T>) {
        require(snapshot.sourceInfo.uri == source.uri) {
            "Snapshot URI ${snapshot.sourceInfo.uri} does not match source URI ${source.uri}"
        }

        sqlClient.save(
            SourceSnapshotInput(
                id = Uuid.generateV7(),
                sourceUri = source.uri,
                retrievedAt = snapshot.sourceInfo.retrievedAt,
                content = snapshot.value.bytes.toByteArray()
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

    fun sourceUris(): ImmutableList<URI> =
        sqlClient.createQuery(SourceSnapshotEntity::class) {
            select(table.sourceUri)
        }.distinct().execute().toImmutableList()

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

    fun wasRetrievedToday(
        source: SnapshotSource<*>,
        zone: TimeZone = APP_TIME_ZONE
    ): Boolean =
        latestRetrievalTimeOf(source)
            ?.toLocalDateTime(zone)?.date == Clock.System.todayIn(zone)
}

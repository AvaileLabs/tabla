package com.availelabs.tabla.persistence.snapshot

import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table
import java.net.URI
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity
@Table(name = "source_snapshot")
interface SourceSnapshotEntity {
    @Id
    val id: Uuid
    val sourceUri: URI
    val retrievedAt: Instant
    val content: ByteArray
}
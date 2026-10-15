package com.availelabs.tabla.nasdaq.persistence

import kotlinx.datetime.LocalDateTime
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import java.net.URI
import kotlin.time.Instant

@Entity
@Table(name = "nasdaq_traded_security_directory")
interface NasdaqDirectoryEntity {

    @Id
    val id: Int

    val sourceUri: URI

    val retrievedAt: Instant

    val fileCreatedAt: LocalDateTime

    @OneToMany(mappedBy = "directory")
    val entries: List<NasdaqDirectoryEntryEntity>
}

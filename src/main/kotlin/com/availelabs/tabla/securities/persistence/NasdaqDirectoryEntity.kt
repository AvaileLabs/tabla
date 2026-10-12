package com.availelabs.tabla.securities.persistence

import kotlinx.datetime.LocalDateTime
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table
import kotlin.time.Instant

@Entity
@Table(name = "nasdaq_directory")
interface NasdaqDirectoryEntity {

    @Id
    val id: Int

    val sourceUri: String

    val retrievedAt: Instant

    val fileCreatedAt: LocalDateTime

    @OneToMany(mappedBy = "directory")
    val entries: List<NasdaqDirectoryEntryEntity>
}

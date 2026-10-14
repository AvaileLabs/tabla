package com.availelabs.tabla.nasdaq.persistence

import org.babyfish.jimmer.sql.*
import kotlin.uuid.Uuid

@Entity
@Table(name = "nasdaq_traded_security_directory_entry")
interface NasdaqDirectoryEntryEntity {

    @Id
    val id: Uuid

    @ManyToOne
    @JoinColumn(name = "directory_id")
    val directory: NasdaqDirectoryEntity

    val security: NasdaqTradedSecurity
}
package com.availelabs.tabla.ingestion.source

@JvmInline
value class EdgarCompanyTickersRawFile(
    override val bytes: ByteArray
) : SourceFile

@JvmInline
value class EdgarFundTickersRawFile(
    override val bytes: ByteArray
) : SourceFile

@JvmInline
value class NasdaqDirectoryRawFile(
    override val bytes: ByteArray
) : SourceFile

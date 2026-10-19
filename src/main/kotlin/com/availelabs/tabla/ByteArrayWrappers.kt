package com.availelabs.tabla

@JvmInline
value class EdgarCompanyTickersRawFile internal constructor(
    override val bytes: ByteArray
) : SourceFile

@JvmInline
value class EdgarFundTickersRawFile internal constructor(
    override val bytes: ByteArray
) : SourceFile

@JvmInline
value class NasdaqDirectoryRawFile internal constructor(
    override val bytes: ByteArray
) : SourceFile


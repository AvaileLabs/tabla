package com.availelabs.tabla.ingestion.internal.source

import kotlinx.io.bytestring.ByteString

value class EdgarCompanyTickersRawFile(
    override val bytes: ByteString
) : SourceFile

value class EdgarFundTickersRawFile(
    override val bytes: ByteString
) : SourceFile

value class NasdaqDirectoryRawFile(
    override val bytes: ByteString
) : SourceFile

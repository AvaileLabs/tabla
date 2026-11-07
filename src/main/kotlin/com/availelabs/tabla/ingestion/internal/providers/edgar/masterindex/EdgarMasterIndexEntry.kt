package com.availelabs.tabla.ingestion.internal.providers.edgar.masterindex

import kotlinx.datetime.LocalDate
import org.jetbrains.kotlinx.dataframe.annotations.ColumnName
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema

@DataSchema
interface EdgarMasterIndexEntry {

    @ColumnName("CIK")
    val cik: String

    @ColumnName("Company Name")
    val companyName: String

    @ColumnName("Form Type")
    val formType: String

    @ColumnName("Date Filed")
    val dateFiled: LocalDate

    @ColumnName("Filename")
    val filename: String
}

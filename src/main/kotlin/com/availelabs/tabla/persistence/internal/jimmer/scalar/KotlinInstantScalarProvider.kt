package com.availelabs.tabla.persistence.internal.jimmer.scalar

import org.babyfish.jimmer.sql.runtime.ScalarProvider
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Component
class KotlinInstantScalarProvider : ScalarProvider<Instant, OffsetDateTime> {

    override fun toScalar(sqlValue: OffsetDateTime): Instant =
        sqlValue.toInstant().toKotlinInstant()

    override fun toSql(scalarValue: Instant): OffsetDateTime =
        scalarValue.toJavaInstant().atOffset(ZoneOffset.UTC)
}

package com.availelabs.tabla.persistence.internal.jimmer.scalar

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import org.springframework.stereotype.Component
import java.time.LocalDateTime as JavaLocalDateTime

@Component
class KotlinLocalDateTimeScalarProvider : ScalarProvider<LocalDateTime, JavaLocalDateTime> {

    override fun toScalar(sqlValue: JavaLocalDateTime): LocalDateTime =
        sqlValue.toKotlinLocalDateTime()

    override fun toSql(scalarValue: LocalDateTime): JavaLocalDateTime =
        scalarValue.toJavaLocalDateTime()
}

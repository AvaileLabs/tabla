package com.availelabs.tabla.persistence.jimmer.scalar

import org.babyfish.jimmer.sql.runtime.ScalarProvider
import org.springframework.stereotype.Component
import java.util.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@Component
class KotlinUuidScalarProvider : ScalarProvider<Uuid, UUID> {

    override fun toScalar(sqlValue: UUID): Uuid =
        sqlValue.toKotlinUuid()

    override fun toSql(scalarValue: Uuid): UUID =
        scalarValue.toJavaUuid()
}
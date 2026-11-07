package com.availelabs.tabla.persistence.internal.jimmer.scalar

import kotlinx.io.bytestring.ByteString
import org.babyfish.jimmer.sql.runtime.ScalarProvider
import org.springframework.stereotype.Component

@Component
class ByteStringScalarProvider : ScalarProvider<ByteString, ByteArray> {

    override fun toScalar(sqlValue: ByteArray): ByteString =
        ByteString(sqlValue)

    override fun toSql(scalarValue: ByteString): ByteArray =
        scalarValue.toByteArray()
}

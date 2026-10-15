package com.availelabs.tabla.persistence.jimmer.scalar

import org.babyfish.jimmer.sql.runtime.ScalarProvider
import org.springframework.stereotype.Component
import java.net.URI

@Component
class UriScalarProvider : ScalarProvider<URI, String> {

    override fun toScalar(sqlValue: String): URI =
        URI.create(sqlValue)

    override fun toSql(scalarValue: URI): String =
        scalarValue.toString()
}

package com.availelabs.tabla

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.modulith.core.ApplicationModules

@SpringBootTest
@Import(PostgresTestConfiguration::class)
class TablaApplicationTests {

    @Test
    fun contextLoads() {
    }
}

class ModularityTests {

    @Test
    fun verifiesModularity() {
        ApplicationModules.of(TablaApplication::class.java).verify()
    }
}

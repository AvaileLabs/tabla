package com.availelabs.tabla

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.modulith.core.ApplicationModules
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(properties = ["tabla.edgar.user-agent=Tabla tests test@example.com"])
@Import(PostgresTestConfiguration::class)
class TablaApplicationTests {

    @MockitoBean(enforceOverride = true)
    private lateinit var lifecycle: ApplicationLifecycle

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

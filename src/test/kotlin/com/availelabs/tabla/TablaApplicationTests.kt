package com.availelabs.tabla

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.modulith.core.ApplicationModules

@SpringBootTest
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

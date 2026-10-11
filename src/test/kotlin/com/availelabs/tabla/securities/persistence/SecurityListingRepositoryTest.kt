package com.availelabs.tabla.securities.persistence

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@Transactional
class SecurityListingRepositoryTest @Autowired constructor(
    private val repository: SecurityListingRepository,
    private val jdbcTemplate: JdbcTemplate
) {

}
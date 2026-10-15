package com.availelabs.tabla.nasdaq.persistence

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional


@SpringBootTest
@Transactional
class NasdaqListingRepositoryTest @Autowired constructor(
    private val repository: NasdaqListingRepository,
    private val jdbcTemplate: JdbcTemplate
) {

}
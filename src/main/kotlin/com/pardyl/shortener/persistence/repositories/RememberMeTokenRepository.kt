package com.pardyl.shortener.persistence.repositories

import com.pardyl.shortener.persistence.entities.RememberMeToken
import org.springframework.data.jpa.repository.JpaRepository

interface RememberMeTokenRepository : JpaRepository<RememberMeToken, String> {
    fun findByUserName(userName: String): List<RememberMeToken>
}

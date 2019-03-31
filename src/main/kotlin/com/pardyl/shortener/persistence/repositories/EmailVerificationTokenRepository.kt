package com.pardyl.shortener.persistence.repositories

import com.pardyl.shortener.persistence.entities.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    fun findByToken(token: String): EmailVerificationToken?
}

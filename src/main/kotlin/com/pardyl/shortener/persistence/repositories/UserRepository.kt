package com.pardyl.shortener.persistence.repositories

import com.pardyl.shortener.persistence.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByUserName(username: String): User?

    fun findByEmail(email: String): User?
}

package com.pardyl.shortener.persistence.repositories

import com.pardyl.shortener.persistence.entities.Link
import com.pardyl.shortener.persistence.entities.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface LinkRepository : JpaRepository<Link, Long> {
    fun findByName(name: String): Link?

    fun findByOwner(user: User, pageable: Pageable): Page<Link>
}

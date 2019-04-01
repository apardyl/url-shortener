package com.pardyl.shortener.persistence.repositories

import com.pardyl.shortener.persistence.entities.Link
import com.pardyl.shortener.persistence.entities.User
import javax.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface LinkRepository : JpaRepository<Link, Long> {
    fun findByName(name: String): Link?

    fun findByOwner(user: User, pageable: Pageable): Page<Link>

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Modifying
    @Query("update Link l set l.visited = l.visited + 1 where l.id = ?1")
    fun incrementVisited(id: Long)
}

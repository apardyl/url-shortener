package com.pardyl.shortener.persistence.entities

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
data class Link(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long?,

    @Column(unique = true, nullable = false)
    var name: String?,

    @Column(nullable = false)
    var url: String?,

    @ManyToOne(optional = false)
    var owner: User?,

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var created: Date?
)

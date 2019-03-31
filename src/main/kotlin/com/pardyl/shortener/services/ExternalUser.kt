package com.pardyl.shortener.services

import com.pardyl.shortener.persistence.entities.Role

data class ExternalUser(
    var userName: String,

    var email: String?,

    var firstName: String?,

    var lastName: String?,

    var role: Role = Role.NOBODY
)

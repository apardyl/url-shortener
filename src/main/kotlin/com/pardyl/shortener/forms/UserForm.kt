package com.pardyl.shortener.forms

import com.pardyl.shortener.persistence.entities.Role

data class UserForm(
    val id: Long?,

    val userName: String,

    val email: String?,

    val password: String?,

    val password2: String?,

    val firstName: String?,

    val lastName: String?,

    val enabled: Boolean,

    val role: Role
)

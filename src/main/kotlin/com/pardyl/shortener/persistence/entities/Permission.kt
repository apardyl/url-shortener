package com.pardyl.shortener.persistence.entities

import org.springframework.security.core.GrantedAuthority

enum class Permission : GrantedAuthority {
    ROLE_SHORTEN,
    ROLE_MANAGE_LINKS,
    ROLE_MANAGE_USERS;

    companion object {
        // Compile time constants for annotations
        const val ROLE_SHORTEN_STR = "ROLE_SHORTEN"
        const val ROLE_MANAGE_LINKS_STR = "ROLE_MANAGE_LINKS"
        const val MANAGE_USERS_STR = "ROLE_MANAGE_USERS"
    }

    override fun getAuthority(): String {
        return name
    }
}

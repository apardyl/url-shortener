package com.pardyl.shortener.persistence.entities

enum class Role(val permissions: List<Permission>) {
    NOBODY(listOf()),
    USER(listOf(Permission.ROLE_SHORTEN)),
    MOD(listOf(Permission.ROLE_SHORTEN, Permission.ROLE_MANAGE_LINKS)),
    ADMIN(listOf(Permission.ROLE_SHORTEN, Permission.ROLE_MANAGE_LINKS, Permission.ROLE_MANAGE_USERS)),
    EXTERNAL(listOf())
}

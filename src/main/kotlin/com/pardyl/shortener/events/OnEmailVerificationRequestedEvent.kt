package com.pardyl.shortener.events

import com.pardyl.shortener.persistence.entities.User
import org.springframework.context.ApplicationEvent

data class OnEmailVerificationRequestedEvent(
    val user: User
) : ApplicationEvent(user)

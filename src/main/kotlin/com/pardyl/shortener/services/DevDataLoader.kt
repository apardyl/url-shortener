package com.pardyl.shortener.services

import com.pardyl.shortener.persistence.entities.Role
import com.pardyl.shortener.persistence.entities.User
import com.pardyl.shortener.persistence.repositories.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DevDataLoader(
    @Value("\${shortener.enable_dev_data_loader:false}") private val enabled: Boolean,
    private var userRepository: UserRepository
) : ApplicationRunner {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    override fun run(args: ApplicationArguments?) {
        if (enabled && userRepository.findByUserName("admin") == null) {
            log.warn("Creating admin user: admin:test")
            userRepository.save(User(0, "admin", "{noop}test", "", "", "",
                true, Role.ADMIN))
        }
    }
}

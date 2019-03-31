package com.pardyl.shortener.services

import com.pardyl.shortener.persistence.entities.Role
import com.pardyl.shortener.persistence.entities.User
import com.pardyl.shortener.persistence.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ExternalUserService(private val userRepository: UserRepository) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExternalUserService::class.java)
    }

    fun loginExternalAccount(externalUser: ExternalUser): Role {
        var localUser = userRepository.findByUserName(externalUser.userName)
        if (localUser == null) {
            logger.info("Creating new external user ${externalUser.userName}")
            localUser = User()
            localUser.userName = externalUser.userName
            localUser.role = Role.EXTERNAL
        }
        externalUser.email?.let { localUser.email = it }
        externalUser.firstName?.let { localUser.firstName = it }
        externalUser.lastName?.let { localUser.lastName = it }
        userRepository.save(localUser)
        return when (localUser.role) {
            Role.EXTERNAL -> externalUser.role
            else -> localUser.role
        }
    }
}

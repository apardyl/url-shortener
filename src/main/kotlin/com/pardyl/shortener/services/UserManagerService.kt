package com.pardyl.shortener.services

import com.pardyl.shortener.persistence.entities.Role
import com.pardyl.shortener.persistence.repositories.EmailVerificationTokenRepository
import com.pardyl.shortener.persistence.repositories.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserManagerService(
    private val userRepository: UserRepository,
    private val tokenRepository: EmailVerificationTokenRepository
) {
    fun hashPassword(password: String): String = "{bcrypt}" + BCryptPasswordEncoder().encode(password)

    fun resetPassword(tokenId: String, password: String): Boolean {
        val token = tokenRepository.findByToken(tokenId)

        if (token == null || !token.isValid()) {
            token?.let { tokenRepository.delete(it) }
            return false
        }

        val user = token.user!!
        user.password = hashPassword(password)
        if (user.role == Role.NOBODY) {
            user.role = Role.USER
        }
        userRepository.save(user)
        tokenRepository.delete(token)
        return true
    }
}

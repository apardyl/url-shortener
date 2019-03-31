package com.pardyl.shortener.configuration

import com.pardyl.shortener.services.ExternalUserService
import com.pardyl.shortener.services.LdapAttributesMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient
import org.springframework.security.kerberos.client.config.SunJaasKrb5LoginConfig
import org.springframework.security.kerberos.client.ldap.KerberosLdapContextSource
import org.springframework.security.ldap.DefaultSpringSecurityContextSource
import org.springframework.security.ldap.authentication.BindAuthenticator
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator
import org.springframework.security.ldap.userdetails.LdapUserDetailsService

@Configuration
data class LdapConfig(
    @Value("\${shortener.ldap.url:}") private val ldapUrl: String,
    @Value("\${shortener.ldap.user.base:}") private val ldapUserBase: String,
    @Value("\${shortener.ldap.user.filter:}") private val ldapUserFilter: String,
    @Value("\${shortener.ldap.use_krb_auth:false}") private val krbAuth: Boolean,
    @Value("\${shortener.ldap.user_dn:}") private val ldapUserDn: String,
    @Value("\${shortener.ldap.password:}") private val ldapPassword: String,
    @Value("\${shortener.ldap.krb_service.principal:}") private val krbAuthPrincipal: String,
    @Value("\${shortener.ldap.krb_service.keytab:}") private val krbAuthKeytab: String,
    private val externalUserService: ExternalUserService,
    private val ldapAttributesMapper: LdapAttributesMapper
) {
    val ldapAuthoritiesPopulator by lazy {
        LdapAuthoritiesPopulator { userData: DirContextOperations, username: String ->
            val extUser = ldapAttributesMapper.getExternalUser(userData, username)
            externalUserService.loginExternalAccount(extUser).permissions
        }
    }

    val ldapContextSource by lazy {
        if (krbAuthPrincipal.isNotEmpty()) {
            val ctx = KerberosLdapContextSource(ldapUrl)
            val loginConfig = SunJaasKrb5LoginConfig()
            loginConfig.setKeyTabLocation(FileSystemResource(krbAuthKeytab))
            loginConfig.setServicePrincipal(krbAuthPrincipal)
            loginConfig.setIsInitiator(true)
            ctx
        } else {
            val ldapContextSource = DefaultSpringSecurityContextSource(ldapUrl)
            if (ldapUserDn.isNotEmpty()) {
                ldapContextSource.userDn = ldapUserDn
                ldapContextSource.password = ldapPassword
            }
            ldapContextSource.afterPropertiesSet()
            ldapContextSource
        }
    }

    val ldapUserSearch by lazy {
        FilterBasedLdapUserSearch(ldapUserBase, ldapUserFilter, ldapContextSource)
    }

    val ldapUserDetailsService by lazy {
        val ldapUserService = LdapUserDetailsService(ldapUserSearch, ldapAuthoritiesPopulator)
        UserDetailsService {
            ldapUserService.loadUserByUsername(it.substringBefore('@'))
        }
    }

    val ldapAuthenticationProvider by lazy {
        when {
            ldapUrl.isEmpty() -> null
            krbAuth -> {
                val provider = KerberosAuthenticationProvider()
                val client = SunJaasKerberosClient()
                provider.setKerberosClient(client)
                provider.setUserDetailsService(ldapUserDetailsService)
                provider
            }
            else -> {
                val ldapAuthenticator = BindAuthenticator(ldapContextSource)
                val userSearch = FilterBasedLdapUserSearch(ldapUserBase, ldapUserFilter, ldapContextSource)
                ldapAuthenticator.setUserSearch(userSearch)
                ldapAuthenticator.afterPropertiesSet()
                LdapAuthenticationProvider(ldapAuthenticator, ldapAuthoritiesPopulator)
            }
        }
    }
}

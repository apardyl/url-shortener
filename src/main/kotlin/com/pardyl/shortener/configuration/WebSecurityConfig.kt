package com.pardyl.shortener.configuration

import com.pardyl.shortener.persistence.repositories.RememberMePersistentTokenRepository
import com.pardyl.shortener.services.LocalUserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.web.CasAuthenticationFilter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.logout.LogoutFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
class WebSecurityConfig(
    private val tokenRepository: RememberMePersistentTokenRepository,
    private val userService: LocalUserService,
    @Value("\${shortener.secret}") private val secret: String,
    @Value("\${shortener.ldap.url:}") private val ldapUrl: String,
    private val ldapConfig: LdapConfig,
    private val casConfig: CasConfig
) : WebSecurityConfigurerAdapter() {
    private inner class DelegatingUserDetailService : UserDetailsService {
        override fun loadUserByUsername(username: String): UserDetails {
            return try {
                userService.loadUserByUsername(username)
            } catch (ex: UsernameNotFoundException) {
                if (ldapUrl.isNotEmpty()) {
                    ldapConfig.ldapUserDetailsService.loadUserByUsername(username)
                } else {
                    throw ex
                }
            }
        }
    }

    override fun configure(http: HttpSecurity) {
        http.exceptionHandling().accessDeniedPage("/403/")

        if (casConfig.casEnabled()) {
            http.httpBasic().authenticationEntryPoint(casConfig.casAuthenticationEntryPoint())
                .and().logout().logoutUrl("/logout/").logoutSuccessUrl("/logout/cas").permitAll()
                .and().csrf().ignoringAntMatchers("/cas**")
                .and().authorizeRequests().antMatchers("/login/").authenticated().anyRequest().permitAll()
                .and().addFilterBefore(casConfig.singleSignOutFilter(), CasAuthenticationFilter::class.java)
                .addFilterBefore(casConfig.logoutFilter(), LogoutFilter::class.java)
        } else {
            http.formLogin().loginPage("/login/").defaultSuccessUrl("/shortener/").permitAll()
                .and().rememberMe().key(secret).tokenRepository(tokenRepository)
                .userDetailsService(DelegatingUserDetailService())
                .and().logout().logoutUrl("/logout/").permitAll()
                .and().authorizeRequests().anyRequest().permitAll()
        }
    }

    override fun configure(web: WebSecurity) {
        web.ignoring()
            .antMatchers("/static/**")
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        if (casConfig.casEnabled()) {
            auth.authenticationProvider(casConfig.casAuthenticationProvider())
        } else {
            auth.userDetailsService(userService)

            if (ldapUrl.isNotEmpty()) {
                auth.authenticationProvider(ldapConfig.ldapAuthenticationProvider)
            }
        }
    }

    @Bean
    @Throws(Exception::class)
    fun casAuthenticationFilter(serviceProperties: ServiceProperties): CasAuthenticationFilter {
        val filter = CasAuthenticationFilter()
        filter.setServiceProperties(serviceProperties)
        filter.setAuthenticationManager(authenticationManager())
        return filter
    }
}

package com.pardyl.shortener.forms

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class UserRegistrationForm(
    @get:Size(min = 3, max = 32)
    var userName: String = "",

    @get:NotBlank
    @get:Email
    var email: String = "",

    @get:NotBlank
    var firstName: String = "",

    @get:NotBlank
    var lastName: String = ""
)

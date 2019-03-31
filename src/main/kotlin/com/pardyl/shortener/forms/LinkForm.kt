package com.pardyl.shortener.forms

import javax.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class LinkForm(
    val id: Long?,

    @get:NotBlank
    val name: String,

    @get:NotBlank
    @get:URL
    val url: String,

    val owner: String?,

    val created: String?
)

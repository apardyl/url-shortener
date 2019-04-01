package com.pardyl.shortener.controllers

import com.pardyl.shortener.exceptions.NotFoundException
import com.pardyl.shortener.persistence.repositories.LinkRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.RedirectView

@Controller
class RedirectController(private val linkRepository: LinkRepository) {

    @GetMapping("/{name}")
    fun redirect(@PathVariable("name") name: String): View {
        val link = linkRepository.findByName(name) ?: throw NotFoundException(name)
        linkRepository.incrementVisited(link.id!!)
        return RedirectView(link.url!!)
    }
}

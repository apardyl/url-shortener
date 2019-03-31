package com.pardyl.shortener.controllers.admin

import com.pardyl.shortener.exceptions.BadRequestException
import com.pardyl.shortener.forms.LinkForm
import com.pardyl.shortener.persistence.entities.Link
import com.pardyl.shortener.persistence.entities.Permission
import com.pardyl.shortener.persistence.repositories.LinkRepository
import com.pardyl.shortener.persistence.repositories.UserRepository
import java.util.Date
import javax.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.RedirectView

@Controller
class LinksController(private val linkRepository: LinkRepository, private val userRepository: UserRepository) {
    companion object {
        private const val linksPerPage = 100
    }

    private fun canManageLinks(): Boolean {
        return SecurityContextHolder.getContext().authentication.authorities.contains(Permission.ROLE_MANAGE_LINKS)
    }

    private fun getUsername(): String {
        return (SecurityContextHolder.getContext().authentication.principal as UserDetails).username
    }

    @Secured(Permission.ROLE_SHORTEN_STR)
    @GetMapping("/shortener/links/")
    fun linkList(): View {
        return RedirectView("/shortener/links/0/")
    }

    @Secured(Permission.ROLE_SHORTEN_STR)
    @GetMapping("/shortener/links/{num}/")
    fun linkList(@PathVariable("num") pageNumber: Int): ModelAndView {
        val links = if (canManageLinks()) {
            linkRepository.findAll(PageRequest.of(pageNumber, linksPerPage))
        } else {
            linkRepository.findByOwner(userRepository.findByUserName(getUsername())!!,
                PageRequest.of(pageNumber, linksPerPage))
        }
        return ModelAndView("admin/link_list", "links", links)
    }

    @Secured(Permission.ROLE_SHORTEN_STR)
    @GetMapping("/shortener/link/{id}/")
    fun link(@PathVariable("id") linkId: Long): ModelAndView {
        val link = linkRepository.findById(linkId)
        if (link.isPresent) {
            val u = link.get()
            if (!canManageLinks() && u.owner!!.userName != getUsername()) {
                throw BadRequestException("No link for id: $linkId")
            }
            val linkForm = LinkForm(u.id, u.name!!, u.url!!, u.owner!!.userName, u.created.toString())
            return ModelAndView("admin/link_edit", "link", linkForm)
        } else {
            throw BadRequestException("No link for id: $linkId")
        }
    }

    @Secured(Permission.ROLE_SHORTEN_STR)
    @PostMapping("/shortener/link/{linkId}/")
    fun linkEdit(
        @PathVariable("linkId") linkId: Long,
        @Valid @ModelAttribute("link") form: LinkForm,
        result: BindingResult
    ): ModelAndView {
        if (form.id != linkId) {
            throw BadRequestException("Link id mismatch")
        }
        if (result.hasErrors()) {
            return ModelAndView("admin/link_edit", HttpStatus.BAD_REQUEST)
        }
        val link = linkRepository.findById(form.id).orElseThrow { BadRequestException("unknown link") }
        if (!canManageLinks() && link.owner!!.userName != getUsername()) {
            throw BadRequestException("No link for id: $linkId")
        }

        link.name = form.name
        link.url = form.url
        linkRepository.save(link)
        return ModelAndView(RedirectView("/shortener/links/"))
    }

    @Secured(Permission.ROLE_SHORTEN_STR)
    @PostMapping("/shortener/link/{id}/delete/")
    fun linkDelete(
        @PathVariable("id") linkId: Long
    ): ModelAndView {
        val link = linkRepository.findById(linkId).orElseThrow { BadRequestException("unknown link") }
        if (!canManageLinks() && link.owner!!.userName != getUsername()) {
            throw BadRequestException("No link for id: $linkId")
        }
        linkRepository.delete(link)
        return ModelAndView(RedirectView("/shortener/links/"))
    }

    @Secured(Permission.ROLE_SHORTEN_STR)
    @GetMapping("/shortener/link/create/")
    fun linkCreate(): ModelAndView {
        return ModelAndView("admin/link_create", "link", LinkForm(null, "", "", null, null))
    }

    @Secured(Permission.ROLE_SHORTEN_STR)
    @PostMapping("/shortener/link/create/")
    fun linkCreatePost(@Valid @ModelAttribute("link") form: LinkForm, result: BindingResult): ModelAndView {
        if (!form.name.isNullOrBlank() && linkRepository.findByName(form.name) != null) {
            result.rejectValue("name", "name.exists", "link name unavailable")
        }

        if (result.hasErrors()) {
            return ModelAndView("admin/link_create", HttpStatus.BAD_REQUEST)
        }
        val user = userRepository.findByUserName(getUsername())!!

        val link = Link(null, form.name, form.url, user, Date())
        linkRepository.save(link)
        return ModelAndView(RedirectView("/shortener/links/"))
    }
}

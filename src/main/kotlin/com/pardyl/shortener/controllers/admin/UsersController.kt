package com.pardyl.shortener.controllers.admin

import com.pardyl.shortener.exceptions.BadRequestException
import com.pardyl.shortener.forms.UserForm
import com.pardyl.shortener.persistence.entities.Permission
import com.pardyl.shortener.persistence.entities.Role
import com.pardyl.shortener.persistence.entities.User
import com.pardyl.shortener.persistence.repositories.UserRepository
import com.pardyl.shortener.services.UserManagerService
import javax.validation.Valid
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.RedirectView

@Controller
class UsersController(private val userRepository: UserRepository, private val userManagerService: UserManagerService) {
    companion object {
        private const val usersPerPage = 100
    }

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(String::class.java, StringTrimmerEditor(true))
    }

    @Secured(Permission.MANAGE_USERS_STR)
    @GetMapping("/shortener/users/")
    fun userList(): View {
        return RedirectView("/shortener/users/0/")
    }

    @Secured(Permission.MANAGE_USERS_STR)
    @GetMapping("/shortener/users/{num}/")
    fun userList(@PathVariable("num") pageNumber: Int): ModelAndView {
        val users = userRepository.findAll(PageRequest.of(pageNumber, usersPerPage))
        return ModelAndView("admin/user_list", "users", users)
    }

    @Secured(Permission.MANAGE_USERS_STR)
    @GetMapping("/shortener/user/{id}/")
    fun userProfile(@PathVariable("id") userId: Long): ModelAndView {
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            val u = user.get()
            val userForm = UserForm(u.id!!, u.userName, u.email, "", "", u.firstName, u.lastName, u.enabled, u.role)
            return ModelAndView("admin/user_profile", "user", userForm)
        } else {
            throw BadRequestException("No user for id: $userId")
        }
    }

    @Secured(Permission.MANAGE_USERS_STR)
    @PostMapping("/shortener/user/{userId}/")
    fun userProfileEdit(
        @PathVariable("userId") userId: Long,
        @Valid @ModelAttribute("user") form: UserForm,
        result: BindingResult
    ): ModelAndView {
        if (form.id != userId) {
            throw BadRequestException("User id mismatch")
        }
        if (form.password != form.password2) {
            result.rejectValue("password2", "password2.notMatch", "passwords do not match")
        }
        if (result.hasErrors()) {
            return ModelAndView("admin/user_profile", HttpStatus.BAD_REQUEST)
        }
        val user = userRepository.findById(form.id).orElseThrow { BadRequestException("unknown user") }
        user.email = form.email
        user.firstName = form.firstName
        user.lastName = form.lastName
        user.enabled = form.enabled
        user.role = form.role
        if (!form.password.isNullOrBlank()) {
            user.password = userManagerService.hashPassword(form.password)
        }
        userRepository.save(user)
        return ModelAndView(RedirectView("/shortener/users/"))
    }

    @Secured(Permission.MANAGE_USERS_STR)
    @PostMapping("/shortener/user/{id}/delete/")
    fun userProfileDelete(
        @PathVariable("id") userId: Long
    ): ModelAndView {
        val user = userRepository.findById(userId).orElseThrow { BadRequestException("unknown user") }
        userRepository.delete(user)
        return ModelAndView(RedirectView("/shortener/users/"))
    }

    @Secured(Permission.MANAGE_USERS_STR)
    @GetMapping("/shortener/user/create/")
    fun userProfileCreate(): ModelAndView {
        return ModelAndView("admin/user_create", "user", UserForm(null, "", "", "", "", "", "", true, Role.USER))
    }

    @Secured(Permission.MANAGE_USERS_STR)
    @PostMapping("/shortener/user/create/")
    fun userProfileCreatePost(@Valid @ModelAttribute("user") form: UserForm, result: BindingResult): ModelAndView {
        if (!form.userName.isBlank() && userRepository.findByUserName(form.userName) != null) {
            result.rejectValue("userName", "username.exists", "username unavailable")
        }

        if (form.email != null && !form.email.isBlank() && userRepository.findByEmail(form.email) != null) {
            result.rejectValue("email", "email.exists", "email already in use")
        }
        if (form.password != form.password2) {
            result.rejectValue("password2", "password2.notMatch", "passwords do not match")
        }
        if (result.hasErrors()) {
            return ModelAndView("admin/user_create", HttpStatus.BAD_REQUEST)
        }
        val user = User(null, form.userName, userManagerService.hashPassword(form.password!!), form.email,
            form.firstName, form.lastName, form.enabled, form.role)
        userRepository.save(user)
        return ModelAndView(RedirectView("/shortener/users/"))
    }
}

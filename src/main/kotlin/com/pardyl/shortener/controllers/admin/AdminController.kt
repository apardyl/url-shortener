package com.pardyl.shortener.controllers.admin

import java.security.Principal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.RedirectView

@Controller
class AdminController {
    @GetMapping("/shortener/")
    fun adminPage(principal: Principal?): View {
        return RedirectView(if (principal == null) "/login/" else "/shortener/links/")
    }
}

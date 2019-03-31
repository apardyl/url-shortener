package com.pardyl.shortener.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.RedirectView

@Controller
class HomeController {
    @GetMapping("/")
    fun home(): View {
        return RedirectView("/shortener/")
    }
}

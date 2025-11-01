package org.littlebookfriends.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CatalogController {

    @GetMapping("/")
    public String index(@RequestParam(required = false, defaultValue = "") String q, Model model) {
        model.addAttribute("query", q);
        model.addAttribute("results", java.util.List.of()); // wird in AB#144 befüllt
        model.addAttribute("cart", java.util.List.of());    // wird in AB#146 befüllt
        return "catalog";
    }
}

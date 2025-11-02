package org.littlebookfriends.order.controller;

import org.littlebookfriends.order.model.Book;
import org.littlebookfriends.order.service.CatalogGateway;
import org.littlebookfriends.order.service.ShoppingCartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CatalogController {

    private final CatalogGateway catalog;
    private final ShoppingCartService cart;

    public CatalogController(CatalogGateway catalog, ShoppingCartService cart) {
        this.catalog = catalog;
        this.cart = cart;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false, defaultValue = "") String q, Model model) {
        List<Book> results = q.isBlank() ? List.of() : catalog.search(q);
        model.addAttribute("query", q);
        model.addAttribute("results", results);
        model.addAttribute("cart", cart.getItems());
        model.addAttribute("cartCount", cart.getTotalQuantity());
        return "catalog";
    }
}

package org.example.order.controller;

import org.example.order.model.Book;
import org.example.order.service.CatalogGateway;
import org.example.order.service.ShoppingCartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String index(@RequestParam(required = false) String keyword, Model model) {
        List<Book> results = catalog.search(keyword);
        model.addAttribute("keyword", keyword);
        model.addAttribute("results", results);
        model.addAttribute("cart", cart.getItems());
        model.addAttribute("cartCount", cart.getTotalQuantity());
        return "catalog";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String isbn, RedirectAttributes redirect) {
        // 1) Das Buch über den Catalog suchen
        var book = catalog.search(isbn).getFirst();
        if (book != null) {
            cart.add(book); // 2) In Warenkorb legen
            redirect.addFlashAttribute("msg", "Buch hinzugefügt: " + book.title());
        }
        else {
            redirect.addFlashAttribute("msg", "Fehler: Buch nicht gefunden");
        }

        // 3) Danach zurück zur Hauptseite
        return "redirect:/"; //redirect:/
    }
}

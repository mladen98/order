package org.example.order.service;

import org.example.order.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.List;


//Hält den Warenkorb für genau einen Benutzer (Session)
@Service
@SessionScope
public class ShoppingCartService {

    private final ShoppingCart cart = new ShoppingCart();

    // Gibt alle Artikel im Warenkorb zurück
    public List<CartItem> getItems() {
        return cart.items();
    }

    // Fügt ein Buch hinzu (AB#147)
    public void add(Book book) {
        cart.add(book);
    }

    // Alles löschen
    public void clear() {
        cart.clear();
    }

    public int getTotalQuantity() {
        return cart.totalQuantity();
    }
}

package org.example.order.model;

import java.util.*;

public class ShoppingCart {

    private final Map<String, CartItem> items = new LinkedHashMap<>();

    /** Liste der Positionen (reihenfolgestabil) */
    public List<CartItem> items() {
        return new ArrayList<>(items.values());
    }

    /** Buch hinzufügen (gleiche ISBN erhöht Menge) */
    public void add(Book book) {
        items.merge(book.isbn(), new CartItem(book, 1),
                (oldv, newv) -> new CartItem(oldv.book(), oldv.qty() + 1));
    }

    /** Alles leeren */
    public void clear() {
        items.clear();
    }

    /** Anzahl unterschiedlicher Positionen */
    public int size() {
        return items.size();
    }

    /** Summe aller Mengen */
    public int totalQuantity() {
        return items.values().stream().mapToInt(CartItem::qty).sum();
    }
}

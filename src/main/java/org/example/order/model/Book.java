package org.example.order.model;

import java.util.List;

public record Book(String isbn, String title, String description, List<Author> authors) {}

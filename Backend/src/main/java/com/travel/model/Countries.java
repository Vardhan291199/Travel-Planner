package com.travel.model;

public record Countries(
        String code,
        String name,
        String emoji,
        String capital,
        String currency,
        String continent,
        java.util.List<String> languages
) {}

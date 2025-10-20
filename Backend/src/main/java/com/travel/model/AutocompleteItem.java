package com.travel.model;

public record AutocompleteItem(
    String label,
    String city,
    String countryCode,
    Double lat,
    Double lon
) {}

package com.travel.model;

public record PlaceItem(
    String name,
    String address,
    Double distanceMeters,
    java.util.List<String> categories,
    Double lat,
    Double lon,
    Double rating,
    Integer priceLevel,
    String website,
    String placeId
) {}

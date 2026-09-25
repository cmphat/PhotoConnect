package com.photoconnect.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlled taxonomy of professional photography specialties.
 * Prevents arbitrary user HTML from polluting the marketplace specialty system.
 */
public enum PhotographerSpecialty {

    PORTRAIT("Portrait"),
    WEDDING("Wedding"),
    FASHION("Fashion"),
    LIFESTYLE("Lifestyle"),
    EVENT("Event"),
    COMMERCIAL("Commercial"),
    PRODUCT("Product"),
    TRAVEL("Travel"),
    ARCHITECTURE("Architecture"),
    DOCUMENTARY("Documentary");

    private final String displayName;

    PhotographerSpecialty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a specialty from an enum name case-insensitively.
     */
    public static Optional<PhotographerSpecialty> fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase();
        for (PhotographerSpecialty specialty : values()) {
            if (specialty.name().equals(normalized)) {
                return Optional.of(specialty);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if a code corresponds to a valid controlled specialty.
     */
    public static boolean isValid(String code) {
        return fromCode(code).isPresent();
    }

    /**
     * Parses a comma-delimited string of specialty enum names into a List.
     * Invalid or empty tokens are safely skipped.
     */
    public static List<PhotographerSpecialty> parseSpecialties(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(PhotographerSpecialty::fromCode)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Converts a collection of specialties into a comma-separated database string.
     */
    public static String toCommaSeparated(Collection<PhotographerSpecialty> specialties) {
        if (specialties == null || specialties.isEmpty()) {
            return null;
        }
        return specialties.stream()
                .map(Enum::name)
                .distinct()
                .collect(Collectors.joining(","));
    }

    /**
     * Converts a comma-delimited string of codes into a human-readable list of display names.
     */
    public static List<String> toDisplayNames(String commaSeparated) {
        List<PhotographerSpecialty> parsed = parseSpecialties(commaSeparated);
        List<String> displayNames = new ArrayList<>();
        for (PhotographerSpecialty s : parsed) {
            displayNames.add(s.getDisplayName());
        }
        return displayNames;
    }
}

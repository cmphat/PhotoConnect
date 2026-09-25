package com.photoconnect.entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PhotographerSpecialtyTest {

    @Test
    void controlledTaxonomyContainsExpectedTenValues() {
        assertThat(PhotographerSpecialty.values()).hasSize(10);
        assertThat(PhotographerSpecialty.PORTRAIT.getDisplayName()).isEqualTo("Portrait");
        assertThat(PhotographerSpecialty.WEDDING.getDisplayName()).isEqualTo("Wedding");
        assertThat(PhotographerSpecialty.FASHION.getDisplayName()).isEqualTo("Fashion");
        assertThat(PhotographerSpecialty.LIFESTYLE.getDisplayName()).isEqualTo("Lifestyle");
        assertThat(PhotographerSpecialty.EVENT.getDisplayName()).isEqualTo("Event");
        assertThat(PhotographerSpecialty.COMMERCIAL.getDisplayName()).isEqualTo("Commercial");
        assertThat(PhotographerSpecialty.PRODUCT.getDisplayName()).isEqualTo("Product");
        assertThat(PhotographerSpecialty.TRAVEL.getDisplayName()).isEqualTo("Travel");
        assertThat(PhotographerSpecialty.ARCHITECTURE.getDisplayName()).isEqualTo("Architecture");
        assertThat(PhotographerSpecialty.DOCUMENTARY.getDisplayName()).isEqualTo("Documentary");
    }

    @Test
    void fromCode_caseInsensitiveMatching() {
        Optional<PhotographerSpecialty> portrait = PhotographerSpecialty.fromCode("portrait");
        assertThat(portrait).isPresent().contains(PhotographerSpecialty.PORTRAIT);

        Optional<PhotographerSpecialty> wedding = PhotographerSpecialty.fromCode("WEDDING");
        assertThat(wedding).isPresent().contains(PhotographerSpecialty.WEDDING);

        Optional<PhotographerSpecialty> invalid = PhotographerSpecialty.fromCode("<script>alert(1)</script>");
        assertThat(invalid).isEmpty();

        Optional<PhotographerSpecialty> empty = PhotographerSpecialty.fromCode("   ");
        assertThat(empty).isEmpty();
    }

    @Test
    void parseSpecialties_and_toCommaSeparated() {
        String input = "PORTRAIT, WEDDING, INVALID_SPECIALTY, FASHION";
        List<PhotographerSpecialty> parsed = PhotographerSpecialty.parseSpecialties(input);

        assertThat(parsed).containsExactly(
                PhotographerSpecialty.PORTRAIT,
                PhotographerSpecialty.WEDDING,
                PhotographerSpecialty.FASHION
        );

        String formatted = PhotographerSpecialty.toCommaSeparated(parsed);
        assertThat(formatted).isEqualTo("PORTRAIT,WEDDING,FASHION");

        List<String> displayNames = PhotographerSpecialty.toDisplayNames(input);
        assertThat(displayNames).containsExactly("Portrait", "Wedding", "Fashion");
    }
}

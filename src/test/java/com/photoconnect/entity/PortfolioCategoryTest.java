package com.photoconnect.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortfolioCategoryTest {

    @Test
    void allExpectedCategoriesExistWithHumanFriendlyDisplayNames() {
        assertThat(PortfolioCategory.PORTRAIT.getDisplayName()).isEqualTo("Portrait");
        assertThat(PortfolioCategory.WEDDING.getDisplayName()).isEqualTo("Wedding");
        assertThat(PortfolioCategory.FASHION.getDisplayName()).isEqualTo("Fashion");
        assertThat(PortfolioCategory.LIFESTYLE.getDisplayName()).isEqualTo("Lifestyle");
        assertThat(PortfolioCategory.STREET.getDisplayName()).isEqualTo("Street");
        assertThat(PortfolioCategory.COMMERCIAL.getDisplayName()).isEqualTo("Commercial");
        assertThat(PortfolioCategory.EVENT.getDisplayName()).isEqualTo("Event");
        assertThat(PortfolioCategory.TRAVEL.getDisplayName()).isEqualTo("Travel");
        assertThat(PortfolioCategory.ARCHITECTURE.getDisplayName()).isEqualTo("Architecture");
        assertThat(PortfolioCategory.OTHER.getDisplayName()).isEqualTo("Other");
    }

    @Test
    void fromFormValue_validValues_shouldParseCaseInsensitively() {
        assertThat(PortfolioCategory.fromFormValue("PORTRAIT")).isEqualTo(PortfolioCategory.PORTRAIT);
        assertThat(PortfolioCategory.fromFormValue("wedding")).isEqualTo(PortfolioCategory.WEDDING);
        assertThat(PortfolioCategory.fromFormValue("Fashion")).isEqualTo(PortfolioCategory.FASHION);
        assertThat(PortfolioCategory.fromFormValue("  street  ")).isEqualTo(PortfolioCategory.STREET);
    }

    @Test
    void fromFormValue_nullOrBlank_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PortfolioCategory.fromFormValue(null));
        assertThrows(IllegalArgumentException.class, () -> PortfolioCategory.fromFormValue(""));
        assertThrows(IllegalArgumentException.class, () -> PortfolioCategory.fromFormValue("   "));
    }

    @Test
    void fromFormValue_unsupportedCategory_shouldThrowIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PortfolioCategory.fromFormValue("UNDERWATER_DRONE"));
        assertThat(ex.getMessage()).contains("Unsupported photography category");
    }
}

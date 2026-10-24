package com.airfrance.airfrance.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestLanguageTest {

    private final RequestLanguage requestLanguage = new RequestLanguage();

    @Test
    void defaultsToEnglishWhenHeaderIsMissingOrUnsupported() {
        assertEquals("en", requestLanguage.resolve(null));
        assertEquals("en", requestLanguage.resolve("de-DE"));
    }

    @Test
    void selectsFrenchFromRegionalLanguageRange() {
        assertEquals("fr", requestLanguage.resolve("fr-CA,fr;q=0.9,en;q=0.8"));
    }

    @Test
    void honoursQualityWeightsAndZeroQualityRanges() {
        assertEquals("en", requestLanguage.resolve("fr;q=0.2,en;q=1"));
        assertEquals("en", requestLanguage.resolve("fr;q=0,en;q=0.9"));
    }
}

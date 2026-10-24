package com.airfrance.airfrance.web;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class RequestLanguage {

    public String resolve(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return "en";
        }

        try {
            for (Locale.LanguageRange range : Locale.LanguageRange.parse(acceptLanguage)) {
                if (range.getWeight() == 0) {
                    continue;
                }
                String language = Locale.forLanguageTag(range.getRange()).getLanguage();
                if (language.equals("fr") || language.equals("en")) {
                    return language;
                }
            }
        } catch (IllegalArgumentException ignored) {
            return "en";
        }
        return "en";
    }
}

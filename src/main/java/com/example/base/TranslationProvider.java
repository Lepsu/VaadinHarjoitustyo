package com.example.base;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;

@Component
public class TranslationProvider implements I18NProvider {

    private static final String BUNDLE_PREFIX = "translation";

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(new Locale("fi"), Locale.ENGLISH);
    }

    @Override
    public String getTranslation(String key,
                                 Locale locale, Object... params) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    BUNDLE_PREFIX, locale);
            String value = bundle.getString(key);
            if (params.length > 0) {
                value = MessageFormat.format(value, params);
            }
            return value;
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }
}
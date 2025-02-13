package com.lexbot.utils.validations;

import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.firestore_dao.LBUser;

import java.util.Map;
import java.util.Set;

public class SimpleValidation {

    public static void validateStrings(String... strings) {
        for (String str : strings) {
            if (str == null || str.isBlank()) throw new IllegalArgumentException("Cannot be null or blank");
        }
    }

    public static void validateNotNulls(Object... objs) {
        for (Object obj : objs) {
            if (obj == null) throw new IllegalArgumentException("Cannot be null");
        }
    }

    public static void validateEmail(String email) {
        validateStrings(email);
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";

        if (!email.matches(regex)) throw new IllegalArgumentException("Invalid email format");
    }

    private static final Map<Class<?>, Set<String>> VALID_FIELDS_MAP = Map.of(
        Chat.class, Set.of("title", "lastUse"),
        LBUser.class, Set.of("username", "email", "password", "status")
    );

    public static void validateUpdates(Class<?> entityClass, Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) throw new IllegalArgumentException("Cannot be null or empty");


        Set<String> validFields = VALID_FIELDS_MAP.get(entityClass);
        if (validFields == null) {
            throw new IllegalArgumentException("No validation rules found for class: " + entityClass.getSimpleName());
        }

        for (String key : updates.keySet()) {
            if (!validFields.contains(key)) {
                throw new IllegalArgumentException("Invalid field: " + key + " for " + entityClass.getSimpleName());
            }
        }
    }

}

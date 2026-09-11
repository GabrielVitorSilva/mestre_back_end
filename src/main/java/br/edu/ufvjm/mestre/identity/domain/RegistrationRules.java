package br.edu.ufvjm.mestre.identity.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public final class RegistrationRules {
    private static final Pattern LOCAL = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*");
    private static final Pattern LABEL = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?");
    private RegistrationRules() {}

    public static String name(String value) {
        if (value == null) throw new InvalidRegistration("Informe o nome.");
        String name = value.strip();
        if (!validUnicode(name) || name.isBlank() || length(name) > 120
                || name.codePoints().anyMatch(Character::isISOControl)) {
            throw new InvalidRegistration("O nome deve conter de 1 a 120 caracteres, sem caracteres de controle.");
        }
        return name;
    }

    public static String email(String value) {
        if (value == null || value.length() > 320) throw invalidEmail();
        String stripped = value.strip();
        if (!stripped.chars().allMatch(c -> c < 128)) throw invalidEmail();
        String email = stripped.toLowerCase(Locale.ROOT);
        if (email.length() > 254) throw invalidEmail();
        String[] parts = email.split("@", -1);
        if (parts.length != 2 || parts[0].length() > 64 || !LOCAL.matcher(parts[0]).matches()) throw invalidEmail();
        String[] labels = parts[1].split("\\.", -1);
        if (labels.length < 2) throw invalidEmail();
        for (String label : labels) if (!LABEL.matcher(label).matches()) throw invalidEmail();
        return email;
    }

    public static void password(String value) {
        if (value == null || !validUnicode(value) || length(value) < 15 || length(value) > 128
                || value.isBlank() || value.codePoints().anyMatch(Character::isISOControl)) {
            throw new InvalidRegistration("A senha deve conter de 15 a 128 caracteres, sem caracteres de controle, e não pode ser apenas espaços.");
        }
    }

    private static InvalidRegistration invalidEmail() {
        return new InvalidRegistration("Informe um e-mail válido de até 254 caracteres.");
    }
    private static int length(String value) { return value.codePointCount(0, value.length()); }
    private static boolean validUnicode(String value) {
        return value.codePoints().noneMatch(c -> c >= 0xD800 && c <= 0xDFFF);
    }
}

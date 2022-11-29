package no.nav.vedtak.log.util;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;

public final class LoggerUtils {

    private static final int DEFAULT_LENGTH = 50;

    private LoggerUtils() {
    }

    public static String removeLineBreaks(String string) {
        return Optional.ofNullable(string)
                .map(s -> s.replaceAll("(\\r|\\n)", ""))
                .orElse(null);
    }

    public static String toStringWithoutLineBreaks(Object object) {
        return Optional.ofNullable(object)
                .map(Object::toString)
                .map(LoggerUtils::removeLineBreaks)
                .orElse(null);
    }

    public static String taint(String value) {
        if (!value.matches("[a-zA-Z0-9]++"))
            throw new IllegalArgumentException(value);
        return value;
    }

    public static String endelse(List<?> liste) {
        return liste.size() == 1 ? "" : "er";
    }

    public static String limit(String tekst) {
        return limit(tekst, DEFAULT_LENGTH);
    }

    public static String limit(String tekst, int max) {
        return Optional.ofNullable(tekst)
                .filter(t -> t.length() >= max)
                .map(s -> s.substring(0, max - 1) + "...")
                .orElse(tekst);
    }

    public static String limit(byte[] bytes, int max) {
        return limit(Arrays.toString(bytes), max);
    }

    public static String partialMask(String value) {
        return partialMask(value, 11);
    }

    public static String partialMask(String value, int length) {
        return (value != null) && (value.length() == length) ? padEnd(value.substring(0, length / 2 + length % 2), length, '*') : value;
    }

    public static String mask(String value) {
        return Optional.ofNullable(value)
                .map(String::stripLeading)
                .filter(not(String::isBlank))
                .map(v -> "*".repeat(v.length()))
                .orElse("<null>");
    }

    public static String encode(String string) {
        return encode(string, UTF_8);
    }

    public static String encode(String string, Charset charset) {
        return Base64.getEncoder().encodeToString(string.getBytes(charset));
    }

    public static String padEnd(String string, int minLength, char padChar) {
        if (string.length() >= minLength) {
            return string;
        }
        StringBuilder sb = new StringBuilder(minLength);
        sb.append(string);
        for (int i = string.length(); i < minLength; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }
}

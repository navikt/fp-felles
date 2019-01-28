package no.nav.vedtak.felles.testutilities;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariablePlaceholderReplacer {

    private static final String placeholderPrefix = "${";
    private static final String placeholderSuffix = "}";
    
    @SuppressWarnings("rawtypes")
    private Map placeholders;

    public VariablePlaceholderReplacer(@SuppressWarnings("rawtypes") Map placeholders) {
        this.placeholders = placeholders;
    }

    @SuppressWarnings("rawtypes")
    public String replacePlaceholders(String input) {
        String processedInput = input;

        String searchTerm;
        String value;
        Map myPlaceholders = placeholders;
        
        for (Iterator itr = myPlaceholders.keySet().iterator(); itr
                .hasNext(); processedInput = replaceAll(processedInput, searchTerm, value == null ? "" : value)) {
            String placeholder = (String) itr.next();
            searchTerm = placeholderPrefix + placeholder + placeholderSuffix;
            value = (String) myPlaceholders.get(placeholder);
        }

        checkForUnmatchedPlaceholderExpression(processedInput);
        return processedInput;
    }

    private String replaceAll(String str, String originalToken, String replacementToken) {
        return str.replaceAll(Pattern.quote(originalToken), Matcher.quoteReplacement(replacementToken));
    }

    private void checkForUnmatchedPlaceholderExpression(String input) {
        String regex = Pattern.quote(placeholderPrefix) + "(.+?)" + Pattern.quote(placeholderSuffix);
        Matcher matcher = Pattern.compile(regex).matcher(input);
        TreeSet<String> unmatchedPlaceHolderExpressions = new TreeSet<>();

        while (matcher.find()) {
            unmatchedPlaceHolderExpressions.add(matcher.group());
        }

        if (!unmatchedPlaceHolderExpressions.isEmpty()) {
            throw new IllegalStateException("Ingen verdi funnet for placeholder: "
                    + String.join(", ", unmatchedPlaceHolderExpressions) + ".  Sjekk miljøvariabler");
        }
    }

}

package edu.u.nus.readmore.Util.Validation;

import java.util.Arrays;
import java.util.regex.Pattern;

public class AccountValidator {
    private static final String VALID_EMAIL_PREFIX_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]{1,64}";
    private static final String VALID_EMAIL_DOMAIN_REGEX = "[a-zA-Z0-9][a-zA-Z0-9-]{0,62}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String[] values = email.split("@");
        return values.length == 2 &&
                isValidEmailPrefix(values[0]) && isValidEmailDomain(values[1]);
    }

    private static boolean isValidEmailDomain(String domain) {
        int length = domain.length();
        boolean isValidDomainLength = length >= 4 && length <= 253;
        String[] domains = domain.split("[.]");
        Pattern pattern = Pattern.compile(VALID_EMAIL_DOMAIN_REGEX);
        boolean isValidDomains = Arrays.stream(domains)
                .allMatch(d -> pattern.matcher(d).matches());
        return isValidDomainLength && isValidDomains;
    }

    private static boolean isValidEmailPrefix(String prefix) {
        return prefix.matches(VALID_EMAIL_PREFIX_REGEX);
    }
}

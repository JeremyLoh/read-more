package edu.u.nus.readmore.Util.Validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AccountValidatorTest {
    // https://help.returnpath.com/hc/en-us/articles/220560587-What-are-the-rules-for-email-address-syntax-
    // https://help.xmatters.com/ondemand/trial/valid_email_format.htm
    private final int TEST_TIMEOUT = 200;

    private String repeat(char value, int count) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < count; i++) {
            output.append(value);
        }
        return output.toString();
    }

    private List<String> getValidDomainTLD() throws IOException {
        Path tldFilePath = Paths.get("src/test/java/edu/u/nus/readmore/Util/Validation/ValidTld.txt");
        try (Stream<String> stream = Files.lines(tldFilePath)) {
            return stream
                    .skip(1)
                    .collect(Collectors.toList());
        }
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void nullEmail() {
        assertFalse(AccountValidator.isValidEmail(null));
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void emptyEmail() {
        assertFalse(AccountValidator.isValidEmail(""));
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void emailRecipientNameIs64Chars() {
        String email = repeat('b', 64) + "@example.com";
        assertTrue(AccountValidator.isValidEmail(email));
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void emailRecipientNameIsLongerThan64Chars() {
        String email = repeat('a', 65) + "@example.com";
        assertFalse(AccountValidator.isValidEmail(email));
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void emailDomainIs253Chars() {
        String email = "name" + "@" +
                repeat('a', 63) +
                "." +
                repeat('b', 63) +
                "." +
                repeat('c', 60) +
                "." +
                repeat('d', 60) +
                ".com";
        assertTrue(AccountValidator.isValidEmail(email));
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void emailDomainIsLongerThan253Chars() {
        String email = "name" + "@" + repeat('a', 254);
        assertFalse(AccountValidator.isValidEmail(email));
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void emailHasMultiplePeriodsInSequence() {
        String email = "name@example..com";
        assertFalse(AccountValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    @ValueSource(strings = {"example.org", "a.com", "A123a.co.uk", "u.nus.edu.sg"})
    void validDomainEmail(String domain) {
        assertTrue(AccountValidator.isValidEmail("test@" + domain));
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void validEmailTLD() throws IOException {
        List<String> tlds = getValidDomainTLD();
        for (String tld : tlds) {
            assertTrue(AccountValidator.isValidEmail("test@example." + tld));
        }
    }

    @Test
    @Timeout(value = TEST_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    void multipleInvalidDashInDomain() {
        assertFalse(AccountValidator.isValidEmail("test@do.--.com"));
    }
}
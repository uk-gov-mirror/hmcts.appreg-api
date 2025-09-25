package uk.gov.hmcts.appregister.courtlocation.validator;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse_;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;

public class CourtLocationSortValidatorTest {
    private CourtLocationsSortValidator validator;

    @BeforeEach
    void before() {
        validator = new CourtLocationsSortValidator();
    }

    @Test
    void allowsName() {
        assertDoesNotThrow(() -> validator.validate(NationalCourtHouse_.NAME));
    }

    @Test
    void allowsCode() {
        assertDoesNotThrow(() -> validator.validate(NationalCourtHouse_.COURT_LOCATION_CODE));
    }

    @Test
    void rejects_unknownProperty() {
        AppRegistryException ex =
                assertThrows(
                        AppRegistryException.class, () -> validator.validate("notARealProperty"));
        Assertions.assertTrue(
                ex.getMessage().contains("Sort property 'notARealProperty' is not allowed"));
    }

    @Test
    void rejects_null() {
        assertThrows(AppRegistryException.class, () -> validator.validate(null));
    }

    @Test
    void rejects_empty() {
        assertThrows(AppRegistryException.class, () -> validator.validate(""));
    }

    @Test
    void rejects_whitespaceOnly() {
        assertThrows(AppRegistryException.class, () -> validator.validate("   "));
    }

    @Test
    void trims_inputButStillCaseSensitive() {
        // Leading/trailing spaces are trimmed, so exact property with spaces is OK:
        assertDoesNotThrow(() -> validator.validate(" " + NationalCourtHouse_.NAME + " "));

        // Case must match exactly (validator does not lower/upper-case normalize):
        assertThrows(
                AppRegistryException.class,
                () -> validator.validate(NationalCourtHouse_.NAME.toUpperCase()));
    }
}

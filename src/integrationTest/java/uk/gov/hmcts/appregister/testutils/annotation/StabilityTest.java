package uk.gov.hmcts.appregister.testutils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This is a junit test annotation that tells a test to test stability i.e. runs multiple times to
 * ensure consistent results. This is useful for GET endpoints where data consistency is important.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@TestMethodOrder(MethodOrderer.Random.class) // optional
@ExtendWith(StabilityTestExtension.class)
@TestTemplate
public @interface StabilityTest {
    /** Number of times to repeat each test. Defaults to 20 */
    int times() default 20;
}

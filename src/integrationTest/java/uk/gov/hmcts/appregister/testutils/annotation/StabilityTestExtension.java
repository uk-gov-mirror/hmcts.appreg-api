package uk.gov.hmcts.appregister.testutils.annotation;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.reflect.Method;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/**
 * A junit test invoker. It runs before each test and tells the junit framework to repeat each test
 * the specified number of times. Is main purpose in life is to ensure stability across a multitude
 * of test runs. This annotation would typically be used for GET endpoints to ensure that the data
 * returned is consistent across multiple invocations.
 *
 * <p>This class acts the same as the native junit RepeatedTest annotation but can also be applied
 * at the class level. Also this test invocation has a fixed repeat count for all tests in the class
 * to ensure consistent stability across the system.
 */
public class StabilityTestExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return isAnnotated(context.getTestMethod(), StabilityTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
            ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        Class clazz = context.getRequiredTestClass();
        StabilityTest ann = (StabilityTest) clazz.getDeclaredAnnotation(StabilityTest.class);
        if (ann == null) {
            ann = method.getAnnotation(StabilityTest.class);
        }

        return IntStream.rangeClosed(1, ann.times())
                .mapToObj(
                        i ->
                                new TestTemplateInvocationContext() {
                                    @Override
                                    public String getDisplayName(int invocationIndex) {
                                        return "Repetition " + i;
                                    }
                                });
    }
}

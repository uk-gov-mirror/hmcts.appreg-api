package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister")
@Slf4j
public class SecurityRules {

    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    @ArchTest
    static final ArchRule security_on_method =
            classes()
                        .that().haveSimpleNameEndingWith("Controller")
                        .should().resideInAPackage(BASE_PACKAGE + ".(*).controller..")
                        .andShould(new PreAuthorizeCheck());

    static class PreAuthorizeCheck extends ArchCondition<JavaClass> {
        public PreAuthorizeCheck() {
            super("have a preauth check ");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (!javaClass.isInterface() && !javaClass.getName().contains("$")) {
                Annotation annotation = null;
                try {
                    annotation = javaClass.getAnnotationOfType(PreAuthorize.class);
                } catch (IllegalArgumentException e) {
                    log.warn("Class does not contain PreAuthorize annotation: {}", javaClass.getName());
                }

                // if we dont have the annotation on the class then ensure each method has it
                if (annotation == null) {
                    for (JavaMethod method : javaClass.getMethods()) {

                        if (isPublic(method)) {
                            boolean methodAnnotation = method.isAnnotatedWith(PreAuthorize.class);

                            if (methodAnnotation) {
                                events.add(SimpleConditionEvent.violated(
                                    javaClass,
                                    "Method %s does not have @PreAuthorize annotation".formatted(javaClass.getName()
                                                                                                     + " " + method.getName())
                                ));
                            }
                        }
                    }
                }
            }
        }

        private boolean isPublic(JavaMethod javaMethod) {
            return "PUBLIC".equals(javaMethod.getModifiers().iterator().next().name());
        }
    }
}

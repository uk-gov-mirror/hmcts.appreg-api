package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

import com.tngtech.archunit.lang.ConditionEvents;

import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister", importOptions = { ImportOption.DoNotIncludeTests.class })
public class LogRules {
    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    @ArchTest
    static final ArchRule enforce_logging_controller =
        classes().that().resideInAPackage(BASE_PACKAGE + ".(*).controller..")
            .should(new LoggingCondition());

    @ArchTest
    static final ArchRule enforce_logging_service =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".(*).service..")
            .should(new LoggingCondition());

    static class LoggingCondition extends ArchCondition<JavaClass> {
        public LoggingCondition() {
            super("have a log field");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (!javaClass.isInterface() && !javaClass.getName().contains("$")) {
                boolean hasLogField = javaClass.getFields().stream()
                    .anyMatch(f -> f.getName().equals("log"));

                if (!hasLogField) {
                    events.add(SimpleConditionEvent.violated(
                        javaClass,
                        "Controller %s does not declare a field named 'log'".formatted(javaClass.getName())
                    ));
                }
            }
        }
    }

}

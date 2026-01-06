package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import com.tngtech.archunit.lang.ArchCondition;

import com.tngtech.archunit.lang.ConditionEvents;

import com.tngtech.archunit.lang.SimpleConditionEvent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameContaining;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister",  importOptions = { ImportOption.DoNotIncludeTests.class })
public class ConfigRules {
    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule config =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".config")
            .and(new ClassCheck())
            .should().haveSimpleNameEndingWith("Config");

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule config_annotation =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".config")
            .and(new ClassCheck())
            .should().beAnnotatedWith(Configuration.class);

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule method_bean =
        methods()
            .that().areDeclaredInClassesThat().resideInAPackage(BASE_PACKAGE + ".config..")
            .and(new ClassMethodCheck())
            .should(new BeanOrExtendClassCheck()).allowEmptyShould(true);

    static class ClassMethodCheck extends DescribedPredicate<JavaMethod> {
        public ClassMethodCheck() {
            super("have a preauth check ");
        }

        @Override
        public boolean test(JavaMethod javaClass) {
            return !javaClass.getOwner().isInterface() && !javaClass.getOwner().getName().contains("$");
        }
    }

    static class ClassCheck extends DescribedPredicate<JavaClass> {
        public ClassCheck() {
            super("have a preauth check ");
        }

        @Override
        public boolean test(JavaClass javaClass) {
            return !javaClass.isInterface() && !javaClass.getName().contains("$");
        }
    }

    static class BeanOrExtendClassCheck extends ArchCondition<JavaMethod> {
        public BeanOrExtendClassCheck() {
            super("have a preauth check ");
        }

        @Override
        public void check(JavaMethod item, ConditionEvents events) {

            boolean methodAnnotation = item.isAnnotatedWith(Bean.class);

              if (!methodAnnotation) {
                  methodAnnotation = item.getOwner().getSuperclass().isPresent();

                    if (!methodAnnotation) {
                        events.add(SimpleConditionEvent.violated(
                            item,
                            "Method %s does not have @Bean OR @Override annotation".formatted(item.getOwner().getName()
                                                                                             + " " + item.getName())
                        ));
                }
            }
        }
    }
}

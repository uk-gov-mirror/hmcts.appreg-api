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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Annotation;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameContaining;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister", importOptions = { ImportOption.DoNotIncludeTests.class })
public class DatabaseRules {
    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule jpa_entities_must_live_in_entity_package =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".common.entity")
            .and(not(simpleNameEndingWith("_")))
            .and(not(simpleNameContaining("$")))
            .should(new DatabaseCheck());

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule repository =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".common.entity.repository")
            .should().haveSimpleNameEndingWith("Repository")
            .andShould().beAssignableTo(JpaRepository.class);

    // 4) Naming conventions (optional but useful)
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule transactional_method =
        methods()
            .that().arePublic().and().areDeclaredInClassesThat()
            .resideInAPackage(BASE_PACKAGE + ".(*).service..")
            .and(new ClassMethodCheck())
            .should().beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class);

    static class DatabaseCheck extends ArchCondition<JavaClass> {
        public DatabaseCheck() {
            super("have a preauth check ");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (!javaClass.getName().contains("$")) {
                javaClass.getAnnotationOfType(Entity.class);
                javaClass.getAnnotationOfType(Table.class);

            }
        }
    }

    static class ClassMethodCheck extends DescribedPredicate<JavaMethod> {
        public ClassMethodCheck() {
            super("have a preauth check ");
        }

        @Override
        public boolean test(JavaMethod javaClass) {
            return !javaClass.getOwner().isInterface() && !javaClass.getOwner().getName().contains("$");
        }
    }
}

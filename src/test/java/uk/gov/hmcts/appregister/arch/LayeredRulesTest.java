package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import groovy.util.logging.Slf4j;
import jakarta.transaction.Transactional;

import org.junit.runner.RunWith;
import org.mapstruct.Mapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.appregister.common.api.SortableOperationEnum;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.exception.ErrorCodeEnum;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister", importOptions = { ImportOption.DoNotIncludeTests.class })
public class LayeredRulesTest {
    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    // 3) Internal layering for each feature (applied by package pattern)
    // Patterns: com.myapp.<feature>.api.., .application.., .domain.., .persistence..
    @ArchTest
    public static final com.tngtech.archunit.lang.ArchRule feature_internal_layers =
        layeredArchitecture().consideringAllDependencies()
            .layer("api").definedBy(BASE_PACKAGE + ".(*).api..")
            .layer("audit").definedBy(BASE_PACKAGE + ".(*).audit..")
            .layer("controller").definedBy(BASE_PACKAGE + ".(*).controller..")
            .layer("mapper").definedBy(BASE_PACKAGE + ".(*).mapper..")
            .layer("service").definedBy(BASE_PACKAGE + ".(*).service..")
            .layer("validator").definedBy(BASE_PACKAGE + ".(*).validator..")
            .layer("common").definedBy(BASE_PACKAGE + ".common..")
            .layer("exception").definedBy(BASE_PACKAGE + ".(*).exception..")
            .layer("generated").definedBy(BASE_PACKAGE + ".generated..")
            .whereLayer("api").mayOnlyAccessLayers("common", "service", "generated")
            .whereLayer("service")
            .mayOnlyAccessLayers("generated", "audit", "common", "mapper", "validator", "exception")
            .whereLayer("validator").mayOnlyAccessLayers("common", "generated", "exception")
            .whereLayer("mapper").mayOnlyAccessLayers("common", "generated")
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage("org.slf4j.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage("java.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage("org.springframework.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage("jakarta.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage("io.swagger.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage("org.mapstruct.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage(" org.openapitools.."));

    /*
    // 4) Naming conventions (optional but useful)
    @ArchTest
    static final com.tngtech.archunit.lang.CompositeArchRule security_on_method =
        CompositeArchRule.of(
            List.of(classes()
                        .that().haveSimpleNameEndingWith("Controller")
                        .should().resideInAPackage(BASE_PACKAGE + ".(*).controller..")
                        .andShould().beAnnotatedWith(PreAuthorize.class).allowEmptyShould(true),
                    methods()
                        .that().areDeclaredInClassesThat().resideInAPackage(BASE_PACKAGE + ".(*).controller..")
                        .should().beAnnotatedWith(PreAuthorize.class).allowEmptyShould(true)));

    // 4) Naming conventions (optional but useful)
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule sec_method =
        methods()
            .that().areDeclaredInClassesThat().resideInAPackage(BASE_PACKAGE + ".(*).controller..")
            .should().beAnnotatedWith(PreAuthorize.class).allowEmptyShould(true);


    // 4) Naming conventions (optional but useful)
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule transactional_method =
        methods()
            .that().arePublic().and().areDeclaredInClassesThat()
            .resideInAPackage(BASE_PACKAGE + ".(*).service..")
            .should().beAnnotatedWith(Transactional.class);



    // 4) Naming conventions (optional but useful)
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule audit =
        classes()
            .that().haveSimpleNameEndingWith("SortFieldEnum")
            .should().resideInAPackage(BASE_PACKAGE + ".(*).api..")
            .andShould().implement(SortableOperationEnum.class);

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule exception2 =
        classes()
            .should().resideInAPackage(BASE_PACKAGE + ".(*).api..").andShould().beAssignableTo(Enum.class);

    // 4) Naming conventions (optional but useful)
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule controllers_should_be_in_api_and_end_with_controller =
        classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage(BASE_PACKAGE + ".(*).controller..")
            .andShould().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class);

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule services_should_be_in_application_and_end_with_service =
        classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage(BASE_PACKAGE + ".(*).service..")
            .andShould().beAnnotatedWith(Component.class);

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule repositories_should_be_in_persistence_and_end_with_repository =
        classes()
            .that().haveSimpleNameEndingWith("Mapper")
            .should().resideInAPackage(BASE_PACKAGE + ".(*).mapper..")
            .andShould().beAnnotatedWith(Mapper.class);


    */
    @ArchTest
    static final SliceRule no_depends_between_features =
        SlicesRuleDefinition.slices()
            .matching(BASE_PACKAGE + ".(*)..")
            .should().notDependOnEachOther();


    @ArchTest
    static final ArchRule security_on_method =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + "..")
            .and().areAssignableFrom(Exception.class)
            .should(new ExceptionCheck());

    static class ExceptionCheck extends ArchCondition<JavaClass> {
        public ExceptionCheck() {
            super("have a preauth check ");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (!javaClass.getSuperclass().get().equals(AppRegistryException.class)) {
               events.add(SimpleConditionEvent.violated(
                                javaClass,
                                "Class %s does not extend AppRegistryException annotation".formatted(javaClass.getName())
                            ));
                    }
                }
            }
        }

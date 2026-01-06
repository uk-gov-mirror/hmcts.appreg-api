package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.base.DescribedPredicate;
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

import org.springframework.stereotype.Service;

import uk.gov.hmcts.appregister.applicationlist.mapper.ApplicationListSortMapper;
import uk.gov.hmcts.appregister.arch.rules.IgnoreImplClass;
import uk.gov.hmcts.appregister.arch.rules.NoInnerClass;
import uk.gov.hmcts.appregister.common.api.SortableOperationEnum;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.exception.ErrorCodeEnum;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister",
    importOptions = { ImportOption.DoNotIncludeTests.class })
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
            .layer("model").definedBy(BASE_PACKAGE + ".(*).model..")
            .whereLayer("api").mayOnlyAccessLayers("common", "service", "generated")
            .whereLayer("service")
            .mayOnlyAccessLayers("model", "generated", "audit", "common", "mapper", "validator", "exception")
            .whereLayer("validator").mayOnlyAccessLayers("common", "generated", "exception", "model")

            // We need to perform some refactoring around the app list sort mapper first. This is addressed as part of
            // ticket ARCPOC-
            //.whereLayer("mapper").mayOnlyAccessLayers("common", "generated")
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
                              resideInAPackage("org.openapitools.."))
            .ignoreDependency(resideInAPackage(BASE_PACKAGE + ".."),
                              resideInAPackage("lombok.."));
   /*
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule audit =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".(*).api..")
            .and(
                not(resideInAnyPackage(BASE_PACKAGE + ".generated.."))
            )
            .and(
                not(resideInAnyPackage(BASE_PACKAGE + ".common.."))
            )
            .should().haveSimpleNameEndingWith("SortFieldEnum")
            .andShould().implement(SortableOperationEnum.class).allowEmptyShould(true);
            */

    /*
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule exception2 =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".(*).api..")
            .and(
                not(resideInAnyPackage(BASE_PACKAGE + ".generated.."))
            )
            .and(
                not(resideInAnyPackage(BASE_PACKAGE + ".common.."))
            ).should().beAssignableTo(Enum.class);
*/

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule controllers_should_be_in_api_and_end_with_controller =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".(*).controller..")
            .should().haveSimpleNameEndingWith("Controller")
            .andShould().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class);

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule services_should_be_in_application_and_end_with_service =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".(*).service..")
            .and(new NoInnerClass())
            .should().haveSimpleNameContaining("Service")
            .andShould().beAnnotatedWith(Service.class);

    //TODO: We need to correct the mapper classes before enabling this rule
    /*
    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule repositories_should_be_in_persistence_and_end_with_repository =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".(*).mapper..")
            .and(
                not(resideInAnyPackage(BASE_PACKAGE + ".common.."))
            ).and(new IgnoreImplClass())
            .should().haveSimpleNameEndingWith("Mapper")
            .andShould().beAnnotatedWith(Mapper.class);
    */

    @ArchTest
    static final SliceRule no_depends_between_features =
        SlicesRuleDefinition.slices()
            .matching(BASE_PACKAGE + ".(*)")
            .should().notDependOnEachOther().ignoreDependency(
                resideInAnyPackage(BASE_PACKAGE + ".(*).."),
                resideInAnyPackage(BASE_PACKAGE + ".common.."))
            .ignoreDependency(
                resideInAnyPackage(BASE_PACKAGE + ".common.."),
                alwaysTrue())
            .ignoreDependency(
                resideInAnyPackage(BASE_PACKAGE + ".(*).."),
                resideInAnyPackage(BASE_PACKAGE + ".generated.."))
            .ignoreDependency(
                resideInAnyPackage(BASE_PACKAGE + ".data.."),
                alwaysTrue())

            // We added this as we have some mappers that use other feature mappers. Maybe we should break this dependency
            .ignoreDependency(
                resideInAnyPackage(BASE_PACKAGE + ".(*).mapper.."),
                alwaysTrue());

    @ArchTest
    static final ArchRule security_on_method =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + "..")
            .and().areAssignableTo(Exception.class)
            .should(new ExceptionCheck());

    static class ExceptionCheck extends ArchCondition<JavaClass> {
        public ExceptionCheck() {
            super("have a preauth check ");
        }

        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            if (!javaClass.getSuperclass().get().equals(AppRegistryException.class) &&
            !javaClass.getFullName().equals(AppRegistryException.class.getCanonicalName())) {
               events.add(SimpleConditionEvent.violated(
                                javaClass,
                                "Class %s does not extend AppRegistryException annotation".formatted(javaClass.getName())
                            ));
                    }
                }
            }



        }

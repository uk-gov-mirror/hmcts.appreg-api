package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.junit.ArchTest;

import org.mapstruct.Mapper;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class MapperRules {
    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule mapper_common =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".common.mapper")
            .should().haveSimpleNameEndingWith("Mapper")
            .andShould().beAnnotatedWith(Mapper.class).andShould().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".common.mapper.");

}

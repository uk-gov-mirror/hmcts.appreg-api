package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import org.mapstruct.Mapper;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister")
public class CommonRules {
    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule common_enum =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".common.enumeration")
            .should().beAssignableFrom(Enum.class);

}

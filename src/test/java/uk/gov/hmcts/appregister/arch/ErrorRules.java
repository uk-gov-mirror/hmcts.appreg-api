package uk.gov.hmcts.appregister.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import uk.gov.hmcts.appregister.common.exception.ErrorCodeEnum;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.base.DescribedPredicate.not;

@AnalyzeClasses(packages = "uk.gov.hmcts.appregister",  importOptions = { ImportOption.DoNotIncludeTests.class })
public class ErrorRules {

    private static final String BASE_PACKAGE = "uk.gov.hmcts.appregister";

    @ArchTest
    public static final com.tngtech.archunit.lang.ArchRule exception =
        classes()
            .that().resideInAPackage(BASE_PACKAGE + ".(*).exception..")
            .and(not(resideInAPackage(BASE_PACKAGE + "..common..")))
            .should().haveSimpleNameEndingWith("Error")
            .andShould().implement(ErrorCodeEnum.class);

    @ArchTest
    public static final com.tngtech.archunit.lang.ArchRule exception2 =
        classes()
                .that().resideInAPackage(BASE_PACKAGE + ".(*).exception..")
            .and(not(resideInAPackage(BASE_PACKAGE + "..common..")))
                .should().beAssignableTo(Enum.class);
}

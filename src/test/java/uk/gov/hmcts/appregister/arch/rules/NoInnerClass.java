package uk.gov.hmcts.appregister.arch.rules;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

public class NoInnerClass extends DescribedPredicate<JavaClass> {
    public NoInnerClass() {
        super("have a log field");
    }

    @Override
    public boolean test(JavaClass javaClass) {
        return  (!javaClass.isInterface() && !javaClass.getName().contains("$"));
    }
}

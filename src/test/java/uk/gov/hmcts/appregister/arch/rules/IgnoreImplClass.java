package uk.gov.hmcts.appregister.arch.rules;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

public class IgnoreImplClass extends DescribedPredicate<JavaClass> {
    public IgnoreImplClass() {
        super("have a log field");
    }

    @Override
    public boolean test(JavaClass javaClass) {
        return  (!javaClass.isInterface() && !javaClass.getName().endsWith("Impl"));
    }
}

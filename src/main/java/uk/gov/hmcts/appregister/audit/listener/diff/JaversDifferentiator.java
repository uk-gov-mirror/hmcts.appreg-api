package uk.gov.hmcts.appregister.audit.listener.diff;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import uk.gov.hmcts.appregister.common.entity.base.Keyable;
import uk.gov.hmcts.appregister.common.enumeration.CrudEnum;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.exception.CommonAppError;
import uk.gov.hmcts.appregister.common.util.ReflectionCaches;

/**
 * An implementation of AuditDifferentiator that uses Javers to determine differences between two
 * objects. See https://javers.org/documentation/getting-started/#create-javers-instance.
 *
 * <p>NOTE: At the time of writing this class does not support differentiating the values of
 * lists.As such we should mark any list as {@link org.javers.core.metamodel.annotation.DiffIgnore}
 * on the {@link uk.gov.hmcts.appregister.common.entity.base.Keyable} entities.
 */
@Slf4j
@RequiredArgsConstructor
public class JaversDifferentiator implements AuditDifferentiator {

    private static Javers JAVERS = JaversBuilder.javers().build();

    /** Represents a null value. We default to a null string. */
    public static final String EMPTY_VALUE = "null";

    /** Do we need to recurse nested objects. */
    private final boolean recurseNestedObjects;

    @Override
    public boolean doesRecurseComplexObjects() {
        return recurseNestedObjects;
    }

    @Override
    public boolean doesRecurseCollectionObjects() {
        return false;
    }

    @Override
    public List<Difference> diff(CrudEnum crudEnum, Keyable oldVal, Keyable newVal) {
        return difference(crudEnum, oldVal, newVal);
    }

    @Override
    public List<Difference> diff(CrudEnum crudEnum, Keyable newVal) {
        return difference(crudEnum, null, newVal);
    }

    /**
     * process the differences for the old and new value.
     *
     * @param oldVal The old value
     * @param newVal The new value
     */
    public List<Difference> difference(CrudEnum crudEnum, Keyable oldVal, Keyable newVal) {
        final List<Difference> diffs = new ArrayList<>();

        if (oldVal == null && newVal == null) {
            log.debug("Two null values have been detected. No differences to process");
            return List.of();
        }

        // make sure if we are comparing old or new then the ids match
        if (newVal != null
                && oldVal != null
                && (newVal.getId() == null || !newVal.getId().equals(oldVal.getId()))) {
            log.debug("Expected the same key {} {}", newVal.getId(), oldVal.getId());
            throw new AppRegistryException(
                    CommonAppError.INTERNAL_SERVER_ERROR,
                    "Cannot compare objects with different keys");
        }

        // make sure if we are comparing old or new then the types match
        if ((newVal != null
                && oldVal != null
                && !newVal.getClass()
                        .getCanonicalName()
                        .equals(oldVal.getClass().getCanonicalName()))) {
            log.debug("Expected the same key {} {}", newVal.getId(), oldVal.getId());
            throw new AppRegistryException(
                    CommonAppError.INTERNAL_SERVER_ERROR,
                    "Cannot compare objects that are not the same type");
        }

        difference(
                crudEnum,
                oldVal,
                newVal,
                diffs,
                ReflectiveAuditDifferentiator.isAuditableAnnotatedForOperation(
                        crudEnum, newVal != null ? newVal.getClass() : oldVal.getClass()));

        return diffs;
    }

    /**
     * process the differences for the new value against nothing i.e. all contents show up as a
     * difference
     *
     * @param newVal The new value
     * @param differenceList the captured differences
     * @param recurseNestedObjects Whether we recurse into nested objects
     * @param recurseCollectionObjects Whether we recurse into collection objects
     */
    private void difference(
            CrudEnum crudEnum,
            Object newVal,
            List<Difference> differenceList,
            boolean recurseNestedObjects,
            boolean recurseCollectionObjects) {
        difference(
                crudEnum,
                null,
                newVal,
                differenceList,
                ReflectiveAuditDifferentiator.isAuditableAnnotatedForOperation(
                        crudEnum, newVal.getClass()));
    }

    /**
     * process the differences for the new value.
     *
     * @param oldVal The old value
     * @param newVal The new value
     * @param differenceList the captured differences
     * @param useAnnotations Whether we should use annotations to determine what diferences to
     *     capture
     */
    private void difference(
            CrudEnum crudEnum,
            Object oldVal,
            Object newVal,
            List<Difference> differenceList,
            boolean useAnnotations) {

        Diff diff = JAVERS.compare(oldVal, newVal);

        // loop through all javers value changes
        for (ValueChange objectChange : diff.getChangesByType(ValueChange.class)) {
            ReflectionCaches.MethodData methodData =
                    ReflectionCaches.getFieldForProperty(
                            objectChange.getAffectedObject().get().getClass(),
                            objectChange.getPropertyName());

            boolean auditingField =
                    shouldChangeBeProcessed(
                            oldVal != null ? oldVal.getClass() : newVal.getClass(),
                            objectChange,
                            useAnnotations,
                            crudEnum);

            if (auditingField) {
                // if the delete change is not of the top level value object then determine if it
                // needs auditing
                if (!objectChange
                        .getAffectedObject()
                        .get()
                        .getClass()
                        .getCanonicalName()
                        .equals(
                                oldVal != null
                                        ? oldVal.getClass().getCanonicalName()
                                        : newVal.getClass().getCanonicalName())) {

                    // check if we need to audit this field or not
                    if (!useAnnotations || (useAnnotations && auditingField)) {
                        Difference difference =
                                new Difference(
                                        methodData.tableName(),
                                        methodData.columnName(),
                                        objectChange.getLeft() == null
                                                ? EMPTY_VALUE
                                                : objectChange.getLeft().toString(),
                                        objectChange.getRight() == null
                                                ? EMPTY_VALUE
                                                : objectChange.getRight().toString());
                        differenceList.add(difference);
                    }
                } else {
                    Difference difference =
                            new Difference(
                                    methodData.tableName(),
                                    methodData.columnName(),
                                    objectChange.getLeft() == null
                                            ? EMPTY_VALUE
                                            : objectChange.getLeft().toString(),
                                    objectChange.getRight() == null
                                            ? EMPTY_VALUE
                                            : objectChange.getRight().toString());
                    differenceList.add(difference);
                }
            } else {
                log.debug(
                        "Skipping auditing for field {} on class {}",
                        objectChange.getPropertyName(),
                        objectChange.getAffectedObject().get().getClass());
            }
        }
    }

    /**
     * should we process the change diff.
     *
     * @param compareClass The top level class that we are processing
     * @param change The change diff
     * @param useAnnotations Whether to use audit annotations
     * @param crudEnum The Crud operation we should audit for
     */
    private boolean shouldChangeBeProcessed(
            Class<?> compareClass, ValueChange change, boolean useAnnotations, CrudEnum crudEnum) {
        ReflectionCaches.MethodData methodData =
                ReflectionCaches.getFieldForProperty(
                        change.getAffectedObject().get().getClass(), change.getPropertyName());

        // if we use annotations to determine processing, check that we have an annotation for the
        // change property
        if (useAnnotations) {
            boolean applicableToCrud =
                    ReflectiveAuditDifferentiator.isFieldAnnotatedForCrudAuditOperation(
                            methodData.field(), crudEnum);

            if (!applicableToCrud) {
                return false;
            }
        }

        // if the change class is not the top level object then we need to check the nested flag to
        // see if it is
        // permitted
        if (!compareClass.equals(change.getAffectedObject().get().getClass())) {
            return isComplexWrapper(change.getAffectedObject().get().getClass())
                    && recurseNestedObjects;
        }

        return true;
    }

    /**
     * Checks if this type is complex i.e. a collection or a keyable object
     *
     * @param type The type to check
     * @return True or false
     */
    public static boolean isComplexWrapper(Class<?> type) {
        log.debug("Is complex : {} {}", type.toString(), Keyable.class.isAssignableFrom(type));
        return isCollection(type) || Keyable.class.isAssignableFrom(type);
    }

    /**
     * Checks if this type is a collection i.e. list
     *
     * @param type The type to check
     * @return True or false
     */
    public static boolean isCollection(Class<?> type) {
        log.debug("Is complex : {} {}", type.toString(), Keyable.class.isAssignableFrom(type));
        return List.class.isAssignableFrom(type);
    }
}

package uk.gov.hmcts.appregister.audit.listener.diff;

import java.util.List;
import uk.gov.hmcts.appregister.common.entity.base.Keyable;
import uk.gov.hmcts.appregister.common.enumeration.CrudEnum;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.exception.CommonAppError;

/**
 * Very similar to a @{link Comparable}, but for diffing purposes.
 */
public interface AuditDifferentiable extends Keyable {
    /**
     * establish the difference between this object and existing.
     *
     * @param crudEnum The audit CRUD operation being performed
     * @param existing The other object to diff against
     * @return The list of differences that exist at the field level
     */
    default List<Difference> diff(CrudEnum crudEnum, Keyable existing) {
        if (!(existing instanceof Difference)) {
            throw new AppRegistryException(
                    CommonAppError.INTERNAL_SERVER_ERROR, "Usage error. Cant establish diff");
        }
        return ReflectiveAuditDifferentiator.difference(crudEnum, existing, this, false, false);
    }
}

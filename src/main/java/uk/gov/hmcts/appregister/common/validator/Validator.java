package uk.gov.hmcts.appregister.common.validator;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A generic validation interface.
 */
@FunctionalInterface
public interface Validator<T, O> {

    /**
     * Validate the provided {@code validatable}.
     *
     * @param validatable The object to validate
     *
     * <p>The general convention is to throw a {@link
     * uk.gov.hmcts.appregister.common.exception.AppRegistryException} if validation fails, but this
     * is not enforced by this interface.
     *
     * @throws uk.gov.hmcts.appregister.common.exception.AppRegistryException if validation fails
     */
    void validate(T validatable);

    /**
     * Validate the provided {@code validatable}.
     *
     * @param validatable The object to validate
     * @param validateSuccess Callback to be executed if validation is successful
     *
     * <p>The general convention is to throw a {@link
     * uk.gov.hmcts.appregister.common.exception.AppRegistryException} if validation fails, but this
     * is not enforced by this interface.
     *
     * @throws uk.gov.hmcts.appregister.common.exception.AppRegistryException if validation fails
     */
    <R> R validate(T validatable, BiFunction<T, O, R> validateSuccess);
}

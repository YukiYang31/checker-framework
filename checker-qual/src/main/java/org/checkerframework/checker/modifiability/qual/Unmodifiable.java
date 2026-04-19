package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Convenience alias meaning {@code @Ungrowable @Unshrinkable @Unreplaceable}. Calling any mutating
 * operation on this collection (growing, shrinking, or replacing) will throw {@link
 * UnsupportedOperationException}.
 *
 * <p>This annotation is not part of the type hierarchy; the Modifiability Checker expands it to
 * {@code @Ungrowable @Unshrinkable @Unreplaceable} on each annotated type.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Unmodifiable {}

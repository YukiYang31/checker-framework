package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

/**
 * Convenience alias unually meaning {@code @Ungrowable @Unshrinkable @Unreplaceable}. Calling any
 * mutating operation on this collection (growing, shrinking, or replacing) may throw {@link
 * UnsupportedOperationException}.
 *
 * <p>As an exception, when written on an {@link Iterator}, this means
 * {@code @Unknowngrowable @Unshrinkable @Unknownreplaceable}. The reason is that every iterator is
 * always {@code @Unknowngrowable} and {@code @Unknownreplaceable}, because {@link Iterator} has no
 * growing or replacing operations.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
public @interface Unmodifiable {}

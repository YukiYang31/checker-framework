package org.checkerframework.checker.modifiability.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

/**
 * The bottom qualifier for the modifiability hierarchy. It is a subtype of both {@link Modifiable}
 * and {@link Unmodifiable}. This qualifier is used internally by the Checker Framework to represent
 * unreachable or contradictory states.
 *
 * <p>Programmers should not write this annotation explicitly.
 *
 * @checker_framework.manual #modifiability-checker Modifiability Checker
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@SubtypeOf({Modifiable.class, Unmodifiable.class})
@InvisibleQualifier
public @interface BottomModifiable {}

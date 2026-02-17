package org.checkerframework.checker.modifiability;

import com.sun.source.tree.MethodInvocationTree;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.modifiability.qual.ThrowsUOE;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.TreeUtils;

/** Visitor for the {@link ModifiabilityChecker}. */
public class ModifiabilityVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {

  /** Create a visitor for the Modifiability Checker. */
  // MDE: Why is the type of `checker` BaseTypeChecker rather than ModifiabilityChecker?
  public ModifiabilityVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  @Override
  public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
    ExecutableElement method = TreeUtils.elementFromUse(node);
    // Check if the method being invoked is annotated with @ThrowsUOE.
    // Methods with this annotation (like SortedSet.addFirst) are guaranteed to throw
    // UnsupportedOperationException at run time, so we report an error immediately.
    if (atypeFactory.getDeclAnnotation(method, ThrowsUOE.class) != null) {
      checker.reportError(node, "usage.throws.uoe", method.getSimpleName());
    }
    return super.visitMethodInvocation(node, p);
  }

  // MDE: Ensure that there are tests that no unsoundness results.  (I didn't check whether they
  // exist.)
  // Suppress the framework's "constructor result must be TOP" check.
  // For Modifiability, constructors may legitimately produce @Modifiable.
  // By default, the BaseTypeChecker requires constructors to return the top type
  // (here @UnknownModifiability). However, many collection constructors (like new ArrayList())
  // produce a @Modifiable object, which is a subtype of @UnknownModifiability.
  // We want to allow this so that we can use these objects for mutation without
  // explicit casting or annotations.
  @Override
  protected void checkConstructorResult(
      AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {}
}

package org.checkerframework.checker.modifiability;

import javax.lang.model.element.ExecutableElement;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;

/** Visitor for the {@link ModifiabilityChecker}. */
public class ModifiabilityVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {

  /**
   * Creates a {@link ModifiabilityVisitor}.
   *
   * @param checker the checker that uses this visitor
   */
  public ModifiabilityVisitor(BaseTypeChecker checker) {
    super(checker);
  }

  @Override
  protected void checkConstructorResult(
      AnnotatedExecutableType constructorType, ExecutableElement constructorElement) {}
}

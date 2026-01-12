package org.checkerframework.checker.modifiability;

import com.sun.source.tree.ExpressionTree;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory.ParameterizedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.TreeUtils;

public class ModifiabilityAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  private final ExecutableElement iteratorMethodElement;
  private final AnnotationMirror UNMODIFIABLE;
  private final AnnotationMirror UNKNOWN_MODIFIABILITY;

  @SuppressWarnings("this-escape")
  public ModifiabilityAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    this.iteratorMethodElement =
        TreeUtils.getMethod("java.lang.Iterable", "iterator", 0, processingEnv);
    this.UNMODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), Unmodifiable.class);
    this.UNKNOWN_MODIFIABILITY =
        AnnotationBuilder.fromClass(getElementUtils(), UnknownModifiability.class);
    postInit();
  }

  @Override
  public ParameterizedExecutableType methodFromUse(
      ExpressionTree tree,
      ExecutableElement methodElt,
      AnnotatedTypeMirror receiverType,
      boolean inferTypeArgs) {

    ParameterizedExecutableType mType =
        super.methodFromUse(tree, methodElt, receiverType, inferTypeArgs);

    if (TreeUtils.isMethodInvocation(tree, iteratorMethodElement, processingEnv)) {

      AnnotatedTypeMirror returnType = mType.executableType.getReturnType();

      if (receiverType.hasPrimaryAnnotation(Unmodifiable.class)) {
        returnType.replaceAnnotation(UNMODIFIABLE);
      } else if (receiverType.hasPrimaryAnnotation(Modifiable.class)) {
        returnType.replaceAnnotation(UNKNOWN_MODIFIABILITY);
      }
    }

    return mType;
  }
}

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
    // Cache the Element for java.lang.Iterable.iterator() to allow fast comparisons
    // later.
    this.iteratorMethodElement =
        TreeUtils.getMethod("java.lang.Iterable", "iterator", 0, processingEnv);
    // Cache annotation mirrors for performance.
    this.UNMODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), Unmodifiable.class);
    this.UNKNOWN_MODIFIABILITY =
        AnnotationBuilder.fromClass(getElementUtils(), UnknownModifiability.class);
    addAliasedTypeAnnotation(Unmodifiable.class, UNKNOWN_MODIFIABILITY);
    postInit();
  }

  /**
   * Refines the return type of the {@code iterator()} method based on the receiver's modifiability.
   *
   * <p>If a collection is {@code @Unmodifiable}, its iterator should also be treated as
   * {@code @Unmodifiable} (meaning {@code remove()} cannot be called).
   *
   * <p>If a collection is {@code @Modifiable}, its iterator is {@code @UnknownModifiability} (the
   * default behavior), which still permits calling {@code remove()}.
   */
  @Override
  public ParameterizedExecutableType methodFromUse(
      ExpressionTree tree,
      ExecutableElement methodElt,
      AnnotatedTypeMirror receiverType,
      boolean inferTypeArgs) {

    ParameterizedExecutableType mType =
        super.methodFromUse(tree, methodElt, receiverType, inferTypeArgs);

    // Special handling for the iterator() method.
    // We want the modifiability of the Iterator to match the modifiability of the
    // Collection.
    if (TreeUtils.isMethodInvocation(tree, iteratorMethodElement, processingEnv)) {

      AnnotatedTypeMirror returnType = mType.executableType.getReturnType();

      if (receiverType.hasPrimaryAnnotation(Unmodifiable.class)) {
        // collection.iterator() on an @Unmodifiable collection returns an @Unmodifiable
        // Iterator.
        returnType.replaceAnnotation(UNMODIFIABLE);
      } else if (receiverType.hasPrimaryAnnotation(Modifiable.class)) {
        // collection.iterator() on a @Modifiable collection returns an
        // @UnknownModifiability Iterator
        // (defaulting to bottom in this hierarchy is treated as safe for use).
        returnType.replaceAnnotation(UNKNOWN_MODIFIABILITY);
      }
    }

    return mType;
  }
}

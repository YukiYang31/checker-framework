package org.checkerframework.checker.modifiability;

import com.sun.source.tree.ExpressionTree;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.modifiability.qual.GrowReplace;
import org.checkerframework.checker.modifiability.qual.GrowShrink;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.ShrinkReplace;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory.ParameterizedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.TreeUtils;

/** The type factory for the Modifiability Checker. */
public class ModifiabilityAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The {@code Collection.iterator()} method. */
  private final ExecutableElement iteratorMethodElement;

  /** The erased {@code java.util.Set} type. */
  private final TypeMirror setErasure;

  /** The erased {@code java.util.Queue} type. */
  private final TypeMirror queueErasure;

  /** The erased {@code java.util.LinkedList} type. */
  private final TypeMirror linkedListErasure;

  /** The erased {@code java.util.Map.Entry} type. */
  private final TypeMirror mapEntryErasure;

  /** The erased {@code java.util.Iterator} type. */
  private final TypeMirror iteratorErasure;

  /** The {@code @}{@link UnknknownModifiability} qualifier. */
  private final AnnotationMirror UNKNOWN_MODIFIABILITY;

  /** The {@code @}{@link Modifiable} qualifier. */
  private final AnnotationMirror MODIFIABLE;

  /** The {@code @}{@link GrowShrink} qualifier. */
  private final AnnotationMirror GROW_SHRINK;

  /** The {@code @}{@link GrowReplace} qualifier. */
  private final AnnotationMirror GROW_REPLACE;

  /** The {@code @}{@link ShrinkReplace} qualifier. */
  private final AnnotationMirror SHRINK_REPLACE;

  /** The {@code @}{@link ShrinkReplace} qualifier. */
  private final AnnotationMirror GROWABLE;

  /** The {@code @}{@link Growable} qualifier. */
  private final AnnotationMirror SHRINKABLE;

  /** The {@code @}{@link Replaceable} qualifier. */
  private final AnnotationMirror REPLACEABLE;

  /** The {@code @}{@link PolyModifiable} qualifier. */
  private final AnnotationMirror POLY_MODIFIABLE;

  @SuppressWarnings("this-escape")
  public ModifiabilityAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    // Cache the Element for java.lang.Iterable.iterator() to allow fast comparisons
    // later.
    this.iteratorMethodElement =
        TreeUtils.getMethod("java.lang.Iterable", "iterator", 0, processingEnv);
    // Cache types
    Types types = getProcessingEnv().getTypeUtils();
    this.setErasure = types.erasure(getElementUtils().getTypeElement("java.util.Set").asType());
    this.queueErasure = types.erasure(getElementUtils().getTypeElement("java.util.Queue").asType());
    this.linkedListErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.LinkedList").asType());
    this.mapEntryErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Map.Entry").asType());
    this.iteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Iterator").asType());

    // Cache annotation mirrors for performance.
    this.UNKNOWN_MODIFIABILITY =
        AnnotationBuilder.fromClass(getElementUtils(), UnknownModifiability.class);
    this.MODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), Modifiable.class);
    this.GROW_SHRINK = AnnotationBuilder.fromClass(getElementUtils(), GrowShrink.class);
    this.GROW_REPLACE = AnnotationBuilder.fromClass(getElementUtils(), GrowReplace.class);
    this.SHRINK_REPLACE = AnnotationBuilder.fromClass(getElementUtils(), ShrinkReplace.class);
    this.GROWABLE = AnnotationBuilder.fromClass(getElementUtils(), Growable.class);
    this.SHRINKABLE = AnnotationBuilder.fromClass(getElementUtils(), Shrinkable.class);
    this.REPLACEABLE = AnnotationBuilder.fromClass(getElementUtils(), Replaceable.class);
    this.POLY_MODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), PolyModifiable.class);

    addAliasedTypeAnnotation(Unmodifiable.class, UNKNOWN_MODIFIABILITY);
    postInit();
  }

  @Override
  protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    return new LinkedHashSet<>(
        Arrays.asList(
            UnknownModifiability.class,
            Modifiable.class,
            PolyModifiable.class,
            GrowReplace.class,
            GrowShrink.class,
            Growable.class,
            Replaceable.class,
            ShrinkReplace.class,
            Shrinkable.class));
  }

  /**
   * Refines the return type of the {@code iterator()} method based on the receiver's modifiability.
   *
   * <p>If a collection is {@code @Unmodifiable}, its iterator should also be treated as
   * {@code @Unmodifiable} (meaning {@code remove()} cannot be called).
   *
   * <p>If a collection is {@code @Modifiable}, its iterator can be either Modifiable or
   * Unmodifiable, depending on the iterator's implementation. To be conservative, we treat it as
   * {@code @UnknownModifiability}, which is a supertype of both.
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
        // Collection.iterator() on an @Unmodifiable collection returns an @Unmodifiable
        // Iterator.
        returnType.replaceAnnotation(UNKNOWN_MODIFIABILITY);
      } else if (receiverType.hasPrimaryAnnotation(Modifiable.class)) {
        // Collection.iterator() on a @Modifiable collection returns an
        // @UnknownModifiability Iterator
        // (defaulting to bottom in this hierarchy is treated as safe for use).
        returnType.replaceAnnotation(UNKNOWN_MODIFIABILITY);
      }
    }

    return mType;
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new ModifiabilityTypeAnnotator(this), super.createTypeAnnotator());
  }

  private class ModifiabilityTypeAnnotator extends TypeAnnotator {
    public ModifiabilityTypeAnnotator(ModifiabilityAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
      super.visitDeclared(type, p);

      // Skip if polymorphic.
      if (type.hasPrimaryAnnotation(POLY_MODIFIABLE)) {
        return null;
      }

      TypeMirror underlyingType = type.getUnderlyingType();
      Types types = getProcessingEnv().getTypeUtils();
      TypeMirror erasure = types.erasure(underlyingType);

      if (types.isSubtype(erasure, setErasure)
          || (types.isSubtype(erasure, queueErasure)
              && !types.isSubtype(erasure, linkedListErasure))) {
        // Set or Queue (but not LinkedList): Drop R bit
        removeReplaceable(type);
      } else if (types.isSubtype(erasure, mapEntryErasure)) {
        // Map.Entry: Drop G and S bits
        removeGrowable(type);
        removeShrinkable(type);
      } else if (types.isSubtype(erasure, iteratorErasure)) {
        // Iterator: Drop G and R bits
        removeGrowable(type);
        removeReplaceable(type);
      }

      return null;
    }
  }

  // The text about "bits" comes out of the blue.  It should have documentation, or it should
  // cross-reference documentation.
  // Helper to remove 'Grow' capability (bit 100)
  private void removeGrowable(AnnotatedTypeMirror type) {
    if (type.hasPrimaryAnnotation(GROWABLE)) { // 100
      type.replaceAnnotation(UNKNOWN_MODIFIABILITY); // 000
    } else if (type.hasPrimaryAnnotation(GROW_SHRINK)) { // 110
      type.replaceAnnotation(SHRINKABLE); // 010
    } else if (type.hasPrimaryAnnotation(GROW_REPLACE)) { // 101
      type.replaceAnnotation(REPLACEABLE); // 001
    } else if (type.hasPrimaryAnnotation(MODIFIABLE)) { // 111
      type.replaceAnnotation(SHRINK_REPLACE); // 011
    }
  }

  // Helper to remove 'Shrink' capability (bit 010)
  private void removeShrinkable(AnnotatedTypeMirror type) {
    if (type.hasPrimaryAnnotation(SHRINKABLE)) { // 010
      type.replaceAnnotation(UNKNOWN_MODIFIABILITY); // 000
    } else if (type.hasPrimaryAnnotation(GROW_SHRINK)) { // 110
      type.replaceAnnotation(GROWABLE); // 100
    } else if (type.hasPrimaryAnnotation(SHRINK_REPLACE)) { // 011
      type.replaceAnnotation(REPLACEABLE); // 001
    } else if (type.hasPrimaryAnnotation(MODIFIABLE)) { // 111
      type.replaceAnnotation(GROW_REPLACE); // 101
    }
  }

  // Helper to remove 'Replace' capability (bit 001)
  private void removeReplaceable(AnnotatedTypeMirror type) {
    if (type.hasPrimaryAnnotation(REPLACEABLE)) { // 001
      type.replaceAnnotation(UNKNOWN_MODIFIABILITY); // 000
    } else if (type.hasPrimaryAnnotation(GROW_REPLACE)) { // 101
      type.replaceAnnotation(GROWABLE); // 100
    } else if (type.hasPrimaryAnnotation(SHRINK_REPLACE)) { // 011
      type.replaceAnnotation(SHRINKABLE); // 010
    } else if (type.hasPrimaryAnnotation(MODIFIABLE)) { // 111
      type.replaceAnnotation(GROW_SHRINK); // 110
    }
  }
}

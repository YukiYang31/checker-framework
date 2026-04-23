package org.checkerframework.checker.modifiability.shrink;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.modifiability.ModifiabilityMethodUtils;
import org.checkerframework.checker.modifiability.qual.BottomShrink;
import org.checkerframework.checker.modifiability.qual.IteratorPreserveRemove;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.PolyShrink;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.UnknownShrink;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.checker.modifiability.qual.Unshrinkable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory.ParameterizedExecutableType;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.framework.type.QualifierUpperBounds;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationMirrorSet;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.TypesUtils;

/** The type factory for the Modifiability Checker. */
public class ShrinkAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The erased {@code java.util.Map.Entry} type. */
  private final TypeMirror mapEntryErasure;

  /** The erased {@code java.util.Iterator} type. */
  private final TypeMirror iteratorErasure;

  // ── Hierarchy qualifiers ──────────

  /** The {@code @}{@link UnknownShrink} qualifier (top of Shrink hierarchy). */
  private AnnotationMirror UNKNOWN_SHRINK;

  /** The {@code @}{@link Shrinkable} qualifier. */
  private AnnotationMirror SHRINKABLE;

  /** The {@code @}{@link Unshrinkable} qualifier. */
  private AnnotationMirror UNSHRINKABLE;

  /** The {@code @}{@link PolyShrink} qualifier. */
  private AnnotationMirror POLY_SHRINK;

  /** The {@code @}{@link IteratorPreserveRemove} marker annotation. */
  private AnnotationMirror ITERATOR_PRESERVE_REMOVE;

  /**
   * Creates a ShrinkAnnotatedTypeFactory.
   *
   * @param checker the associated type-checker
   */
  @SuppressWarnings("this-escape")
  public ShrinkAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    // Cache type erasures.
    Types types = getProcessingEnv().getTypeUtils();
    this.mapEntryErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Map.Entry").asType());
    this.iteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Iterator").asType());

    // Initialize annotation mirrors after the hierarchy is established.
    this.UNKNOWN_SHRINK = AnnotationBuilder.fromClass(getElementUtils(), UnknownShrink.class);
    this.SHRINKABLE = AnnotationBuilder.fromClass(getElementUtils(), Shrinkable.class);
    this.UNSHRINKABLE = AnnotationBuilder.fromClass(getElementUtils(), Unshrinkable.class);
    this.POLY_SHRINK = AnnotationBuilder.fromClass(getElementUtils(), PolyShrink.class);
    this.ITERATOR_PRESERVE_REMOVE =
        AnnotationBuilder.fromClass(getElementUtils(), IteratorPreserveRemove.class);

    addAliasedTypeAnnotation(Modifiable.class, SHRINKABLE);
    addAliasedTypeAnnotation(Unmodifiable.class, UNSHRINKABLE);
    addAliasedTypeAnnotation(UnknownModifiability.class, UNKNOWN_SHRINK);
    addAliasedTypeAnnotation(PolyModifiable.class, POLY_SHRINK);
    postInit();
  }

  @Override
  protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    return new LinkedHashSet<>(
        Arrays.asList(
            UnknownShrink.class,
            Shrinkable.class,
            Unshrinkable.class,
            BottomShrink.class,
            PolyShrink.class));
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new ShrinkTypeAnnotator(this), super.createTypeAnnotator());
  }

  @Override
  protected ParameterizedExecutableType methodFromUse(
      MethodInvocationTree tree, boolean inferTypeArgs) {
    ParameterizedExecutableType mType = super.methodFromUse(tree, inferTypeArgs);
    AnnotatedExecutableType method = mType.executableType();

    if (isIteratorMethod(tree, method)) {
      refineIteratorReturnType(tree, method);
    }

    if (!ModifiabilityMethodUtils.isCollectionsPlumeWithoutDuplicates(tree)) {
      return mType;
    }

    AnnotatedTypeMirror argumentType = getAnnotatedType(tree.getArguments().get(0));
    if (argumentType.hasPrimaryAnnotation(SHRINKABLE)) {
      method.getReturnType().replaceAnnotation(SHRINKABLE);
    } else {
      method.getReturnType().replaceAnnotation(UNKNOWN_SHRINK);
    }
    return mType;
  }

  /**
   * Refines {@code iterator()} return type based on {@code @IteratorPreserveRemove}.
   *
   * <p>If the receiver is {@code @Shrinkable} and either the receiver type use or the invoked
   * method receiver type has {@code @IteratorPreserveRemove}, then the result is {@code @Shrinkable
   * Iterator}. Otherwise, shrinkability precision is dropped to {@code @UnknownShrink}.
   */
  private void refineIteratorReturnType(
      MethodInvocationTree tree, AnnotatedExecutableType methodType) {
    AnnotatedTypeMirror returnType = methodType.getReturnType();
    if (returnType.hasPrimaryAnnotation(UNSHRINKABLE)
        || returnType.hasPrimaryAnnotation(SHRINKABLE)) {
      // Keep explicit unshrinkable iterator contracts (for example, CopyOnWriteArrayList).
      return;
    }

    Tree receiverTree = TreeUtils.getReceiverTree(tree);
    if (receiverTree == null) {
      return;
    }
    AnnotatedTypeMirror receiverType = getAnnotatedType(receiverTree);

    if (receiverType.hasPrimaryAnnotation(UNSHRINKABLE)) {
      returnType.replaceAnnotation(UNSHRINKABLE);
    }

    if (!receiverType.hasPrimaryAnnotation(SHRINKABLE)) {
      return;
    }

    // receiver type is @Shrinkable. check for @IteratorPreserveRemove
    ExecutableElement invokedMethod = TreeUtils.elementFromUse(tree);
    boolean receiverTreeHasMarker =
        AnnotationUtils.containsSameByClass(
            TreeUtils.typeOf(receiverTree).getAnnotationMirrors(), IteratorPreserveRemove.class);
    AnnotatedTypeMirror methodReceiverType = methodType.getReceiverType();
    boolean methodElementReceiverHasMarker =
        invokedMethod != null
            && AnnotationUtils.containsSameByClass(
                invokedMethod.getReceiverType().getAnnotationMirrors(),
                IteratorPreserveRemove.class);
    boolean preserveRemove =
        hasIteratorPreserveRemove(receiverType)
            || receiverTreeHasMarker
            || (methodReceiverType != null && hasIteratorPreserveRemove(methodReceiverType))
            || methodElementReceiverHasMarker;

    if (preserveRemove) {
      returnType.replaceAnnotation(SHRINKABLE);
    } else {
      returnType.replaceAnnotation(UNKNOWN_SHRINK);
    }
  }

  /**
   * Returns true if this invocation is an instance {@code iterator()} method returning Iterator.
   */
  private boolean isIteratorMethod(MethodInvocationTree tree, AnnotatedExecutableType methodType) {
    if (!tree.getArguments().isEmpty()) {
      return false;
    }
    ExecutableElement invokedMethod = TreeUtils.elementFromUse(tree);
    if (invokedMethod == null || !invokedMethod.getSimpleName().contentEquals("iterator")) {
      return false;
    }
    TypeMirror returnUnderlying = methodType.getReturnType().getUnderlyingType();
    return TypesUtils.isErasedSubtype(returnUnderlying, iteratorErasure, types);
  }

  /** Returns true if {@code type} has the {@code @IteratorPreserveRemove} marker annotation. */
  private boolean hasIteratorPreserveRemove(AnnotatedTypeMirror type) {
    if (type.hasPrimaryAnnotation(ITERATOR_PRESERVE_REMOVE)) {
      return true;
    }
    return AnnotationUtils.containsSameByClass(
        type.getUnderlyingType().getAnnotationMirrors(), IteratorPreserveRemove.class);
  }

  /**
   * Removes capabilities that cannot be supported by structural constraints of the collection type:
   *
   * <ul>
   *   <li>Map.Entry: remove Shrink capabilities
   * </ul>
   */
  private class ShrinkTypeAnnotator extends TypeAnnotator {

    /**
     * Creates a new ShrinkTypeAnnotator.
     *
     * @param factory the associated type factory
     */
    public ShrinkTypeAnnotator(ShrinkAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
      super.visitDeclared(type, p);

      // Skip structural refinement for polymorphic types.
      if (type.hasPrimaryAnnotation(POLY_SHRINK)) {
        return null;
      }

      TypeMirror underlyingType = type.getUnderlyingType();

      if (TypesUtils.isErasedSubtype(underlyingType, mapEntryErasure, types)) {
        // Map.Entry: no shrink.
        type.replaceAnnotation(UNKNOWN_SHRINK);
      }

      return null;
    }
  }

  @Override
  protected QualifierUpperBounds createQualifierUpperBounds() {
    return new QualifierUpperBounds(this) {
      private final AnnotationMirrorSet unknownShrink =
          AnnotationMirrorSet.singleton(UNKNOWN_SHRINK);

      @Override
      public AnnotationMirrorSet getBoundQualifiers(TypeMirror type) {
        if (TypesUtils.isErasedSubtype(type, mapEntryErasure, types)) {
          // Elements of a map entry can never be shrunk, so treat them as @UnknownShrink. Even if
          // they are annotation @Growable in a stubfile.
          return unknownShrink;
        }
        return super.getBoundQualifiers(type);
      }
    };
  }
}

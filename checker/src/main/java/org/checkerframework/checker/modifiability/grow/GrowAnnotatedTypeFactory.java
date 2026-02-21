package org.checkerframework.checker.modifiability.grow;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyGrow;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.UnknownGrow;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;

/** The type factory for the Modifiability Checker. */
public class GrowAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

  /** The erased {@code java.util.Map.Entry} type. */
  private final TypeMirror mapEntryErasure;

  /** The erased {@code java.util.Iterator} type. */
  private final TypeMirror iteratorErasure;

  // ── Hierarchy qualifiers ──────────

  /** The {@code @}{@link UnknownGrow} qualifier (top of Grow hierarchy). */
  private AnnotationMirror UNKNOWN_GROW;

  /** The {@code @}{@link Growable} qualifier (bottom of Grow hierarchy). */
  private AnnotationMirror GROWABLE;

  /** The {@code @}{@link PolyGrow} qualifier. */
  private AnnotationMirror POLY_GROW;

  @SuppressWarnings("this-escape")
  public GrowAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    // Cache type erasures.
    Types types = getProcessingEnv().getTypeUtils();
    this.mapEntryErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Map.Entry").asType());
    this.iteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Iterator").asType());

    // Initialize annotation mirrors after the hierarchy is established.
    this.UNKNOWN_GROW = AnnotationBuilder.fromClass(getElementUtils(), UnknownGrow.class);
    this.GROWABLE = AnnotationBuilder.fromClass(getElementUtils(), Growable.class);
    this.POLY_GROW = AnnotationBuilder.fromClass(getElementUtils(), PolyGrow.class);

    addAliasedTypeAnnotation(Modifiable.class, GROWABLE);
    addAliasedTypeAnnotation(Unmodifiable.class, UNKNOWN_GROW);
    addAliasedTypeAnnotation(UnknownModifiability.class, UNKNOWN_GROW);
    addAliasedTypeAnnotation(PolyModifiable.class, POLY_GROW);
    postInit();
  }

  @Override
  protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    return new LinkedHashSet<>(Arrays.asList(UnknownGrow.class, Growable.class, PolyGrow.class));
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new GrowTypeAnnotator(this), super.createTypeAnnotator());
  }

  /**
   * Removes capabilities that cannot be supported by structural constraints of the collection type:
   *
   * <ul>
   *   <li>Set or Queue (not LinkedList): remove Replace capability → set Replace to
   *       {@code @UnknownReplace}
   *   <li>Map.Entry: remove Grow and Shrink capabilities
   *   <li>Iterator: remove Grow and Replace capabilities
   * </ul>
   */
  private class GrowTypeAnnotator extends TypeAnnotator {
    public GrowTypeAnnotator(GrowAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
      super.visitDeclared(type, p);

      // Skip structural refinement for polymorphic types.
      if (type.hasPrimaryAnnotation(POLY_GROW)) {
        return null;
      }

      TypeMirror underlyingType = type.getUnderlyingType();
      Types types = getProcessingEnv().getTypeUtils();
      TypeMirror erasure = types.erasure(underlyingType);

      if (types.isSubtype(erasure, mapEntryErasure)) {
        // Map.Entry: no grow.
        type.replaceAnnotation(UNKNOWN_GROW);
      } else if (types.isSubtype(erasure, iteratorErasure)) {
        // Iterator: no grow.
        type.replaceAnnotation(UNKNOWN_GROW);
      }

      return null;
    }
  }
}

package org.checkerframework.checker.modifiability;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyGrow;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.PolyReplace;
import org.checkerframework.checker.modifiability.qual.PolyShrink;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.UnknownGrow;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.UnknownReplace;
import org.checkerframework.checker.modifiability.qual.UnknownShrink;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.framework.type.typeannotator.ListTypeAnnotator;
import org.checkerframework.framework.type.typeannotator.TypeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

/** The type factory for the Modifiability Checker. */
public class ModifiabilityAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {

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

  // ── Hierarchy qualifiers (9 total: 3 tops + 3 bottoms + 3 poly) ──────────

  /** The {@code @}{@link UnknownGrow} qualifier (top of Grow hierarchy). */
  private AnnotationMirror UNKNOWN_GROW;

  /** The {@code @}{@link UnknownShrink} qualifier (top of Shrink hierarchy). */
  private AnnotationMirror UNKNOWN_SHRINK;

  /** The {@code @}{@link UnknownReplace} qualifier (top of Replace hierarchy). */
  private AnnotationMirror UNKNOWN_REPLACE;

  /** The {@code @}{@link Growable} qualifier (bottom of Grow hierarchy). */
  private AnnotationMirror GROWABLE;

  /** The {@code @}{@link Shrinkable} qualifier (bottom of Shrink hierarchy). */
  private AnnotationMirror SHRINKABLE;

  /** The {@code @}{@link Replaceable} qualifier (bottom of Replace hierarchy). */
  private AnnotationMirror REPLACEABLE;

  /** The {@code @}{@link PolyGrow} qualifier. */
  private AnnotationMirror POLY_GROW;

  /** The {@code @}{@link PolyShrink} qualifier. */
  private AnnotationMirror POLY_SHRINK;

  /** The {@code @}{@link PolyReplace} qualifier. */
  private AnnotationMirror POLY_REPLACE;

  // ── Alias qualifiers (not in hierarchy; expanded by the tree annotator) ──

  /** The {@code @}{@link Modifiable} alias annotation. */
  private AnnotationMirror MODIFIABLE;

  /** The {@code @}{@link Unmodifiable} alias annotation. */
  private AnnotationMirror UNMODIFIABLE;

  /** The {@code @}{@link UnknownModifiability} alias annotation. */
  private AnnotationMirror UNKNOWN_MODIFIABILITY;

  /** The {@code @}{@link PolyModifiable} alias annotation. */
  private AnnotationMirror POLY_MODIFIABLE;

  @SuppressWarnings("this-escape")
  public ModifiabilityAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    // Cache type erasures.
    Types types = getProcessingEnv().getTypeUtils();
    this.setErasure = types.erasure(getElementUtils().getTypeElement("java.util.Set").asType());
    this.queueErasure = types.erasure(getElementUtils().getTypeElement("java.util.Queue").asType());
    this.linkedListErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.LinkedList").asType());
    this.mapEntryErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Map.Entry").asType());
    this.iteratorErasure =
        types.erasure(getElementUtils().getTypeElement("java.util.Iterator").asType());

    postInit();
  }

  @Override
  protected void postInit() {
    super.postInit();
    // Initialize annotation mirrors after the hierarchy is established.
    this.UNKNOWN_GROW = AnnotationBuilder.fromClass(getElementUtils(), UnknownGrow.class);
    this.UNKNOWN_SHRINK = AnnotationBuilder.fromClass(getElementUtils(), UnknownShrink.class);
    this.UNKNOWN_REPLACE = AnnotationBuilder.fromClass(getElementUtils(), UnknownReplace.class);
    this.GROWABLE = AnnotationBuilder.fromClass(getElementUtils(), Growable.class);
    this.SHRINKABLE = AnnotationBuilder.fromClass(getElementUtils(), Shrinkable.class);
    this.REPLACEABLE = AnnotationBuilder.fromClass(getElementUtils(), Replaceable.class);
    this.POLY_GROW = AnnotationBuilder.fromClass(getElementUtils(), PolyGrow.class);
    this.POLY_SHRINK = AnnotationBuilder.fromClass(getElementUtils(), PolyShrink.class);
    this.POLY_REPLACE = AnnotationBuilder.fromClass(getElementUtils(), PolyReplace.class);
    this.MODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), Modifiable.class);
    this.UNMODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), Unmodifiable.class);
    this.UNKNOWN_MODIFIABILITY =
        AnnotationBuilder.fromClass(getElementUtils(), UnknownModifiability.class);
    this.POLY_MODIFIABLE = AnnotationBuilder.fromClass(getElementUtils(), PolyModifiable.class);
  }

  @Override
  protected Set<Class<? extends Annotation>> createSupportedTypeQualifiers() {
    return new LinkedHashSet<>(
        Arrays.asList(
            // Grow hierarchy
            UnknownGrow.class,
            Growable.class,
            PolyGrow.class,
            // Shrink hierarchy
            UnknownShrink.class,
            Shrinkable.class,
            PolyShrink.class,
            // Replace hierarchy
            UnknownReplace.class,
            Replaceable.class,
            PolyReplace.class));
  }

  /**
   * Adds a tree annotator that expands alias annotations ({@code @Modifiable},
   * {@code @Unmodifiable}, {@code @UnknownModifiability}, {@code @PolyModifiable}) into their
   * constituent qualifiers across all three hierarchies.
   */
  @Override
  protected TreeAnnotator createTreeAnnotator() {
    return new ListTreeAnnotator(
        new ModifiabilityAliasAnnotator(this), super.createTreeAnnotator());
  }

  /**
   * Expands alias annotations written in source code into their equivalent multi-hierarchy
   * qualifiers:
   *
   * <ul>
   *   <li>{@code @Modifiable} → {@code @Growable @Shrinkable @Replaceable}
   *   <li>{@code @Unmodifiable} or {@code @UnknownModifiability} →
   *       {@code @UnknownGrow @UnknownShrink @UnknownReplace}
   *   <li>{@code @PolyModifiable} → {@code @PolyGrow @PolyShrink @PolyReplace}
   * </ul>
   */
  private class ModifiabilityAliasAnnotator extends TreeAnnotator {
    public ModifiabilityAliasAnnotator(AnnotatedTypeFactory factory) {
      super(factory);
    }

    /**
     * Expands alias annotations found in the given list onto the given type.
     *
     * @param rawAnnos raw annotations from the source tree
     * @param type the type to modify
     */
    private void expandAliases(
        List<? extends AnnotationMirror> rawAnnos, AnnotatedTypeMirror type) {
      for (AnnotationMirror anno : rawAnnos) {
        if (AnnotationUtils.areSame(anno, MODIFIABLE)) {
          type.replaceAnnotation(GROWABLE);
          type.replaceAnnotation(SHRINKABLE);
          type.replaceAnnotation(REPLACEABLE);
          break;
        } else if (AnnotationUtils.areSame(anno, UNMODIFIABLE)
            || AnnotationUtils.areSame(anno, UNKNOWN_MODIFIABILITY)) {
          type.replaceAnnotation(UNKNOWN_GROW);
          type.replaceAnnotation(UNKNOWN_SHRINK);
          type.replaceAnnotation(UNKNOWN_REPLACE);
          break;
        } else if (AnnotationUtils.areSame(anno, POLY_MODIFIABLE)) {
          type.replaceAnnotation(POLY_GROW);
          type.replaceAnnotation(POLY_SHRINK);
          type.replaceAnnotation(POLY_REPLACE);
          break;
        }
      }
      return super.visitAnnotatedType(node, type);
    }
  }

  @Override
  protected TypeAnnotator createTypeAnnotator() {
    return new ListTypeAnnotator(new ModifiabilityTypeAnnotator(this), super.createTypeAnnotator());
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
  private class ModifiabilityTypeAnnotator extends TypeAnnotator {
    public ModifiabilityTypeAnnotator(ModifiabilityAnnotatedTypeFactory factory) {
      super(factory);
    }

    @Override
    public Void visitDeclared(AnnotatedDeclaredType type, Void p) {
      super.visitDeclared(type, p);

      // Skip structural refinement for polymorphic types.
      if (type.hasPrimaryAnnotation(POLY_GROW)
          || type.hasPrimaryAnnotation(POLY_SHRINK)
          || type.hasPrimaryAnnotation(POLY_REPLACE)) {
        return null;
      }

      TypeMirror underlyingType = type.getUnderlyingType();
      Types types = getProcessingEnv().getTypeUtils();
      TypeMirror erasure = types.erasure(underlyingType);

      if (types.isSubtype(erasure, setErasure)
          || (types.isSubtype(erasure, queueErasure)
              && !types.isSubtype(erasure, linkedListErasure))) {
        // Set or Queue (but not LinkedList): no replace methods → remove Replace capability.
        removeReplaceable(type);
      } else if (types.isSubtype(erasure, mapEntryErasure)) {
        // Map.Entry: no grow or shrink methods → remove both capabilities.
        removeGrowable(type);
        removeShrinkable(type);
      } else if (types.isSubtype(erasure, iteratorErasure)) {
        // Iterator: no grow or replace methods → remove both capabilities.
        removeGrowable(type);
        removeReplaceable(type);
      }

      return null;
    }
  }

  /**
   * Removes the Grow capability by setting the Grow hierarchy qualifier to {@code @UnknownGrow}.
   */
  private void removeGrowable(AnnotatedTypeMirror type) {
    type.replaceAnnotation(UNKNOWN_GROW);
  }

  /**
   * Removes the Shrink capability by setting the Shrink hierarchy qualifier to
   * {@code @UnknownShrink}.
   */
  private void removeShrinkable(AnnotatedTypeMirror type) {
    type.replaceAnnotation(UNKNOWN_SHRINK);
  }

  /**
   * Removes the Replace capability by setting the Replace hierarchy qualifier to
   * {@code @UnknownReplace}.
   */
  private void removeReplaceable(AnnotatedTypeMirror type) {
    type.replaceAnnotation(UNKNOWN_REPLACE);
  }
}

package org.checkerframework.checker.modifiability;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import javax.lang.model.element.AnnotationMirror;
import org.checkerframework.checker.modifiability.qual.UnmodifiableParam;
import org.checkerframework.framework.source.SourceVisitor;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

/** Visitor for the aggregate ModifiabilityChecker. */
public class ModifiabilityVisitor extends SourceVisitor<Void, Void> {

  /** Fully-qualified name for {@link UnmodifiableParam}. */
  private static final String unmodifiableParamQualifiedName = UnmodifiableParam.class.getName();

  /** {@link ModifiabilityChecker}. */
  private final ModifiabilityChecker checker;

  /**
   * Creates a {@link SourceVisitor} to use for scanning a source tree.
   *
   * @param checker the modifiability checker to invoke on the input source tree
   */
  protected ModifiabilityVisitor(ModifiabilityChecker checker) {
    super(checker);
    this.checker = checker;
  }

  boolean inParameterType = false;

  /**
   * Collects the {@code @UnmodifiableParam} annotations that are permitted in this method's formal
   * and receiver parameters. Pushes them on the stack before visiting the method body, then pops
   * the stack. {@link #visitAnnotation} uses the stack entry to distinguish allowed parameter
   * annotations from disallowed uses elsewhere in the same method.
   */
  @Override
  public Void visitMethod(MethodTree tree, Void p) {
    storeSuppressWarningsAnno(tree);
    scan(tree.getModifiers(), p);
    scan(tree.getReturnType(), p);
    scan(tree.getTypeParameters(), p);
    inParameterType = true;
    scan(tree.getParameters(), p);
    scan(tree.getReceiverParameter(), p);
    inParameterType = false;
    scan(tree.getThrows(), p);
    scan(tree.getBody(), p);
    scan(tree.getDefaultValue(), p);
    return null;
  }

  @Override
  public Void visitAnnotation(AnnotationTree tree, Void p) {
    // The implementation of annotationFromAnnotationTree just returns a field of tree, so it's fine
    // to always get it.
    AnnotationMirror annotation = TreeUtils.annotationFromAnnotationTree(tree);
    if (!inParameterType
        && annotation != null
        && AnnotationUtils.areSameByName(annotation, unmodifiableParamQualifiedName)) {
      checker.reportError(tree, "unmodparam.location");
    }
    return super.visitAnnotation(tree, p);
  }
}

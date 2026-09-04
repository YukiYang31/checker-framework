package org.checkerframework.checker.modifiability;

import java.util.Set;
import org.checkerframework.checker.modifiability.iterator.IteratorChecker;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.source.SourceChecker;

/**
 * Base class for the Modifiability sub-checkers.
 *
 * <p>This class exists so {@link
 * org.checkerframework.framework.source.SourceChecker#getMessagesProperties()} finds the shared
 * {@code messages.properties} file in {@code org.checkerframework.checker.modifiability}. The Grow,
 * SeqGrow, Shrink, and Replace checkers all report diagnostics whose message keys are defined
 * there; without this common superclass, those keys would need to be duplicated in each
 * sub-checker's package.
 */
public abstract class ModifiabilityBaseChecker extends BaseTypeChecker {

  /** Creates a new ModifiabilityBaseChecker. */
  protected ModifiabilityBaseChecker() {}

  /**
   * Returns true if this checker refines the result of {@code iterator()} and {@code
   * listIterator()}, and therefore needs to read the Iterator Checker's qualifiers. The Iterator
   * Checker itself does not (it would be its own subchecker), and neither does the SeqGrow Checker,
   * because an iterator has no sequenced-grow methods.
   *
   * @return true if this checker needs the Iterator Checker as a subchecker
   */
  protected boolean usesIteratorChecker() {
    return true;
  }

  @Override
  protected Set<Class<? extends SourceChecker>> getImmediateSubcheckerClasses() {
    Set<Class<? extends SourceChecker>> checkers = super.getImmediateSubcheckerClasses();
    if (usesIteratorChecker()) {
      checkers.add(IteratorChecker.class);
    }
    return checkers;
  }
}

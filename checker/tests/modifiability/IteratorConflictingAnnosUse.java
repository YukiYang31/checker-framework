import java.util.Iterator;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

// line 12 is throwing an error
// IteratorConflictingAnnosUse.java:12: error: [conflicting.annos]
// when running modifiabilityTest, but this error is not thrown when running this file alone.
public class IteratorConflictingAnnosUse {
  private final IteratorConflictingAnnosTypeTuple inputTypes =
      new IteratorConflictingAnnosTypeTuple();

  @Unmodifiable Iterator<String> reproduce() {
    return inputTypes.iterator();
  }
}

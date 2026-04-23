import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

class IteratorConflictingAnnosTypeTuple implements Iterable<String> {
  private final ArrayList<String> list = new ArrayList<>();

  @Override
  public @Unmodifiable Iterator<String> iterator(IteratorConflictingAnnosTypeTuple this) {
    return Collections.unmodifiableList(list).iterator();
  }
}

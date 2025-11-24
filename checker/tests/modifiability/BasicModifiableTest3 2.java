import java.util.List;
import org.checkerframework.checker.lock.qual.GuardSatisfied;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

public class BasicModifiableTest3 {

  /*
  @ReleasesNoLocks
  @SideEffectsOnly("this")
  @EnsuresNonEmpty("this")
  */
  <E> boolean my_add(@GuardSatisfied List<E> this_one, E e) {
    return true;
  }

  void testBasicModifiable() {
    // Unmodifiable collections should not allow mutation
    @Unmodifiable List<String> unmodifiableList = List.of("test1", "test2");
    // :: error: (method.invocation)
    my_add(unmodifiableList, "test3");
  }
}

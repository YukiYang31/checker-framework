import java.util.List;
import org.checkerframework.checker.modifiability.qual.Modifiable;

class TestHierarchy {
  void testHierarchy(@Unmodifiable List<String> unmod, @Modifiable List<String> mod) {

    @Unmodifiable List<String> b;
    b = unmod;
    // :: error: (assignment)
    b = mod;

    @Modifiable List<String> c;
    // :: error: (assignment)
    c = unmod;
    c = mod;
  }
}

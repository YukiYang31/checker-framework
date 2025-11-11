import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.PolyModifiable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

public class PolyModifiableTest {
  // A simple polymorphic method
  @PolyModifiable
  Object identity(@PolyModifiable Object x) {
    return x;
  }

  void testPoly(
      @Modifiable Object mod, @Unmodifiable Object unmod, @UnknownModifiability Object any) {

    // Allowed: preserving same modifiability
    @Modifiable Object a = identity(mod);
    @Unmodifiable Object b = identity(unmod);
    @UnknownModifiability Object c = identity(any);

    // :: error: (assignment)
    @Modifiable Object d = identity(unmod); // cannot upcast from Unmodifiable → Modifiable

    // :: error: (assignment)
    @Unmodifiable Object e = identity(mod); // cannot downcast from Modifiable → Unmodifiable
  }
}

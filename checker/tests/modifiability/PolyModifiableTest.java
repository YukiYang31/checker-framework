import org.checkerframework.checker.modifiability.qual.BottomModifiable;
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
      @Modifiable Object mod,
      @Unmodifiable Object unmod,
      @UnknownModifiability Object any,
      @BottomModifiable Object bottom) {

    // Allowed: preserving same modifiability
    @Modifiable Object a = identity(mod);
    @Unmodifiable Object b = identity(unmod);
    @UnknownModifiability Object c = identity(any);
    @BottomModifiable Object d = identity(bottom);

    // :: error: (assignment)
    @Modifiable Object e = identity(unmod); // cannot upcast from Unmodifiable → Modifiable
    // :: error: (assignment)
    @Modifiable Object f = identity(any); // cannot upcast from Unknown → Modifiable
    @Modifiable Object h = identity(bottom); // ok to assign Bottom → Modifiable

    // :: error: (assignment)
    @Unmodifiable Object i = identity(mod); // cannot downcast from Modifiable → Unmodifiable
    // :: error: (assignment)
    @Unmodifiable Object j = identity(any); // cannot downcast from Unknown → Unmodifiable
    @Unmodifiable Object l = identity(bottom); // ok to assign Bottom → Unmodifiable

    @UnknownModifiability Object m = identity(mod); // ok to assign Modifiable → Unknown
    @UnknownModifiability Object n = identity(unmod); // ok to assign Unmodifiable → Unknown
    @UnknownModifiability Object o = identity(bottom); // ok to assign Bottom → Unknown

    // :: error: (assignment)
    @BottomModifiable Object p = identity(mod); // cannot upcast from Modifiable → Bottom
    // :: error: (assignment)
    @BottomModifiable Object q = identity(unmod); // cannot upcast from Unmodifiable → Bottom
    // :: error: (assignment)
    @BottomModifiable Object r = identity(any); // cannot upcast from Unknown → Bottom
  }
}

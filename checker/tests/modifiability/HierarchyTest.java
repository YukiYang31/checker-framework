import org.checkerframework.checker.modifiability.qual.BottomModifiable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

class HierarchyTest {

  void testHierarchy(
      @UnknownModifiability Object any, @Modifiable Object mod, @Unmodifiable Object unmod) {

    // --- Assignments to UnknownModifiability (top) ---
    @UnknownModifiability Object a1 = any;
    @UnknownModifiability Object a2 = mod;
    @UnknownModifiability Object a3 = unmod; // OK: both subtypes of UnknownModifiability

    // --- Assignments to Modifiable ---
    @Modifiable Object m1 = mod; // OK
    // :: error: (assignment)
    @Modifiable Object m2 = any; // error: UnknownModifiability → Modifiable not allowed
    // :: error: (assignment)
    @Modifiable Object m3 = unmod; // error: Unmodifiable → Modifiable not allowed

    // --- Assignments to Unmodifiable ---
    @Unmodifiable Object u1 = unmod; // OK
    // :: error: (assignment)
    @Unmodifiable Object u2 = any; // error: UnknownModifiability → Unmodifiable not allowed
    // :: error: (assignment)
    @Unmodifiable Object u3 = mod; // error: Modifiable → Unmodifiable not allowed

    // --- Reflexivity (bottom always OK) ---
    @BottomModifiable Object b = null;
    @Modifiable Object m4 = b; // OK: BottomModifiable <: Modifiable
    @Unmodifiable Object u4 = b; // OK: BottomModifiable <: Unmodifiable
    @UnknownModifiability Object a4 = b; // OK: BottomModifiable <: UnknownModifiability
  }
}

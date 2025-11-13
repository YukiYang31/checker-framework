import org.checkerframework.checker.modifiability.qual.BottomModifiable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

/**
 * Tests the subtyping relationships among the modifiability qualifiers: @UnknownModifiability (top)
 * / \ @Modifiable @Unmodifiable \ / @BottomModifiable (bottom)
 */
class HierarchyTest {

  void testHierarchy(
      @UnknownModifiability Object any,
      @Modifiable Object mod,
      @Unmodifiable Object unmod,
      @BottomModifiable Object b) {

    // ============================================================
    // Assignments to @UnknownModifiability (top)
    // ============================================================

    @UnknownModifiability Object a1 = any; // OK
    @UnknownModifiability Object a2 = mod; // OK: Modifiable <: Unknown
    @UnknownModifiability Object a3 = unmod; // OK: Unmodifiable <: Unknown
    @UnknownModifiability Object a4 = b; // OK: Bottom <: Unknown
    // ============================================================
    // Assignments to @Modifiable
    // ============================================================

    @Modifiable Object m1 = mod; // OK
    // :: error: (assignment)
    @Modifiable Object m2 = any; // Error: Unknown → Modifiable not allowed
    // :: error: (assignment)
    @Modifiable Object m3 = unmod; // Error: Unmodifiable → Modifiable not allowed
    @Modifiable Object m4 = b; // OK: Bottom <: Modifiable

    // ============================================================
    // Assignments to @Unmodifiable
    // ============================================================

    @Unmodifiable Object u1 = unmod; // OK
    // :: error: (assignment)
    @Unmodifiable Object u2 = any; // Error: Unknown → Unmodifiable not allowed
    // :: error: (assignment)
    @Unmodifiable Object u3 = mod; // Error: Modifiable → Unmodifiable not allowed
    @Unmodifiable Object u4 = b; // OK: Bottom <: Unmodifiable

    // ============================================================
    // Assignments to @BottomModifiable (bottom)
    // ============================================================

    @BottomModifiable Object b1 = b; // OK
    // :: error: (assignment)
    @BottomModifiable Object b2 = any; // Error: Unknown → Bottom not allowed
    // :: error: (assignment)
    @BottomModifiable Object b3 = mod; // Error: Modifiable → Bottom not allowed
    // :: error: (assignment)
    @BottomModifiable Object b4 = unmod; // Error: Unmodifiable → Bottom not allowed
  }
}

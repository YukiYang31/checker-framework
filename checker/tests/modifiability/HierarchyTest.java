import org.checkerframework.checker.modifiability.qual.BottomGrow;
import org.checkerframework.checker.modifiability.qual.BottomReplace;
import org.checkerframework.checker.modifiability.qual.BottomShrink;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Replaceable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.Ungrowable;
import org.checkerframework.checker.modifiability.qual.UnknownGrow;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.UnknownReplace;
import org.checkerframework.checker.modifiability.qual.UnknownShrink;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;
import org.checkerframework.checker.modifiability.qual.Unreplaceable;
import org.checkerframework.checker.modifiability.qual.Unshrinkable;

/**
 * Tests the three independent 4-element lattices of the Modifiability Checker.
 *
 * <p>Each hierarchy is: Unknown* (top/default), two incomparable siblings (*able and Un*able), and
 * Bottom* (bottom).
 *
 * <p>An annotated type carries one qualifier per hierarchy. Assignment is valid if and only if the
 * RHS is a subtype in every hierarchy simultaneously.
 */
class HierarchyTest {

  void testGrow(
      @Growable Object g, @Ungrowable Object ug, @UnknownGrow Object u, @BottomGrow Object b) {
    @UnknownGrow Object u1 = g;
    @UnknownGrow Object u2 = ug;
    @UnknownGrow Object u3 = b;

    @Growable Object g1 = b;
    @Ungrowable Object ug1 = b;

    // :: error: [assignment]
    @Growable Object g2 = ug;
    // :: error: [assignment]
    @Ungrowable Object ug2 = g;
    // :: error: [assignment]
    @Growable Object g3 = u;
    // :: error: [assignment]
    @Ungrowable Object ug3 = u;
    // :: error: [assignment]
    @BottomGrow Object b1 = g;
    // :: error: [assignment]
    @BottomGrow Object b2 = ug;
    // :: error: [assignment]
    @BottomGrow Object b3 = u;
  }

  void testShrink(
      @Shrinkable Object s,
      @Unshrinkable Object us,
      @UnknownShrink Object u,
      @BottomShrink Object b) {
    @UnknownShrink Object u1 = s;
    @UnknownShrink Object u2 = us;
    @UnknownShrink Object u3 = b;

    @Shrinkable Object s1 = b;
    @Unshrinkable Object us1 = b;

    // :: error: [assignment]
    @Shrinkable Object s2 = us;
    // :: error: [assignment]
    @Unshrinkable Object us2 = s;
    // :: error: [assignment]
    @Shrinkable Object s3 = u;
    // :: error: [assignment]
    @Unshrinkable Object us3 = u;
  }

  void testReplace(
      @Replaceable Object r,
      @Unreplaceable Object ur,
      @UnknownReplace Object u,
      @BottomReplace Object b) {
    @UnknownReplace Object u1 = r;
    @UnknownReplace Object u2 = ur;
    @UnknownReplace Object u3 = b;

    @Replaceable Object r1 = b;
    @Unreplaceable Object ur1 = b;

    // :: error: [assignment]
    @Replaceable Object r2 = ur;
    // :: error: [assignment]
    @Unreplaceable Object ur2 = r;
    // :: error: [assignment]
    @Replaceable Object r3 = u;
    // :: error: [assignment]
    @Unreplaceable Object ur3 = u;
  }

  void testCombinations(
      @Growable @Shrinkable @Replaceable Object gsr,
      @Growable @Shrinkable Object gs,
      @Growable @Replaceable Object gr,
      @Shrinkable @Replaceable Object sr,
      @Growable Object g,
      @Shrinkable Object s,
      @Replaceable Object r,
      @Ungrowable @Unshrinkable @Unreplaceable Object none,
      @UnknownGrow @UnknownShrink @UnknownReplace Object unknown) {

    @Growable Object gv1 = gsr;
    @Growable Object gv2 = gs;
    @Growable Object gv3 = gr;
    // :: error: [assignment]
    @Growable Object gv4 = sr;
    // :: error: [assignment]
    @Growable Object gv5 = s;
    // :: error: [assignment]
    @Growable Object gv6 = r;
    // :: error: [assignment]
    @Growable Object gv7 = none;
    // :: error: [assignment]
    @Growable Object gv8 = unknown;

    @Shrinkable Object sv1 = gsr;
    @Shrinkable Object sv2 = gs;
    // :: error: [assignment]
    @Shrinkable Object sv3 = gr;
    @Shrinkable Object sv4 = sr;
    // :: error: [assignment]
    @Shrinkable Object sv5 = g;
    // :: error: [assignment]
    @Shrinkable Object sv6 = r;
    // :: error: [assignment]
    @Shrinkable Object sv7 = none;
    // :: error: [assignment]
    @Shrinkable Object sv8 = unknown;

    @Replaceable Object rv1 = gsr;
    // :: error: [assignment]
    @Replaceable Object rv2 = gs;
    @Replaceable Object rv3 = gr;
    @Replaceable Object rv4 = sr;
    // :: error: [assignment]
    @Replaceable Object rv5 = g;
    // :: error: [assignment]
    @Replaceable Object rv6 = s;
    // :: error: [assignment]
    @Replaceable Object rv7 = none;
    // :: error: [assignment]
    @Replaceable Object rv8 = unknown;

    @UnknownGrow @UnknownShrink @UnknownReplace Object tv1 = gsr;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv2 = gs;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv3 = gr;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv4 = sr;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv5 = g;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv6 = s;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv7 = r;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv8 = none;
    @UnknownGrow @UnknownShrink @UnknownReplace Object tv9 = unknown;
  }

  void testAliases(
      @Modifiable Object mod, @Unmodifiable Object unmod, @UnknownModifiability Object unknown) {

    @Growable Object g1 = mod;
    @Shrinkable Object s1 = mod;
    @Replaceable Object r1 = mod;

    @Ungrowable Object ug1 = unmod;
    @Unshrinkable Object us1 = unmod;
    @Unreplaceable Object ur1 = unmod;

    @UnknownGrow Object ug2 = unknown;
    @UnknownShrink Object us2 = unknown;
    @UnknownReplace Object ur2 = unknown;

    @UnknownModifiability Object unknown1 = mod;
    @UnknownModifiability Object unknown2 = unmod;

    // :: error: [assignment]
    @Unmodifiable Object unmod1 = mod;
    // :: error: [assignment]
    @Unmodifiable Object unmod2 = unknown;
    // :: error: [assignment]
    @Growable Object g2 = unmod;
    // :: error: [assignment]
    @Modifiable Object mod1 = unmod;
  }
}

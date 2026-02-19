// import java.util.List;
// import java.util.Set;
// import java.util.Map;
// import org.checkerframework.checker.modifiability.qual.*;

// public class PolyShrinkTest {

//     // @PolyShrink behaves like Map.keySet()
//     // It preserves Shrink capability from argument.
//     // Grow and Replace should be defaulted to Unknown (absent).
//     @PolyShrink Set<String> keySet(@PolyShrink Map<String, String> map) {
//        return null;
//     }

//     void testPolyShrink(
//         @Modifiable Map<String, String> mod,
//         @GrowShrink Map<String, String> gs,
//         @Growable Map<String, String> g,
//         @Shrinkable Map<String, String> s,
//         @Unmodifiable Map<String, String> unmod
//     ) {
//         // Input: Modifiable (G, S, R)
//         // Output: Shrinkable (S).
//         // G and R stripped.
//         @Shrinkable Set<String> s1 = keySet(mod);
//         // :: error: (assignment)
//         @Growable Set<String> g1 = keySet(mod); // No G
//         // :: error: (assignment)
//         @Replaceable Set<String> r1 = keySet(mod); // No R
//         // :: error: (assignment)
//         @Modifiable Set<String> m1 = keySet(mod); // No G/R

//         // Input: GrowShrink (G, S)
//         // Output: Shrinkable (S).
//         // G stripped.
//         @Shrinkable Set<String> s2 = keySet(gs);
//         // :: error: (assignment)
//         @Growable Set<String> g2 = keySet(gs); // No G

//         // Input: Growable (G)
//         // Output: UnknownModifiability (None).
//         // G stripped. No S to preserve.
//         @UnknownModifiability Set<String> u3 = keySet(g);
//         // :: error: (assignment)
//         @Shrinkable Set<String> s3 = keySet(g); // No S
//         // :: error: (assignment)
//         @Growable Set<String> g3 = keySet(g); // No G

//         // Input: Shrinkable (S)
//         // Output: Shrinkable (S).
//         @Shrinkable Set<String> s4 = keySet(s);
//         // :: error: (assignment)
//         @Growable Set<String> g4 = keySet(s);

//         // Input: Unmodifiable (None)
//         // Output: UnknownModifiability.
//         @UnknownModifiability Set<String> u5 = keySet(unmod);
//         // :: error: (assignment)
//         @Shrinkable Set<String> s5 = keySet(unmod);
//     }
// }

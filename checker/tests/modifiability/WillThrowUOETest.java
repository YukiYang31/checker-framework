import java.util.SortedSet;
import java.util.TreeSet;

public class WillThrowUOETest {

  void testSortedSet(SortedSet<String> s) {
    // These methods are annotated with @WillThrowUOE in jdk.astub for SortedSet

    // :: error: (usage.will.throw.uoe)
    s.addFirst("foo");

    // :: error: (usage.will.throw.uoe)
    s.addLast("bar");
  }

  void testImplementation(TreeSet<String> ts) {
    // Since TreeSet implements SortedSet, it inherits the methods.
    // The stub file annotates the interface SortedSet.
    // We verify that calling them on a concrete implementation also triggers the warning.

    // :: error: (usage.will.throw.uoe)
    ts.addFirst("foo");

    // :: error: (usage.will.throw.uoe)
    ts.addLast("bar");
  }
}

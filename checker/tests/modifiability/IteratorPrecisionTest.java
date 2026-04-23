import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.checkerframework.checker.modifiability.qual.IteratorPreserveRemove;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.Unshrinkable;

/** Reproduces precision loss through an unannotated Set local from Map.keySet(). */
public class IteratorPrecisionTest {

  void arrayListIterator() {
    // ok
    ArrayList<String> list = new ArrayList<>();
    @Shrinkable Iterator<String> iterator = list.iterator();
    iterator.remove();

    // should still be ok, but is throwing error.
    @IteratorPreserveRemove List<String> list2 = new ArrayList<>();
    @Shrinkable Iterator<String> iterator2 = list2.iterator();
    iterator2.remove();
  }

  void copyOnWriteArrayListIterator() {
    // as expected,
    CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
    @Unshrinkable Iterator<String> iterator = list.iterator();
    // :: error: [method.invocation]
    iterator.remove();

    // throw error.
    List<String> list2 = new CopyOnWriteArrayList<>();
    // TODO!!!!: below the Iterator is default to be unknown shrink because the logic goes:
    // if the current iterator return unknown (which is what List.iterator() returns),
    //    then if the receiver is @Shrinkable and has @IteratorPreserveRemove, then the result is
    // @Shrinkable.
    //    otherwise, the result is unknown.
    // think: should we add a special case for CopyOnWriteArrayList to return unshrinkable iterator?
    Iterator<String> iterator2 = list2.iterator();
    // :: error: [method.invocation]
    iterator2.remove();
  }

  void KeySetIterator() {
    @Modifiable Map<String, String> map = new LinkedHashMap<>();
    map.put("a", "1");
    map.put("b", "2");

    // should be ok, but is throwing error.
    map.keySet().iterator().remove();

    // ok
    @IteratorPreserveRemove
    @Shrinkable Set<String> keys = map.keySet();

    // The iterator supports remove(), but the type is treated as unknown shrinkability.
    @Shrinkable Iterator<String> lost = keys.iterator();
  }
}

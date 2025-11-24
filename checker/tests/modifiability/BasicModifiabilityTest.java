import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.UnknownModifiability;
import org.checkerframework.checker.modifiability.qual.Unmodifiable;

// this checkes the correctness of annotation in List.java and ONLY the initialization of ArrayList
public class BasicModifiabilityTest {

  void testBasicModifiable() {
    // Modifiable collections should allow mutation
    List<String> modifiableList = new ArrayList<>();
    modifiableList.add("test");
    modifiableList.remove(0);

    // Unmodifiable collections should not allow mutation
    @Unmodifiable List<String> unmodifiableList = List.of("test1", "test2");
    // :: error: (method.invocation)
    unmodifiableList.add("test3");
    // :: error: (method.invocation)
    unmodifiableList.remove(0);
    // :: error: (method.invocation)
    unmodifiableList.remove("test1");
  }

  void testUnmodifiableFactoryMethods() {
    // These should be inferred as @Unmodifiable
    List<String> list1 = List.of("a", "b");
    // :: error: (method.invocation)
    list1.add("c");

    // Explicit unmodifiable type + unmodifiable factory
    @Unmodifiable List<String> list1u = List.of("x", "y");

    // :: error: (assignment)
    @Modifiable List<String> cannotBeMod1 = List.of("m1", "m2");

    List<String> list2 = List.copyOf(new ArrayList<>());
    // :: error: (method.invocation)
    list2.add("c");
    // :: error: (method.invocation)
    list2.remove(0);

    List<String> src = new ArrayList<>();
    src.add("s");

    @Unmodifiable List<String> list2u = List.copyOf(src);
    // :: error: (assignment)
    @Modifiable List<String> cannotBeMod2 = List.copyOf(src);
  }

  void testModifiableFactoryMethods() {
    // This should be inferred as @Modifiable
    List<String> modifiableList = new ArrayList<>();
    modifiableList.add("test");
    modifiableList.remove(0);
  }

  void testToArrayModifiability(
      @Modifiable List<String> modList,
      @Unmodifiable List<String> unmodList,
      @UnknownModifiability List<String> anyList) {

    // toArray is annotated to return @Modifiable Object[]
    @Modifiable Object[] arr1 = modList.toArray();
    @Modifiable Object[] arr2 = unmodList.toArray();

    @Modifiable String[] fromMod = modList.toArray(new String[0]);
    @Modifiable String[] fromUnmod = unmodList.toArray(new String[0]);
    @Modifiable String[] fromAny = anyList.toArray(new String[0]);

    // It should be an error to treat the returned array as @Unmodifiable.
    // :: error: (assignment)
    @Unmodifiable Object[] bad1 = modList.toArray();
    // :: error: (assignment)
    @Unmodifiable Object[] bad2 = unmodList.toArray();
    // :: error: (assignment)
    @Unmodifiable String[] u1 = modList.toArray(new String[0]);
    // :: error: (assignment)
    @Unmodifiable String[] u2 = unmodList.toArray(new String[0]);

    // But assigning to @UnknownModifiability is OK (Unknown is the top type).
    @UnknownModifiability String[] anyArr = modList.toArray(new String[0]);
  }

  void testToArrayArgumentModifiability(
      List<String> list,
      @Modifiable String[] modArr,
      @Unmodifiable String[] unmodArr,
      @UnknownModifiability String[] anyArr) {

    // Argument array modifiability should not matter: parameter is T[] with
    // default @UnknownModifiability on the array object.

    list.toArray(modArr);
    list.toArray(unmodArr);
    list.toArray(anyArr);
  }

  void testIteratorPolymorphic(
      @Modifiable List<String> modList, @Unmodifiable List<String> unmodList) {

    // iterator() is annotated with @PolyModifiable on both receiver and return type.
    // So the iterator from a modifiable list is modifiable:
    @Modifiable Iterator<String> itMod = modList.iterator();

    // And the iterator from an unmodifiable list is unmodifiable:
    @Unmodifiable Iterator<String> itUnmod = unmodList.iterator();

    // Trying to treat the unmodifiable iterator as modifiable should be rejected.
    // :: error: (assignment)
    @Modifiable Iterator<String> badIt = unmodList.iterator();
  }

  void testMutatingBulkOperations(@Modifiable List<String> mod, @Unmodifiable List<String> unmod) {
    List<String> other = new ArrayList<>();
    other.add("x");

    // OK on modifiable
    mod.addAll(other);
    mod.removeAll(other);
    mod.retainAll(other);

    // :: error: (method.invocation)
    unmod.addAll(other);
    // :: error: (method.invocation)
    unmod.removeAll(other);
    // :: error: (method.invocation)
    unmod.retainAll(other);
  }
}

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import org.checkerframework.checker.modifiability.qual.Modifiable;
import org.checkerframework.checker.modifiability.qual.Shrinkable;
import org.checkerframework.checker.modifiability.qual.Unshrinkable;

public class IteratorPrecisionTest {

  enum TestEnum {
    A,
    B
  }

  void arrayListIterator() {
    // ok
    ArrayList<String> list = new ArrayList<>();
    @Shrinkable Iterator<String> iterator = list.iterator();
    iterator.remove();

    // should still be ok
    List<String> list2 = new ArrayList<>();
    Iterator<String> iterator2 = list2.iterator();
    iterator2.remove();
  }

  void copyOnWriteArrayListIterator() {
    // as expected,
    CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
    @Unshrinkable Iterator<String> iterator = list.iterator();

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

  void UnmodListIterator() {
    List<String> list = List.of("a", "b");
    @Unshrinkable Iterator<String> iterator = list.iterator();
    // :: error: [method.invocation]
    iterator.remove();
  }

  void KeySetIterator() {
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("a", "1");
    map.put("b", "2");
    map.keySet().iterator().remove();

    @Shrinkable Set<String> keys = map.keySet();
    @Shrinkable Iterator<String> iterator = keys.iterator();

    @Modifiable Map<String, String> map2 = new LinkedHashMap<>();
    map2.put("a", "1");
    map2.put("b", "2");

    // should be ok
    map2.keySet().iterator().remove();

    // ok
    @Shrinkable Set<String> keys2 = map2.keySet();

    // The iterator supports remove(), but the type is treated as unknown shrinkability.
    @Shrinkable Iterator<String> lost = keys2.iterator();
  }

  void setIteratorPreservesRemove() {
    Set<String> hashSet = new HashSet<>();
    @Shrinkable Iterator<String> hashSetIterator = hashSet.iterator();
    hashSetIterator.remove();

    Set<String> treeSet = new TreeSet<>();
    @Shrinkable Iterator<String> treeSetIterator = treeSet.iterator();
    treeSetIterator.remove();
  }

  void dequeIteratorPreservesRemove() {
    Deque<String> arrayDeque = new ArrayDeque<>();
    @Shrinkable Iterator<String> arrayDequeIterator = arrayDeque.iterator();
    arrayDequeIterator.remove();

    Deque<String> linkedList = new LinkedList<>();
    @Shrinkable Iterator<String> linkedListIterator = linkedList.iterator();
    linkedListIterator.remove();
  }

  void queueIteratorPreservesRemove() {
    Queue<String> priorityQueue = new PriorityQueue<>();
    @Shrinkable Iterator<String> priorityQueueIterator = priorityQueue.iterator();
    priorityQueueIterator.remove();

    BlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(1);
    @Shrinkable Iterator<String> arrayBlockingQueueIterator = arrayBlockingQueue.iterator();
    arrayBlockingQueueIterator.remove();

    BlockingQueue<String> linkedBlockingQueue = new LinkedBlockingQueue<>();
    @Shrinkable Iterator<String> linkedBlockingQueueIterator = linkedBlockingQueue.iterator();
    linkedBlockingQueueIterator.remove();

    TransferQueue<String> linkedTransferQueue = new LinkedTransferQueue<>();
    @Shrinkable Iterator<String> linkedTransferQueueIterator = linkedTransferQueue.iterator();
    linkedTransferQueueIterator.remove();
  }

  void factoryIteratorPreservesRemove() {
    Set<TestEnum> enumSet = EnumSet.of(TestEnum.A, TestEnum.B);
    @Shrinkable Iterator<TestEnum> enumSetIterator = enumSet.iterator();
    enumSetIterator.remove();

    Set<TestEnum> enumSetCopy = EnumSet.copyOf(enumSet);
    @Shrinkable Iterator<TestEnum> enumSetCopyIterator = enumSetCopy.iterator();
    enumSetCopyIterator.remove();

    Enumeration<String> enumeration = Collections.enumeration(List.of("a", "b"));
    List<String> list = Collections.list(enumeration);
    @Shrinkable Iterator<String> listIterator = list.iterator();
    listIterator.remove();
  }
}

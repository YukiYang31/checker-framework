// Tests the `inconsistent.constructor.result.type` and `bottom.annotation.on.receiver` checks,
// which are performed by ModifiabilityBaseVisitor.processClassTree.

import java.util.AbstractList;
import org.checkerframework.checker.modifiability.qual.BottomGrowable;
import org.checkerframework.checker.modifiability.qual.Growable;
import org.checkerframework.checker.modifiability.qual.Ungrowable;

public class ConstructorResultTypeTest {

  // All the constructors declare the same result qualifier, so there is no error.
  static class ConsistentConstructors extends AbstractList<String> {
    @Growable ConsistentConstructors() {}

    @Growable ConsistentConstructors(int capacity) {}

    @Override
    public String get(int index) {
      return "value";
    }

    @Override
    public int size() {
      return 0;
    }
  }

  // The second constructor disagrees with the first.
  static class InconsistentConstructors extends AbstractList<String> {
    @Growable InconsistentConstructors() {}

    // :: error: [inconsistent.constructor.result.type]
    @Ungrowable InconsistentConstructors(int capacity) {}

    @Override
    public String get(int index) {
      return "value";
    }

    @Override
    public int size() {
      return 0;
    }
  }

  // The bottom qualifier is meaningless on a receiver, so writing it is an error.
  static class BottomReceiver extends AbstractList<String> {
    @Growable BottomReceiver() {}

    @Override
    public String get(int index) {
      return "value";
    }

    @Override
    public int size() {
      return 0;
    }

    // :: error: [bottom.annotation.on.receiver]
    public void bottomReceiver(@BottomGrowable BottomReceiver this) {}
  }
}

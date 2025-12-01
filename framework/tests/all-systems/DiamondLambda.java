import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiamondLambda {
  void foo(Map<String, Set<String>> map, String sequence) {
    // :: error: (method.invocation)
    Set<String> set = map.computeIfAbsent(sequence, __ -> new HashSet<>());
  }
}

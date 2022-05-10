package mil.usmc.mls2.stratis.modules.smv.utility;

import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.SpaPostResponse;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtility {

  public static boolean validateSerialNumberInput(List<String> serials, Integer qty, SpaPostResponse spaPostResponse) {
    if (serials == null) {
      spaPostResponse.addWarning(String.format("%s serial numbers required", qty));
      return true;
    }

    val serialCount = serials.size();
    if (serialCount != qty) {
      spaPostResponse.addWarning(String.format("%s serial numbers required, supplied %s", qty, serialCount));
      return true;
    }

    Set<String> allSerials = new HashSet<>();
    Set<String> duplicates = serials.stream()
        .filter(n -> !allSerials.add(n)) //Set.add() returns false if the item was already in the set.
        .collect(Collectors.toSet());

    if (!CollectionUtils.isEmpty(duplicates)) {
      spaPostResponse.addWarning(String.format("Duplicate Serial numbers found.  Duplicate(s): %s", duplicates.stream().collect(Collectors.joining(","))));
      return true;
    }

    return false;
  }
}

package mil.usmc.mls2.stratis.common.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.KeyValue;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyValueUtils {

  public static <K, V> List<KeyValue<K, V>> toKeyValueList(List<? extends KeyValueSupport<K, V>> list) {
    if (list == null) {
      return new ArrayList<>(0);
    }

    List<KeyValue<K, V>> keyValueList = new ArrayList<>(list.size());
    for (KeyValueSupport<K, V> keyValueSupport : list) {
      keyValueList.add(keyValueSupport.toKeyValue());
    }

    return keyValueList;
  }
}
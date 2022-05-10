package mil.usmc.mls2.stratis.common.utility;

import org.apache.commons.collections4.KeyValue;

public interface KeyValueSupport<K, V> {

  KeyValue<K, V> toKeyValue();
}

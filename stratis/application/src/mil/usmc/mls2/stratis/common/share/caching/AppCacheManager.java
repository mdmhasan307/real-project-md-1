package mil.usmc.mls2.stratis.common.share.caching;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.map.IMap;

import java.util.Collection;

/**
 * Application cluster cache manager
 */
@SuppressWarnings("unused")
public interface AppCacheManager extends ApplicationCacheManager {

  <K, V> IMap<K, V> getMap(String name);

  Collection<DistributedObject> getDistributedObjects();
}


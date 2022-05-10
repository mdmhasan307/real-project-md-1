package mil.usmc.mls2.stratis.common.share.caching;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

@SuppressWarnings("unused")
interface ApplicationCacheManager {

  String getInstanceName();

  <V extends Serializable> void set(String cacheName, Serializable key, V value);

  <V extends Serializable> V get(String cacheName, Serializable key);

  <V extends Serializable> boolean contains(String cacheName, V value);

  void remove(String cacheName, Serializable key);

  void clearCache(String cacheName);

  void clearCaches(Collection<String> cacheNames);

  Set<Object> getKeys(String cacheName);
}

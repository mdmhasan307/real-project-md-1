package mil.usmc.mls2.stratis.common.share.caching;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Implementation
 * - distributedCachePrefix is used for all cache names to avoid collisions in caching services across applications
 */
@Slf4j
@RequiredArgsConstructor
class DefaultApplicationCacheManager implements ApplicationCacheManager {

  private final HazelcastCacheManager cacheManager;

  public String getInstanceName() {
    return cacheManager.getHazelcastInstance().getName();
  }

  @Override
  public <V extends Serializable> void set(String cacheName, Serializable key, V value) {
    val map = getMap(cacheName);
    map.set(key, value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Serializable> V get(String cacheName, Serializable key) {
    val map = getMap(cacheName);
    return (V) map.get(key);
  }

  @Override
  public boolean contains(String cacheName, Serializable key) {
    val map = getMap(cacheName);
    return map.containsKey(key);
  }

  @Override
  public void remove(String cacheName, Serializable key) {
    val map = getMap(cacheName);
    map.evict(key);
  }

  @Override
  public void clearCache(String cacheName) {
    val map = getMap(cacheName);
    map.clear();
  }

  @Override
  public void clearCaches(Collection<String> cacheNames) {
    for (val cacheName : cacheNames) clearCache(cacheName);
  }

  @Override
  public Set<Object> getKeys(String cacheName) {
    val map = getMap(cacheName);
    return map.keySet();
  }

  public final <K, V> IMap<K, V> getMap(String cacheName) {
    val realCacheName = resolveCacheName(cacheName);
    return cacheManager.getHazelcastInstance().getMap(realCacheName);
  }

  public Collection<DistributedObject> getDistributedObjects() {
    return getCacheManager().getHazelcastInstance().getDistributedObjects();
  }

  protected HazelcastCacheManager getCacheManager() {
    return cacheManager;
  }

  private String resolveCacheName(String cacheName) {
    return cacheName;
  }
}

package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * abandonedConnectionTimeout: (secs)
 * the time a *borrowed* connection can remain idle before it is considered abandoned and forcibly reclaimed by the pool.
 * <p>
 * timeToLiveConnectionTimeout: (secs)
 * the time a *borrowed* connection can remain in use before being forcibly reclaimed by the pool.
 * <p>
 * inactiveConnectionTimeout: (secs)
 * the time an *available* connection can remain idle before it is removed from the pool.
 * <p>
 * maxConnectionReuseTime: (secs)
 * the time a pooled connection can exist (in entirety), before being gracefully removed from the pool (when not in use).
 * <p>
 * connectionWaitTimeout: (secs)
 * the time the pool will wait to provide an available connection before raising a timeout exception.
 * <p>
 * maxStatements:
 * The maximum number of statements that can be cached for a connection (includes implicit AND explicit caching)
 * The maximum number of statements property specifies the number of statements to cache for each connection.
 * The property only applies to the Oracle JDBC driver.
 * If the property is not set, or if it is set to 0, then statement caching is disabled.
 * By default, statement caching is disabled. When statement caching is enabled, a statement cache is associated with each physical
 * connection maintained by the connection pool. A single statement cache is *not* shared across all physical connections.
 * <p>
 * This value does *not* apply to the statement cache size of connections created by ImplicitStatementCache
 * note: see the number of allowable cursors in the database before adjusting this setting (potential cursors = number of connections * max statements)
 * <p>
 * <p>
 * validateConnectionOnBorrow:
 * ...
 * <p>
 * secondsToTrustIdleConnection
 * ...
 * note: only applicable if validateConnectionOnBorrow=true
 * <p>
 * implicitStatementCacheSize:
 * Configures the size of the JDBC Driver's internal statement cache
 * This is preferred over the standard statement-caching-per-connection strategy
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jdbc")
public class JdbcProperties {

  private int initialPoolSize;
  private int minPoolSize;
  private int maxPoolSize;

  private int abandonedConnectionTimeout;
  private int connectionWaitTimeout;
  private int inactiveConnectionTimeout;
  private int maxConnectionReuseTime;
  private int timeToLiveConnectionTimeout;
  private int timeoutCheckInterval;
  private boolean validateConnectionOnBorrow = true;
  private int secondsToTrustIdleConnection;

  private int loginTimeout; //ADF uses this

  private Integer implicitStatementCacheSize;
  private Integer maxStatements;
}

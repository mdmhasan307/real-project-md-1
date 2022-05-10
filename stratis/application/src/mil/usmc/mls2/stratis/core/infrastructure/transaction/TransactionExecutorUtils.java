package mil.usmc.mls2.stratis.core.infrastructure.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Convenience class to be used in conjunction with TransactionExecutor
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionExecutorUtils {

  public static Consumer<Exception> logAndIgnore(Logger log) {
    return logAndIgnore("exception in transaction", log);
  }

  public static Consumer<Exception> logAndIgnore(String message, Logger log) {
    return e -> log.error(message, e);
  }
}

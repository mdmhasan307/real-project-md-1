package mil.usmc.mls2.stratis.core.infrastructure.transaction;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
class TransactionTrackerThreadInfo {

  private final String threadName;
  private final Map<String, TransactionTrackerInfo> transactionMap = new HashMap<>();

  TransactionTrackerThreadInfo(String threadName) {
    this.threadName = threadName;
  }

  Map<String, TransactionTrackerInfo> getTransactionMap() {
    return transactionMap;
  }

  void registerTransaction(String transactionId) {
    if (transactionMap.containsKey(transactionId)) {
      TransactionTrackerInfo transactionInfo = transactionMap.get(transactionId);

      if (log.isWarnEnabled()) {
        log.warn("registerTransactionTimestamp: aborting registration operation, transaction timestamp already registered for transaction id '{}', current entry timestamp:{}", transactionId, transactionInfo.getTimestamp());
      }

      return;
    }

    val timestamp = Instant.now();
    val transactionInfo = new TransactionTrackerInfo(transactionId, timestamp);
    transactionMap.put(transactionId, transactionInfo);

    if (log.isTraceEnabled()) {
      log.trace("registerTransactionTimestamp: registering timestamp for transaction id '{}', timestamp: {}", transactionId, timestamp);
      log.trace("Thread {}: transactions registered: {}", threadName, transactionMap.size());
    }
  }

  void unregisterTransaction(String transactionId) {
    transactionMap.remove(transactionId);

    if (log.isTraceEnabled()) {
      log.trace("Unregistered transaction '{}'", transactionId);
      log.trace("Thread {}: transactions registered: {}", threadName, transactionMap.size());
    }
  }
}

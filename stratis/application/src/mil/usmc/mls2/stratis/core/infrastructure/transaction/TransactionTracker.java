package mil.usmc.mls2.stratis.core.infrastructure.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionTracker {

  private static ThreadLocal<TransactionTrackerThreadInfo> transactionTrackerThreadInfos = new ThreadLocal<>();

  static void registerTransaction(String transactionId) {
    TransactionTrackerThreadInfo threadInfo = getTransactionThreadInfo(false);
    if (threadInfo == null) {
      threadInfo = new TransactionTrackerThreadInfo(Thread.currentThread().getName());
      transactionTrackerThreadInfos.set(threadInfo);
    }

    threadInfo.registerTransaction(transactionId);
  }

  static void unregisterTransaction(String transactionId) {
    if (transactionId == null) {
      return;
    }

    val threadInfo = getTransactionThreadInfo(false);
    if (threadInfo == null) {
      log.warn("Cannot unregister transaction {}' - transaction thread info is null", transactionId);
      return;
    }

    threadInfo.unregisterTransaction(transactionId);
  }

  @SuppressWarnings("unused")
  public static String getCurrentTransactionName() {
    return TransactionSynchronizationManager.getCurrentTransactionName();
  }

  public static Instant getCurrentTransactionTimestamp() {
    val transactionId = TransactionSynchronizationManager.getCurrentTransactionName();
    if (transactionId != null) {
      log.trace("Retrieving current transaction timestamp using transaction '{}'", transactionId);
      val timestamp = getTransactionTimestamp(transactionId);
      log.trace("Returning transaction timestamp of {} for transaction '{}'", timestamp, transactionId);
      return timestamp;
    }
    else {
      log.trace("Current transaction is null, returning new Date.");
      return Instant.now();
    }
  }

  private static TransactionTrackerInfo getTransactionInfo(String transactionId) {
    val threadInfo = getTransactionThreadInfo(true);
    val transactionMap = threadInfo.getTransactionMap();
    val transactionInfo = transactionMap.get(transactionId);
    if (transactionInfo == null) {
      throw new IllegalStateException(String.format("TransactionTrackerInfo not found for transaction '%s' - map contains %d transactions..", transactionId, transactionMap.size()));
    }

    return transactionInfo;
  }

  private static Instant getTransactionTimestamp(String transactionId) {
    return getTransactionInfo(transactionId).getTimestamp();
  }

  private static TransactionTrackerInfo getCurrentTransactionInfo() {
    return getTransactionInfo(TransactionSynchronizationManager.getCurrentTransactionName());
  }

  private static TransactionTrackerThreadInfo getTransactionThreadInfo(boolean raiseExceptionIfNull) {
    val threadInfo = transactionTrackerThreadInfos.get();
    if (threadInfo == null && raiseExceptionIfNull) {
      val transactionId = TransactionSynchronizationManager.getCurrentTransactionName();
      val msg = String.format("Cannot retrieve transaction thread info for transaction '%s' - transaction thread info is null", transactionId);
      log.warn(msg);
      throw new IllegalStateException(msg);
    }

    return threadInfo;
  }
}
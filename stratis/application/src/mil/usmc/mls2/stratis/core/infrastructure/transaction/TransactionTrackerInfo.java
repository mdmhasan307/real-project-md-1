package mil.usmc.mls2.stratis.core.infrastructure.transaction;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Getter
class TransactionTrackerInfo {

  private final String transactionId;
  private final Instant timestamp;

  TransactionTrackerInfo(String transactionId, Instant timestamp) {
    this.transactionId = transactionId;
    this.timestamp = timestamp;
  }
}

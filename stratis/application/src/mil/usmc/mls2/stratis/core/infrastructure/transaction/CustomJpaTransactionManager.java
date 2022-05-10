package mil.usmc.mls2.stratis.core.infrastructure.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
public class CustomJpaTransactionManager extends JpaTransactionManager {

  public CustomJpaTransactionManager(EntityManagerFactory emf) {
    super(emf);
  }

  @Override
  protected void doBegin(Object transaction, TransactionDefinition definition) {
    val transactionId = definition.getName();
    log.debug("doBegin: transaction definition is: {}, {}", transactionId, definition);

    super.doBegin(transaction, definition);
  }

  @Override
  protected void doCommit(DefaultTransactionStatus status) {
    val transactionId = TransactionSynchronizationManager.getCurrentTransactionName();
    log.debug("doCommit: this transaction is '{}'", transactionId);

    try {
      super.doCommit(status);
    }
    catch (Exception e) {
      log.warn("doCommit: failed for transaction '{}'", transactionId, e);
      throw e;
    }
  }

  @Override
  protected void doRollback(DefaultTransactionStatus status) {
    val transactionId = TransactionSynchronizationManager.getCurrentTransactionName();
    log.info("doRollback for transaction '{}'", transactionId);

    super.doRollback(status);
  }
}
package mil.usmc.mls2.stratis.core.infrastructure.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
class TransactionExecutor {

  private static final String STANDARD_ERROR_MESSAGE = "transaction failed, raising exception";

  public void execute(String name, PlatformTransactionManager transactionManager, Runnable runnable) {
    execute(name, transactionManager, runnable, null);
  }

  public void execute(String name, PlatformTransactionManager transactionManager, Consumer<TransactionStatus> consumer) {
    execute(name, transactionManager, consumer, null);
  }

  public <T> T execute(String name, PlatformTransactionManager transactionManager, Callable<T> callable) {
    return execute(name, transactionManager, callable, null);
  }

  public <T> T execute(String name, PlatformTransactionManager transactionManager, Function<TransactionStatus, T> txCallback) {
    return execute(name, transactionManager, txCallback, null);
  }

  public void execute(String name, PlatformTransactionManager transactionManager, Runnable runnable, Consumer<Exception> exceptionHandler) {
    val transactionTemplate = createTransactionTemplate(name, transactionManager);
    TransactionCallback<Void> voidTxCallback = ts -> {
      runnable.run();
      return null;
    };

    try {
      transactionTemplate.execute(voidTxCallback);
    }
    catch (Exception e) {
      if (exceptionHandler != null) {
        exceptionHandler.accept(e);
      }
      else {
        log.error(STANDARD_ERROR_MESSAGE, e);
        throw e;
      }
    }
  }

  public <T> T execute(String name, PlatformTransactionManager transactionManager, Callable<T> callable, Function<Exception, T> exceptionHandler) {
    val transactionTemplate = createTransactionTemplate(name, transactionManager);
    try {
      TransactionCallback<T> txCallback = ts -> {
        try {
          return callable.call();
        }
        catch (Exception e) {
          // no-op
          throw new StratisRuntimeException("failed to executable callable", e);
        }
      };
      return transactionTemplate.execute(txCallback);
    }
    catch (Exception e) {
      if (exceptionHandler != null) {
        return exceptionHandler.apply(e);
      }
      else {
        log.error(STANDARD_ERROR_MESSAGE, e);
        throw e;
      }
    }
  }

  public <T> T execute(String name, PlatformTransactionManager transactionManager, Function<TransactionStatus, T> func, Function<Exception, T> exceptionHandler) {
    val transactionTemplate = createTransactionTemplate(name, transactionManager);
    TransactionCallback<T> txCallback = func::apply;

    try {
      return transactionTemplate.execute(txCallback);
    }
    catch (Exception e) {
      if (exceptionHandler != null) {
        return exceptionHandler.apply(e);
      }
      else {
        log.error(STANDARD_ERROR_MESSAGE, e);
        throw e;
      }
    }
  }

  public void execute(String name, PlatformTransactionManager transactionManager, Consumer<TransactionStatus> consumer, Consumer<Exception> exceptionHandler) {
    val transactionTemplate = createTransactionTemplate(name, transactionManager);
    TransactionCallback<Void> voidTxCallback = ts -> {
      consumer.accept(ts);
      return null;
    };

    try {
      transactionTemplate.execute(voidTxCallback);
    }
    catch (Exception e) {
      if (exceptionHandler != null) {
        exceptionHandler.accept(e);
      }
      else {
        log.error(STANDARD_ERROR_MESSAGE, e);
        throw e;
      }
    }
  }

  private TransactionTemplate createTransactionTemplate(String name, PlatformTransactionManager transactionManager) {
    final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    transactionTemplate.setName(name);
    transactionTemplate.setReadOnly(false);

    return transactionTemplate;
  }
}

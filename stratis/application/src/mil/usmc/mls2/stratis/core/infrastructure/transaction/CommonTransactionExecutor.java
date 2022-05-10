package mil.usmc.mls2.stratis.core.infrastructure.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class CommonTransactionExecutor {

  @Qualifier(GlobalConstants.COMMON_TRANSACTION_MANAGER)
  private final PlatformTransactionManager transactionManager;

  private final TransactionExecutor transactionExecutor;

  public void execute(String name, Runnable runnable) {
    transactionExecutor.execute(name, transactionManager, runnable);
  }

  public void execute(String name, Consumer<TransactionStatus> consumer) {
    transactionExecutor.execute(name, transactionManager, consumer);
  }

  public <T> T execute(String name, Callable<T> callable) {
    return transactionExecutor.execute(name, transactionManager, callable);
  }

  public <T> T execute(String name, Function<TransactionStatus, T> txCallback) {
    return transactionExecutor.execute(name, transactionManager, txCallback);
  }

  public void execute(String name, Runnable runnable, Consumer<Exception> exceptionHandler) {
    transactionExecutor.execute(name, transactionManager, runnable, exceptionHandler);
  }

  public <T> T execute(String name, Callable<T> callable, Function<Exception, T> exceptionHandler) {
    return transactionExecutor.execute(name, transactionManager, callable, exceptionHandler);
  }

  public <T> T execute(String name, Function<TransactionStatus, T> func, Function<Exception, T> exceptionHandler) {
    return transactionExecutor.execute(name, transactionManager, func, exceptionHandler);
  }

  public void execute(String name, Consumer<TransactionStatus> consumer, Consumer<Exception> exceptionHandler) {
    transactionExecutor.execute(name, transactionManager, consumer, exceptionHandler);
  }
}

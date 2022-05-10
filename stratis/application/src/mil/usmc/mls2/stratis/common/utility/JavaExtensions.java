package mil.usmc.mls2.stratis.common.utility;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Java Extension utility library
 * <p>
 * All methods are prefixed with an underscore (following JavaScript's underscore library approach) to indicate special Java extension
 * syntax.
 */
@Slf4j
@UtilityClass
@SuppressWarnings("unused")
public class JavaExtensions {

  /**
   * Executes runnable if supplied predicate is true
   * Primarily used to improve performance and readability of conditional statements.
   *
   * @param predicate predicate to evaluate
   * @param runnable  runnable
   */
  public void _if(boolean predicate, Runnable runnable) {
    if (predicate) runnable.run();
  }

  public void _ifThrow(boolean predicate, Supplier<RuntimeException> exceptionSupplier) {
    if (predicate) throw exceptionSupplier.get();
  }

  public void _ifNot(boolean predicate, Runnable runnable) {
    if (!predicate) runnable.run();
  }

  /**
   * Executes the given runnable if the predicate is false
   *
   * @param predicate predicate to evaluate
   */
  public void _require(boolean predicate, Runnable runnable) {
    if (!predicate) runnable.run();
  }

  /**
   * Raises a null pointer exception if the provided object reference is null
   *
   * @param object  object to evaluate
   * @param message message to include in exception
   */
  public void _nonNull(Object object, String message) {
    if (object == null) throw new NullPointerException(message);
  }

  /**
   * EXCEPTION AND DEBUG TOOL Returns the invoking class name of the caller's class
   */
  public String _invokingClassName() {
    return new Throwable().getStackTrace()[1].getClassName(); // NOSONAR intentional use of constant index to array
  }

  /**
   * Attempts to put the current thread to sleep, trapping any exceptions that may occur.
   */
  public void _safeSleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (Exception e) {
      // do nothing
    }
  }

  public void _safeSleep(long duration, TimeUnit timeUnit) {
    try {
      timeUnit.sleep(duration);
    }
    catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Returns the classname of the caller
   */
  public String _className() {
    val t = new Throwable();
    t.fillInStackTrace();
    return t.getStackTrace()[1].getClassName(); // NOSONAR intentional use of constant index to array
  }

  public String _className(Class<?> clazz) {
    return clazz.getName();
  }

  /**
   * Returns the package of the caller
   */
  public String _packageName() {
    val t = new Throwable();
    t.fillInStackTrace();
    return t.getStackTrace()[1].getClass().getPackage().getName(); // NOSONAR intentional use of constant index to array
  }

  /**
   * Returns the class path of the package the class belongs to
   */
  public String _packageClassPath(Class<?> clazz) {
    val result = clazz.getPackage().getName();
    return result.replace('.', '/');
  }

  @SafeVarargs
  public <T extends Comparable<T>> boolean _equalsAny(T object, T... objects) {
    for (T tempObject : objects) {
      if (Objects.equals(object, tempObject)) {
        return true;
      }
    }

    return false;
  }

  public boolean _instanceOfAny(Object object, Class<?>... classes) {
    return Arrays.stream(classes).anyMatch(c -> c.isAssignableFrom(object.getClass()));
  }
}
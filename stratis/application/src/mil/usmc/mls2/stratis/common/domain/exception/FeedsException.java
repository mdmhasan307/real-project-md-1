package mil.usmc.mls2.stratis.common.domain.exception;

/**
 * The superclass for all runtime exceptions that can occur during the normal operation of the Feed Services system.
 */
public class FeedsException extends RuntimeException {

  public FeedsException(String message) {
    super(message);
  }

  public FeedsException(String message, Exception e) {
    super(message, e);
  }

  public FeedsException(Exception e) {
    super(e);
  }

  public static FeedsException of(String message) {
    return new FeedsException(message);
  }

  public static FeedsException of(String message, Exception e) {
    return new FeedsException(message, e);
  }
}

package mil.usmc.mls2.stratis.common.domain.exception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StratisRuntimeException extends RuntimeException implements StratisException {

  private final List<String> userMessages = new ArrayList<>();
  private final List<String> systemMessages = new ArrayList<>();

  public StratisRuntimeException(String message) {
    super(message);
    if (message != null) {
      userMessages.add(message);
      systemMessages.add(message);
    }
  }

  public StratisRuntimeException(String userMessage, String systemMessage) {
    super(userMessage);
    if (userMessage != null) {
      userMessages.add(userMessage);
    }
    if (systemMessage != null) {
      systemMessages.add(systemMessage);
    }
  }

  public StratisRuntimeException(String message, Throwable t) {
    super(message, t);
    if (message != null) {
      userMessages.add(message);
      systemMessages.add(message);
    }
  }

  public StratisRuntimeException(String userMessage, String systemMessage, Throwable t) {
    super(userMessage, t);
    if (userMessage != null) {
      userMessages.add(userMessage);
    }
    if (systemMessage != null) {
      systemMessages.add(systemMessage);
    }
  }

  public StratisRuntimeException(String message, StratisRuntimeException se) {
    super(message, se);
    if (message != null) {
      userMessages.add(message);
      systemMessages.add(message);
    }
    userMessages.addAll(se.getUserMessages());
    systemMessages.addAll(se.getSystemMessages());
  }

  public StratisRuntimeException(String userMessage, String systemMessage, StratisRuntimeException se) {
    super(userMessage, se);
    if (userMessage != null) {
      userMessages.add(userMessage);
    }
    if (systemMessage != null) {
      systemMessages.add(systemMessage);
    }
    userMessages.addAll(se.getUserMessages());
    systemMessages.addAll(se.getSystemMessages());
  }

  public StratisRuntimeException(String message, Collection<String> messages) {
    super(message);
    if (message != null) {
      userMessages.add(message);
      systemMessages.add(message);
    }
    if (messages != null) {
      userMessages.addAll(messages);
      systemMessages.addAll(messages);
    }
  }

  @Override
  public List<String> getUserMessages() {
    return userMessages;
  }

  @Override
  public List<String> getSystemMessages() {
    return systemMessages;
  }
}

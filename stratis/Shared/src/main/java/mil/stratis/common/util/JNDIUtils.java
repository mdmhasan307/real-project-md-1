package mil.stratis.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public final class JNDIUtils {

  private static final String PREFIX = "java:comp/env/";
  private static JNDIUtils instance = null;
  private static InitialContext ctx = null;

  /**
   * Private Constructor.
   */
  private JNDIUtils() {
    ctx = this.getContext();
  }

  /**
   * Returns an instance of JNDIUtils.class.
   *
   * @return instance of JNDIUtils class
   */
  public static JNDIUtils getInstance() {
    if (instance == null) {
      instance = new JNDIUtils();
    }
    return instance;
  }

  /**
   * Returns an instance of InitialContext.
   *
   * @return InitialContext
   */
  private InitialContext getContext() {
    InitialContext context = null;
    try {
      context = new InitialContext();
    }
    catch (NamingException e) {
      log.error("Error creating JNDI connection", e);
    }
    return context;
  }

  /**
   * Used to retrieve the value of a property in the context realm.
   *
   * @param prop - name of the property requested
   * @return String value
   */
  public String getProperty(final String prop) {
    String envValue = null;
    try {
      envValue = (String) ctx.lookup(PREFIX + prop);
    }
    catch (NamingException e) {
      log.error("Error looking up JNDI property: {}", prop, e);
    }
    return envValue;
  }
}



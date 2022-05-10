package mil.stratis.view.session;

import lombok.NoArgsConstructor;

/**
 * Helper Bean to help navigation around the taskflow when normal taskflow
 * processing can't be used.
 */
@NoArgsConstructor
public class NavigationBag extends Navigation {

  /**
   * Does nothing.  But it's bean, so we have to have a 'set' method.
   */
  public void setCreateAccountURL() {
    //NO-OP
  }

  /**
   * Gets the taskflow url to create a new account.
   *
   * @return the URL
   */
  public String getCreateAccountURL() {
    return getCorrectURL("register");
  }

  /**
   * Does nothing.  But it's bean, so we have to have a 'set' method.
   */
  @SuppressWarnings("unused")
  public void setHomeURL() {
    //NO-OP
  }

  /**
   * Gets the URL to go to the application home page.
   *
   * @return the url.
   */
  public String getHomeURL() {
    return getCorrectURL("home");
  }
}

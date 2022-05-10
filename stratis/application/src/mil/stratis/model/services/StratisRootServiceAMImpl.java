package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ApplicationModuleImpl;

@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class StratisRootServiceAMImpl extends ApplicationModuleImpl {

  /**
   * Container's getter for LoginModule1.
   *
   * @return LoginModule1
   */
  public ApplicationModuleImpl getLoginModule1() {
    return (ApplicationModuleImpl) findApplicationModule("LoginModule1");
  }
}

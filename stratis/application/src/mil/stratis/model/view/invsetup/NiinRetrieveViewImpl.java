package mil.stratis.model.view.invsetup;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinRetrieveViewImpl extends ViewObjectImpl {

  /**
   * Returns the variable value for Variable.
   *
   * @return variable value for Variable
   */
  public String getVariable() {
    return (String) ensureVariableManager().getVariableValue("Variable");
  }

  /**
   * Sets <code>value</code> for variable Variable.
   *
   * @param value value to bind as Variable
   */
  public void setVariable(String value) {
    ensureVariableManager().setVariableValue("Variable", value);
  }

  /**
   * Returns the variable value for startNIIN.
   *
   * @return variable value for startNIIN
   */
  public String getstartNIIN() {
    return (String) ensureVariableManager().getVariableValue("startNIIN");
  }

  /**
   * Sets <code>value</code> for variable startNIIN.
   *
   * @param value value to bind as startNIIN
   */
  public void setstartNIIN(String value) {
    ensureVariableManager().setVariableValue("startNIIN", value);
  }

  /**
   * Returns the variable value for endNIIN.
   *
   * @return variable value for endNIIN
   */
  public String getendNIIN() {
    return (String) ensureVariableManager().getVariableValue("endNIIN");
  }

  /**
   * Sets <code>value</code> for variable endNIIN.
   *
   * @param value value to bind as endNIIN
   */
  public void setendNIIN(String value) {
    ensureVariableManager().setVariableValue("endNIIN", value);
  }
}

package mil.stratis.model.view.hh;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class PickingGenBulkQryVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for wacIdStr
   */
  public String getwacIdStr() {
    return (String) getNamedWhereClauseParam("wacIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable wacIdStr
   */
  public void setwacIdStr(String value) {
    setNamedWhereClauseParam("wacIdStr", value);
  }

  /**
   * Gets the bind variable value for pidStr
   */
  public String getpidStr() {
    return (String) getNamedWhereClauseParam("pidStr");
  }

  /**
   * Sets <code>value</code> for bind variable pidStr
   */
  public void setpidStr(String value) {
    setNamedWhereClauseParam("pidStr", value);
  }

  /**
   * Gets the bind variable value for issueTypeStr
   */
  public String getissueTypeStr() {
    return (String) getNamedWhereClauseParam("issueTypeStr");
  }

  /**
   * Sets <code>value</code> for bind variable issueTypeStr
   */
  public void setissueTypeStr(String value) {
    setNamedWhereClauseParam("issueTypeStr", value);
  }
}

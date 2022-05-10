package mil.stratis.model.view.inv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class InventoryItemVOImpl extends ViewObjectImpl {

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
   * Gets the bind variable value for usrId
   */
  public String getusrId() {
    return (String) getNamedWhereClauseParam("usrId");
  }

  /**
   * Sets <code>value</code> for bind variable usrId
   */
  public void setusrId(String value) {
    setNamedWhereClauseParam("usrId", value);
  }

  public String getAssignToUserId() {
    return (String) getNamedWhereClauseParam("assignToUserId");
  }

  public void setAssignToUserId(String value) {
    setNamedWhereClauseParam("assignToUserId", value);
  }
}

package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class LocationSearchBulkAvailVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for locLabelStr
   */
  public String getlocLabelStr() {
    return (String) getNamedWhereClauseParam("locLabelStr");
  }

  /**
   * Sets <code>value</code> for bind variable locLabelStr
   */
  public void setlocLabelStr(String value) {
    setNamedWhereClauseParam("locLabelStr", value);
  }

  /**
   * Gets the bind variable value for secureFlag
   */
  public String getsecureFlag() {
    return (String) getNamedWhereClauseParam("secureFlag");
  }

  /**
   * Sets <code>value</code> for bind variable secureFlag
   */
  public void setsecureFlag(String value) {
    setNamedWhereClauseParam("secureFlag", value);
  }

  /**
   * Gets the bind variable value for wareHouseId
   */
  public String getwareHouseId() {
    return (String) getNamedWhereClauseParam("wareHouseId");
  }

  /**
   * Sets <code>value</code> for bind variable wareHouseId
   */
  public void setwareHouseId(String value) {
    setNamedWhereClauseParam("wareHouseId", value);
  }

  /**
   * Gets the bind variable value for wacNumberStr
   */
  public String getwacNumberStr() {
    return (String) getNamedWhereClauseParam("wacNumberStr");
  }

  /**
   * Sets <code>value</code> for bind variable wacNumberStr
   */
  public void setwacNumberStr(String value) {
    setNamedWhereClauseParam("wacNumberStr", value);
  }
}

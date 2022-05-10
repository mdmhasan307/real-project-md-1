package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class LocationSearchAvailVOImpl extends ViewObjectImpl {

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
   * Gets the bind variable value for sideStr
   */
  public String getsideStr() {
    return (String) getNamedWhereClauseParam("sideStr");
  }

  /**
   * Sets <code>value</code> for bind variable sideStr
   */
  public void setsideStr(String value) {
    setNamedWhereClauseParam("sideStr", value);
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
   * Gets the bind variable value for mechFlagH
   */
  public String getmechFlagH() {
    return (String) getNamedWhereClauseParam("mechFlagH");
  }

  /**
   * Sets <code>value</code> for bind variable mechFlagH
   */
  public void setmechFlagH(String value) {
    setNamedWhereClauseParam("mechFlagH", value);
  }

  /**
   * Gets the bind variable value for mechFlagV
   */
  public String getmechFlagV() {
    return (String) getNamedWhereClauseParam("mechFlagV");
  }

  /**
   * Sets <code>value</code> for bind variable mechFlagV
   */
  public void setmechFlagV(String value) {
    setNamedWhereClauseParam("mechFlagV", value);
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

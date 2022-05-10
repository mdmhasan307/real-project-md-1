package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class LocationSearchVOImpl extends ViewObjectImpl {
 
  /**
   * Gets the bind variable value for niinIdStr
   */
  public String getniinIdStr() {
    return (String) getNamedWhereClauseParam("niinIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable niinIdStr
   */
  public void setniinIdStr(String value) {
    setNamedWhereClauseParam("niinIdStr", value);
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
   * Gets the bind variable value for ccStr
   */
  public String getccStr() {
    return (String) getNamedWhereClauseParam("ccStr");
  }

  /**
   * Sets <code>value</code> for bind variable ccStr
   */
  public void setccStr(String value) {
    setNamedWhereClauseParam("ccStr", value);
  }
}

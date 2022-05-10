package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class LocationVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for newWeight
   */
  public Double getnewWeight() {
    return (Double) getNamedWhereClauseParam("newWeight");
  }

  /**
   * Sets <code>value</code> for bind variable newWeight
   */
  public void setnewWeight(Double value) {
    setNamedWhereClauseParam("newWeight", value);
  }

  /**
   * Gets the bind variable value for newCube
   */
  public Double getnewCube() {
    return (Double) getNamedWhereClauseParam("newCube");
  }

  /**
   * Sets <code>value</code> for bind variable newCube
   */
  public void setnewCube(Double value) {
    setNamedWhereClauseParam("newCube", value);
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
   * Gets the bind variable value for whId
   */
  public String getwhId() {
    return (String) getNamedWhereClauseParam("whId");
  }

  /**
   * Sets <code>value</code> for bind variable whId
   */
  public void setwhId(String value) {
    setNamedWhereClauseParam("whId", value);
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
   * Gets the bind variable value for bayStr
   */
  public String getbayStr() {
    return (String) getNamedWhereClauseParam("bayStr");
  }

  /**
   * Sets <code>value</code> for bind variable bayStr
   */
  public void setbayStr(String value) {
    setNamedWhereClauseParam("bayStr", value);
  }

  /**
   * Gets the bind variable value for locLevelStr
   */
  public String getlocLevelStr() {
    return (String) getNamedWhereClauseParam("locLevelStr");
  }

  /**
   * Sets <code>value</code> for bind variable locLevelStr
   */
  public void setlocLevelStr(String value) {
    setNamedWhereClauseParam("locLevelStr", value);
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
   * Gets the bind variable value for expDtStr
   */
  public String getexpDtStr() {
    return (String) getNamedWhereClauseParam("expDtStr");
  }

  /**
   * Sets <code>value</code> for bind variable expDtStr
   */
  public void setexpDtStr(String value) {
    setNamedWhereClauseParam("expDtStr", value);
  }
}

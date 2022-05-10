package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinLocListVVOFImpl extends ViewObjectImpl {

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

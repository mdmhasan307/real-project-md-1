package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class LocationVORImpl extends ViewObjectImpl {

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
   * Gets the bind variable value for mechFlag
   */
  public String getmechFlag() {
    return (String) getNamedWhereClauseParam("mechFlag");
  }

  /**
   * Sets <code>value</code> for bind variable mechFlag
   */
  public void setmechFlag(String value) {
    setNamedWhereClauseParam("mechFlag", value);
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
}

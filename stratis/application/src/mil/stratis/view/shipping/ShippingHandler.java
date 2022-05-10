package mil.stratis.view.shipping;

import lombok.NoArgsConstructor;
import lombok.val;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.model.binding.DCIteratorBinding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.List;

@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class ShippingHandler extends BackingHandler {

  private int iUserId;
  private int iWorkstationId;
  private String uri;

  public void init() {
    val userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
    init(userInfo.getWorkstationId(), userInfo.getUserId(), userInfo.getURI());
    setWorkstationId(userInfo.getWorkstationId());
    setUserId(userInfo.getUserId());
    setURI(userInfo.getURI());
  }

  public void init(int workstationId, int userId, String uri) {
    setWorkstationId(workstationId);
    setUserId(userId);
    setURI(uri);
  }

  protected void refreshPage() {
    FacesContext fc = FacesContext.getCurrentInstance();
    String refreshpage = fc.getViewRoot().getViewId();
    ViewHandler viewH = fc.getApplication().getViewHandler();
    UIViewRoot uiv = viewH.createView(fc, refreshpage);
    uiv.setViewId(refreshpage);
    fc.setViewRoot(uiv);
  }

  private void setURI(String uri) {
    this.uri = uri;
  }

  public String getURI() {
    return this.uri;
  }

  private void setUserId(Object id) {
    this.iUserId = NumberUtils.toInt(id.toString(), 0);
  }

  public int getUserId() {
    return iUserId;
  }

  private void setWorkstationId(Object id) {
    this.iWorkstationId = NumberUtils.toInt(id.toString(), 0);
  }

  /**
   * Returns the id of the current workstation (e.g., SHIP012243) logged into
   *
   * @return int
   */
  public int getWorkstationId() {
    return iWorkstationId;
  }

  protected boolean isEmpty(Object object) {
    if (object == null) return true;
    return StringUtils.isBlank(object.toString());
  }

  public void displayMessage(StringBuilder msgOutput) {
    /* display message to GUI */
    if (msgOutput.length() > 0) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msgOutput.toString(), null);
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.addMessage(null, msg);
    }
  }

  public void displaySuccessMessage(StringBuilder msgOutput) {
    /* display message to GUI */
    if (msgOutput.length() > 0) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, msgOutput.toString(), null);
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.addMessage(null, msg);
    }
  }

  protected void saveIterator(String iteratorName) {
    DCIteratorBinding iter = ADFUtils.findIterator(iteratorName);
    // commit the change
    iter.getDataControl().commitTransaction();
    iter.executeQuery();
  }

  /**
   * Returns the associated label of a selected item in the any navigation list
   *
   * @return String
   */
  protected String getListLabel(String value, List<SelectItem> list) {
    for (SelectItem l : list) {
      if (value.equals(l.getValue().toString()))
        return l.getLabel();
    }
    return "";
  }
}

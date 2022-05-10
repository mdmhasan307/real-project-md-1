package mil.stratis.view.inventory;

import lombok.NoArgsConstructor;
import mil.stratis.model.services.InventoryModuleImpl;
import mil.stratis.view.session.MdssBackingBean;
import org.apache.myfaces.trinidad.component.core.nav.CoreCommandButton;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@NoArgsConstructor
public class InventoryLoad extends MdssBackingBean {

  Boolean invPendingFlag = true;
  int divider;
  int selecter;

  private transient CoreCommandButton nextButton;
  private transient CoreCommandButton nextButton2;
  
  public void onPageLoad() {

    int flag = 0;
    String message = "";

    InventoryModuleImpl Service = getInventoryAMService();

    setDivider(Service.getDividerIndex());
    //Retrieve the Select index for the bin from the
    //application module
    setSelecter(Service.getSelectIndex());

    flag = Service.checkInvSize(false);

    if (flag == 1) {
      this.nextButton.setDisabled(true);
      this.nextButton2.setDisabled(true);
    }
    else if (flag == 2) {
      this.nextButton.setDisabled(true);
      this.nextButton2.setDisabled(true);
    }
    else if (flag == 3)
      this.setInvPendingFlag(true);
    else if (flag == 0) {
      this.setInvPendingFlag(false);
      message = "There are currently no inventories pending";
      StringBuffer buffmessage = new StringBuffer(message);
      this.displayMessage(buffmessage);
    }
  }

  public void displayMessage(StringBuffer msgOutput) {
    /* display message to GUI */
    if (msgOutput.length() > 0) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, msgOutput.toString(), null);
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.addMessage(null, msg);
    }
  }

  public void setInvPendingFlag(Boolean invPendingFlag) {
    this.invPendingFlag = invPendingFlag;
  }

  public Boolean getInvPendingFlag() {
    return invPendingFlag;
  }

  public void setDivider(int divider) {
    this.divider = divider;
  }

  public int getDivider() {
    return divider;
  }

  public void setSelecter(int selecter) {
    this.selecter = selecter;
  }

  public int getSelecter() {
    return selecter;
  }

  public void setNextButton(CoreCommandButton nextButton) {
    this.nextButton = nextButton;
  }

  public CoreCommandButton getNextButton() {
    return nextButton;
  }

  public void setNextButton2(CoreCommandButton nextButton2) {
    this.nextButton2 = nextButton2;
  }

  public CoreCommandButton getNextButton2() {
    return nextButton2;
  }
}

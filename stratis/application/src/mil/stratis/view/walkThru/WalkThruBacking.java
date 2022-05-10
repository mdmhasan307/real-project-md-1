package mil.stratis.view.walkThru;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.RegUtils;
import mil.stratis.common.util.StringUtil;
import mil.stratis.common.util.Util;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.ELUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.jbo.Row;
import oracle.jbo.domain.Number;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.sql.SQLException;
import java.util.Iterator;

@Slf4j
public class WalkThruBacking extends MdssBackingBean {

  private transient RichTable walkThruTable;
  private transient RichInputText docID;
  private transient String errStr = "error";
  private String function = "Walk Thru";
  private String walkThruInputField = "Document Number";
  private String walkThruIterator = "WalkThruQueueVO1Iterator";

  public WalkThruBacking() {
    super();
  }

  public void onPageLoad() {
    if (!Util.isEmpty(walkThruTable) && getWalkThruAMService().isFromPickbyWalkThru()) {
      AdfFacesContext.getCurrentInstance().addPartialTarget(walkThruTable);
      getWalkThruAMService().refreshWalkThruTable();
      getWalkThruAMService().setFromPickbyWalkThru(false);
    }
  }

  /**
   * METHOD      : addToWalkThruList
   * AUTHOR      : Hyun Koo
   * DESCRIPTION : Performs validations for error messaging and
   * calls backend methods for adding the Document Number to WALKTHRU_QUEUE table.
   */
  public void addToWalkThruQueue(ActionEvent actionEvent) {

    String docNumber = Util.trimUpperCaseClean(getDocID().getLocalValue());
    //docNumber = getDocID().getLocalValue().toString();
    if (StringUtil.isEmpty(docNumber)) {
      errStr = errStr + "DOCNUM_ERR";
      JSFUtils.addFacesErrorMessage("DOCNUM_ERR", "Please enter DOCUMENT NUMBER.");
      return;
    }
    else if (docNumber.length() != 14) {
      JSFUtils.addFacesErrorMessage("DOCNUM_ERR", "Document Number must be 14 alphanumeric characters.");
      return;
    }
    else if (!RegUtils.isAlphaNumeric(docNumber)) {
      JSFUtils.addFacesErrorMessage("DOCNUM_ERR", "Document Number must be alphanumeric characters only.");
      return;
    }
    String userId = JSFUtils.getManagedBeanValue("userbean.userId").toString().trim();
    int userIds = Integer.parseInt(userId);
    Integer integerUerId = new Integer(userIds);
    Number number = null;
    try {
      number = new Number(integerUerId);
    }
    catch (SQLException e) {
      log.debug("An exception has been caught in walk thru number converstion:  {}", e.getMessage());
    }
    ELUtils.set("#{bindings.CreatedBy.inputValue}", number);
    // save to database
    String mssg = getWalkThruAMService().insertDocumentNumber(docNumber, userId, function, walkThruInputField);
    if (mssg.equalsIgnoreCase("success")) {
      displaySuccessMessage(function + " saved successfully.");
      resetInputText("documentNumber");
    }
    else if (mssg.equals("ERROR1")) {
      displayMessage("Unable to save " + function +
          ".  Duplicate fields (" + walkThruInputField +
          ") are not allowed.  Check the table above to make sure these fields do not already exist.  " +
          "Try again.");
    }
    else if (mssg.equals("ERROR2")) {
      displayMessage("Unable to save Document Number. Walk Thru Document Number already exists.");
    }
    else if (mssg.equals("ERROR3")) {
      displayMessage("Unable to save " + function + " Document Number.");
    }
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      submitDeleteWalkThru(null);
      RichPopup popup;
      popup = (RichPopup) JSFUtils.findComponentInRoot("confirmDelete");
      popup.hide();

      //Refresh accTable
      AdfFacesContext.getCurrentInstance().addPartialTarget(walkThruTable);
    }
  }

  public void submitDeleteWalkThru(ActionEvent event) {
    String deleteKey = null;

    Object oldRowKey = walkThruTable.getRowKey();
    Iterator selection = walkThruTable.getSelectedRowKeys().iterator();

    try {
      Object rowKey = selection.next();
      walkThruTable.setRowKey(rowKey);
      JUCtrlHierNodeBinding rowData = (JUCtrlHierNodeBinding) walkThruTable.getRowData();
      Row r = rowData.getRow();
      if (r.getAttribute("DocumentNumber") != null) {
        deleteKey = (r.getAttribute("DocumentNumber").toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      walkThruTable.setRowKey(oldRowKey);
    }

    if (deleteKey == null || deleteKey.equals("")) {
      displayMessage("[ERROR] Unknown Record for deletion.");
    }
    else {
      try {
        String message = getWalkThruAMService().deleteDocumentNumber(deleteKey);

        if (message.equals("")) {
          DCIteratorBinding iter = ADFUtils.findIterator(walkThruIterator);
          iter.executeQuery();
          displaySuccessMessage("Walk Thru deleted.");
        }
        else {
          displayMessage(message);
        }
      }
      catch (Exception e) {
        displayMessage("[ERROR] Error occurred while trying to delete ");
      }
    }
  }

  public void setWalkThruTable(RichTable walkThruTable) {
    this.walkThruTable = walkThruTable;
  }

  public RichTable getWalkThruTable() {
    return walkThruTable;
  }

  public void setDocID(RichInputText docID) {
    this.docID = docID;
  }

  public RichInputText getDocID() {
    return docID;
  }

  private void resetInputText(String id) {
    RichInputText input = (RichInputText) JSFUtils.findComponentInRoot(id);
    input.setSubmittedValue(null);
    input.resetValue();
    AdfFacesContext.getCurrentInstance().addPartialTarget(input);
  }

  protected void displayMessage(String msgOutput) {
    /* display message to GUI */
    if (msgOutput.length() > 0) {
      FacesMessage msg =
          new FacesMessage(FacesMessage.SEVERITY_ERROR, msgOutput, null);
      FacesContext facesContext = FacesContext.getCurrentInstance();
      facesContext.addMessage("test", msg);
    }
  }

  protected void displaySuccessMessage(String msgOutput) {
    FacesMessage msg = new FacesMessage(msgOutput);
    FacesContext facesContext = FacesContext.getCurrentInstance();
    facesContext.addMessage("test", msg);
  }
}

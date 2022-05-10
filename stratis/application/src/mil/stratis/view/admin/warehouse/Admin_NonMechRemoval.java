package mil.stratis.view.admin.warehouse;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.WarehouseSetupImpl;
import mil.stratis.view.shipping.ShippingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Iterator;

@Slf4j
@NoArgsConstructor
@SuppressWarnings("java:S101") // (FUTURE) renaming of backing Beans has impacts on ADF.  research for future update.
public class Admin_NonMechRemoval extends ShippingHandler {

  private transient RichTable nonMechLocationTable;

  private void removeBulkLocation() {
    StringBuilder msgOutput = new StringBuilder();

    //* invoke the service to update the data
    WarehouseSetupImpl whService = getWarehouseSetupModule();

    Object oldRowKey = nonMechLocationTable.getRowKey();
    Iterator<Object> selection = nonMechLocationTable.getSelectedRowKeys().iterator();
    Object location = "";
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        nonMechLocationTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) nonMechLocationTable.getRowData();
        Row r = rowData.getRow();
        Object locationId = r.getAttribute("LocationId");
        location = r.getAttribute("LocationLabel");
        log.debug("row  {}", locationId.toString());

        if (whService.submitRemoveBulkLocations(Util.cleanString(locationId))) {
          //* display message to GUI
          msgOutput.setLength(0);
          msgOutput.append("Successfully removed bulk location  ").append(location).append('.');
          FacesMessage msg = new FacesMessage(msgOutput.toString());
          FacesContext facesContext = FacesContext.getCurrentInstance();
          facesContext.addMessage("INFO", msg);
        }
        else {
          //* display message to GUI
          JSFUtils.addFacesErrorMessage("ERROR", "Unable to remove bulk location " + location + '.');
        }
      }
    }
    catch (Exception e) {
      JSFUtils.addFacesErrorMessage("ERROR", "Unable to remove bulk location " + location + '.');
      AdfLogUtility.logException(e);
    }
    finally {
      nonMechLocationTable.setRowKey(oldRowKey);
    }

    //* refresh iterator
    OperationBinding op1 = ADFUtils.getDCBindingContainer().getOperationBinding("ExecuteWithoutParams");
    op1.execute();
  }

  public void dialogListener(DialogEvent dialogEvent) {
    if (dialogEvent.getOutcome().equals(DialogEvent.Outcome.ok)) {
      removeBulkLocation();
    }
  }

  @SuppressWarnings("unused")
  public void selectAll(ActionEvent event) {
    RowKeySet rks = new RowKeySetImpl();
    CollectionModel model = (CollectionModel) nonMechLocationTable.getValue();

    int rowcount = model.getRowCount();
    for (int i = 0; i < rowcount; i++) {
      model.setRowIndex(i);
      Object key = model.getRowKey();
      rks.add(key);
    }
    nonMechLocationTable.setSelectedRowKeys(rks);
  }

  @SuppressWarnings("unused")
  public void selectNone(ActionEvent event) {
    RowKeySet rks = new RowKeySetImpl();
    nonMechLocationTable.setSelectedRowKeys(rks);
  }

  public void setNonMechLocationTable(RichTable nonMechLocationTable) {
    this.nonMechLocationTable = nonMechLocationTable;
  }

  public RichTable getNonMechLocationTable() {
    return nonMechLocationTable;
  }
}

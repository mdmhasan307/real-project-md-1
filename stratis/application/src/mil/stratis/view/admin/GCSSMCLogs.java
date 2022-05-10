package mil.stratis.view.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.Util;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.jbo.Row;
import oracle.jbo.uicli.binding.JUCtrlValueBindingRef;

import javax.faces.event.ActionEvent;
import java.util.Iterator;

@Slf4j
@NoArgsConstructor
public class GCSSMCLogs extends AdminBackingHandler {

  private transient RichTable gcssmcImportsLogTable;
  private static String logIterator = "GCSSImportsLog1Iterator";

  @SuppressWarnings("unused")
  public void saveToFile(ActionEvent event) {

    String selectedKey = "";

    Object oldRowKey = gcssmcImportsLogTable.getRowKey();
    Iterator selection = gcssmcImportsLogTable.getSelectedRowKeys().iterator();
    try {
      while (selection.hasNext()) {
        Object rowKey = selection.next();
        gcssmcImportsLogTable.setRowKey(rowKey);
        JUCtrlValueBindingRef rowData = (JUCtrlValueBindingRef) gcssmcImportsLogTable.getRowData();
        Row r = rowData.getRow();
        if (r.getAttribute("GcssmcImportsDataId") != null)
          selectedKey = (r.getAttribute("GcssmcImportsDataId").toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      gcssmcImportsLogTable.setRowKey(oldRowKey);
    }

    if (selectedKey == null || selectedKey.equals("")) {
      displayMessage("[ERROR] Unknown Record selection.");
      return;
    }

    try {
      if (getImportFilesService().exportGCSSMCXML(Util.cleanInt(selectedKey))) {
        JSFUtils.addFacesInformationMessage("Log saved successfully.");
      }
      else {
        displayMessage("Save failed.");
      }
    }
    catch (Exception e2) {
      displayMessage("[ERROR] Error occurred while trying to update ");
    }
  }

  public void setGcssmcImportsLogTable(RichTable gcssmcImportsLogTable) {
    this.gcssmcImportsLogTable = gcssmcImportsLogTable;
  }

  public RichTable getGcssmcImportsLogTable() {
    return gcssmcImportsLogTable;
  }

  public static void setLogIterator(String logIterator) {
    GCSSMCLogs.logIterator = logIterator;
  }

  public static String getLogIterator() {
    return logIterator;
  }
}

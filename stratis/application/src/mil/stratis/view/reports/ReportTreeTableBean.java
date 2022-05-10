package mil.stratis.view.reports;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.view.rich.component.rich.data.RichTreeTable;
import org.apache.myfaces.trinidad.model.ChildPropertyTreeModel;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;

import javax.faces.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ReportTreeTableBean {

  private List<StandardReport> reportList = new ArrayList<>();

  ChildPropertyTreeModel reportNameVal;
  //Component Binding of af:treeTable in bean
  private RichTreeTable treeTableBind;

  // shared report info bean
  private ReportsInfo reportsInfoBean;
  private transient String actionForwardStr = "";

  public void setTreeTableBind(RichTreeTable treeTableBind) {
    this.treeTableBind = treeTableBind;
  }

  public RichTreeTable getTreeTableBind() {
    return treeTableBind;
  }

  public ReportTreeTableBean() {
    super();
    StandardReport reportName = null;

    // Adding "Workload" Report Group
    StandardReport reportGroup1 = new StandardReport("WORKLOAD");
    //Adding "Workload" Report Names
    reportName = new StandardReport("Workload Detail (Picking Packing Shipping)");
    reportGroup1.addReports(reportName);
    reportName = new StandardReport("Workload Detail (Stowing)");
    reportGroup1.addReports(reportName);
    reportName = new StandardReport("Workload Detail (Expiring NIINs)");
    reportGroup1.addReports(reportName);
    reportName = new StandardReport("Workload Detail (NSN Updates)");
    reportGroup1.addReports(reportName);
    reportName = new StandardReport("Workload Detail (Pending Inventories)");
    reportGroup1.addReports(reportName);

    // Adding "Recon" Report Group
    StandardReport reportGroup2 = new StandardReport("RECON REPORT");
    //Adding "Recon" Report Names
    reportName = new StandardReport("Recon Report Summary");
    reportGroup2.addReports(reportName);
    reportName = new StandardReport("Onhand Serviceable Qty Details");
    reportGroup2.addReports(reportName);
    reportName = new StandardReport("Onhand Un-Serviceable Qty Details");
    reportGroup2.addReports(reportName);
    reportName = new StandardReport("Onhand Serial Controlled Serviceable Qty Details");
    reportGroup2.addReports(reportName);
    reportName = new StandardReport("Onhand Serial Controlled Un-Serviceable Qty Details");
    reportGroup2.addReports(reportName);

    // Adding "Recon" Report Group
    StandardReport reportGroup3 = new StandardReport("IMPORTS");
    //Adding "Recon" Report Names
    reportName = new StandardReport("File Import Logs Report (DASF)");
    reportGroup3.addReports(reportName);
    reportName = new StandardReport("File Import Logs Report (GABF)");
    reportGroup3.addReports(reportName);
    reportName = new StandardReport("File Import Logs Report (GBOF)");
    reportGroup3.addReports(reportName);
    reportName = new StandardReport("File Import Logs Report (MATS)");
    reportGroup3.addReports(reportName);
    reportName = new StandardReport("File Import Logs Report (MHIF)");
    reportGroup3.addReports(reportName);

    // Adding "INTERFACE TRANSACTIONS" Report Group
    StandardReport reportGroup4 = new StandardReport("INTERFACE TRANSACTIONS");
    //Adding "INTERFACE TRANSACTIONS" Report Names
    reportName = new StandardReport("Interface Transactions");
    reportGroup4.addReports(reportName);

    // Adding "RECEIPTS" Report Group
    StandardReport reportGroup5 = new StandardReport("RECEIPTS");
    //Adding "RECEIPTS" Report Names
    reportName = new StandardReport("Active Receipt Report");
    reportGroup5.addReports(reportName);
    reportName = new StandardReport("Receipt History Report");
    reportGroup5.addReports(reportName);

    // Adding "Serial Lot Control Numbers" Report Group
    StandardReport reportGroup6 = new StandardReport("SERIAL LOT CONTROL NUMBERS");
    //Adding "Serial Lot Control Numbers" Report Names
    reportName = new StandardReport("Serial Lot Control Numbers Report");
    reportGroup6.addReports(reportName);

    // Adding "SHELF LIFE" Report Group
    StandardReport reportGroup7 = new StandardReport("SHELF LIFE");
    //Adding "SHELF LIFE" Report Names
    reportName = new StandardReport("Shelf-Life Report");
    reportGroup7.addReports(reportName);

    // Adding "INVENTORY" Report Group
    StandardReport reportGroup8 = new StandardReport("INVENTORY");
    //Adding "INVENTORY" Report Names
    reportName = new StandardReport("Inventory History Report");
    reportGroup8.addReports(reportName);

    // Adding "EMPLOYEES" Report Group
    StandardReport reportGroup9 = new StandardReport("EMPLOYEES");
    //Adding "EMPLOYEES" Report Names
    reportName = new StandardReport("Employee Workload History");
    reportGroup9.addReports(reportName);

    // Adding "DASF" Report Group
    StandardReport reportGroup10 = new StandardReport("DASF");
    //Adding "DASF" Report Names
    reportName = new StandardReport("DASF");
    reportGroup10.addReports(reportName);

    // Adding "LOCATOR DECK" Report Group
    StandardReport reportGroup11 = new StandardReport("LOCATOR DECK");
    //Adding "LOCATOR DECK" Report Names
    reportName = new StandardReport("Locator Deck");
    reportGroup11.addReports(reportName);

    // Adding all list to topList
    reportList.add(reportGroup1);
    reportList.add(reportGroup2);
    reportList.add(reportGroup3);
    reportList.add(reportGroup4);
    reportList.add(reportGroup5);
    reportList.add(reportGroup6);
    reportList.add(reportGroup7);
    reportList.add(reportGroup8);
    reportList.add(reportGroup9);
    reportList.add(reportGroup10);
    reportList.add(reportGroup11);

    // Filtering Child Data, ChildPropertyTreeModel creates a TreeModel from a List of beans, see
    // https://myfaces.apache.org/trinidad/trinidad-api/apidocs/org/apache/myfaces/trinidad/model/ChildPropertyTreeModel.html
    reportNameVal = new ChildPropertyTreeModel(reportList, "reports");
  }

  public ChildPropertyTreeModel getReportNameVal() {
    return reportNameVal;
  }

  public void setReportsInfoBean(ReportsInfo reportsInfo) {
    this.reportsInfoBean = reportsInfo;
  }

  public ReportsInfo getReportsInfoBean() {
    return reportsInfoBean;
  }

  /**
   * Method to get Selected row value from POJO based treeTable
   * To Know about creation of POJO based tree
   * Check-http://www.awasthiashish.com/2014/07/populate-aftreetable-programatically.html
   */
  public void getSelectedRowAction(ActionEvent actionEvent) {
    if (reportsInfoBean == null)
      log.debug("reportsInfoBean is null");
    else
      log.debug("reportsInfoBean is NOT null");
    //Get Collection Model from treeTable binding
    CollectionModel treeModel = null;
    treeModel = (CollectionModel) this.getTreeTableBind().getValue();
    //Get selected row keys from treeTable
    RowKeySet selectedChildKeys = getTreeTableBind().getSelectedRowKeys();

    if (!selectedChildKeys.isEmpty()) {
      List<StandardReport> reportList = (List<StandardReport>) treeModel.getWrappedData();
      //Create iterator from RowKeySet
      Iterator selectedCharIter = selectedChildKeys.iterator();
      //Iterate over RowKeySet to get all selected childs of treeTable
      while (selectedCharIter.hasNext()) {
        List val = (List) selectedCharIter.next();
        //Get report group (Parent) List of selected row
        StandardReport rpt = reportList.get(Integer.parseInt(val.get(0).toString()));
        //Get report name list of selected report group
        List<StandardReport> rptList = rpt.getReports();
        //If selected record is child (Report Name)
        if (val.size() > 1) {
          StandardReport r = rptList.get(Integer.parseInt(val.get(1).toString()));
          log.debug("Report Name -  {}", r.getName());
          reportsInfoBean.resetInputParameters();
          reportsInfoBean.setTargetReportName(r.getName());
          actionForwardStr = "GoReports_InputParam";
        }
        else {
          log.debug("Report Group -  {}", rpt.getName());
        }
      }
    }
    else {
      log.debug("error - report is not selected");
      JSFUtils.addFacesErrorMessage("REPORT_MISSING", "Please select report.");
      actionForwardStr = "";
    }
  }

  public String actionForwardStrForReport() {
    return actionForwardStr;
  }
}

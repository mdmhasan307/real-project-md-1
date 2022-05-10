package mil.stratis.view.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.input.RichInputDate;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;
import oracle.jbo.DMLConstraintException;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.ucp.jdbc.JDBCConnectionPoolStatistics;
import oracle.ucp.jdbc.PoolDataSource;
import org.apache.myfaces.trinidad.model.RowKeySet;

import javax.faces.application.FacesMessage;
import javax.faces.component.UISelectItems;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlInputSecret;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.InitialContext;
import java.util.Iterator;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class AdminBackingHandler extends BackingHandler {

  protected void logDbPoolInfo() {
    try {
      AdfDbCtxLookupUtils adfDbCtxLookupUtils = ContextUtils.getBean(AdfDbCtxLookupUtils.class);

      InitialContext ctx = new InitialContext();
      PoolDataSource ds = (PoolDataSource) ctx.lookup(adfDbCtxLookupUtils.getDbCtxLookupPath());

      JDBCConnectionPoolStatistics stats = ds.getStatistics();

      StringBuilder sb = new StringBuilder(1000);
      sb.append("UCP Pool statistics").append('\n');
      sb.append("  total connections: ").append(stats.getTotalConnectionsCount()).append('\n');
      sb.append("  available conns  : ").append(stats.getAvailableConnectionsCount()).append('\n');
      sb.append("  borrowed conns   : ").append(stats.getBorrowedConnectionsCount()).append('\n');
      sb.append("  conns created #  : ").append(stats.getConnectionsCreatedCount()).append('\n');
      sb.append("  conns closed #   : ").append(stats.getConnectionsClosedCount()).append('\n');

      sb.append("  cumulative conns borrowed #: ").append(stats.getCumulativeConnectionBorrowedCount()).append('\n');
      sb.append("  peak connections count     : ").append(stats.getPeakConnectionsCount());

      log.info(sb.toString());
    }
    catch (Exception e) {
      log.warn("Failed to log pool statistics!", e);
    }
  }

  /**
   * Returns the associated label of a selected item in the any navigation list.
   * Date: 1/17/08
   *
   * @return String
   */
  protected String getListLabel(String value, List list) {
    for (Object obj : list) {
      Object selectItemsValue = ((UISelectItems) obj).getValue();
      if (selectItemsValue != null) {
        List<SelectItem> l = (List<SelectItem>) selectItemsValue;
        for (SelectItem li : l) {
          if (value.equals(li.getValue().toString())) {
            return li.getLabel();
          }
        }
      }
    }
    return "";
  }

  protected void saveIterator(String iteratorName) {

    try {
      DCIteratorBinding iter = ADFUtils.findIterator(iteratorName);
      if (iter != null) {
        // commit the change
        iter.getDataControl().commitTransaction();
        iter.executeQuery();
      }
    }
    catch (Exception e) { AdfLogUtility.logException(e); }
  }

  protected void updateTableRangeChange(Row firstRow, String iteratorName) {
    DCIteratorBinding iter = ADFUtils.findIterator(iteratorName);
    if (iter != null) {
      iter.setCurrentRowWithKey(firstRow.getKey().toStringFormat(true));
    }
  }

  protected void saveIterator(String iteratorName, String function,
                              String fields) {

    DCIteratorBinding iter = ADFUtils.findIterator(iteratorName);
    try {
      if (iter != null) {

        //* validate the row (does not validate the entire iterator (ie batch save))
        iter.getCurrentRow().validate();

        //* commit the change
        iter.getDataControl().commitTransaction();
        //* so the pointer does not jump to top of screen
        iter.executeQuery();
      }
      displaySuccessMessage(function + " saved successfully.");
    }
    catch (DMLConstraintException dml) {
      log.debug("[{}] {}", function, dml.toString());
      iter.getDataControl().rollbackTransaction();
      displayMessage("Unable to save " + function +
          ".  Duplicate fields (" + fields +
          ") are not allowed.  Check the table above to make sure these fields do not already exist.  Try Again.");
    }
    catch (Exception e) {
      displayMessage("Unable to save " + function + ".");
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method refreshes the iterator and keeps the same current row
   */
  protected void refreshIteratorKeepPosition(DCIteratorBinding iter) {

    //* so the pointer does not jump to top of screen
    Key currentRowKey = iter.getCurrentRow().getKey();
    iter.executeQuery();
    iter.setCurrentRowWithKey(currentRowKey.toStringFormat(true));
  }

  protected void reset(String iteratorName) {
    try {
      DCIteratorBinding iterByName = ADFUtils.findIterator(iteratorName);
      Row r = iterByName.getCurrentRow();
      r.refresh(Row.REFRESH_UNDO_CHANGES);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  protected void cancel(String iteratorName) {
    try {
      DCIteratorBinding iterByName = ADFUtils.findIterator(iteratorName);
      Row r = iterByName.getCurrentRow();
      int combination = Row.REFRESH_REMOVE_NEW_ROWS | Row.REFRESH_WITH_DB_FORGET_CHANGES;
      r.refresh(combination);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  protected void saveIteratorKeepPosition(String iteratorName, String function, String fields, RichTable table, boolean donotEXECUTEQUERY) {

    boolean success;
    DCIteratorBinding iter;

    try {
      iter = ADFUtils.findIterator(iteratorName);
      if (iter != null) {
        //* validate the row (does not validate the entire iterator)
        iter.getCurrentRow().validate();
        success = true;
      }
      else {
        //* failed to get the iterator
        success = false;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);

      // do not rollback, in order to allow the user time on the UI to update the failed input or cancel
      // except rollback would cause less dirty transactions
      displayMessage("Unable to save " + function + " due to errors validating row(s).  Re-enter data or Press Cancel to discard this row.");
      throw new StratisRuntimeException("Unable to save " + function + " due to errors validating row(s).");
    }

    if (success) {
      try {
        //* commit the change
        iter.getDataControl().commitTransaction();
        //* so the pointer does not jump to top of screen
        if (donotEXECUTEQUERY)
          refreshIteratorKeepPosition2(iter, table);
        else
          refreshIteratorKeepPosition(iter, table);

        displaySuccessMessage(function + " saved successfully.");
      }
      catch (DMLConstraintException dml) {
        log.error("[{}] {}", function, dml.toString());

        // do not rollback, in order to allow the user time on the UI to update the failed input or cancel
        // except rollback would cause less dirty transactions
        iter.getDataControl().rollbackTransaction();
        displayMessage("Unable to save " + function +
            ".  Duplicate fields (" + fields +
            ") are not allowed.  Check the table above to make sure these fields do not already exist.  " +
            "Try again.");

        // do not throw exception, to continue in the process and keep the buttons from showing up
      }
      catch (oracle.jbo.RowInconsistentException ie) {
        //* updated 12/31/08 [User]oracle.jbo.RowInconsistentException: JBO-25014: Another user has changed the row with primary key oracle.jbo.Key[1729 ].

        try {
          //* commit the change
          iter.getDataControl().commitTransaction();
          //* so the pointer does not jump to top of screen
          if (donotEXECUTEQUERY)
            refreshIteratorKeepPosition2(iter, table);
          else
            refreshIteratorKeepPosition(iter, table);

          displaySuccessMessage(function + " saved successfully.");
        }
        catch (Exception e) {
          log.error("[{}] {}", function, e.toString());

          // do not rollback, in order to allow the user time on the UI to update the failed input or cancel
          // except rollback would cause less dirty transactions
          displayMessage("Unable to save " + function + ".  Re-enter data or Press cancel to discard this row.");
          throw new StratisRuntimeException("Unable to save " + function);
        }
      }
      catch (Exception e) {
        // do not rollback, in order to allow the user time on the UI to update the failed input or cancel
        // except rollback would cause less dirty transactions
        displayMessage("Unable to save " + function + ".  Re-enter data or Press cancel to discard this row.");
        AdfLogUtility.logException(e);
        throw new StratisRuntimeException("Unable to save " + function);
      }
    }
  }

  protected void resetKeepPosition(String iteratorName) {
    //* get the iterator used to populate the building table
    try {
      DCIteratorBinding iterByName = ADFUtils.findIterator(iteratorName);
      Row r = iterByName.getCurrentRow();
      r.refresh(Row.REFRESH_UNDO_CHANGES);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method refreshes the iterator and keeps the same current row.
   * An updated, more detailed version of refreshIterator_KeepPosition.
   * This method first gets and updates the new inserted
   * (redundant for an update, since the row was already in database).
   * Using the key from the row, it executes the query (only if needed) to
   * get the latest data.  Lastly the iterator's current row is set to the key
   * <p>
   * NOTE:  Since we don't want all iterators of the view to be affected
   * (if used on other pages), we use the iterator
   * instead of the view object when set the current row.
   * <p>
   * Designed to be used without batch save master-detail forms.
   */

  //* refresh the new inserted row to get a key
  //* use the table selection versus the current row of the iterator,
  //* just in case executeQuery called elsewhere and position lost
  protected void refreshIteratorKeepPosition(DCIteratorBinding iterArg, RichTable table) {
    RowSetIterator rsIter = iterArg.getRowSetIterator();
    RowKeySet keySet = table.getSelectedRowKeys();
    Row newRow = null;
    String theKey;

    theKey = iterArg.getCurrentRowWithKeyValue();   // This should be the current key string.

    if (keySet != null) {
      Iterator<Object> iter = keySet.iterator();
      if (iter != null) {
        while (iter.hasNext()) {
          Object rowKey = iter.next();
          Key k = new Key(new Object[]{rowKey});
          newRow = rsIter.getRow(k);
          if (newRow != null) { // newRow is null here ???
            newRow.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);
          }
          theKey = k.toStringFormat(true);
        }
      }
    }

    if (newRow != null) {
      iterArg.executeQuery();
      iterArg.setCurrentRowWithKey(theKey);
    }
  }

  //* refresh the new inserted row to get a key
  //* use the table selection versus the current row of the iterator,
  //* just in case executeQuery called elsewhere and position lost

  protected void refreshIteratorKeepPosition2(DCIteratorBinding iterArg, RichTable table) {
    RowSetIterator rsIter = iterArg.getRowSetIterator();
    Row newRow;
    RowKeySet keySet;

    if (table != null) {
      keySet = table.getSelectedRowKeys();
      if (keySet != null) {
        Iterator<Object> iter = keySet.iterator();
        if (iter != null) {
          while (iter.hasNext()) {
            Object rowKey = iter.next();
            Key k = new Key(new Object[]{rowKey});
            newRow = rsIter.getRow(k);
            if (newRow != null) {
              newRow.refresh(Row.REFRESH_WITH_DB_FORGET_CHANGES);
            }
          }
        }
      }
    }
  }

  protected void refreshIterator(DCIteratorBinding iter) {
    iter.executeQuery();
  }

  private boolean isEmptyHandler(ValueHolder component) {
    if (null != component) {
      Object componentValue = component.getValue();
      return null == componentValue || componentValue.toString().trim().isEmpty();
    }
    else {
      log.error("Component passed was null.");
      return true;
    }
  }

  protected boolean isEmpty(RichSelectOneChoice component) {
    return isEmptyHandler(component);
  }

  protected boolean isEmpty(RichInputText component) {
    return isEmptyHandler(component);
  }

  protected boolean isEmpty(RichInputDate component) {
    return isEmptyHandler(component);
  }

  protected boolean isEmpty(HtmlInputText component) {
    return isEmptyHandler(component);
  }

  protected boolean isEmpty(HtmlInputSecret component) {
    return isEmptyHandler(component);
  }

  /**
   * This function is used to determine if a value is "not a number".
   * Must be a positive integer number to be considered "not a number"
   *
   * @return boolean
   */
  protected boolean isNaN(RichInputText txt) {
    boolean notANumber = false;
    try {
      int i = Integer.parseInt(txt.getValue().toString());
      if (i < 0)
        notANumber = true;
    }
    catch (Exception e) {
      notANumber = true;
    }
    return notANumber;
  }

  /**
   * This function is used to determine if a value is "not a decimal".
   * Must be a positive decimal number to be considered "not a decimal"
   *
   * @return boolean
   */
  protected boolean isNaD(RichInputText txt) {
    boolean notADecimal = false;
    if (txt != null) {
      try {
        double d = Double.parseDouble(txt.getValue().toString());
        if (d < 0)
          notADecimal = true;
      }
      catch (Exception e) {
        notADecimal = true;
      }
    }
    else {
      log.trace("Component was null.");
    }
    return notADecimal;
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

  protected String uppercase = "text-transform:uppercase;";

  protected void setFocus(RichInputText cit) {
    if (cit != null) {
      cit.setInlineStyle("border-color:red;");
      cit.setContentStyle("text-transform:uppercase;");
    }
  }

  protected void setFocus(RichSelectOneChoice csoc) {
    if (csoc != null) {
      csoc.setInlineStyle("background-color:red;background-repeat:no-repeat;");
    }
  }
}

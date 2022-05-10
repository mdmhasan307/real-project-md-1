package mil.stratis.model.threads.imports;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.model.services.SysAdminImpl;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.server.DBTransaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.PreparedStatement;

@Slf4j
@NoArgsConstructor
public class ImportCourierIORThread implements Runnable {

  String fileName;
  DBTransaction db;
  SysAdminImpl sysadmin;
  int totalRowCount = 0;

  public void init(String filename, DBTransaction db, SysAdminImpl sysadmin) {
    this.fileName = filename;
    this.db = db;
    this.sysadmin = sysadmin;

    Thread t = new Thread(this);
    t.start();
  }

  public void run() {
    log.debug("running... {}", fileName);

    int rowCount = 0;
    int issuesInserted = 0;
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String tempLine = "";

      clearCourierDataloadLog();
      setCourierLog("INFO: COURIER WALKTHRU STARTED", "INFO: COURIER WALKTHRU STARTED", 0);
      boolean b_issue_not_created = false;
      while ((tempLine = br.readLine()) != null) {
        rowCount++;
        if (!checkIssueExists(tempLine, rowCount)) {
          if (setException(tempLine)) {
            issuesInserted++;
          }
        }
      }
      setCourierLog("INFO: COURIER WALKTHRU COMPLETED", "INFO: COURIER WALKTHRU COMPLETED: Issues added: " + issuesInserted + " out of " + rowCount, 0);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void clearCourierDataloadLog() {
    try {
      log.debug("clearing courior walkthru from dataload_log");
      try (PreparedStatement pstmt =
               db.createPreparedStatement("DELETE FROM REF_DATALOAD_LOG WHERE INTERFACE_NAME='COURIER'", 0)) {
        pstmt.execute();
        db.commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private boolean checkIssueExists(String importData, int rowCount) {
    //Courier file too short.
    try {

      if (importData.length() < 80) {
        setCourierLog("ERROR: COURIER FILE ERROR", "ERROR: BAD FORMAT", rowCount);
      }
      else {
        String docNum = importData.substring(29, 43);
        String msg = sysadmin.checkDuplicateDocNumInIssue(docNum);
        if (msg != null && msg.length() > 0) {
          setCourierLog("ERROR: COURIER FILE ERROR", "ERROR: " + msg, rowCount);
          return true;
        }
        else {
          return false;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return true;
  }

  private boolean setException(String importData) {
    PreparedStatement pstmt = null;
    boolean successful = false;
    try {
      String sql = "begin INSERT INTO error_queue (status, created_date, created_by, notes, eid) " +
          " values (?,sysdate,1,?, 81); end;";
      pstmt = db.createPreparedStatement(sql, 0);
      pstmt.setString(1, "OPEN");
      pstmt.setString(2, importData);
      pstmt.executeUpdate();
      pstmt.close();
      db.commit();
      successful = true;
    }
    catch (Exception e) {
      db.rollback();
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (pstmt != null) {
          pstmt.close();
          pstmt = null;
        }
      }
      catch (Exception e) {log.trace("Exception caught and ignored", e);}
    }
    return successful;
  }

  private void setCourierLog(String rowStr, String descrStr, int rowNum) throws Exception {
    PreparedStatement pstmt = null;
    try {
      String sql = "begin INSERT INTO ref_dataload_log (interface_name, created_date, created_by, data_row, description, line_no) " +
          " values (?,sysdate,1,?,?,?); end;";
      pstmt = db.createPreparedStatement(sql, 0);
      pstmt.setString(1, "COURIER");
      pstmt.setString(2, rowStr);
      pstmt.setString(3, descrStr);
      pstmt.setInt(4, rowNum);
      pstmt.executeUpdate();
      pstmt.close();
      db.commit();
    }
    catch (Exception e) {
      db.rollback();
      AdfLogUtility.logException(e);
      throw new Exception();
    }
    finally {
      try {
        if (pstmt != null) {
          pstmt.close();
          pstmt = null;
        }
      }
      catch (Exception e) {log.trace("Exception caught and ignored", e);}
    }
  }
}

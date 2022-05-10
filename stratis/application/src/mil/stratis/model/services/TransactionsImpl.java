package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.PadUtil;
import mil.stratis.common.util.StringUtil;
import mil.stratis.model.services.common.Transactions;
import mil.stratis.model.view.wlm.SiteInfoViewImpl;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.SessionData;
import oracle.jbo.server.ApplicationModuleImpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class TransactionsImpl extends ApplicationModuleImpl implements Transactions {

  public String getCurrentJulian(int numChars) {
    String retVal = "";
    try {

      Calendar cal = Calendar.getInstance();
      SimpleDateFormat simpleDF = new SimpleDateFormat("yyyyDDD");
      String formattedDate = simpleDF.format(cal.getTime());

      if (numChars == 5)
        retVal += formattedDate.charAt(2);
      if (numChars == 4 || numChars == 5)
        retVal += formattedDate.charAt(3);

      if (numChars >= 3 && numChars <= 5) {
        retVal += formattedDate.charAt(4);
        retVal += formattedDate.charAt(5);
        retVal += formattedDate.charAt(6);
        return retVal;
      }
      if (numChars < 3 || numChars > 5)
        return "   ";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    retVal = "   ";
    return retVal;
  }

  public String returnNextAvailableDocumentNumber() {
    String docreturn = "";
    try {
      SiteInfoViewImpl siteview = getSiteInfoView1();

      if (siteview.getRowCount() > 0) {
        Object AACobj = siteview.getRowAtRangeIndex(0).getAttribute("Aac");

        // AAC
        if (AACobj != null)
          docreturn += AACobj;

        //Get current JDate
        docreturn += this.getCurrentJulian(4);

        int index = 0;
        try (PreparedStatement st = getDBTransaction().createPreparedStatement("select DOC_SER_NUM_SEQ.nextval from dual", 0)) {
          try (ResultSet rs = st.executeQuery()) {
            rs.next();
            index = rs.getInt(1);
          }
        }
        // the current serial_number index

        // check how we are going to do this
        if (index < 10) {
          docreturn += "000";
        }
        else if ((index < 100)) {
          docreturn += "00";
        }
        else if ((index < 1000)) {
          docreturn += "0";
        }

        docreturn += index;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    // return the new number
    return docreturn;
  }

  /**
   * AUTHOR   : John P. Owens - Stanley Associates
   * DATE     : Oct 2, 2007
   * DESC     : This function inserts a given transaction record into the
   * SPOOL Table.
   */
  public boolean insertSpoolRecord(String defMode, String docId, String spoolRec, Object niin_id) {
    boolean bVal = false;
    try {
      /* defMode Table
       * O : AS1, AS2, YLL, D6_, D8_, D7_, D9_, Z7_, Z0_, ZBE
       * A : SRO (backorder), ZZF, (Some cases D9A/D8A?)
       * H : DWF
       * */

      //Set Priority Code
      String sPriority = "";
      if (docId.equalsIgnoreCase("AS1") || docId.equalsIgnoreCase("AS2") || docId.equalsIgnoreCase("Z7K") || docId.equalsIgnoreCase("Z01") || docId.equalsIgnoreCase("Z0A") || docId.equalsIgnoreCase("Z7 ") || docId.equalsIgnoreCase("Z2M") || docId.equalsIgnoreCase("SRO"))
        sPriority = "02";
      else {
        if (docId.equalsIgnoreCase("D6A") || docId.equalsIgnoreCase("D6T") || docId.equalsIgnoreCase("D7J") || docId.equalsIgnoreCase("ZBE") || docId.equalsIgnoreCase("D7A") || docId.equalsIgnoreCase("DAC") || docId.toUpperCase(Locale.US).startsWith("D8") || docId.toUpperCase(Locale.US).startsWith("D9") || docId.equalsIgnoreCase("DWF"))
          sPriority = "05";
        else {
          if (docId.equalsIgnoreCase("YLL"))
            sPriority = "07";
          else {
            if (docId.equalsIgnoreCase("ZZF"))
              sPriority = "01";
          }
        }
      }

      try (PreparedStatement spoolAdd = getDBTransaction().createPreparedStatement("insert into spool(spool_id, spool_def_mode, priority, timestamp, transaction_type, rec_data, niin_id) values(spool_id_seq.nextval, ?,?,sysdate,?,?, ?)", 0)) {
        // input the values we are going to add
        spoolAdd.setString(1, defMode);
        spoolAdd.setString(2, sPriority);
        spoolAdd.setString(3, docId);
        spoolAdd.setString(4, spoolRec);
        spoolAdd.setObject(5, niin_id);

        spoolAdd.execute();
      }
      bVal = true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return bVal;
  }

  /**
   * AUTHOR   : John P. Owens - Stanley Associates
   * DATE     : Oct 26, 2008
   * DESC     : This function searches the spool table for D8A and D9A records for the given NIIN and CC and
   * sums up (or cancels out) the gains and losses for the day for that given NIIN & CC
   * The resulting single record gets placed in the SPOOL table
   */
  public void sumGainsAndLosses(String niin, String cc, Object niin_id) {
    try {
      int sum = 0;
      int numGained = 0;
      int numLost = 0;
      int qtyGained = 0;
      int qtyLost = 0;
      String sTrans = "";
      String d8trans = "";
      String d9trans = "";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select rec_data from spool where spool_def_mode = 'C' and transaction_type = 'D8A'", 0)) {
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            if (rs.getString(1).substring(11, 20).equals(niin) && rs.getString(1).substring(65, 66).equals(cc)) {
              //NIIN and CC match
              qtyGained += Integer.parseInt(rs.getString(1).substring(24, 29));
              d8trans = rs.getString(1);
              numGained++;
            }
          }
        }
      }

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select rec_data from spool where spool_def_mode = 'C' and transaction_type = 'D9A'", 0)) {
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            if (rs.getString(1).substring(11, 20).equals(niin) && rs.getString(1).substring(65, 66).equals(cc)) {
              //NIIN and CC match
              qtyLost += Integer.parseInt(rs.getString(1).substring(24, 29));
              d9trans = rs.getString(1);
              numLost++;
            }
          }
        }
      }

      if (numGained > 1 || numLost > 1 || (numGained > 0 && numLost > 0)) {
        //Delete the previous Rows
        try (PreparedStatement psDel = getDBTransaction().createPreparedStatement("delete from spool where spool_def_mode = 'C' and (transaction_type = 'D8A' or transaction_type = 'D9A') and substr(rec_data, 12, 9) = '" + niin + "'", 0)) {
          psDel.execute();
          this.getTransaction().commit();
        }
        catch (Exception sp) {
          this.getTransaction().rollback();
        }

        sum = qtyGained - qtyLost;
        String sQty = "00000";

        if (sum > 0) {
          //There is a net gain for this NIIN & CC
          sQty += qtyGained - qtyLost;
          sTrans = d8trans.substring(0, 24);
          sTrans += sQty.substring(sQty.length() - 5, sQty.length());
          sTrans += d8trans.substring(29, 80);
          this.insertSpoolRecord("C", "D8A", sTrans, niin_id);
          this.getDBTransaction().commit();
        }
        else if (sum < 0) {
          //There is a net loss for this NIIN & CC
          sQty += qtyLost - qtyGained;
          sTrans = d9trans.substring(0, 24);
          sTrans += sQty.substring(sQty.length() - 5, sQty.length());
          sTrans += d9trans.substring(29, 80);
          this.insertSpoolRecord("C", "D9A", sTrans, niin_id);
          this.getDBTransaction().commit();
        } //else gains and losses cancel and nothing is added
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int processSerialLotForD8D9(String invItemId, String niinIdStr) {
    try {
      boolean _found = false;
      String fsc = "";
      String qty = "0";
      String nlId = "0";
      String niin = "";
      String ui = "";
      String pc = "A";
      String srFlg = "N";
      String lcFlg = "N";
      String cc = "A";
      String trnDt = "";
      String expDt = "";
      String srlNum = "";
      String lotNum = "";
      String foundSrlLot = "";
      String newSrlLot = ""; //Query to get all the NIIN(s) that are accounted via serial_lot_num_track
      String sql = "select nl.niin_loc_id, to_char(nl.expiration_date,'YYYY-MM-DD HH24:MI:SS'), " + " to_char(sysdate,'YYYY-MM-DD HH24:MI:SS'), nl.cc, nvl(nl.pc,'A'), nvl(nf.fsc,''), nf.niin, nf.ui," + " nvl(nf.serial_control_flag,'N'), nvl(nf.lot_control_flag,'N') from  niin_location nl, niin_info nf, inventory_item itm where " + " nf.niin_id = ? and nl.niin_id = nf.niin_id and nl.niin_loc_id = itm.niin_loc_id ";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setObject(1, niinIdStr);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            nlId = rs.getString(1);
            expDt = rs.getString(2);
            trnDt = rs.getString(3);
            cc = rs.getString(4);
            pc = rs.getString(5);
            fsc = rs.getString(6);
            niin = rs.getString(7);
            ui = rs.getString(8);
            srFlg = rs.getString(9);
            lcFlg = rs.getString(10);
          }
        }
      }
      //Query to get all the NIIN(s) found
      sql = "select nvl(isl.LOT_CON_NUM,'NONE'), nvl(isl.SERIAL_NUMBER,'NONE'), isl.LOCATION_ID, nvl(isl.CC,'A'),  sum(isl.QTY) " + " from  inv_serial_lot_num isl, inventory_item iii " + " where isl.INVENTORY_ITEM_ID = iii.INVENTORY_ITEM_ID and " + "      isl.INV_DONE_FLAG = 'N' and " + "      iii.inventory_id in  " + "      (select i.inventory_id from inventory i, inventory_item ix " + "        where i.inventory_id = ix.inventory_id and " + "              ix.inventory_item_id = ?) " + "        group by nvl(isl.LOT_CON_NUM,'NONE'), nvl(isl.SERIAL_NUMBER,'NONE'), isl.LOCATION_ID, nvl(isl.CC,'A')";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setObject(1, invItemId);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            if (foundSrlLot.length() > 0) foundSrlLot = foundSrlLot + ",";
            foundSrlLot = foundSrlLot + rs.getString(1) + "#" + rs.getString(2) + "#" + rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5);
          }
        }
      }

      String existingSrlLot = "";
      //Query to get all the NIIN(s) that are accounted via serial_lot_num_track
      sql = "select nvl(sr.LOT_CON_NUM,'NONE'),nvl(sr.SERIAL_NUMBER,'NONE'), sr.LOCATION_ID, nvl(sr.CC,'A'), sum(nvl(sr.QTY,1)) " + " from  serial_lot_num_track sr where sr.niin_id = ? and nvl(sr.issued_flag,'N') = 'N'  " + "      and nvl(sr.LOT_CON_NUM,'NONE')||'#'||nvl(sr.SERIAL_NUMBER,'NONE')||'#'||sr.LOCATION_ID||'#'||nvl(sr.CC,'A') in " + "      ( select nvl(isr.LOT_CON_NUM,'NONE')||'#'||nvl(isr.SERIAL_NUMBER,'NONE')||'#'||isr.LOCATION_ID||'#'||nvl(isr.CC,'A') from inv_serial_lot_num isr " + "        where isr.niin_id = ? and nvl(isr.inv_done_flag,'N') = 'N') " + "group by nvl(sr.LOT_CON_NUM,'NONE'),nvl(sr.SERIAL_NUMBER,'NONE'),sr.LOCATION_ID, nvl(sr.CC,'A')";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setObject(1, niinIdStr);
        pstmt.setObject(2, niinIdStr);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            if (existingSrlLot.length() > 0) existingSrlLot = existingSrlLot + ",";
            existingSrlLot = existingSrlLot + rs.getString(1) + "#" + rs.getString(2) + "#" + rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5);
          }
        }
      }

      String lostSrlLot = "";
      //Query to get all the NIIN(s) that are not accounted via serial_lot_num_track
      sql = "select nvl(sr.LOT_CON_NUM,'NONE'),nvl(sr.SERIAL_NUMBER,'NONE'), sr.LOCATION_ID, nvl(sr.CC,'A'), sum(nvl(sr.QTY,1)) " + " from  serial_lot_num_track sr where sr.niin_id = ? and nvl(sr.issued_flag,'N') = 'N' " + "      and nvl(sr.LOT_CON_NUM,'NONE')||'#'||nvl(sr.SERIAL_NUMBER,'NONE')||'#'||sr.LOCATION_ID||'#'||nvl(sr.CC,'A') not in " + "      ( select nvl(isr.LOT_CON_NUM,'NONE')||'#'||nvl(isr.SERIAL_NUMBER,'NONE')||'#'||isr.LOCATION_ID||'#'||nvl(isr.CC,'A') from inv_serial_lot_num isr " + "        where isr.niin_id = ? and nvl(isr.inv_done_flag,'N') = 'N')" + "group by nvl(sr.LOT_CON_NUM,'NONE'),nvl(sr.SERIAL_NUMBER,'NONE'),sr.LOCATION_ID, nvl(sr.CC,'A')";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
        pstmt.setObject(1, niinIdStr);
        pstmt.setObject(2, niinIdStr);
        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            if (lostSrlLot.length() > 0) lostSrlLot = lostSrlLot + ",";
            lostSrlLot = lostSrlLot + rs.getString(1) + "#" + rs.getString(2) + "#" + rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5);
          }
        }
      }

      //Process this to either get new serial/lot or gained qty or loss qty
      int sCou = 0;
      int cCou = 0;
      int mCou = 0;
      int curQty = 0;
      int mQty = 0;
      int chgQty = 0;
      String existingQtyChanged = "";
      if (foundSrlLot.length() > 0)
        mCou = foundSrlLot.split(",").length;
      if (existingSrlLot.length() > 0)
        cCou = existingSrlLot.split(",").length;
      while ((sCou < mCou) && (cCou >= 0)) {
        mQty = Integer.parseInt(foundSrlLot.split(",")[sCou].split("#")[4]);
        if ((existingSrlLot.length() > 0) && (existingSrlLot.contains(foundSrlLot.split(",")[sCou].split("#")[0] + "#" + foundSrlLot.split(",")[sCou].split("#")[1] + "#" + foundSrlLot.split(",")[sCou].split("#")[2] + "#" + foundSrlLot.split(",")[sCou].split("#")[3]))) {
          curQty = Integer.parseInt((existingSrlLot.split(foundSrlLot.split(",")[sCou].split("#")[0] + "#" + foundSrlLot.split(",")[sCou].split("#")[1] + "#" + foundSrlLot.split(",")[sCou].split("#")[2] + "#" + foundSrlLot.split(",")[sCou].split("#")[3] + "#")[1].split(",")[0])); //existing qty
          chgQty = mQty - curQty;
        }
        else {
          // New Srl and or Lot
          if (newSrlLot.length() > 0)
            newSrlLot = newSrlLot + ",";
          newSrlLot = newSrlLot + foundSrlLot.split(",")[sCou].split("#")[0] + "#" + foundSrlLot.split(",")[sCou].split("#")[1] + "#" + foundSrlLot.split(",")[sCou].split("#")[2] + "#" + foundSrlLot.split(",")[sCou].split("#")[3] + "#" + foundSrlLot.split(",")[sCou].split("#")[4];
        }
        if (chgQty != 0) { //Existing with Qty changed
          if (existingQtyChanged.length() > 0)
            existingQtyChanged = existingQtyChanged + ",";
          existingQtyChanged = existingQtyChanged + foundSrlLot.split(",")[sCou].split("#")[0] + "#" + foundSrlLot.split(",")[sCou].split("#")[1] + "#" + foundSrlLot.split(",")[sCou].split("#")[2] + "#" + foundSrlLot.split(",")[sCou].split("#")[3] + "#" + String.valueOf(chgQty);
        }
        sCou++;
      }
      //Existing serial or lot num gained or lost in count
      sCou = 0;
      cCou = 0;
      mCou = 0;
      curQty = 0;
      mQty = 0;
      chgQty = 0;
      if (existingQtyChanged.length() > 0)
        cCou = existingQtyChanged.split(",").length;
      while (sCou < cCou) {
        //Query to get all the NIIN(s) that are accounted via serial_lot_num_track
        sql = "update serial_lot_num_track set qty = (nvl(qty,0) + ?) where " + " nvl(lot_con_num,'NONE')|| '#' || nvl(serial_number,'NONE')|| '#' || location_id = ? and niin_id = ? and cc = ?";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setInt(1, Integer.parseInt(existingQtyChanged.split(",")[sCou].split("#")[4]));
          pstmt.setString(2, existingQtyChanged.split(",")[sCou].split("#")[0] + "#" + existingQtyChanged.split(",")[sCou].split("#")[1] + "#" + existingQtyChanged.split(",")[sCou].split("#")[2]);
          pstmt.setString(3, niinIdStr);
          pstmt.setString(4, existingQtyChanged.split(",")[sCou].split("#")[3]);
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
        }

        sCou++;
      }
      //Lost serial and lot numbers
      sCou = 0;
      cCou = 0;
      mCou = 0;
      curQty = 0;
      mQty = 0;
      chgQty = 0;
      if (lostSrlLot.length() > 0)
        cCou = lostSrlLot.split(",").length;
      while (sCou < cCou) {
        //Query to get all the NIIN(s) that are accounted via serial_lot_num_track
        sql = "delete serial_lot_num_track  where " + " nvl(lot_con_num,'NONE')|| '#' || nvl(serial_number,'NONE')|| '#' || location_id = ? and niin_id = ? and cc = ? ";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setString(1, lostSrlLot.split(",")[sCou].split("#")[0] + "#" + lostSrlLot.split(",")[sCou].split("#")[1] + "#" + lostSrlLot.split(",")[sCou].split("#")[2]);
          pstmt.setString(2, niinIdStr);
          pstmt.setString(3, lostSrlLot.split(",")[sCou].split("#")[3]);
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
        }

        sCou++;
      }
      //For newly found serial numbers and lot con numbers
      sCou = 0;
      cCou = 0;
      mCou = 0;
      curQty = 0;
      mQty = 0;
      chgQty = 0;
      if (newSrlLot.length() > 0)
        cCou = newSrlLot.split(",").length;
      while (sCou < cCou) {
        //Query to get all the NIIN(s) that are accounted via serial_lot_num_track
        sql = "insert into serial_lot_num_track  (lot_con_num, serial_number, qty,expiration_date, niin_id,cc,  issued_flag, timestamp,location_id) values " + " (?,?,?,to_date(?,'YYYY-MM-DD HH24:MI:SS'),?,?,'N',sysdate,?)";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setString(1, (newSrlLot.split(",")[sCou].split("#")[0].equalsIgnoreCase("NONE") ? "" : newSrlLot.split(",")[sCou].split("#")[0]));
          pstmt.setString(2, (newSrlLot.split(",")[sCou].split("#")[1].equalsIgnoreCase("NONE") ? "" : newSrlLot.split(",")[sCou].split("#")[1]));
          pstmt.setString(3, newSrlLot.split(",")[sCou].split("#")[4]);
          pstmt.setString(4, expDt);
          pstmt.setString(5, niinIdStr);
          pstmt.setString(6, newSrlLot.split(",")[sCou].split("#")[3]);
          pstmt.setString(7, newSrlLot.split(",")[sCou].split("#")[2]);
          pstmt.executeUpdate();
          this.getDBTransaction().commit();
        }

        sCou++;
      }
      return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  /**
   * AUTHOR   : John P. Owens - Stanley Associates
   * DATE     : Sept 28, 2007
   * DESC     : This function creates an ASX Transaction (Shipment Confirmation)
   * An AS1 identifies that an issue is ready for delivery.
   * To be created once an item is put into a Floor Location
   * (Once Packing ends and Shipping Begins)
   * --DO NOT USE AS1 FOR WALK-THRU ISSUES--
   * An AS2 transaction (Delivery Confirmation) informs when an item
   * has been delivered
   */
  public int sendAsxTrans(String scnVal, int transNum) {
    try {
      String sTrans = "AS";
      if (transNum >= 1 && transNum <= 2)
        sTrans += transNum;
      else
        return -6;

      String sDocId = sTrans;
      SiteInfoViewImpl siteinfo = getSiteInfoView1();
      if (siteinfo.getRowCount() > 0) {
        // get the routing id from the table
        Object ricobj = siteinfo.getRowAtRangeIndex(0).getAttribute("RoutingId");
        //ADD RIC
        if (ricobj != null)
          sTrans += ricobj;
        else
          sTrans += "   ";
      }
      else
        sTrans += "   ";

      //Media Status Code (Leave Blank) [pos 7]
      sTrans += " ";

      //* Determine if there is a shipping manifest
      PreparedStatement pstmtMANIFEST = getDBTransaction().createPreparedStatement("select lead_tcn from shipping s, shipping_manifest m where s.shipping_manifest_id = m.shipping_manifest_id and scn=?", 0);
      pstmtMANIFEST.setString(1, scnVal);
      ResultSet rsMANIFEST = pstmtMANIFEST.executeQuery();
      String sTransManifest = "";
      String manifest = "";
      if (rsMANIFEST.next()) {
        manifest = rsMANIFEST.getString(1);
      }
      rsMANIFEST.close();
      pstmtMANIFEST.close();
      if (!StringUtil.isEmpty(manifest)) {
        sTransManifest += manifest.concat("                 ").substring(0, 17);
      }
      else {
        //do not allow empty manifest return
        return -5;
      }

      String niinId = "";
      String documentNumber = "";
      String issueType = "";
      String suffix = "";
      String suppAddress = "";
      String fundCode = "";
      String issuePriorityDesignator = "";
      PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin_id, document_number, issue_type, suffix, supplementary_address, fund_code, issue_priority_designator from issue where scn = ?", 0);
      stR.setString(1, scnVal);
      ResultSet rs = stR.executeQuery();
      if (rs.next()) {
        niinId = rs.getString(1);
        documentNumber = rs.getString(2);
        issueType = rs.getString(3);
        suffix = rs.getString(4);
        suppAddress = rs.getString(5);
        fundCode = rs.getString(6);
        issuePriorityDesignator = rs.getString(7);
      }
      rs.close();
      stR.close();

      if (!StringUtil.isEmpty(niinId)) {
        //There is niin information to lookup
        boolean _niinFound = false;
        PreparedStatement stNiin = getDBTransaction().createPreparedStatement("select nvl(fsc,''), niin, ui from niin_info where niin_id = ?", 0);
        stNiin.setString(1, niinId);
        ResultSet rsNiin = stNiin.executeQuery();
        if (rsNiin.next()) {
          //Add FSC
          if (rsNiin.getObject(1) != null) {
            sTrans += rsNiin.getString(1);
            _niinFound = true;
          }
          else {
            _niinFound = false;
          }

          //Add NIIN
          if (_niinFound) {
            if (rsNiin.getObject(2) != null) {
              sTrans += rsNiin.getString(2);
              _niinFound = true;
            }
            else {
              _niinFound = false;
            }
          }

          if (_niinFound) {
            //Add Special Material Identification Code - Leave Blank
            sTrans += "  ";
            //Add Unit of Issue
            String ui_ = rsNiin.getString(3);
            if (!StringUtil.isEmpty(ui_)) {
              sTrans += PadUtil.padIt(ui_, 2);
            }
            else {
              sTrans += "  ";
            }
            _niinFound = true;
          }
        }
        rsNiin.close();
        stNiin.close();

        if (!_niinFound)
          return -51;
      }
      else {
        if (!StringUtil.isEmpty(issueType)) {
          if (issueType.equals("T")) {
            boolean _niinFound = false;
            PreparedStatement stNiin = getDBTransaction().createPreparedStatement("select nvl(n.fsc,''), n.niin, n.ui from niin_info n, receipt r, receipt_issue i where n.niin_id = r.niin_id and i.rcn = r.rcn and i.scn = ?", 0);
            stNiin.setObject(1, scnVal);
            ResultSet rsNiin = stNiin.executeQuery();
            if (rsNiin.next()) {
              if (rsNiin.getObject(1) != null) {
                //Add FSC
                if (rsNiin.getObject(1) != null) {
                  sTrans += rsNiin.getString(1);
                  _niinFound = true;
                }
                else {
                  _niinFound = false;
                }

                //Add NIIN
                if (_niinFound) {
                  if (rsNiin.getObject(2) != null) {
                    sTrans += rsNiin.getString(2);
                    _niinFound = true;
                  }
                  else {
                    _niinFound = false;
                  }
                }

                if (_niinFound) {
                  //Add Special Material Identification Code - Leave Blank
                  sTrans += "  ";
                  //Add Unit of Issue
                  String ui_ = rsNiin.getString(3);
                  if (!StringUtil.isEmpty(ui_)) {
                    sTrans += PadUtil.padIt(ui_, 2);
                  }
                  else {
                    sTrans += "  ";
                  }
                  _niinFound = true;
                }
              }
              else {
                sTrans += "                ";
              }
            }
            else {
              sTrans += "                ";
            }
            rsNiin.close();
            stNiin.close();
          }
          else
            return -60;
        }
        else
          return -61;
      }

      //Add Quantity - Pad with leading 0s to 5 spaces
      //Quantity Packed
      try (PreparedStatement stR2 = getDBTransaction().createPreparedStatement("select sum(QTY_PICKED) from picking where scn = ? and status = ?", 0)) {
        stR2.setString(1, scnVal);
        if (transNum == 1) stR2.setString(2, "PACKED");
        else stR2.setString(2, "SHIPPED");
        try (ResultSet rs2 = stR2.executeQuery()) {
          if (rs2.next()) {
            int iQty = 0;
            if (rs2.getObject(1) != null) iQty = rs2.getInt(1);
            sTrans += PadUtil.padItZeros(String.valueOf(iQty), 5);
          }
        }
      }

      //Add Document Number
      if (!StringUtil.isEmpty(documentNumber)) {
        sTrans += PadUtil.padIt(documentNumber, 14);
      }
      else {
        return -4;
      }

      //Add Suffix (Unless Item is Cross Docked
      if (!StringUtil.isEmpty(issueType) && issueType.equals("C")) {
        sTrans += " ";
      }
      else {
        if (!StringUtil.isEmpty(suffix))
          sTrans += suffix;
        else
          sTrans += " ";
      }

      //Add Supplementary Address
      if (!StringUtil.isEmpty(suppAddress)) {
        //Check Length, fill with spaces
        sTrans += PadUtil.padIt(suppAddress, 6);
      }
      else
        sTrans += "      ";

      val currentJulianDate = this.getCurrentJulian(3);

      //Hold Code 1 - Leave Blank
      sTrans += "A";
      //Fund Code 2
      if (!StringUtil.isEmpty(fundCode))
        sTrans += fundCode;
      else
        sTrans += "  ";
      //Distribution Code 1 - Leave Blank
      sTrans += " ";
      //Weapon System Code 2 - Leave Blank
      sTrans += "  ";
      //Date Shipped 3
      sTrans += currentJulianDate;
      //Priority Code 2 - Issue_Priority_Designator

      //TCN 15 - Fill with the 8 character LD Con #

      //* Shipping Manifest
      sTrans += sTransManifest;

      //Mode of Shipment 1
      sTrans += "9";
      //Date avail for Ship 3
      sTrans += currentJulianDate;

      //Insert Record into Spool
      if (!this.insertSpoolRecord("C", sDocId, sTrans, niinId)) {
        this.getDBTransaction().rollback();
        return -555;
      }
      else {
        this.getDBTransaction().commit();
      }

      siteinfo.clearCache();
      return 1;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  /**
   * AUTHOR   : John P. Owens - Stanley Associates
   * DATE     : Oct 30, 2007
   * DESC     : This function processes a receipt to see if a D6_ transaction needs
   * to be generated
   * PARMS    : rcn - receipt control number
   * SID - Stow Identification Label
   */
  public boolean processReceiptTransaction(int rcn, String sid, String rcnScn, int userID) {

    try {
      int iQtyStowed = 0, iQtyDocked = 0, iQtyReceived = 0, iNotStowed = 0, iCount = 0;

      String sDocNum = "";
      boolean isCrossdock = (rcnScn != null);

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select receipt.document_number, receipt.quantity_inducted, receipt.quantity_released, receipt.quantity_stowed from receipt where receipt.rcn = ?", 0)) {
        stR.setInt(1, rcn);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            sDocNum = rs.getString(1);
            iQtyReceived = rs.getInt(2);
            iQtyDocked = rs.getInt(3);
            iQtyStowed = rs.getInt(4);
          }
        }
      }

      try (PreparedStatement stR2 = getDBTransaction().createPreparedStatement("select count(*) from stow where rcn = ? and status <> 'STOWED' and status <> 'STOW LOSS1' and status <> 'STOWLOSS47'", 0)) {
        stR2.setInt(1, rcn);
        try (ResultSet rs2 = stR2.executeQuery()) {
          if (rs2.next()) {
            if (rs2.getObject(1) != null) iNotStowed = rs2.getInt(1);
          }
          else {
            iNotStowed = 0;
          }
        }
      }

      boolean doneflag = false;
      //Do not create any transactions unless this receipt is finished.
      if ((iQtyStowed == 0) && (iQtyDocked == iQtyReceived))
        doneflag = true;
        //If items are there to stow do not go for creating transactions
      else if (iNotStowed > 0) {
        iQtyStowed = 0;
        return doneflag;
      } //looks like it crossed this if because not pending STOWS

      //We have not even created stows.
      if ((!doneflag) && (iQtyStowed == 0))
        return doneflag;

      try (PreparedStatement ps = getDBTransaction().createPreparedStatement("select count(*) from ref_dasf where document_number = ?", 0)) {
        ps.setString(1, sDocNum);
        try (ResultSet rs3 = ps.executeQuery()) {
          if (rs3.next()) {
            iCount = rs3.getInt(1);
          }
        }
      }

      //Create Standard Receipt Transactions
      if (iQtyStowed > 0) {
        try (PreparedStatement stR3 = getDBTransaction().createPreparedStatement("select stow_qty from stow where rcn = ? and status = 'STOW LOSS1'", 0)) {
          stR3.setInt(1, rcn);
          try (ResultSet rs4 = stR3.executeQuery()) {
            iQtyStowed = 0;
            while (rs4.next()) {
              iQtyStowed += rs4.getInt(1);
            }
          }
        }

        try (PreparedStatement stR3 = getDBTransaction().createPreparedStatement("select qty_to_be_stowed from stow where rcn = ? and (status = 'STOWED' or status = 'STOWLOSS47')", 0)) {
          stR3.setInt(1, rcn);
          try (ResultSet rs4 = stR3.executeQuery()) {
            while (rs4.next()) {
              iQtyStowed += rs4.getInt(1);
            }
          }
        }

        //Update any pending stow records status to stowed.
        try (PreparedStatement stUd = getDBTransaction().createPreparedStatement("update stow set status = 'STOWED' where rcn = ? and status = 'STOW LOSS1'", 0)) {
          stUd.setInt(1, rcn);
          stUd.execute();
          this.getDBTransaction().commit();
        }

        try (PreparedStatement uQ = getDBTransaction().createPreparedStatement("update error_queue set modified_by = ?, modified_date = sysdate where (key_num=? and eid=80) or (key_num=? and eid=47)", 0)) {
          uQ.setInt(1, userID);
          uQ.setString(2, String.valueOf(rcn));
          uQ.setString(3, sid);
          uQ.executeUpdate();
          getDBTransaction().commit();
        }

        try (PreparedStatement uQ = getDBTransaction().createPreparedStatement("delete from error_queue where (key_num=? and eid=80) or (key_num=? and eid=47)", 0)) {
          uQ.setString(1, String.valueOf(rcn));
          uQ.setString(2, sid);
          uQ.executeUpdate();
          getDBTransaction().commit();
        }
      }

      return true;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return false;
  }

  public void prepareSession(SessionData sessionData) {
    super.prepareSession(sessionData);
  }

  /**
   * Container's getter for SiteInfoView1
   */
  public SiteInfoViewImpl getSiteInfoView1() {
    return (SiteInfoViewImpl) findViewObject("SiteInfoView1");
  }

  /**
   * This routine is called to delete Z0 transaction from SPOOL for a given SCN
   */
  public void deleteZ0FromSpool(String scnVal) {
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select s.spool_id " + " from spool s " + " where (transaction_type in ('Z0A', 'Z01')) " + " and concat(substr(s.rec_data, 30,14), substr(s.rec_data, 12,9)) in  " + "  (select concat(i.document_number, n.niin)  " + "   from issue i, niin_info n " + "   where i.scn = ? " + "   and i.niin_id = n.niin_id) ", 0)) {
      int spoolId = 0;
      //Check if Z0A or Z01 is already created for this issue
      stR.setString(1, scnVal);
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          spoolId = rs.getInt(1);
        }
      }

      if (spoolId > 0) {
        try (PreparedStatement delete = getDBTransaction().createPreparedStatement("delete from spool where spool_id = ?", 0)) {
          delete.setInt(1, spoolId);
          delete.executeUpdate();
          this.getDBTransaction().commit();
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public String getSiteGCCSSMCFlag() {
    String flag = "N";
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nvl(GCSS_MC,'N') from site_info", 0)) {
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          flag = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  /**
   * Container's getter for GCSSMCTransactions1
   */
  public GCSSMCTransactionsImpl getGCSSMCTransactions1() {
    return (GCSSMCTransactionsImpl) findApplicationModule("GCSSMCTransactions1");
  }

  private boolean isGCSSMC() {
    boolean gcssmc = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select count(*) from site_info where nvl(gcss_mc, 'N') = 'Y'", 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0) gcssmc = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      gcssmc = false;
    }
    return gcssmc;
  }
}

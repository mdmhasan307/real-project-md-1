package mil.stratis.model.services;

import exmlservice.I009.ObjectFactory;
import exmlservice.I009.ShipmentReceiptsInCollection;
import exmlservice.I009.ShipmentReceiptsInCollection.MRec.DetRec;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.GCSSMCUtil;
import mil.stratis.common.util.PadUtil;
import mil.stratis.common.util.StringUtil;
import mil.stratis.common.util.Util;
import mil.stratis.model.view.gcssmc.DocIdListVOImpl;
import mil.stratis.model.view.gcssmc.SiteInfoGCSSMCFlagImpl;
import mil.stratis.model.view.gcssmc.SiteInfoMHIFRangeVOImpl;
import mil.usmc.mls2.stratis.core.service.gcss.I009SpoolService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import oracle.jbo.domain.Date;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.sql.CLOB;
import org.apache.commons.collections4.CollectionUtils;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class GCSSMCTransactionsImpl extends ApplicationModuleImpl {

  public String getSiteAAC() {
    String siteaac = "";
    try (PreparedStatement sitePs = getDBTransaction().createPreparedStatement("select aac from site_info", 0)) {
      try (ResultSet siteRs = sitePs.executeQuery()) {
        if (siteRs.next()) {
          siteaac = siteRs.getString(1);
          if (siteaac == null)
            siteaac = "";
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      siteaac = "";
    }
    return siteaac;
  }

  public String getSiteRIC() {
    String siteric = "";
    try (PreparedStatement sitePs = getDBTransaction().createPreparedStatement("select routing_id from site_info", 0)) {
      try (ResultSet siteRs = sitePs.executeQuery()) {
        if (siteRs.next()) {
          siteric = siteRs.getString(1);
          if (siteric == null)
            siteric = "";
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      siteric = "";
    }
    return siteric.toUpperCase();
  }

  public String getSiteConus() {
    String siteconus = "";
    try (PreparedStatement sitePs = getDBTransaction().createPreparedStatement("select nvl(conus_ocunus_flag,'N') from site_info", 0)) {
      try (ResultSet siteRs = sitePs.executeQuery()) {
        if (siteRs.next()) {
          siteconus = siteRs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      siteconus = "";
    }
    return siteconus;
  }

  public boolean updateSiteInterfacesStatus(String interfaceName,
                                            String status) {
    boolean success;
    String sql = "update site_interfaces set last_impexp_date = sysdate, status=? WHERE interface_name = ?";

    try (PreparedStatement psFTPdate = getDBTransaction().createPreparedStatement(sql, 0)) {
      psFTPdate.setString(1, interfaceName);
      psFTPdate.setString(2, status);
      psFTPdate.execute();
      getDBTransaction().commit();
      success = true;
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
      success = false;
    }
    return success;
  }

  private XMLGregorianCalendar todayDate() {
    Date d = new Date(new Timestamp(System.currentTimeMillis()));
    String[] arrD = d.toString().split("-"); //2011-03-04 00:00:00 -> 3 array (yyyy mm and dd with time)
    String[] arrE = arrD[2].split(" "); //04 00:00:00 -> day 04
    String[] arrF = arrE[1].split(":"); //00:00:00 -> 3 array hh mm ss
    GregorianCalendar c = new GregorianCalendar();
    c.set(Integer.parseInt(arrD[0]), Integer.parseInt(arrD[1]) - 1, Integer.parseInt(arrE[0]), Integer.parseInt(arrF[0]), Integer.parseInt(arrF[1]), 0);

    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  private XMLGregorianCalendar gregDate(String yyyyMMdd) {

    String[] arrD = yyyyMMdd.split("-");
    if (arrD[2].length() < 4) {
      arrD[2] += " 00:00:00";
    }

    String[] arrE = arrD[2].split(" ");

    String[] arrF = arrE[1].split(":");
    GregorianCalendar c = new GregorianCalendar();
    c.set(Integer.parseInt(arrD[0]), Integer.parseInt(arrD[1]) - 1, Integer.parseInt(arrE[0]), Integer.parseInt(arrF[0]), Integer.parseInt(arrF[1]), 0);

    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return todayDate();
  }

  private void listControlled(List<DetRec> details, ObjectFactory factory, String pidOrScn, boolean pid) {
    log.debug("listControlled for {}", pidOrScn);

    String sql = "select " +
        "nvl(s.serial_number,nvl(ps.serial_number,'')), " +
        "nvl(s.lot_con_num,nvl(ps.lot_con_num,'')), " +
        "case when (to_char(nvl(ps.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')) < (to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')) " +
        "then (to_char(nvl(ps.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')) " +
        "else (to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')) end, " +
        "case when nvl(ps.qty,'0')<1 then  nvl(s.qty,'0') else nvl(ps.qty,'0') end, nvl(s.cc,'A') " +
        "from serial_lot_num_track s, picking p, pick_serial_lot_num ps " +
        "where ";
    sql += (pid) ? " ps.pid=? " : " ps.scn=? ";
    sql += " and p.pid(+) = ps.pid " +
        "and ps.serial_lot_num_track_id = s.serial_lot_num_track_id(+) ";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

      if (pid) {
        pstmt.setString(1, pidOrScn);
      }
      else {
        try {
          pstmt.setString(1, pidOrScn.substring(0, 9));
        }
        catch (Exception e) {
          pstmt.setString(1, pidOrScn);
        }
      }

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
          detail.setSerN(rs.getString(1));
          detail.setLotN(rs.getString(2));
          if (Util.isNotEmpty(detail.getLotN()))
            detail.setLotED(gregDate(rs.getString(3)));
          detail.setCC(rs.getString(5));  // can only walk thru on cc A
          detail.setQLot(new BigInteger(rs.getString(4)));
          details.add(detail);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void listControlledRcn(List<DetRec> details, ObjectFactory factory, String rcn, int qty, boolean d6t) {
    log.debug("listControlledRcn for {} :: header Qty {}", rcn, qty);
    int i = 0;
    int sumQty = 0;
    try {

      if (d6t) {
        log.debug("list controlled for crossdock first");
        String sqlCXD = "select nvl(ps.serial_number,''), nvl(ps.lot_con_num,''), (to_char(nvl(ps.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), ps.qty, nvl(s.cc,'A') from pick_serial_lot_num ps, serial_lot_num_track s, issue i where ps.serial_lot_num_track_id=s.serial_lot_num_track_id(+) and ps.scn=i.scn and i.rcn = ? order by ps.pick_serial_lot_num ";
        sqlCXD += " asc";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCXD, 0)) {
          // locate any cross first

          pstmt.setString(1, rcn);

          try (ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
              DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
              detail.setSerN(rs.getString(1));
              detail.setLotN(rs.getString(2));
              if (Util.isNotEmpty(detail.getLotN()))
                detail.setLotED(gregDate(rs.getString(3)));
              detail.setCC(rs.getString(5));

              int qLot = rs.getInt(4);
              detail.setQLot((qty > qLot) ? new BigInteger(String.valueOf(qLot)) : new BigInteger(rs.getString(4)));

              details.add(detail);
              sumQty += rs.getInt(4);
              i++;
            }
          }
        }
      }

      String sql = "select nvl(s.serial_number,''), nvl(s.lot_con_num,''), (to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), s.qty_to_be_stowed, r.cc from receipt r, stow s, niin_info n where r.rcn=s.rcn and r.niin_id=n.niin_id and r.rcn= ? and (n.serial_control_flag='Y' or n.lot_control_flag='Y') order by s.stow_id ";
      if (d6t) sql += " asc";
      else sql += " desc";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

        pstmt.setString(1, rcn);

        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next() && sumQty <= qty) {
            DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
            detail.setSerN(rs.getString(1));
            detail.setLotN(rs.getString(2));
            if (Util.isNotEmpty(detail.getLotN()))
              detail.setLotED(gregDate(rs.getString(3)));
            detail.setCC(rs.getString(5));

            int qLot = rs.getInt(4);
            detail.setQLot((qty > qLot) ? new BigInteger(String.valueOf(qLot)) : new BigInteger(rs.getString(4)));

            details.add(detail);
            sumQty += rs.getInt(4);
            i++;
          }
        }
      }

      if (!d6t) {
        // locate any cross last
        log.debug("list controlled for crossdock last");
        String sqlCXD = "select nvl(ps.serial_number,''), nvl(ps.lot_con_num,''), (to_char(nvl(ps.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), ps.qty, nvl(s.cc,'A') from pick_serial_lot_num ps, serial_lot_num_track s, issue i where ps.serial_lot_num_track_id=s.serial_lot_num_track_id(+) and ps.scn=i.scn and i.rcn = ? order by ps.pick_serial_lot_num ";
        sqlCXD += " desc";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCXD, 0)) {

          pstmt.setString(1, rcn);

          try (ResultSet rs = pstmt.executeQuery()) {
            sumQty = 0;
            while (rs.next()) {
              DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
              detail.setSerN(rs.getString(1));
              detail.setLotN(rs.getString(2));
              if (Util.isNotEmpty(detail.getLotN()))
                detail.setLotED(gregDate(rs.getString(3)));
              detail.setCC(rs.getString(5));
              int qLot = rs.getInt(4);
              detail.setQLot((qty > qLot) ? new BigInteger(String.valueOf(qLot)) : new BigInteger(rs.getString(4)));

              details.add(detail);
              sumQty += rs.getInt(4);
              i++;
            }
          }
        }
      }

      log.debug("listControlledRcn found {} controlled items", i);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void listControlledDwf(List<DetRec> details, ObjectFactory factory, String scn) {
    log.debug("listControlledDwf for {}", scn);
    try {

      // locate any cross first
      String sqlCXD = "select nvl(s.serial_number,''), nvl(s.lot_con_num,''), (to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), s.qty, nvl(s.cc,'A') from pick_serial_lot_num ps, serial_lot_num_track s, issue i where ps.serial_lot_num_track_id=s.serial_lot_num_track_id(+) and ps.scn=i.scn and i.scn = ? and (s.serial_number is not null or s.lot_con_num is not null) union " +
          "select nvl(ps.serial_number,''), nvl(ps.lot_con_num,''), (to_char(nvl(ps.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), ps.qty, nvl(s.cc,'A') from pick_serial_lot_num ps, serial_lot_num_track s, issue i where ps.serial_lot_num_track_id=s.serial_lot_num_track_id(+) and ps.scn=i.scn and i.scn = ?  and (ps.serial_number is not null or ps.lot_con_num is not null) ";

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCXD, 0)) {

        pstmt.setString(1, scn);
        pstmt.setString(2, scn);

        try (ResultSet rs = pstmt.executeQuery()) {
          while (rs.next()) {
            DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
            detail.setSerN(rs.getString(1));
            detail.setLotN(rs.getString(2));
            if (Util.isNotEmpty(detail.getLotN()))
              detail.setLotED(gregDate(rs.getString(3)));
            detail.setCC(rs.getString(5));
            detail.setQLot(new BigInteger(rs.getString(4)));
            details.add(detail);
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  //MCF 11/16/2015: Walk-thru change, no Z0 Transactions anymore
  public int sendZ0GCSSMCTransaction(String scnVal, String pinVal, String pidVal, int iPartialQty) {
    log.debug("Started to print XXXXX {} :: {} :: {} :: iPartialQty= {}", scnVal, pinVal, pidVal, iPartialQty);
    try {
      boolean errors = false;
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
      String cc = "";
      String sTrans = "Z0";
      String sConus = this.getSiteConus();

      sTrans += (sConus.equals("Y")) ? "A" : "1";
      record.setDIC(sTrans);
      record.setRIC(getSiteRIC());
      record.setIPAAC(getSiteAAC());
      Integer niinId = null;

      String sql = "select n.niin, n.ui, i.document_number, i.rdd, lpad(i.issue_priority_designator, 2,'0'), i.cc, i.qty_issued, nvl(i.signal_code,''), nvl(i.ero_number,''), nvl(i.supplementary_address,''), nvl(i.project_code,''), nvl(i.cost_jon,''), i.scn, n.niin_id from niin_info n, issue i ";

      if (iPartialQty == 1)
        sql += " where i.scn = ? and i.niin_id = n.niin_id";
      else
        sql += ", picking p where i.niin_id=n.niin_id and i.scn=p.scn and p.pid=? ";

      try {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

          if (iPartialQty == 1) pstmt.setString(1, scnVal);
          else pstmt.setString(1, pidVal);

          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
              record.setNIIN(rs.getString(1));
              record.setUOI(rs.getString(2));
              record.setSDN(rs.getString(3));
              record.setRDD(rs.getString(4));
              record.setPri(rs.getString(5));
              cc = rs.getString(6);
              if (cc.equals("A")) record.setQCCA(new BigInteger(rs.getString(7)));
              else record.setQCCF(new BigInteger(rs.getString(7)));

              record.setSC(rs.getString(8));
              record.setSRN(rs.getString(9)); //* ero
              record.setSupADD(rs.getString(10));
              record.setProj(rs.getString(11));
              record.setJON(rs.getString(12));
              record.setTxnDate(todayDate());
              record.setKeyD(todayDate());
              record.setSCN(rs.getString(13));
              niinId = rs.getInt(14);
            }
          }
        }

        //redmine 14625, 14781
        //iPartialQty set to 1 means hardcard walkthru
        //iPartialQty set to 0 means normal walkthru and will require individual pid for each pick completed
        String sqlPicks = "select sum(qty_picked) from picking where scn = ? ";
        if (iPartialQty == 0) sqlPicks += "and pid=? ";
        sqlPicks += "and (status='PICKED' or status='WALKTHRU')";

        try (PreparedStatement stPicks2 = getDBTransaction().createPreparedStatement(sqlPicks, 0)) {

          stPicks2.setString(1, scnVal);
          if (iPartialQty == 0) {
            stPicks2.setString(2, pidVal);
          }

          try (ResultSet rsPicks2 = stPicks2.executeQuery()) {
            if (rsPicks2.next()) {
              if (cc.equals("A")) record.setQCCA(new BigInteger(rsPicks2.getString(1)));
              else record.setQCCF(new BigInteger(rsPicks2.getString(1)));
            }
          }
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
        errors = true;
      }

      if (!errors) {
        listControlled(record.getDetRec(), factory, (Util.isEmpty(pidVal)) ? scnVal : pidVal, (iPartialQty == 0));
        val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
        val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

        if (!marshallAndSpoolSuccess)
          return -555;

        return 0;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  public int sendSTWGCSSMCTransaction(String sidVal) {
    try {
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
      String cc;
      record.setDIC("STW");
      record.setRIC(getSiteRIC());
      record.setIPAAC(getSiteAAC());
      Integer niinId = null;

      if (spoolHasSid(sidVal)) {
        log.error("Duplicate transaction found... terminating transaction.");
        return -2;
      }

      try (PreparedStatement stmt = getDBTransaction().createPreparedStatement(
          "select s.sid, r.rcn, r.document_number, n.niin, n.ui, r.cc,  " +
              "s.stow_qty, nvl(r.suffix,''), nvl(s.serial_number,''), nvl(s.lot_con_num,''), " +
              "(to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), n.serial_control_flag, n.lot_control_flag, n.niin_id " +
              "                from stow s, receipt r, niin_info n  " +
              "                where r.rcn = s.rcn and " +
              "                       r.niin_id = n.niin_id and " +
              "                       s.sid = ? and s.stow_qty > 0", 0)) {
        stmt.setString(1, sidVal);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            record.setSID(rs.getString(1));
            record.setRCN(rs.getString(2));
            record.setSDN(rs.getString(3));
            record.setNIIN(rs.getString(4));
            record.setUOI(rs.getString(5));
            record.setSfx(rs.getString(8));
            record.setTxnDate(todayDate());
            record.setKeyD(todayDate());

            cc = rs.getString(6);
            if (cc.equals("A")) record.setQCCA(new BigInteger(rs.getString(7)));
            else record.setQCCF(new BigInteger(rs.getString(7)));

            List<DetRec> details = record.getDetRec();

            if ((Util.isNotEmpty(rs.getString(9)) || Util.isNotEmpty(rs.getString(10)))
                && (rs.getString(12).equals("Y") || rs.getString(13).equals("Y"))) {
              DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
              detail.setCC(cc);
              detail.setLotN(rs.getString(10));
              if (Util.isNotEmpty(detail.getLotN()))
                detail.setLotED(gregDate(rs.getString(11)));
              detail.setSerN(rs.getString(9));
              detail.setQLot(new BigInteger(rs.getString(7)));
              details.add(detail);
            }
            niinId = rs.getInt(14);
          }
        }
      }

      val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
      val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

      if (!marshallAndSpoolSuccess)
        return -555;

      return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  public int sendSTLGCSSMCTransaction(String sidVal) {
    try {
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
      String cc;
      int stowloss = 0;
      record.setDIC("STL");
      record.setRIC(getSiteRIC());
      record.setIPAAC(getSiteAAC());
      Integer niinId = null;

      try (PreparedStatement stmt = getDBTransaction().createPreparedStatement("select a.sid, a.rcn, b.document_number, c.niin, c.ui, b.cc, (a.qty_to_be_stowed - nvl(a.stow_qty,0)) stow_loss, c.niin_id  \n" +
          "                                from stow a, receipt b, niin_info c  \n" +
          "                                where a.rcn = b.rcn and  \n" +
          "                                       b.niin_id = c.niin_id and  \n" +
          "                                       sid = ?", 0)) {
        stmt.setString(1, sidVal);
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            record.setSID(rs.getString(1));
            record.setRCN(rs.getString(2));
            record.setSDN(rs.getString(3));
            record.setNIIN(rs.getString(4));
            record.setUOI(rs.getString(5));
            record.setTxnDate(todayDate());
            record.setKeyD(todayDate());

            cc = rs.getString(6);
            if (cc.equals("A")) record.setQCCA(new BigInteger(PadUtil.padItZeros(rs.getString(7), 5)));
            else record.setQCCF(new BigInteger(PadUtil.padItZeros(rs.getString(7), 5)));
            stowloss = Integer.parseInt(rs.getString(7));
            niinId = rs.getInt(8);
            try (PreparedStatement stmt2 = getDBTransaction().createPreparedStatement("select nvl(s.serial_number,''), nvl(s.lot_con_num,''), (to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), (qty_to_be_stowed - nvl(stow_qty, 0)) " +
                "from stow s, receipt r, niin_info n " +
                "where s.rcn=r.rcn and r.niin_id=n.niin_id " +
                "and (n.serial_control_flag = 'Y' or n.lot_control_flag='Y') " +
                "and s.sid = ? and s.status = 'STOW LOSS1'", 0)) {
              stmt2.setString(1, sidVal);
              try (ResultSet rs2 = stmt2.executeQuery()) {
                List<DetRec> details = record.getDetRec();
                if (rs2.next()) {
                  if (Util.isNotEmpty(rs2.getString(1)) || Util.isNotEmpty(rs2.getString(2))) {
                    DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
                    detail.setCC(cc);
                    detail.setLotN(rs2.getString(2));
                    if (Util.isNotEmpty(detail.getLotN()))
                      detail.setLotED(gregDate(rs2.getString(3)));
                    detail.setSerN(rs2.getString(1));
                    detail.setQLot(new BigInteger(PadUtil.padItZeros(rs2.getString(4), 5)));
                    details.add(detail);
                  }
                }
              }
            }
            catch (Exception e) {
              AdfLogUtility.logException(e);
            }
          }
        }
      }

      val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
      val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

      if (!marshallAndSpoolSuccess)
        return -555;

      return stowloss;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return -1;
  }

  public int sendD6GCSSMCTransaction(String rcn) {
    return this.sendD6GCSSMCTransaction(rcn, false);
  }

  /**
   * This function creates a D6T and/or D6A Long Transaction
   * (Generated when a receipt is completed)
   */
  public int sendD6GCSSMCTransaction(String rcn, boolean d6aFromD6t) {
    log.debug("Generate D6_ XXXXX {}", rcn);
    try {
      boolean errors = false;
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
      ShipmentReceiptsInCollection.MRec recordD6A = null;
      String cc = "";
      boolean d6a = false;
      int qty = 0;
      int d6aQty = 0;
      DasfItem dasfItem = null;
      Integer niinId = null;

      record.setIPAAC(getSiteAAC());

      try {
        String documentNumber = "";
        String suffix = "";
        String niin = "";

        String sql = "select r.document_number, nvl(r.suffix,''), n.niin, n.ui, r.quantity_inducted, r.cc, r.quantity_invoiced, r.ri, n.niin_id " +
            "from receipt r, niin_info n " +
            "where r.niin_id = n.niin_id and " +
            "r.rcn = ?";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setString(1, rcn);
          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
              documentNumber = rs.getString(1);
              record.setSDN(documentNumber);
              suffix = rs.getString(2);
              record.setSfx(suffix);
              record.setSfx(rs.getString(2));

              niin = rs.getString(3);
              record.setNIIN(niin);
              record.setUOI(rs.getString(4));
              cc = rs.getString(6);
              if (cc.equals("A")) record.setQCCA(new BigInteger(rs.getString(5)));
              else record.setQCCF(new BigInteger(rs.getString(5)));
              qty = rs.getInt(5);
              record.setQM(new BigInteger(rs.getString(7)));
              record.setRIC(rs.getString(8));

              record.setTxnDate(todayDate());
              record.setKeyD(todayDate());
              record.setRCN(rcn);
              niinId = rs.getInt(9);
            }
          }
        }

        dasfItem = isDASF(documentNumber, niin, qty);
        boolean onDASF = Util.isNotEmpty(dasfItem.refDasfId);
        if (onDASF) {
          // on dasf
          if (checkDASFDuplicate(documentNumber, suffix)) {
            log.error("Duplicate transaction found... terminating D6T transaction.");
            return -2;
          }
          else {
            updateDASF(dasfItem.refDasfId, qty);
            record.setDIC("D6T");
          }
        }
        else {
          // not on dasf
          if (checkNonDASFDuplicate(documentNumber, suffix) && !d6aFromD6t) {
            log.error("Duplicate transaction found... terminating D6A transaction.");
            return -2;
          }
          else {
            recordD6A = factory.createShipmentReceiptsInCollectionMRec();
            recordD6A.setIPAAC(getSiteAAC());
            recordD6A.setDIC("D6A");
            recordD6A.setSDN(record.getSDN());
            recordD6A.setSfx(record.getSfx());

            recordD6A.setNIIN(record.getNIIN());
            recordD6A.setUOI(record.getUOI());
            d6aQty = qty;
            if (cc.equals("A")) recordD6A.setQCCA(new BigInteger(String.valueOf(d6aQty)));
            else recordD6A.setQCCF(new BigInteger(String.valueOf(d6aQty)));
            recordD6A.setQM(new BigInteger(String.valueOf(d6aQty)));
            recordD6A.setRIC(getSiteRIC());
            //leave d6a ric alone

            recordD6A.setTxnDate(todayDate());
            recordD6A.setKeyD(todayDate());
            recordD6A.setRCN(rcn);
            d6a = true;
          }
        }
      }
      catch (Exception e) {
        errors = true;
        AdfLogUtility.logException(e);
      }

      if (!errors) {

        val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
        if (Util.cleanInt(dasfItem.qtyDue) > 0) {
          //* if dasf due not greater than 0, then do not process D6T
          listControlledRcn(record.getDetRec(), factory, rcn, qty, true);

          val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

          if (!marshallAndSpoolSuccess)
            return -555;
        }
        else {
          log.debug("missing trans");
        }

        if (d6a && recordD6A != null) {
          listControlledRcn(recordD6A.getDetRec(), factory, rcn, d6aQty, false);

          val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(recordD6A, niinId, 0);
          if (!marshallAndSpoolSuccess)
            return -555;
        }
        else {
          log.debug("missing trans2");
        }

        ArrayList<ShipmentReceiptsInCollection.MRec> recordDWFs = new ArrayList<>();

        String sqlCXD = "select i.scn, i.document_number, i.suffix, nvl(r.qty,'0') from receipt_issue r, issue i " +
            "where r.rcn = i.rcn and r.scn=i.scn and " +
            "r.rcn = ?";

        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCXD, 0)) {
          pstmt.setString(1, rcn);
          try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
              //* generate any DWF
              ShipmentReceiptsInCollection.MRec recordDWF = factory.createShipmentReceiptsInCollectionMRec();

              recordDWF.setDIC("DWF");
              recordDWF.setIPAAC(getSiteAAC());
              recordDWF.setSfx(record.getSfx());
              recordDWF.setRecSDN(record.getSDN());
              log.debug("dwf {}", recordDWF.getRecSDN());

              recordDWF.setNIIN(record.getNIIN());
              recordDWF.setUOI(record.getUOI());

              int receiptIssueQty = rs.getInt(4);
              if (receiptIssueQty <= 0) log.debug("DWF not printing qty for rcn {}", rcn);
              if (cc.equals("A")) recordDWF.setQCCA(new BigInteger(rs.getString(4)));
              else recordDWF.setQCCF(new BigInteger(rs.getString(4)));
              recordDWF.setRIC(getSiteRIC());
              recordDWF.setTxnDate(todayDate());
              recordDWF.setKeyD(todayDate());
              recordDWF.setRCN(rcn);
              recordDWF.setSCN(rs.getString(1));
              recordDWF.setSDN(rs.getString(2));
              listControlledDwf(recordDWF.getDetRec(), factory, recordDWF.getSCN());

              int sumQty = 0;
              for (DetRec detail : recordDWF.getDetRec()) {
                sumQty += detail.getQLot().intValue();
              }
              if (sumQty > 0 && receiptIssueQty <= 0) {
                log.debug("DWF sumQty for rcn {}", rcn);
                if (cc.equals("A")) recordDWF.setQCCA(new BigInteger(String.valueOf(sumQty)));
                else recordDWF.setQCCF(new BigInteger(String.valueOf(sumQty)));
              }
              recordDWFs.add(recordDWF);
            }
          }
        }
        for (ShipmentReceiptsInCollection.MRec recordDWF : recordDWFs) {
          val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(recordDWF, niinId, 0);
          if (!marshallAndSpoolSuccess)
            return -555;
        }
        return 0;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  /**
   * Returns ref_dasf_id if the document is on the DASF.
   *
   * @return boolean
   *     Date 3/28/2011
   */
  public DasfItem isDASF(String documentNumber, String niin, int qty) {
    DasfItem dasfItem = new DasfItem();
    dasfItem.docNum = documentNumber;

    String refDasfId = "";
    log.debug("? {} {} ++ {}", documentNumber, niin, qty);

    String sql = "select ref_dasf_id, to_char(quantity_due) from ref_dasf " +
        "where document_number=? and (to_char(quantity_due)>0)";

    if (Util.isNotEmpty(niin)) {
      sql += " and record_niin=?";
      dasfItem.niin = niin;
    }

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

      pstmt.setString(1, documentNumber.trim());
      if (Util.isNotEmpty(niin)) pstmt.setString(2, niin.trim());

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          refDasfId = rs.getString(1);

          int qty1 = Util.cleanInt(rs.getString(2));
          if (qty1 < 1) refDasfId = null;

          dasfItem.refDasfId = refDasfId;
          dasfItem.qtyDue = String.valueOf(qty1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    if (Util.isNotEmpty(refDasfId)) log.debug("is on DASF {} {} ++ {}", documentNumber, niin, qty);
    return dasfItem;
  }

  /**
   * Returns ref_dasf_id if the document is on the DASF.
   * <p>
   * Modified Date 3/28/2011
   */
  public void updateDASF(String ref_dasf_id, int qty) {

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update ref_dasf set quantity_due= " +
            "  case " +
            "    when ((to_char(quantity_due)-?) < 0) then 0 " +
            "    else (to_char(quantity_due)-?) " +
            "  end " +
            " where ref_dasf_id=?", 0)) {
      pstmt.setString(1, String.valueOf(qty));
      pstmt.setString(2, String.valueOf(qty));
      pstmt.setString(3, ref_dasf_id);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (Exception e) {
      getDBTransaction().rollback();
    }
    if (Util.isNotEmpty(ref_dasf_id)) log.debug("update DASF {} ++ {}", ref_dasf_id, qty);
  }

  /**
   * This function creates a SRO Long Transaction
   * (Generated when an pick is completed)
   */
  // FUTURE verify if this matches what the sendSROGCSSMCTransaction in i009SpoolServiceImpl does (MatsProcessing did that)
  public int sendSROGCSSMCTransaction(String pidVal) {
    log.debug("Generate SRO XXXXX {}", pidVal);
    try {
      boolean errors = false;
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
      String cc;
      record.setDIC("SRO");
      record.setRIC(getSiteRIC());
      record.setIPAAC(getSiteAAC());
      Integer niinId = null;

      try {

        String sql = "select * from ( " +
            "select n.niin, n.ui, i.document_number, i.rdd, " +
            "lpad(i.issue_priority_designator, 2,'0'), i.cc, i.qty, " +
            "nvl(i.signal_code,''), nvl(i.ero_number,''), " +
            "nvl(i.supplementary_address,''), nvl(i.project_code,''), " +
            "nvl(i.cost_jon,''), i.scn, p.pin, nvl(i.suffix,''), p.qty_picked, n.niin_id, i.created_date " +
            "from niin_info n, issue i, picking p " +
            "where p.pid = ? " +
            "and i.niin_id = n.niin_id and i.scn=p.scn " +
            "union " +
            "select n.niin, n.ui, i.document_number, i.rdd, " +
            "lpad(i.issue_priority_designator, 2,'0'), i.cc, i.qty, " +
            "nvl(i.signal_code,''), nvl(i.ero_number,''), " +
            "nvl(i.supplementary_address,''), nvl(i.project_code,''), " +
            "nvl(i.cost_jon,''), i.scn, p.pin, nvl(i.suffix,''), p.qty_picked, n.niin_id, i.created_date " +
            "from niin_info n, issue_hist i, picking_hist p " +
            "where p.pid = ? " +
            "and i.niin_id = n.niin_id and i.scn=p.scn " +
            "and i.created_date > sysdate -60 " +
            ") order by created_date desc";

        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
          pstmt.setString(1, pidVal);
          pstmt.setString(2, pidVal);
          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
              record.setNIIN(rs.getString(1));
              record.setUOI(rs.getString(2));
              record.setSDN(rs.getString(3));
              record.setRDD(rs.getString(4));
              record.setPri(rs.getString(5));
              cc = rs.getString(6);
              if (cc.equals("A")) record.setQCCA(new BigInteger(PadUtil.padItZeros(rs.getString(16), 5)));
              else record.setQCCF(new BigInteger(PadUtil.padItZeros(rs.getString(16), 5)));

              record.setSC(rs.getString(8));
              record.setSRN(rs.getString(9)); //* ero
              record.setSupADD(rs.getString(10));
              record.setProj(rs.getString(11));
              record.setJON(rs.getString(12));
              record.setTxnDate(todayDate());
              record.setKeyD(todayDate());
              record.setSCN(rs.getString(13));
              record.setPIN(rs.getString(14));
              record.setSfx(rs.getString(15));
              niinId = rs.getInt(17);
            }
          }
        }
      }
      catch (Exception e) {
        errors = true;
        AdfLogUtility.logException(e);
      }

      if (!errors) {
        listControlled(record.getDetRec(), factory, pidVal, true);

        val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
        val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

        if (!marshallAndSpoolSuccess)
          return -555;

        return 0;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  // FUTURE verify if this matches what the sendAE1GCSSMCTransaction in i009SpoolServiceImpl does (MatsProcessing did that)
  public int sendAE1GCSSMCTransaction(String scnVal) {
    /*
     * DESC     : This function creates a AE1 Long Transaction
     *            (Generated when an issue is completed or immediately after
     *            an issue is generated if the warehouse has a quantity of 0
     *            on-hand)
     *            This version of the AE1 function is for Issues that have been
     *            sent into the warehouse for processing.   If the warehouse had
     *            a quantity of 0 on-hand at the beginning of the day, the proper
     *            tables will not have values populated to be able to generate
     *            this transaction properly and will require data population from
     *            other tables.
     * PARAMS   : scnVal (The SCN value of the issue transaction)
     */

    ObjectFactory factory = new ObjectFactory();
    ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
    record.setDIC("AE1");
    record.setRIC(getSiteRIC());
    record.setIPAAC(getSiteAAC());
    record.setTxnDate(todayDate());
    record.setKeyD(todayDate());
    record.setSCN(scnVal);

    Integer niinId;
    String cc;
    try {
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select n.niin, n.ui, i.document_number, nvl(i.suffix,''),lpad(i.issue_priority_designator, 2,'0'), i.qty, i.cc, n.niin_id from niin_info n, issue i where i.scn = ? and i.niin_id = n.niin_id", 0)) {

        stR.setString(1, scnVal);

        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            record.setNIIN(rs.getString(1));
            record.setUOI(rs.getString(2));
            record.setSDN(rs.getString(3));
            record.setSfx(rs.getString(4));
            cc = rs.getString(7);
            record.setPri(rs.getString(5));
            niinId = rs.getInt(8);

            //Quantity Issued
            int qtyIssued;
            int qtyDenied;
            try (PreparedStatement ps2 = getDBTransaction().createPreparedStatement("select sum(qty_picked) from picking where scn = ? and status = 'PACKED'", 0)) {
              ps2.setString(1, scnVal);
              try (ResultSet rs3 = ps2.executeQuery()) {
                qtyIssued = rs.getInt(6);
                if (rs3.next()) {
                  qtyDenied = qtyIssued - rs3.getInt(1);
                }
                else {
                  qtyDenied = qtyIssued;
                }
              }
            }
            if (cc.equals("A")) record.setQCCA(new BigInteger("00000" + qtyDenied));
            else record.setQCCF(new BigInteger("00000" + qtyDenied));
            record.setStatus("BA");
            if (qtyDenied > 0) {
              if (cc.equals("A")) record.setQCCA(new BigInteger("0" + qtyDenied));
              else record.setQCCF(new BigInteger("0" + qtyDenied));
              record.setStatus("M5");
            }

            val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
            //Create the xml spool rec for denail
            if (qtyDenied > 0) {
              val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

              if (!marshallAndSpoolSuccess)
                return -555;
            }
            //Case all are denied
            if ((qtyIssued - qtyDenied) == 0) {
              return 0;
            }
            //Ceate the xml spool recs for issues
            BigInteger bi = BigInteger.valueOf(qtyIssued - qtyDenied);
            if (cc.equals("A")) record.setQCCA(bi);
            else record.setQCCF(bi);
            record.setStatus("BA");
            log.debug("AE1 SCN and Suffix in before niin serial call XXXX  scn {}", scnVal);

            listControlled(record.getDetRec(), factory, scnVal, false);

            val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

            if (!marshallAndSpoolSuccess)
              return -555;

            return 0;
          }
          else {
            return -2;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  // FUTURE verify if this matches what the sendAsxTrans in i009SpoolServiceImpl does (MatsProcessing did that)
  public int sendAsxTrans(String scnVal, int transNum) {
    log.debug("Generate ASx XXXXX {}", scnVal);
    //* 01-24-2012 update made to ASx/SRO transaction GCSS mode to address when
    //* scn from previous years overlap
    try {
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
      String cc = "";
      String sTrans = "AS" + transNum;
      //WALK-THRU is transNum == 3
      //MCF:11/20/2015: New requirement to send transactions via hardcard.
      if (transNum == 3) {
        sTrans = "AS1";
      }

      record.setDIC(sTrans);
      record.setRIC(getSiteRIC());
      record.setIPAAC(getSiteAAC());
      Integer niinId = null;

      try (PreparedStatement pstmtMANIFEST = getDBTransaction().createPreparedStatement("select * from ( " +
          "select lead_tcn, tcn, s.created_date from shipping s, shipping_manifest m where s.shipping_manifest_id = m.shipping_manifest_id and scn=? " +
          "union select lead_tcn, tcn, s.created_date from shipping_hist s, shipping_manifest_hist m where s.shipping_manifest_id = m.shipping_manifest_id and scn=? " +
          ") order by created_date desc ", 0)) {
        //* Determine if there is a shipping manifest

        pstmtMANIFEST.setString(1, scnVal);
        pstmtMANIFEST.setString(2, scnVal);
        try (ResultSet rsMANIFEST = pstmtMANIFEST.executeQuery()) {
          String manifest = "";
          if (rsMANIFEST.next()) {
            manifest = rsMANIFEST.getString(1);
            record.setTCN(rsMANIFEST.getString(2));
          }
          rsMANIFEST.close();
          pstmtMANIFEST.close();
          if (GCSSMCUtil.isEmpty(manifest)) {
            //do not allow empty manifest return
            if (transNum != 3)
              return -5;
          }
        }
      }

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select * from ( " +
          "select n.niin, n.ui, i.document_number, nvl(i.suffix,''),lpad(i.issue_priority_designator, 2,'0'), nvl(i.qty_issued, '0'), nvl(i.cc,'A'), nvl(i.supplementary_address, ''), nvl(i.fund_code,''),nvl(i.rcn, ''), n.niin_id, i.created_date from niin_info n, issue i where i.scn = ? and i.niin_id = n.niin_id " +
          "union select n.niin, n.ui, i.document_number, nvl(i.suffix,''),lpad(i.issue_priority_designator, 2,'0'), nvl(i.qty_issued, '0'), i.cc, nvl(i.supplementary_address, ''), nvl(i.fund_code,''),nvl(i.rcn, ''), n.niin_id, i.created_date from niin_info n, issue_hist i where i.scn = ?  and i.created_date > sysdate -60 and i.niin_id = n.niin_id " +
          ") order by created_date desc", 0)) {

        pstmt.setString(1, scnVal);
        pstmt.setString(2, scnVal);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            record.setNIIN(rs.getString(1));
            record.setUOI(rs.getString(2));
            record.setSDN(rs.getString(3));
            record.setSfx(rs.getString(4));
            cc = rs.getString(7);
            record.setPri(rs.getString(5));

            if (cc.equals("A")) record.setQCCA(new BigInteger(rs.getString(6)));
            else record.setQCCF(new BigInteger(rs.getString(6)));

            record.setSupADD(rs.getString(8));  // null pointer exception
            record.setFund(rs.getString(9)); //* ero
            record.setRCN(rs.getString(10));
            record.setSCN(scnVal);

            record.setTxnDate(todayDate());
            record.setKeyD(todayDate());
            niinId = rs.getInt(11);
          }
        }

        //Quantity Packed
        boolean qtyPackedNotFound = true;
        try (PreparedStatement pstmt2 = getDBTransaction().createPreparedStatement("select sum(QTY_PICKED) from picking where scn = ? and status = ? ", 0)) {
          pstmt2.setString(1, scnVal);
          if (transNum == 1)
            pstmt2.setString(2, "PACKED");
          else if (transNum == 2)
            pstmt2.setString(2, "SHIPPED");
          else
            pstmt2.setString(2, "WALKTHRU");
          try (ResultSet rs = pstmt2.executeQuery()) {
            if (rs.next()) {
              int iQty = rs.getInt(1);
              qtyPackedNotFound = false;
              if (cc.equals("A")) record.setQCCA(new BigInteger(String.valueOf(iQty)));
              else record.setQCCF(new BigInteger(String.valueOf(iQty)));
            }
          }
        }

        if (qtyPackedNotFound) {
          //Quantity Packed - check history
          try (PreparedStatement pstmt2 = getDBTransaction().createPreparedStatement("select sum(QTY_PICKED) from picking_hist where scn = ? and status = ? and created_date > sysdate -60", 0)) {
            pstmt2.setString(1, scnVal);
            if (transNum == 1)
              pstmt2.setString(2, "PACKED");
            else if (transNum == 2)
              pstmt2.setString(2, "SHIPPED");
            else
              pstmt2.setString(2, "WALKTHRU");
            try (ResultSet rs = pstmt2.executeQuery()) {
              if (rs.next()) {
                int iQty = rs.getInt(1);

                if (cc.equals("A")) record.setQCCA(new BigInteger(String.valueOf(iQty)));
                else record.setQCCF(new BigInteger(String.valueOf(iQty)));
              }
            }
          }
        }
      }

      listControlled(record.getDetRec(), factory, scnVal, false);

      val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
      val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);
      if (!marshallAndSpoolSuccess)
        return -555;
      return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return -1;
  }

  private void buildCurr(List<ControlledItem> curr, Object niinId, Object invItemId, Object niinLocId, String cc, boolean byNiin) {

    String sqlCurr = "select n.niin, n.ui, nvl(s.LOT_CON_NUM,''), nvl(s.SERIAL_NUMBER,''), " +
        "s.cc, (to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), " +
        "s.qty, n.niin_id, s.location_id, s.serial_lot_num_track_id from niin_info n, serial_lot_num_track s ";

    if (niinLocId != null) {
      sqlCurr += ", niin_location nl where s.niin_id=n.niin_id and s.location_id=nl.location_id and n.niin_id=? and nl.niin_loc_id=? and s.cc=? and s.issued_flag='N'";
    }
    else {
      if (byNiin) {
        sqlCurr += "where s.niin_id=n.niin_id and n.niin_id = ? and s.location_id in (select location_id from inventory_item where inventory_id in (select inventory_id from inventory_item where inventory_item_id=?)) and s.cc=? and s.issued_flag='N'";
      }
      else {
        sqlCurr += "where s.niin_id=n.niin_id and n.niin_id = ? and s.location_id in (select location_id from inventory_item where inventory_item_id=?) and s.cc=? and s.issued_flag='N'";
      }
    }
    log.debug("{}, {}, {}", niinId, invItemId, sqlCurr);

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlCurr, 0)) {

      pstmt.setObject(1, niinId);
      if (niinLocId != null) pstmt.setObject(2, niinLocId);
      else pstmt.setObject(2, invItemId);
      pstmt.setString(3, cc);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          ControlledItem item = new ControlledItem();
          item.niin = rs.getString(1);
          item.ui = rs.getString(2);
          item.lot = rs.getString(3);
          item.srl = rs.getString(4);
          item.cc = rs.getString(5);
          item.exp = rs.getString(6);
          item.qty = rs.getString(7);
          item.niinId = rs.getString(8);
          item.locationId = rs.getString(9);
          item.srlLotId = rs.getString(10);
          curr.add(item);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void buildInv(List<ControlledItem> inv, Object niinId, Object invItemId, Object niinLocId, String cc, boolean byNiin) {

    String sqlInv = "select n.niin, n.ui,  nvl(s.LOT_CON_NUM,''),nvl(s.SERIAL_NUMBER,''), " +
        "s.cc, (to_char(nvl(s.expiration_date,'01-JAN-9999'),'YYYY-MM-DD HH24:MI:SS')), " +
        "s.qty, n.niin_id, s.location_id from inventory i, inventory_item m, niin_info n, inv_serial_lot_num s " +
        "where i.inventory_id=m.inventory_id and m.inventory_item_id=s.inventory_item_id " +
        "and s.niin_id=n.niin_id  " +
        "and s.cc=? ";

    if (byNiin) {
      sqlInv += "and s.location_id in (select location_id from inventory_item where inventory_id in (select inventory_id from inventory_item where inventory_item_id=?)) and n.niin_id = ? ";
    }
    else {
      sqlInv += "and s.inventory_item_id in (select inventory_item_id from inventory_item  where inventory_id in " +
          "(select inventory_id from inventory_item where inventory_item_id = ?)) ";
    }
    if (niinLocId != null) {
      sqlInv += " and s.location_id in (select location_id from niin_location where niin_loc_id=?)";
    }
    log.debug(sqlInv);
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlInv, 0)) {
      pstmt.setString(1, cc);
      pstmt.setObject(2, invItemId);
      int offset = 3;
      if (byNiin) {
        pstmt.setObject(3, niinId);
        offset++;
      }
      if (niinLocId != null) {
        pstmt.setObject(offset, niinLocId);
      }

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          ControlledItem item = new ControlledItem();
          item.niin = rs.getString(1);
          item.ui = rs.getString(2);
          item.lot = rs.getString(3);
          item.srl = rs.getString(4);
          item.cc = rs.getString(5);
          item.exp = rs.getString(6);
          item.qty = rs.getString(7);
          item.niinId = rs.getString(8);
          item.locationId = rs.getString(9);
          inv.add(item);
          log.debug(item.toString());
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void compareD89List(List<ControlledItem> d8,
                              List<ControlledItem> d9,
                              List<ControlledItem> currList,
                              List<ControlledItem> invList) {
    log.debug("compare");
    for (ControlledItem inv : invList) {
      //* add controlled items not found in the current list
      for (ControlledItem curr : currList) {

        if (!inv.found && !curr.found) {
          if (Util.cleanString(inv.srl).equals(Util.cleanString(curr.srl))) {

            if (Util.cleanString(inv.lot).equals(Util.cleanString(curr.lot))) {
              if (Util.cleanString(inv.locationId).equals(Util.cleanString(curr.locationId))) {
                if (Util.cleanInt(inv.qty) == Util.cleanInt(curr.qty)) {
                  inv.found = true;
                  curr.found = true;
                  log.debug("found match {}", inv);
                }
                else {
                  int adjQty = Util.cleanInt(inv.qty) - Util.cleanInt(curr.qty);
                  if (adjQty < 0) {
                    inv.adjQty = String.valueOf(adjQty);
                    inv.qty = String.valueOf(Util.cleanInt(inv.adjQty) * (-1));
                    inv.transType = "D9";
                    inv.srlLotId = curr.srlLotId;
                    d9.add(inv);
                  }
                  else if (adjQty > 0) {
                    inv.adjQty = String.valueOf(adjQty);
                    inv.qty = inv.adjQty;
                    inv.transType = "D8";
                    inv.srlLotId = curr.srlLotId;
                    d8.add(inv);
                  }
                  inv.found = true;
                  curr.found = true;
                  log.debug("found mismatch qty {}", inv);
                }
              }
            }
          }
        }
      }
    }

    for (ControlledItem inv : invList) {
      if (!inv.found) {
        if (Util.isNotEmpty(inv.lot) || Util.isNotEmpty(inv.srl)) {
          inv.adjQty = inv.qty;
          inv.transType = "D8";
          d8.add(inv);
          log.debug("gain {}", inv);
        }
      }
    }
    for (ControlledItem curr : currList) {
      if (!curr.found) {
        if (Util.isNotEmpty(curr.lot) || Util.isNotEmpty(curr.srl)) {
          curr.adjQty = "-" + curr.qty;
          curr.transType = "D9";
          d9.add(curr);
          log.debug("lose {}", curr);
        }
      }
    }
  }

  private boolean populateAndProcessRecord(List<ControlledItem> controlledItems, String dic, String cc, ShipmentReceiptsInCollection.MRec record, Integer niinId, Integer userId) {
    boolean marshallAndSpoolSuccess = false;
    val recordUpdated = populateRecord(controlledItems, dic, "cc", record);
    if (recordUpdated) {
      val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
      marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, userId);
    }
    return marshallAndSpoolSuccess;
  }

  private boolean populateRecord(List<ControlledItem> gainOrLoss,
                                 String dic, String cc, ShipmentReceiptsInCollection.MRec record) {
    ObjectFactory factory = new ObjectFactory();

    record.setRIC(getSiteRIC());
    record.setIPAAC(getSiteAAC());
    record.setDIC(dic);
    record.setSDN(String.valueOf(returnNextAvailableDocumentNumber()));

    record.setTxnDate(todayDate());
    record.setKeyD(todayDate());

    List<ShipmentReceiptsInCollection.MRec.DetRec> details = record.getDetRec();
    int count = 0;
    int sum = 0;
    for (ControlledItem item : gainOrLoss) {
      if (count == 0) {

        record.setNIIN(item.niin);
        record.setUOI(item.ui);
        cc = item.cc;
      }
      log.debug(item.transType);
      DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
      detail.setSerN(item.srl);
      detail.setLotN(item.lot);
      if (Util.isNotEmpty(item.lot)) {
        detail.setLotED(gregDate(item.exp));
      }
      detail.setQLot(new BigInteger(PadUtil.padItZeros(item.qty, 5)));
      detail.setCC(item.cc); // must change to support 2 cc

      details.add(detail);
      count++;
      sum += Util.cleanInt(item.qty);
    }

    if (cc.equals("A")) record.setQCCA(new BigInteger(PadUtil.padItZeros(String.valueOf(sum), 5)));
    else record.setQCCF(new BigInteger(PadUtil.padItZeros(String.valueOf(sum), 5)));

    return sum > 0;
  }

  private void insertControlledItem(List<ControlledItem> inv) {
    try (PreparedStatement spoolAdd = getDBTransaction().createPreparedStatement(
        "insert into serial_lot_num_track (niin_id, serial_number, lot_con_num, cc, expiration_date, issued_flag, " +
            "timestamp, qty, location_id, iuid) values (?,?,?,?,to_date(?,'YYYY-MM-DD HH24:MI:SS')," +
            "'N', sysdate,?,?,?)", 0)) {
      for (ControlledItem item : inv) {
        spoolAdd.setObject(1, item.niinId);
        spoolAdd.setString(2, item.srl);
        spoolAdd.setString(3, item.lot);
        spoolAdd.setString(4, item.cc);
        spoolAdd.setObject(5, item.exp);
        spoolAdd.setObject(6, item.qty);
        spoolAdd.setObject(7, item.locationId);
        spoolAdd.setString(8, item.iuid);
        spoolAdd.addBatch();
      }
      spoolAdd.executeBatch();
      this.getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
  }

  private void deleteControlledItem(List<ControlledItem> inv) {
    try (PreparedStatement spoolAdd = getDBTransaction().createPreparedStatement("delete from serial_lot_num_track  where serial_lot_num_track_id=? and niin_id=? and issued_flag='N'", 0)) {
      for (ControlledItem item : inv) {
        spoolAdd.setObject(1, item.srlLotId);
        spoolAdd.setObject(2, item.niinId);
        spoolAdd.addBatch();
      }

      spoolAdd.executeBatch();
      this.getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
  }

  private void incrControlledItem(List<ControlledItem> inv) {
    List<ControlledItem> insert = new ArrayList<>();
    try {
      try (PreparedStatement spoolAdd = getDBTransaction().createPreparedStatement("update serial_lot_num_track set qty=qty+?, serial_number=?, lot_con_num=?, expiration_date=to_date(?,'YYYY-MM-DD HH24:MI:SS') where niin_id=? and location_id=? and cc=? and serial_lot_num_track_id=? and issued_flag='N'", 0)) {
        for (ControlledItem item : inv) {
          if (Util.isNotEmpty(item.srlLotId)) {
            log.debug("update serial_lot_num_track qty+= {}", item.adjQty);
            spoolAdd.setString(1, item.adjQty);
            spoolAdd.setString(2, item.srl);
            spoolAdd.setString(3, item.lot);
            spoolAdd.setString(4, item.exp);

            spoolAdd.setString(5, item.niinId);
            spoolAdd.setString(6, item.locationId);
            spoolAdd.setString(7, item.cc);
            spoolAdd.setString(8, item.srlLotId);
            spoolAdd.addBatch();
          }
          else {
            insert.add(item);
          }
        }
        spoolAdd.executeBatch();
        this.getDBTransaction().commit();
      }

      try (PreparedStatement spoolAdd = getDBTransaction().createPreparedStatement("delete from serial_lot_num_track where serial_lot_num_track_id IN (select sl.serial_lot_num_track_id from serial_lot_num_track sl left join pick_serial_lot_num pl on sl.serial_lot_num_track_id = pl.serial_lot_num_track_id where pl.pick_serial_lot_num IS NULL and sl.qty <1 and sl.issued_flag='N')", 0)) {
        spoolAdd.execute();
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      this.getDBTransaction().rollback();
    }
    insertControlledItem(insert);
  }

  /**
   * For controlled items only
   * Function to generate a D8 or D9 GCSSMC transaction
   * Result is inserted into the spool table
   */
  public int sendDxxGCSSMCTransaction(Object niinId, Object invItemId,
                                      Object niinLocId, boolean byNiin, Integer userID) {

    try {
      ObjectFactory factory = new ObjectFactory();

      List<ControlledItem> curr = new ArrayList<>();
      List<ControlledItem> inv = new ArrayList<>();
      buildCurr(curr, niinId, invItemId, niinLocId, "A", byNiin);
      buildInv(inv, niinId, invItemId, niinLocId, "A", byNiin);

      List<ControlledItem> currF = new ArrayList<>();
      List<ControlledItem> invF = new ArrayList<>();
      buildCurr(currF, niinId, invItemId, niinLocId, "F", byNiin);
      buildInv(invF, niinId, invItemId, niinLocId, "F", byNiin);

      log.debug("curr :: {}", curr);
      log.debug("inv :: {}", inv);
      log.debug("currF :: {}", currF);
      log.debug("invF :: {}", invF);

      boolean done = false;

      val niinIdInt = Integer.parseInt(niinId.toString());
      if (CollectionUtils.isEmpty(curr)) {
        //* d8 entire inventory list
        ShipmentReceiptsInCollection.MRec recordD8 = factory.createShipmentReceiptsInCollectionMRec();
        val processed = populateAndProcessRecord(inv, "D8A", "A", recordD8, niinIdInt, userID);
        if (processed)
          insertControlledItem(inv);

        done = true;
      }
      else if (CollectionUtils.isEmpty(inv)) {
        //* d9 entire current list
        ShipmentReceiptsInCollection.MRec recordD9 = factory.createShipmentReceiptsInCollectionMRec();
        val processed = populateAndProcessRecord(curr, "D9A", "A", recordD9, niinIdInt, userID);
        if (processed)
          deleteControlledItem(curr);
        done = true;
      }

      if (!done) {
        List<ControlledItem> d8 = new ArrayList<>();
        List<ControlledItem> d9 = new ArrayList<>();
        compareD89List(d8, d9, curr, inv);

        if (CollectionUtils.isNotEmpty(d8)) {
          ShipmentReceiptsInCollection.MRec recordD8 = factory.createShipmentReceiptsInCollectionMRec();
          try {
            val processed = populateAndProcessRecord(d8, "D8A", "A", recordD8, niinIdInt, userID);
            if (processed)
              incrControlledItem(d8);
          }
          catch (Exception e) {
            AdfLogUtility.logException(e);
          }
        }
        if (CollectionUtils.isNotEmpty(d9)) {

          log.debug("show me d9");
          for (ControlledItem d : d9)
            log.debug(d.toString());
          ShipmentReceiptsInCollection.MRec recordD9 = factory.createShipmentReceiptsInCollectionMRec();
          try {
            val processed = populateAndProcessRecord(d9, "D9A", "A", recordD9, niinIdInt, userID);
            if (processed)
              incrControlledItem(d9);
          }
          catch (Exception e1) {
            log.error("Exception during d9 processing", e1);
          }
        }
      }

      boolean doneF = false;
      if (CollectionUtils.isEmpty(currF)) {
        //* d8 entire inventory list
        ShipmentReceiptsInCollection.MRec recordD8 = factory.createShipmentReceiptsInCollectionMRec();
        try {
          val processed = populateAndProcessRecord(invF, "D8A", "F", recordD8, niinIdInt, userID);
          if (processed)
            insertControlledItem(invF);

          doneF = true;
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }
      else if (CollectionUtils.isEmpty(invF)) {
        //* d9 entire current list
        ShipmentReceiptsInCollection.MRec recordD9 = factory.createShipmentReceiptsInCollectionMRec();
        try {
          val processed = populateAndProcessRecord(currF, "D9A", "F", recordD9, niinIdInt, userID);
          if (processed)
            deleteControlledItem(currF);
          doneF = true;
        }
        catch (Exception e) {
          AdfLogUtility.logException(e);
        }
      }

      if (!doneF) {
        List<ControlledItem> d8F = new ArrayList<>();
        List<ControlledItem> d9F = new ArrayList<>();
        compareD89List(d8F, d9F, currF, invF);

        if (CollectionUtils.isNotEmpty(d8F)) {
          log.debug("show me d8F");
          for (ControlledItem d : d8F)
            log.debug(d.toString());
          ShipmentReceiptsInCollection.MRec recordD8 = factory.createShipmentReceiptsInCollectionMRec();
          val processed = populateAndProcessRecord(d8F, "D8A", "F", recordD8, niinIdInt, userID);
          if (processed)
            incrControlledItem(d8F);
        }
        if (CollectionUtils.isNotEmpty(d9F)) {

          log.debug("show me d9F");
          for (ControlledItem d : d9F)
            log.debug(d.toString());
          ShipmentReceiptsInCollection.MRec recordD9 = factory.createShipmentReceiptsInCollectionMRec();
          val processed = populateAndProcessRecord(d9F, "D9A", "F", recordD9, niinIdInt, userID);
          if (processed)
            incrControlledItem(d9F);
        }
      }

      return 0;
    }
    catch (Exception e) {
      log.error("Exception during sendDxxGCSSMCTransaction", e);
    }
    return -1;
  }

  /**
   * For non-controlled items only
   */
  public int sendDxxGCSSMCTransaction(Integer niinId, int balChange,
                                      String dic, String invItemId,
                                      String immedCC, Integer userId) {
    if (balChange < 1) return 0;

    val balChangeBigInt = BigInteger.valueOf(balChange);

    try {
      ObjectFactory factory = new ObjectFactory();
      ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();

      record.setDIC(dic);
      record.setRIC(getSiteRIC());
      record.setIPAAC(getSiteAAC());

      String sql;
      if (!invItemId.equals("0")) {
        sql = "select n.niin, n.ui, nl.cc " +
            "from niin_info n, inventory_item i, niin_location nl " +
            "where n.niin_id=i.niin_id and i.niin_loc_id=nl.niin_loc_id " +
            "and n.niin_id = ? and i.inventory_item_id=? ";
      }
      else {
        sql = "select n.niin, n.ui " +
            "from niin_info n " +
            "where n.niin_id = ? ";
      }

      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

        pstmt.setObject(1, niinId);
        if (!invItemId.equals("0")) {
          pstmt.setObject(2, invItemId);
        }

        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            record.setNIIN(rs.getString(1));
            record.setUOI(rs.getString(2));
            record.setSDN(String.valueOf(returnNextAvailableDocumentNumber()));
            if (!invItemId.equals("0")) {
              if (Util.isEmpty(immedCC)) {
                immedCC = rs.getString(3);
              }
            }
            if (Util.cleanString(immedCC).equals("A")) record.setQCCA(balChangeBigInt);
            else record.setQCCF(balChangeBigInt);

            record.setTxnDate(todayDate());
            record.setKeyD(todayDate());

            val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
            val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, userId);

            if (!marshallAndSpoolSuccess)
              return -555;
          }
        }
      }
      return 0;
    }
    catch (Exception e) {
      log.error("Errors found processing sendDxxGCSSMCTransaction", e);
      return -1;
    }
  }

  public int sendDACGCSSMCTransaction(int niinId, int niinLocId,
                                      String oldCC, Object serialNumber,
                                      Object lotControlNumber, Object qty) {

    if (Util.cleanInt(qty) < 1) return 0; //* do not send 0 qty
    ObjectFactory factory = new ObjectFactory();
    ShipmentReceiptsInCollection.MRec record = factory.createShipmentReceiptsInCollectionMRec();
    record.setDIC("DAC");
    record.setRIC(getSiteRIC());
    record.setIPAAC(getSiteAAC());

    String sql;
    if (serialNumber == null && lotControlNumber == null) {
      sql = "select n.niin, n.ui, nl.qty, nvl(nl.pc, '') from niin_location nl, niin_info n where nl.niin_id=n.niin_id and n.niin_id=? and nl.niin_loc_id=? ";
    }
    else {
      sql = "select n.niin, n.ui, s.qty, nvl(s.serial_number,''), nvl(s.lot_con_num,''), to_char(s.expiration_date,'YYYY-MM-DD HH24:MI:SS'), s.cc from serial_lot_num_track s, niin_info n where s.niin_id=n.niin_id and s.niin_id=? and s.location_id in (select location_id from niin_location where niin_loc_id=?) and s.cc=?";
    }

    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(sql, 0)) {
      stR.setInt(1, niinId);
      stR.setInt(2, niinLocId);
      if (serialNumber != null || lotControlNumber != null)
        stR.setString(3, (oldCC.equals("A")) ? "F" : "A");

      int sum = 0;
      try (ResultSet rs = stR.executeQuery()) {
        int i = 0;
        List<DetRec> details = record.getDetRec();
        while (rs.next()) {

          if (i == 0) {
            record.setSDN(returnNextAvailableDocumentNumber());
            record.setNIIN(rs.getString(1));
            record.setUOI(rs.getString(2));
            record.setFCC(oldCC);
            record.setTxnDate(todayDate());
            record.setKeyD(todayDate());
          }

          sum += rs.getInt(3);

          if (serialNumber != null || lotControlNumber != null) {
            DetRec detail = factory.createShipmentReceiptsInCollectionMRecDetRec();
            detail.setSerN(rs.getString(4));
            detail.setLotN(rs.getString(5));
            if (Util.isNotEmpty(detail.getLotN()))
              detail.setLotED(gregDate(rs.getString(6)));
            detail.setCC(rs.getString(7)); // changeToCC
            detail.setQLot(new BigInteger(PadUtil.padItZeros(rs.getString(3), 5)));
            details.add(detail);
          }
          i++;
        }
      }
      if (oldCC.equals("A")) record.setQCCF(new BigInteger(String.valueOf(sum)));
      else record.setQCCA(new BigInteger(String.valueOf(sum)));
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return -1;
    }

    val i009SpoolService = ContextUtils.getBean(I009SpoolService.class);
    val marshallAndSpoolSuccess = i009SpoolService.marshallAndSpool(record, niinId, 0);

    if (!marshallAndSpoolSuccess)
      return -555;

    return 0;
  }

  public String returnNextAvailableDocumentNumber() {
    String docreturn = "";

    //will return between 0 to 999

    try (PreparedStatement st = getDBTransaction().createPreparedStatement("select DOC_SER_NUM_GCSSMC_SEQ.nextval from dual", 0)) {
      docreturn += this.getSiteAAC();
      docreturn += Util.getCurrentJulian(4);
      int index;

      try (ResultSet rs = st.executeQuery()) {
        rs.next();
        index = rs.getInt(1);
      }

      int alphaIndex = (index / 1000) + 64;
      index = index % 1000;
      if (index < 10) {
        docreturn = docreturn + (char) alphaIndex;
        docreturn += "00";
      }
      else if (index < 100) {
        docreturn = docreturn + (char) alphaIndex;
        docreturn += "0";
      }
      else {
        docreturn = docreturn + (char) alphaIndex;
      }
      docreturn += index;
      log.debug("document num: {}", docreturn);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    // return the new number
    return docreturn;
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
   * Container's getter for SiteInfoGCSSMCFlag1
   */
  public SiteInfoGCSSMCFlagImpl getSiteInfoGCSSMCFlag1() {
    return (SiteInfoGCSSMCFlagImpl) findViewObject("SiteInfoGCSSMCFlag1");
  }

  /**
   * Container's getter for DocIdListVO1
   */
  public DocIdListVOImpl getDocIdListVO1() {
    return (DocIdListVOImpl) findViewObject("DocIdListVO1");
  }

  private void prepareSpoolBatch() {

    try {
      //* UPdate first batch only if the receipt is complete
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "update spool set status = 'READY' where spool_def_mode = 'G' " +
              " and (status = 'BATCH' or status = 'CROSS') and gcssmc_xml is not null " +
              "and rcn not in  (select r.rcn from receipt r, stow s where r.rcn=s.rcn(+) " +
              "and r.status='RECEIPT COMPLETE' and (s.status='STOW READY' or s.status like '%BYPASS%') " +
              "union select r.rcn from receipt_hist r, stow_hist s where r.rcn=s.rcn(+) " +
              "and r.status='RECEIPT COMPLETE' and (s.status='STOW READY' or s.status like '%BYPASS%') " +
              "and r.created_date > sysdate-60)", 0)) {
        log.debug("Updating first batch since receipt is complete.");
        stR.executeUpdate();
        this.getDBTransaction().commit();
      }

      //* Update the second batch dependent on above batch to have been sent
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "update spool set status = 'READY' where spool_def_mode = 'G' " +
              " and (status = 'SHIP' or status = 'DELIVER') and gcssmc_xml is not null " +
              "and rcn in (select distinct rcn from spool where status='COMPLETE')", 0)) {
        log.debug("Updating second batch since previous batch has been sent.");
        stR.executeUpdate();
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }
  }

  public boolean isInterfacesOn() {
    boolean retVal = false;
    String sql = "select nvl(interfaces_on,'N') from site_info where gcss_mc='Y'";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          if (rs.getString(1).equals("Y")) {
            retVal = true;
          }
        }
      }
    }
    catch (Exception e) {
      retVal = false;
    }
    return retVal;
  }

  /**
   * Container's getter for SiteInfoMHIFRangeVO1
   */
  public SiteInfoMHIFRangeVOImpl getSiteInfoMHIFRangeVO1() {
    return (SiteInfoMHIFRangeVOImpl) findViewObject("SiteInfoMHIFRangeVO1");
  }

  public int importGCSSMC(String interface_name, String rec_data) {
    int result;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("insert into gcssmc_imports_data (interface_name, status, rec_data, created_by, created_date " + ") values (?,'READY',?,1,sysdate)", 0)) {
      if (rec_data == null || rec_data.length() <= 0)
        return -2;

      pstmt.setString(1, interface_name);
      oracle.sql.CLOB c = oracle.sql.CLOB.createTemporary(pstmt.getConnection(), false, CLOB.DURATION_SESSION);
      c.setString(1, rec_data);
      pstmt.setClob(2, c);
      pstmt.executeUpdate();
      getDBTransaction().commit();
      pstmt.close();
      c.freeTemporary();
      result = 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
      result = -1;
    }
    return result;
  }

  public static class ControlledItem {

    public String niinId = "";
    public String niin = "";
    public String ui = "";
    public String lot = "";
    public String srl = "";
    public String cc = "";
    public String exp = "";
    public String qty = "";
    public String locationId = "";
    public String srlLotId = "";
    public final String iuid = "";
    public String adjQty = "";
    public String transType = "";
    public boolean found = false;

    public String toString() {
      return niin + "::" + ui + "::" + lot + "::" + srl + "::" + cc + "::" + exp + "::" + qty;
    }
  }

  public static class DasfItem {

    public String niin = "";
    public String docNum = "";
    public String qtyDue = "";
    public String ui = "";
    public String refDasfId = "";
  }

  public boolean checkDuplicate(String rcn) {
    String documentNumber = "";
    String suffix = "";
    String niin = "";
    int qty = 0;
    String sql = "select r.document_number, nvl(r.suffix,''), n.niin, r.quantity_inducted " +
        "from receipt r, niin_info n " +
        "where r.niin_id = n.niin_id and " +
        "r.rcn = ?";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, rcn);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          documentNumber = rs.getString(1);
          suffix = rs.getString(2);
          niin = rs.getString(3);
          qty = rs.getInt(4);
        }
      }

      val dasfItem = isDASF(documentNumber, niin, qty);
      boolean onDASF = Util.isNotEmpty(dasfItem.refDasfId);
      if (onDASF && checkDASFDuplicate(documentNumber, suffix)) {
        log.debug("Duplicate transaction found.");
        return true;
      }
      else if (!onDASF && checkNonDASFDuplicate(documentNumber, suffix)) {
        log.debug("Duplicate transaction found.");
        return true;
      }
    }
    catch (SQLException e) {
      log.error("SQL exception checking for duplicate D6 transactions.");
    }
    return false;
  }

  public boolean checkDASFDuplicate(String documentNumber, String suffix) throws SQLException {
    boolean documentDuplicate = spoolHasDocumentNumber(documentNumber);
    boolean d6ADuplicate = spoolHasD6ADocumentNumber(documentNumber);
    boolean documentAndSuffixDuplicate = spoolHasDocumentNumberAndSuffix(documentNumber, suffix);
    boolean hasDocumentNumberOnly = StringUtil.isNotEmpty(documentNumber) && StringUtil.isEmpty(suffix);
    boolean hasDocumentNumberAndSuffix = StringUtil.isNotEmpty(documentNumber) && StringUtil.isNotEmpty(suffix);

    return (d6ADuplicate ||
        hasDocumentNumberOnly && (documentDuplicate || documentAndSuffixDuplicate) ||
        hasDocumentNumberAndSuffix && documentAndSuffixDuplicate);
  }

  public boolean checkNonDASFDuplicate(String documentNumber, String suffix) throws SQLException {
    boolean documentDuplicate = spoolHasDocumentNumber(documentNumber);
    boolean documentAndSuffixDuplicate = spoolHasDocumentNumberAndSuffix(documentNumber, suffix);

    return (documentDuplicate || documentAndSuffixDuplicate);
  }

  private boolean spoolHasDocumentNumber(String documentNumber) throws SQLException {
    int count = 0;
    String sql = "select count(*) from v_spool_check where transaction_type in ('D6A', 'D6T') and document_number = ?";

    try (PreparedStatement statement = getDBTransaction().createPreparedStatement(sql, 0)) {
      statement.setString(1, documentNumber);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) count = result.getInt(1);
      }
    }

    return count > 0;
  }

  private boolean spoolHasD6ADocumentNumber(String documentNumber) throws SQLException {
    int count = 0;
    String sql = "select count(*) from v_spool_check where transaction_type = 'D6A' and document_number = ?";

    try (PreparedStatement statement = getDBTransaction().createPreparedStatement(sql, 0)) {
      statement.setString(1, documentNumber);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) count = result.getInt(1);
      }
    }

    return count > 0;
  }

  private boolean spoolHasDocumentNumberAndSuffix(String documentNumber, String suffix) throws SQLException {
    int count = 0;
    String sql = "select count(*) from v_spool_check where transaction_type in ('D6A', 'D6T') and document_number = ? and suffix = ?";

    try (PreparedStatement statement = getDBTransaction().createPreparedStatement(sql, 0)) {
      statement.setString(1, documentNumber);
      statement.setString(2, suffix);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) count = result.getInt(1);
      }
    }

    return count > 0;
  }

  private boolean spoolHasSid(String sid) throws SQLException {
    int count = 0;
    String sql = "select count(*) from v_spool_check where transaction_type = 'STW' and sid = ?";

    try (PreparedStatement statement = getDBTransaction().createPreparedStatement(sql, 0)) {
      statement.setString(1, sid);
      try (ResultSet result = statement.executeQuery()) {
        if (result.next()) count = result.getInt(1);
      }
    }

    return count > 0;
  }
}

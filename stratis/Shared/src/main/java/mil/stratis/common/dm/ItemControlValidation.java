package mil.stratis.common.dm;

import mil.stratis.common.db.DbCommon;
import mil.stratis.common.util.StringUtil;
import oracle.jbo.server.DBTransaction;

public class ItemControlValidation extends DbCommon {

  private boolean itemValid = false;
  private final String serial;
  private final String niinId;

  public ItemControlValidation(String serial, String niinId, DBTransaction db) {
    this.serial = serial;
    this.niinId = niinId;

    itemOnHand(db);
  }

  public boolean isItemValid() {
    return this.itemValid;
  }

  private void itemOnHand(DBTransaction db) {
    String sql =
        "select count(*) " + "from serial_lot_num_track s " + "where " + "s.serial_number=? and " + "s.niin_id=? ";

    String value = getSingleReturnValue(sql, serial, niinId, db);
    this.itemValid = StringUtil.cleanInt(value) < 1;

    if (itemValid) {
      String sqlHist =
          "select count(*) " + "from serial_lot_num_track_hist s " + "where " + "s.serial_number=? and " +
              "s.niin_id=? ";

      String valueHist = getSingleReturnValue(sqlHist, serial, niinId, db);
      this.itemValid = StringUtil.cleanInt(valueHist) < 1;
    }

    if (itemValid) {
      String sql2 =
          "select count(*) " + "from pick_serial_lot_num p, issue i " + "where p.scn=i.scn and " + "p.serial_number=? and " +
              "i.niin_id=? ";

      String value2 = getSingleReturnValue(sql2, serial, niinId, db);
      this.itemValid = StringUtil.cleanInt(value2) < 1;
    }

    if (itemValid) {
      String sql2Hist =
          "select count(*) " + "from pick_serial_lot_num_hist p, issue i " + "where p.scn=i.scn and " +
              "p.serial_number=? and " + "i.niin_id=? ";

      String value2Hist = getSingleReturnValue(sql2Hist, serial, niinId, db);
      this.itemValid = StringUtil.cleanInt(value2Hist) < 1;
    }
  }
}

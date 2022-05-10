package mil.stratis.model.entity.rcv;

import lombok.NoArgsConstructor;
import mil.stratis.model.entity.loc.LocationImpl;
import mil.stratis.model.entity.loc.NiinLocationImpl;
import oracle.jbo.Key;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class StowImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    SID {
      public Object get(StowImpl obj) {
        return obj.getSid();
      }

      public void put(StowImpl obj, Object value) {
        obj.setSid((String) value);
      }
    },
    QTY_TO_BE_STOWED {
      public Object get(StowImpl obj) {
        return obj.getQtyToBeStowed();
      }

      public void put(StowImpl obj, Object value) {
        obj.setQtyToBeStowed((Number) value);
      }
    },
    RCN {
      public Object get(StowImpl obj) {
        return obj.getRcn();
      }

      public void put(StowImpl obj, Object value) {
        obj.setRcn((Number) value);
      }
    },
    NIIN_LOC_ID {
      public Object get(StowImpl obj) {
        return obj.getNiinLocId();
      }

      public void put(StowImpl obj, Object value) {
        obj.setNiinLocId((Number) value);
      }
    },
    CREATED_BY {
      public Object get(StowImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(StowImpl obj, Object value) {
        obj.setCreatedBy((Number) value);
      }
    },
    CREATED_DATE {
      public Object get(StowImpl obj) {
        return obj.getCreatedDate();
      }

      public void put(StowImpl obj, Object value) {
        obj.setCreatedDate((Date) value);
      }
    },
    MODIFIED_BY {
      public Object get(StowImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(StowImpl obj, Object value) {
        obj.setModifiedBy((Number) value);
      }
    },
    MODIFIED_DATE {
      public Object get(StowImpl obj) {
        return obj.getModifiedDate();
      }

      public void put(StowImpl obj, Object value) {
        obj.setModifiedDate((Date) value);
      }
    },
    STOW_ID {
      public Object get(StowImpl obj) {
        return obj.getStowId();
      }

      public void put(StowImpl obj, Object value) {
        obj.setStowId((DBSequence) value);
      }
    },
    PID {
      public Object get(StowImpl obj) {
        return obj.getPid();
      }

      public void put(StowImpl obj, Object value) {
        obj.setPid((Number) value);
      }
    },
    STATUS {
      public Object get(StowImpl obj) {
        return obj.getStatus();
      }

      public void put(StowImpl obj, Object value) {
        obj.setStatus((String) value);
      }
    },
    CANCEL_REASON {
      public Object get(StowImpl obj) {
        return obj.getCancelReason();
      }

      public void put(StowImpl obj, Object value) {
        obj.setCancelReason((String) value);
      }
    },
    EXPIRATION_DATE {
      public Object get(StowImpl obj) {
        return obj.getExpirationDate();
      }

      public void put(StowImpl obj, Object value) {
        obj.setExpirationDate((Date) value);
      }
    },
    DATE_OF_MANUFACTURE {
      public Object get(StowImpl obj) {
        return obj.getDateOfManufacture();
      }

      public void put(StowImpl obj, Object value) {
        obj.setDateOfManufacture((Date) value);
      }
    },
    LOCATION_ID {
      public Object get(StowImpl obj) {
        return obj.getLocationId();
      }

      public void put(StowImpl obj, Object value) {
        obj.setLocationId((Number) value);
      }
    },
    LOT_CON_NUM {
      public Object get(StowImpl obj) {
        return obj.getLotConNum();
      }

      public void put(StowImpl obj, Object value) {
        obj.setLotConNum((String) value);
      }
    },
    CASE_WEIGHT_QTY {
      public Object get(StowImpl obj) {
        return obj.getCaseWeightQty();
      }

      public void put(StowImpl obj, Object value) {
        obj.setCaseWeightQty((Number) value);
      }
    },
    PACKED_DATE {
      public Object get(StowImpl obj) {
        return obj.getPackedDate();
      }

      public void put(StowImpl obj, Object value) {
        obj.setPackedDate((Date) value);
      }
    },
    SERIAL_NUMBER {
      public Object get(StowImpl obj) {
        return obj.getSerialNumber();
      }

      public void put(StowImpl obj, Object value) {
        obj.setSerialNumber((String) value);
      }
    },
    STOW_QTY {
      public Object get(StowImpl obj) {
        return obj.getStowQty();
      }

      public void put(StowImpl obj, Object value) {
        obj.setStowQty((Number) value);
      }
    },
    SCAN_IND {
      public Object get(StowImpl obj) {
        return obj.getScanInd();
      }

      public void put(StowImpl obj, Object value) {
        obj.setScanInd((String) value);
      }
    },
    BYPASS_COUNT {
      public Object get(StowImpl obj) {
        return obj.getBypassCount();
      }

      public void put(StowImpl obj, Object value) {
        obj.setBypassCount((Number) value);
      }
    },
    ASSIGN_TO_USER {
      public Object get(StowImpl obj) {
        return obj.getAssignToUser();
      }

      public void put(StowImpl obj, Object value) {
        obj.setAssignToUser((Number) value);
      }
    },
    STOWED_BY {
      public Object get(StowImpl obj) {
        return obj.getStowedBy();
      }

      public void put(StowImpl obj, Object value) {
        obj.setStowedBy((Number) value);
      }
    },
    STOWED_DATE {
      public Object get(StowImpl obj) {
        return obj.getStowedDate();
      }

      public void put(StowImpl obj, Object value) {
        obj.setStowedDate((Date) value);
      }
    },
    RECEIPT {
      public Object get(StowImpl obj) {
        return obj.getReceipt();
      }

      public void put(StowImpl obj, Object value) {
        obj.setReceipt((ReceiptImpl) value);
      }
    },
    LOCATION {
      public Object get(StowImpl obj) {
        return obj.getLocation();
      }

      public void put(StowImpl obj, Object value) {
        obj.setLocation((LocationImpl) value);
      }
    },
    NIIN_LOCATION {
      public Object get(StowImpl obj) {
        return obj.getNiinLocation();
      }

      public void put(StowImpl obj, Object value) {
        obj.setNiinLocation((NiinLocationImpl) value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(StowImpl object);

    public abstract void put(StowImpl object, Object value);

    public int index() {
      return StowImpl.AttributesEnum.firstIndex() + ordinal();
    }

    public static int firstIndex() {
      return FIRST_INDEX;
    }

    public static int count() {
      return StowImpl.AttributesEnum.firstIndex() + StowImpl.AttributesEnum.staticValues().length;
    }

    public static AttributesEnum[] staticValues() {
      return AttributesEnum.values();
    }
  }

  public static final int SID = AttributesEnum.SID.index();
  public static final int QTYTOBESTOWED = AttributesEnum.QTY_TO_BE_STOWED.index();
  public static final int RCN = AttributesEnum.RCN.index();
  public static final int NIINLOCID = AttributesEnum.NIIN_LOC_ID.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int CREATEDDATE = AttributesEnum.CREATED_DATE.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int MODIFIEDDATE = AttributesEnum.MODIFIED_DATE.index();
  public static final int STOWID = AttributesEnum.STOW_ID.index();
  public static final int PID = AttributesEnum.PID.index();
  public static final int STATUS = AttributesEnum.STATUS.index();
  public static final int CANCELREASON = AttributesEnum.CANCEL_REASON.index();
  public static final int EXPIRATIONDATE = AttributesEnum.EXPIRATION_DATE.index();
  public static final int DATEOFMANUFACTURE = AttributesEnum.DATE_OF_MANUFACTURE.index();
  public static final int LOCATIONID = AttributesEnum.LOCATION_ID.index();
  public static final int LOTCONNUM = AttributesEnum.LOT_CON_NUM.index();
  public static final int CASEWEIGHTQTY = AttributesEnum.CASE_WEIGHT_QTY.index();
  public static final int PACKEDDATE = AttributesEnum.PACKED_DATE.index();
  public static final int SERIALNUMBER = AttributesEnum.SERIAL_NUMBER.index();
  public static final int STOWQTY = AttributesEnum.STOW_QTY.index();
  public static final int SCANIND = AttributesEnum.SCAN_IND.index();
  public static final int BYPASSCOUNT = AttributesEnum.BYPASS_COUNT.index();
  public static final int ASSIGNTOUSER = AttributesEnum.ASSIGN_TO_USER.index();
  public static final int STOWEDBY = AttributesEnum.STOWED_BY.index();
  public static final int STOWEDDATE = AttributesEnum.STOWED_DATE.index();
  public static final int RECEIPT = AttributesEnum.RECEIPT.index();
  public static final int LOCATION = AttributesEnum.LOCATION.index();
  public static final int NIINLOCATION = AttributesEnum.NIIN_LOCATION.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.entities.rcv.Stow");
  }

  /**
   * Gets the attribute value for Sid, using the alias name Sid
   */
  public String getSid() {
    return (String) getAttributeInternal(SID);
  }

  /**
   * Sets <code>value</code> as the attribute value for Sid
   */
  public void setSid(String value) {
    setAttributeInternal(SID, value);
  }

  /**
   * Gets the attribute value for QtyToBeStowed, using the alias name QtyToBeStowed
   */
  public Number getQtyToBeStowed() {
    return (Number) getAttributeInternal(QTYTOBESTOWED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QtyToBeStowed
   */
  public void setQtyToBeStowed(Number value) {
    setAttributeInternal(QTYTOBESTOWED, value);
  }

  /**
   * Gets the attribute value for Rcn, using the alias name Rcn
   */
  public Number getRcn() {
    return (Number) getAttributeInternal(RCN);
  }

  /**
   * Sets <code>value</code> as the attribute value for Rcn
   */
  public void setRcn(Number value) {
    setAttributeInternal(RCN, value);
  }

  /**
   * Gets the attribute value for NiinLocId, using the alias name NiinLocId
   */
  public Number getNiinLocId() {
    return (Number) getAttributeInternal(NIINLOCID);
  }

  /**
   * Gets the attribute value for CreatedBy, using the alias name CreatedBy
   */
  public Number getCreatedBy() {
    return (Number) getAttributeInternal(CREATEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedBy
   */
  public void setCreatedBy(Number value) {
    setAttributeInternal(CREATEDBY, value);
  }

  /**
   * Gets the attribute value for CreatedDate, using the alias name CreatedDate
   */
  public Date getCreatedDate() {
    return (Date) getAttributeInternal(CREATEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedDate
   */
  public void setCreatedDate(Date value) {
    setAttributeInternal(CREATEDDATE, value);
  }

  /**
   * Gets the attribute value for ModifiedBy, using the alias name ModifiedBy
   */
  public Number getModifiedBy() {
    return (Number) getAttributeInternal(MODIFIEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedBy
   */
  public void setModifiedBy(Number value) {
    setAttributeInternal(MODIFIEDBY, value);
  }

  /**
   * Gets the attribute value for ModifiedDate, using the alias name ModifiedDate
   */
  public Date getModifiedDate() {
    return (Date) getAttributeInternal(MODIFIEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedDate
   */
  public void setModifiedDate(Date value) {
    setAttributeInternal(MODIFIEDDATE, value);
  }

  /**
   * Gets the attribute value for StowId, using the alias name StowId
   */
  public DBSequence getStowId() {
    return (DBSequence) getAttributeInternal(STOWID);
  }

  /**
   * Gets the attribute value for Pid, using the alias name Pid
   */
  public Number getPid() {
    return (Number) getAttributeInternal(PID);
  }

  /**
   * Sets <code>value</code> as the attribute value for Pid
   */
  public void setPid(Number value) {
    setAttributeInternal(PID, value);
  }

  /**
   * getAttrInvokeAccessor: generated method. Do not modify.
   */
  @Override
  protected Object getAttrInvokeAccessor(int index,
                                         AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      return AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].get(this);
    }
    return super.getAttrInvokeAccessor(index, attrDef);
  }

  /**
   * setAttrInvokeAccessor: generated method. Do not modify.
   */
  @Override
  protected void setAttrInvokeAccessor(int index, Object value,
                                       AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].put(this, value);
      return;
    }
    super.setAttrInvokeAccessor(index, value, attrDef);
  }

  /**
   * Gets the associated entity ReceiptImpl
   */
  public ReceiptImpl getReceipt() {
    return (ReceiptImpl) getAttributeInternal(RECEIPT);
  }

  /**
   * Sets <code>value</code> as the associated entity ReceiptImpl
   */
  public void setReceipt(ReceiptImpl value) {
    setAttributeInternal(RECEIPT, value);
  }

  /**
   * Gets the associated entity NiinLocationImpl
   */
  public NiinLocationImpl getNiinLocation() {
    return (NiinLocationImpl) getAttributeInternal(NIINLOCATION);
  }

  /**
   * Sets <code>value</code> as the associated entity NiinLocationImpl
   */
  public void setNiinLocation(NiinLocationImpl value) {
    setAttributeInternal(NIINLOCATION, value);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinLocId
   */
  public void setNiinLocId(Number value) {
    setAttributeInternal(NIINLOCID, value);
  }

  /**
   * Gets the attribute value for Status, using the alias name Status
   */
  public String getStatus() {
    return (String) getAttributeInternal(STATUS);
  }

  /**
   * Sets <code>value</code> as the attribute value for Status
   */
  public void setStatus(String value) {
    setAttributeInternal(STATUS, value);
  }

  /**
   * Gets the attribute value for CancelReason, using the alias name CancelReason
   */
  public String getCancelReason() {
    return (String) getAttributeInternal(CANCELREASON);
  }

  /**
   * Sets <code>value</code> as the attribute value for CancelReason
   */
  public void setCancelReason(String value) {
    setAttributeInternal(CANCELREASON, value);
  }

  /**
   * Gets the attribute value for ExpirationDate, using the alias name ExpirationDate
   */
  public Date getExpirationDate() {
    return (Date) getAttributeInternal(EXPIRATIONDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ExpirationDate
   */
  public void setExpirationDate(Date value) {
    setAttributeInternal(EXPIRATIONDATE, value);
  }

  /**
   * Gets the attribute value for DateOfManufacture, using the alias name DateOfManufacture
   */
  public Date getDateOfManufacture() {
    return (Date) getAttributeInternal(DATEOFMANUFACTURE);
  }

  /**
   * Sets <code>value</code> as the attribute value for DateOfManufacture
   */
  public void setDateOfManufacture(Date value) {
    setAttributeInternal(DATEOFMANUFACTURE, value);
  }

  /**
   * Gets the attribute value for LocationId, using the alias name LocationId
   */
  public Number getLocationId() {
    return (Number) getAttributeInternal(LOCATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocationId
   */
  public void setLocationId(Number value) {
    setAttributeInternal(LOCATIONID, value);
  }

  /**
   * Gets the attribute value for LotConNum, using the alias name LotConNum
   */
  public String getLotConNum() {
    return (String) getAttributeInternal(LOTCONNUM);
  }

  /**
   * Sets <code>value</code> as the attribute value for LotConNum
   */
  public void setLotConNum(String value) {
    setAttributeInternal(LOTCONNUM, value);
  }

  /**
   * Gets the attribute value for CaseWeightQty, using the alias name CaseWeightQty
   */
  public Number getCaseWeightQty() {
    return (Number) getAttributeInternal(CASEWEIGHTQTY);
  }

  /**
   * Sets <code>value</code> as the attribute value for CaseWeightQty
   */
  public void setCaseWeightQty(Number value) {
    setAttributeInternal(CASEWEIGHTQTY, value);
  }

  /**
   * Gets the attribute value for PackedDate, using the alias name PackedDate
   */
  public Date getPackedDate() {
    return (Date) getAttributeInternal(PACKEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for PackedDate
   */
  public void setPackedDate(Date value) {
    setAttributeInternal(PACKEDDATE, value);
  }

  /**
   * Gets the associated entity LocationImpl
   */
  public LocationImpl getLocation() {
    return (LocationImpl) getAttributeInternal(LOCATION);
  }

  /**
   * Sets <code>value</code> as the associated entity LocationImpl
   */
  public void setLocation(LocationImpl value) {
    setAttributeInternal(LOCATION, value);
  }

  /**
   * @param stowId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence stowId) {
    return new Key(new Object[]{stowId});
  }

  /**
   * Gets the attribute value for SerialNumber, using the alias name SerialNumber
   */
  public String getSerialNumber() {
    return (String) getAttributeInternal(SERIALNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for SerialNumber
   */
  public void setSerialNumber(String value) {
    setAttributeInternal(SERIALNUMBER, value);
  }

  /**
   * Sets <code>value</code> as the attribute value for StowId
   */
  public void setStowId(DBSequence value) {
    setAttributeInternal(STOWID, value);
  }

  /**
   * Gets the attribute value for StowQty, using the alias name StowQty
   */
  public Number getStowQty() {
    return (Number) getAttributeInternal(STOWQTY);
  }

  /**
   * Sets <code>value</code> as the attribute value for StowQty
   */
  public void setStowQty(Number value) {
    setAttributeInternal(STOWQTY, value);
  }

  /**
   * Gets the attribute value for ScanInd, using the alias name ScanInd
   */
  public String getScanInd() {
    return (String) getAttributeInternal(SCANIND);
  }

  /**
   * Sets <code>value</code> as the attribute value for ScanInd
   */
  public void setScanInd(String value) {
    setAttributeInternal(SCANIND, value);
  }

  /**
   * Gets the attribute value for BypassCount, using the alias name BypassCount
   */
  public Number getBypassCount() {
    return (Number) getAttributeInternal(BYPASSCOUNT);
  }

  /**
   * Sets <code>value</code> as the attribute value for BypassCount
   */
  public void setBypassCount(Number value) {
    setAttributeInternal(BYPASSCOUNT, value);
  }

  /**
   * Gets the attribute value for AssignToUser, using the alias name AssignToUser
   */
  public Number getAssignToUser() {
    return (Number) getAttributeInternal(ASSIGNTOUSER);
  }

  /**
   * Sets <code>value</code> as the attribute value for AssignToUser
   */
  public void setAssignToUser(Number value) {
    setAttributeInternal(ASSIGNTOUSER, value);
  }

  /**
   * Gets the attribute value for StowedBy, using the alias name StowedBy
   */
  public Number getStowedBy() {
    return (Number) getAttributeInternal(STOWEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for StowedBy
   */
  public void setStowedBy(Number value) {
    setAttributeInternal(STOWEDBY, value);
  }

  /**
   * Gets the attribute value for StowedDate, using the alias name StowedDate
   */
  public Date getStowedDate() {
    return (Date) getAttributeInternal(STOWEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for StowedDate
   */
  public void setStowedDate(Date value) {
    setAttributeInternal(STOWEDDATE, value);
  }
}
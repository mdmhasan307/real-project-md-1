package mil.stratis.model.entity.loc;

import lombok.NoArgsConstructor;
import mil.stratis.model.entity.site.NiinInfoImpl;
import oracle.jbo.Key;
import oracle.jbo.RowIterator;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class NiinLocationImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    NIIN_LOC_ID {
      public Object get(NiinLocationImpl obj) {
        return obj.getNiinLocId();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setNiinLocId((DBSequence) value);
      }
    },
    NIIN_ID {
      public Object get(NiinLocationImpl obj) {
        return obj.getNiinId();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setNiinId((Number) value);
      }
    },
    LOCATION_ID {
      public Object get(NiinLocationImpl obj) {
        return obj.getLocationId();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setLocationId((Number) value);
      }
    },
    QTY {
      public Object get(NiinLocationImpl obj) {
        return obj.getQty();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setQty((Number) value);
      }
    },
    EXPIRATION_DATE {
      public Object get(NiinLocationImpl obj) {
        return obj.getExpirationDate();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setExpirationDate((Date) value);
      }
    },
    DATE_OF_MANUFACTURE {
      public Object get(NiinLocationImpl obj) {
        return obj.getDateOfManufacture();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setDateOfManufacture((Date) value);
      }
    },
    CC {
      public Object get(NiinLocationImpl obj) {
        return obj.getCc();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setCc((String) value);
      }
    },
    LOCKED {
      public Object get(NiinLocationImpl obj) {
        return obj.getLocked();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setLocked((String) value);
      }
    },
    CREATED_BY {
      public Object get(NiinLocationImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setCreatedBy((Number) value);
      }
    },
    CREATED_DATE {
      public Object get(NiinLocationImpl obj) {
        return obj.getCreatedDate();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setCreatedDate((Date) value);
      }
    },
    MODIFIED_BY {
      public Object get(NiinLocationImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setModifiedBy((Number) value);
      }
    },
    MODIFIED_DATE {
      public Object get(NiinLocationImpl obj) {
        return obj.getModifiedDate();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setModifiedDate((Date) value);
      }
    },
    PROJECT_CODE {
      public Object get(NiinLocationImpl obj) {
        return obj.getProjectCode();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setProjectCode((String) value);
      }
    },
    PC {
      public Object get(NiinLocationImpl obj) {
        return obj.getPc();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setPc((String) value);
      }
    },
    LAST_INV_DATE {
      public Object get(NiinLocationImpl obj) {
        return obj.getLastInvDate();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setLastInvDate((Date) value);
      }
    },
    CASE_WEIGHT_QTY {
      public Object get(NiinLocationImpl obj) {
        return obj.getCaseWeightQty();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setCaseWeightQty((Number) value);
      }
    },
    LOT_CON_NUM {
      public Object get(NiinLocationImpl obj) {
        return obj.getLotConNum();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setLotConNum((String) value);
      }
    },
    SERIAL_NUMBER {
      public Object get(NiinLocationImpl obj) {
        return obj.getSerialNumber();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setSerialNumber((String) value);
      }
    },
    PACKED_DATE {
      public Object get(NiinLocationImpl obj) {
        return obj.getPackedDate();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setPackedDate((Date) value);
      }
    },
    NUM_EXTENTS {
      public Object get(NiinLocationImpl obj) {
        return obj.getNumExtents();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setNumExtents((Number) value);
      }
    },
    NUM_COUNTS {
      public Object get(NiinLocationImpl obj) {
        return obj.getNumCounts();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setNumCounts((Number) value);
      }
    },
    UNDER_AUDIT {
      public Object get(NiinLocationImpl obj) {
        return obj.getUnderAudit();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setUnderAudit((String) value);
      }
    },
    OLD_UI {
      public Object get(NiinLocationImpl obj) {
        return obj.getOldUi();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setOldUi((String) value);
      }
    },
    NSN_REMARK {
      public Object get(NiinLocationImpl obj) {
        return obj.getNsnRemark();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setNsnRemark((String) value);
      }
    },
    EXP_REMARK {
      public Object get(NiinLocationImpl obj) {
        return obj.getExpRemark();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setExpRemark((String) value);
      }
    },
    OLD_QTY {
      public Object get(NiinLocationImpl obj) {
        return obj.getOldQty();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setOldQty((Number) value);
      }
    },
    RECALL_FLAG {
      public Object get(NiinLocationImpl obj) {
        return obj.getRecallFlag();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setRecallFlag((String) value);
      }
    },
    RECALL_DATE {
      public Object get(NiinLocationImpl obj) {
        return obj.getRecallDate();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setRecallDate((Date) value);
      }
    },
    LOCATION {
      public Object get(NiinLocationImpl obj) {
        return obj.getLocation();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setLocation((LocationImpl) value);
      }
    },
    NIIN_INFO {
      public Object get(NiinLocationImpl obj) {
        return obj.getNiinInfo();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setNiinInfo((NiinInfoImpl) value);
      }
    },
    INVENTORY_ITEM {
      public Object get(NiinLocationImpl obj) {
        return obj.getInventoryItem();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    },
    PICKING {
      public Object get(NiinLocationImpl obj) {
        return obj.getPicking();
      }

      public void put(NiinLocationImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(NiinLocationImpl object);

    public abstract void put(NiinLocationImpl object, Object value);

    public int index() {
      return AttributesEnum.firstIndex() + ordinal();
    }

    public static int firstIndex() {
      return FIRST_INDEX;
    }

    public static int count() {
      return AttributesEnum.firstIndex() + AttributesEnum.staticValues().length;
    }

    public static AttributesEnum[] staticValues() {
      return AttributesEnum.values();
    }
  }

  public static final int NIINLOCID = AttributesEnum.NIIN_LOC_ID.index();
  public static final int NIINID = AttributesEnum.NIIN_ID.index();
  public static final int LOCATIONID = AttributesEnum.LOCATION_ID.index();
  public static final int QTY = AttributesEnum.QTY.index();
  public static final int EXPIRATIONDATE = AttributesEnum.EXPIRATION_DATE.index();
  public static final int DATEOFMANUFACTURE = AttributesEnum.DATE_OF_MANUFACTURE.index();
  public static final int CC = AttributesEnum.CC.index();
  public static final int LOCKED = AttributesEnum.LOCKED.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int CREATEDDATE = AttributesEnum.CREATED_DATE.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int MODIFIEDDATE = AttributesEnum.MODIFIED_DATE.index();
  public static final int PROJECTCODE = AttributesEnum.PROJECT_CODE.index();
  public static final int PC = AttributesEnum.PC.index();
  public static final int LASTINVDATE = AttributesEnum.LAST_INV_DATE.index();
  public static final int CASEWEIGHTQTY = AttributesEnum.CASE_WEIGHT_QTY.index();
  public static final int LOTCONNUM = AttributesEnum.LOT_CON_NUM.index();
  public static final int SERIALNUMBER = AttributesEnum.SERIAL_NUMBER.index();
  public static final int PACKEDDATE = AttributesEnum.PACKED_DATE.index();
  public static final int NUMEXTENTS = AttributesEnum.NUM_EXTENTS.index();
  public static final int NUMCOUNTS = AttributesEnum.NUM_COUNTS.index();
  public static final int UNDERAUDIT = AttributesEnum.UNDER_AUDIT.index();
  public static final int OLDUI = AttributesEnum.OLD_UI.index();
  public static final int NSNREMARK = AttributesEnum.NSN_REMARK.index();
  public static final int EXPREMARK = AttributesEnum.EXP_REMARK.index();
  public static final int OLDQTY = AttributesEnum.OLD_QTY.index();
  public static final int RECALLFLAG = AttributesEnum.RECALL_FLAG.index();
  public static final int RECALLDATE = AttributesEnum.RECALL_DATE.index();
  public static final int LOCATION = AttributesEnum.LOCATION.index();
  public static final int NIININFO = AttributesEnum.NIIN_INFO.index();
  public static final int INVENTORYITEM = AttributesEnum.INVENTORY_ITEM.index();
  public static final int PICKING = AttributesEnum.PICKING.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.loc.NiinLocation");
  }

  /**
   * Gets the attribute value for NiinLocId, using the alias name NiinLocId.
   *
   * @return the value of NiinLocId
   */
  public DBSequence getNiinLocId() {
    return (DBSequence) getAttributeInternal(NIINLOCID);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinLocId.
   *
   * @param value value to set the NiinLocId
   */
  public void setNiinLocId(DBSequence value) {
    setAttributeInternal(NIINLOCID, value);
  }

  /**
   * Gets the attribute value for NiinId, using the alias name NiinId.
   *
   * @return the value of NiinId
   */
  public Number getNiinId() {
    return (Number) getAttributeInternal(NIINID);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinId.
   *
   * @param value value to set the NiinId
   */
  public void setNiinId(Number value) {
    setAttributeInternal(NIINID, value);
  }

  /**
   * Gets the attribute value for LocationId, using the alias name LocationId.
   *
   * @return the value of LocationId
   */
  public Number getLocationId() {
    return (Number) getAttributeInternal(LOCATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocationId.
   *
   * @param value value to set the LocationId
   */
  public void setLocationId(Number value) {
    setAttributeInternal(LOCATIONID, value);
  }

  /**
   * Gets the attribute value for Qty, using the alias name Qty.
   *
   * @return the value of Qty
   */
  public Number getQty() {
    return (Number) getAttributeInternal(QTY);
  }

  /**
   * Sets <code>value</code> as the attribute value for Qty.
   *
   * @param value value to set the Qty
   */
  public void setQty(Number value) {
    setAttributeInternal(QTY, value);
  }

  /**
   * Gets the attribute value for ExpirationDate, using the alias name ExpirationDate.
   *
   * @return the value of ExpirationDate
   */
  public Date getExpirationDate() {
    return (Date) getAttributeInternal(EXPIRATIONDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ExpirationDate.
   *
   * @param value value to set the ExpirationDate
   */
  public void setExpirationDate(Date value) {
    setAttributeInternal(EXPIRATIONDATE, value);
  }

  /**
   * Gets the attribute value for DateOfManufacture, using the alias name DateOfManufacture.
   *
   * @return the value of DateOfManufacture
   */
  public Date getDateOfManufacture() {
    return (Date) getAttributeInternal(DATEOFMANUFACTURE);
  }

  /**
   * Sets <code>value</code> as the attribute value for DateOfManufacture.
   *
   * @param value value to set the DateOfManufacture
   */
  public void setDateOfManufacture(Date value) {
    setAttributeInternal(DATEOFMANUFACTURE, value);
  }

  /**
   * Gets the attribute value for Cc, using the alias name Cc.
   *
   * @return the value of Cc
   */
  public String getCc() {
    return (String) getAttributeInternal(CC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Cc.
   *
   * @param value value to set the Cc
   */
  public void setCc(String value) {
    setAttributeInternal(CC, value);
  }

  /**
   * Gets the attribute value for Locked, using the alias name Locked.
   *
   * @return the value of Locked
   */
  public String getLocked() {
    return (String) getAttributeInternal(LOCKED);
  }

  /**
   * Sets <code>value</code> as the attribute value for Locked.
   *
   * @param value value to set the Locked
   */
  public void setLocked(String value) {
    setAttributeInternal(LOCKED, value);
  }

  /**
   * Gets the attribute value for CreatedBy, using the alias name CreatedBy.
   *
   * @return the value of CreatedBy
   */
  public Number getCreatedBy() {
    return (Number) getAttributeInternal(CREATEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedBy.
   *
   * @param value value to set the CreatedBy
   */
  public void setCreatedBy(Number value) {
    setAttributeInternal(CREATEDBY, value);
  }

  /**
   * Gets the attribute value for CreatedDate, using the alias name CreatedDate.
   *
   * @return the value of CreatedDate
   */
  public Date getCreatedDate() {
    return (Date) getAttributeInternal(CREATEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedDate.
   *
   * @param value value to set the CreatedDate
   */
  public void setCreatedDate(Date value) {
    setAttributeInternal(CREATEDDATE, value);
  }

  /**
   * Gets the attribute value for ModifiedBy, using the alias name ModifiedBy.
   *
   * @return the value of ModifiedBy
   */
  public Number getModifiedBy() {
    return (Number) getAttributeInternal(MODIFIEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedBy.
   *
   * @param value value to set the ModifiedBy
   */
  public void setModifiedBy(Number value) {
    setAttributeInternal(MODIFIEDBY, value);
  }

  /**
   * Gets the attribute value for ModifiedDate, using the alias name ModifiedDate.
   *
   * @return the value of ModifiedDate
   */
  public Date getModifiedDate() {
    return (Date) getAttributeInternal(MODIFIEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedDate.
   *
   * @param value value to set the ModifiedDate
   */
  public void setModifiedDate(Date value) {
    setAttributeInternal(MODIFIEDDATE, value);
  }

  /**
   * Gets the attribute value for ProjectCode, using the alias name ProjectCode.
   *
   * @return the value of ProjectCode
   */
  public String getProjectCode() {
    return (String) getAttributeInternal(PROJECTCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ProjectCode.
   *
   * @param value value to set the ProjectCode
   */
  public void setProjectCode(String value) {
    setAttributeInternal(PROJECTCODE, value);
  }

  /**
   * Gets the attribute value for Pc, using the alias name Pc.
   *
   * @return the value of Pc
   */
  public String getPc() {
    return (String) getAttributeInternal(PC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Pc.
   *
   * @param value value to set the Pc
   */
  public void setPc(String value) {
    setAttributeInternal(PC, value);
  }

  /**
   * Gets the attribute value for LastInvDate, using the alias name LastInvDate.
   *
   * @return the value of LastInvDate
   */
  public Date getLastInvDate() {
    return (Date) getAttributeInternal(LASTINVDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for LastInvDate.
   *
   * @param value value to set the LastInvDate
   */
  public void setLastInvDate(Date value) {
    setAttributeInternal(LASTINVDATE, value);
  }

  /**
   * Gets the attribute value for CaseWeightQty, using the alias name CaseWeightQty.
   *
   * @return the value of CaseWeightQty
   */
  public Number getCaseWeightQty() {
    return (Number) getAttributeInternal(CASEWEIGHTQTY);
  }

  /**
   * Sets <code>value</code> as the attribute value for CaseWeightQty.
   *
   * @param value value to set the CaseWeightQty
   */
  public void setCaseWeightQty(Number value) {
    setAttributeInternal(CASEWEIGHTQTY, value);
  }

  /**
   * Gets the attribute value for LotConNum, using the alias name LotConNum.
   *
   * @return the value of LotConNum
   */
  public String getLotConNum() {
    return (String) getAttributeInternal(LOTCONNUM);
  }

  /**
   * Sets <code>value</code> as the attribute value for LotConNum.
   *
   * @param value value to set the LotConNum
   */
  public void setLotConNum(String value) {
    setAttributeInternal(LOTCONNUM, value);
  }

  /**
   * Gets the attribute value for SerialNumber, using the alias name SerialNumber.
   *
   * @return the value of SerialNumber
   */
  public String getSerialNumber() {
    return (String) getAttributeInternal(SERIALNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for SerialNumber.
   *
   * @param value value to set the SerialNumber
   */
  public void setSerialNumber(String value) {
    setAttributeInternal(SERIALNUMBER, value);
  }

  /**
   * Gets the attribute value for PackedDate, using the alias name PackedDate.
   *
   * @return the value of PackedDate
   */
  public Date getPackedDate() {
    return (Date) getAttributeInternal(PACKEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for PackedDate.
   *
   * @param value value to set the PackedDate
   */
  public void setPackedDate(Date value) {
    setAttributeInternal(PACKEDDATE, value);
  }

  /**
   * Gets the attribute value for NumExtents, using the alias name NumExtents.
   *
   * @return the value of NumExtents
   */
  public Number getNumExtents() {
    return (Number) getAttributeInternal(NUMEXTENTS);
  }

  /**
   * Sets <code>value</code> as the attribute value for NumExtents.
   *
   * @param value value to set the NumExtents
   */
  public void setNumExtents(Number value) {
    setAttributeInternal(NUMEXTENTS, value);
  }

  /**
   * Gets the attribute value for NumCounts, using the alias name NumCounts.
   *
   * @return the value of NumCounts
   */
  public Number getNumCounts() {
    return (Number) getAttributeInternal(NUMCOUNTS);
  }

  /**
   * Sets <code>value</code> as the attribute value for NumCounts.
   *
   * @param value value to set the NumCounts
   */
  public void setNumCounts(Number value) {
    setAttributeInternal(NUMCOUNTS, value);
  }

  /**
   * Gets the attribute value for UnderAudit, using the alias name UnderAudit.
   *
   * @return the value of UnderAudit
   */
  public String getUnderAudit() {
    return (String) getAttributeInternal(UNDERAUDIT);
  }

  /**
   * Sets <code>value</code> as the attribute value for UnderAudit.
   *
   * @param value value to set the UnderAudit
   */
  public void setUnderAudit(String value) {
    setAttributeInternal(UNDERAUDIT, value);
  }

  /**
   * Gets the attribute value for OldUi, using the alias name OldUi.
   *
   * @return the value of OldUi
   */
  public String getOldUi() {
    return (String) getAttributeInternal(OLDUI);
  }

  /**
   * Sets <code>value</code> as the attribute value for OldUi.
   *
   * @param value value to set the OldUi
   */
  public void setOldUi(String value) {
    setAttributeInternal(OLDUI, value);
  }

  /**
   * Gets the attribute value for NsnRemark, using the alias name NsnRemark.
   *
   * @return the value of NsnRemark
   */
  public String getNsnRemark() {
    return (String) getAttributeInternal(NSNREMARK);
  }

  /**
   * Sets <code>value</code> as the attribute value for NsnRemark.
   *
   * @param value value to set the NsnRemark
   */
  public void setNsnRemark(String value) {
    setAttributeInternal(NSNREMARK, value);
  }

  /**
   * Gets the attribute value for ExpRemark, using the alias name ExpRemark.
   *
   * @return the value of ExpRemark
   */
  public String getExpRemark() {
    return (String) getAttributeInternal(EXPREMARK);
  }

  /**
   * Sets <code>value</code> as the attribute value for ExpRemark.
   *
   * @param value value to set the ExpRemark
   */
  public void setExpRemark(String value) {
    setAttributeInternal(EXPREMARK, value);
  }

  /**
   * Gets the attribute value for OldQty, using the alias name OldQty.
   *
   * @return the value of OldQty
   */
  public Number getOldQty() {
    return (Number) getAttributeInternal(OLDQTY);
  }

  /**
   * Sets <code>value</code> as the attribute value for OldQty.
   *
   * @param value value to set the OldQty
   */
  public void setOldQty(Number value) {
    setAttributeInternal(OLDQTY, value);
  }

  /**
   * Gets the attribute value for RecallFlag, using the alias name RecallFlag.
   *
   * @return the value of RecallFlag
   */
  public String getRecallFlag() {
    return (String) getAttributeInternal(RECALLFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for RecallFlag.
   *
   * @param value value to set the RecallFlag
   */
  public void setRecallFlag(String value) {
    setAttributeInternal(RECALLFLAG, value);
  }

  /**
   * Gets the attribute value for RecallDate, using the alias name RecallDate.
   *
   * @return the value of RecallDate
   */
  public Date getRecallDate() {
    return (Date) getAttributeInternal(RECALLDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for RecallDate.
   *
   * @param value value to set the RecallDate
   */
  public void setRecallDate(Date value) {
    setAttributeInternal(RECALLDATE, value);
  }

  /**
   * getAttrInvokeAccessor: generated method. Do not modify.
   *
   * @param index   the index identifying the attribute
   * @param attrDef the attribute
   * @return the attribute value
   */
  @Override
  protected Object getAttrInvokeAccessor(int index, AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      return AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].get(this);
    }
    return super.getAttrInvokeAccessor(index, attrDef);
  }

  /**
   * setAttrInvokeAccessor: generated method. Do not modify.
   *
   * @param index   the index identifying the attribute
   * @param value   the value to assign to the attribute
   * @param attrDef the attribute
   */
  @Override
  protected void setAttrInvokeAccessor(int index, Object value, AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].put(this, value);
      return;
    }
    super.setAttrInvokeAccessor(index, value, attrDef);
  }

  /**
   * @return the associated entity LocationImpl.
   */
  public LocationImpl getLocation() {
    return (LocationImpl) getAttributeInternal(LOCATION);
  }

  /**
   * Sets <code>value</code> as the associated entity LocationImpl.
   */
  public void setLocation(LocationImpl value) {
    setAttributeInternal(LOCATION, value);
  }

  /**
   * @return the associated entity mil.stratis.model.entity.site.NiinInfoImpl.
   */
  public NiinInfoImpl getNiinInfo() {
    return (NiinInfoImpl) getAttributeInternal(NIININFO);
  }

  /**
   * Sets <code>value</code> as the associated entity mil.stratis.model.entity.site.NiinInfoImpl.
   */
  public void setNiinInfo(NiinInfoImpl value) {
    setAttributeInternal(NIININFO, value);
  }

  /**
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getInventoryItem() {
    return (RowIterator) getAttributeInternal(INVENTORYITEM);
  }

  /**
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getPicking() {
    return (RowIterator) getAttributeInternal(PICKING);
  }

  /**
   * @param niinLocId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence niinLocId) {
    return new Key(new Object[]{niinLocId});
  }
}

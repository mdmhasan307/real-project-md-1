package mil.stratis.model.entity.inv;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.RowIterator;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class InventoryImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    INVENTORY_ID {
      public Object get(InventoryImpl obj) {
        return obj.getInventoryId();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setInventoryId((DBSequence) value);
      }
    },
    INV_SERIAL_NUM {
      public Object get(InventoryImpl obj) {
        return obj.getInvSerialNum();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setInvSerialNum((String) value);
      }
    },
    INV_CUTOFF_DATE {
      public Object get(InventoryImpl obj) {
        return obj.getInvCutoffDate();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setInvCutoffDate((Date) value);
      }
    },
    REQUEST_DATE {
      public Object get(InventoryImpl obj) {
        return obj.getRequestDate();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setRequestDate((Date) value);
      }
    },
    CREATED_BY {
      public Object get(InventoryImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setCreatedBy((Number) value);
      }
    },
    CREATED_DATE {
      public Object get(InventoryImpl obj) {
        return obj.getCreatedDate();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setCreatedDate((Date) value);
      }
    },
    MODIFIED_BY {
      public Object get(InventoryImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setModifiedBy((Number) value);
      }
    },
    MODIFIED_DATE {
      public Object get(InventoryImpl obj) {
        return obj.getModifiedDate();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setModifiedDate((Date) value);
      }
    },
    START_LOCATION {
      public Object get(InventoryImpl obj) {
        return obj.getStartLocation();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setStartLocation((String) value);
      }
    },
    END_LOCATION {
      public Object get(InventoryImpl obj) {
        return obj.getEndLocation();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setEndLocation((String) value);
      }
    },
    DESCRIPTION {
      public Object get(InventoryImpl obj) {
        return obj.getDescription();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setDescription((String) value);
      }
    },
    STATUS {
      public Object get(InventoryImpl obj) {
        return obj.getStatus();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setStatus((String) value);
      }
    },
    INVENTORY_ITEM {
      public Object get(InventoryImpl obj) {
        return obj.getInventoryItem();
      }

      public void put(InventoryImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    };
    private static final int FIRST_INDEX = 0;

    public abstract Object get(InventoryImpl object);

    public abstract void put(InventoryImpl object, Object value);

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

  public static final int INVENTORYID = AttributesEnum.INVENTORY_ID.index();
  public static final int INVSERIALNUM = AttributesEnum.INV_SERIAL_NUM.index();
  public static final int INVCUTOFFDATE = AttributesEnum.INV_CUTOFF_DATE.index();
  public static final int REQUESTDATE = AttributesEnum.REQUEST_DATE.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int CREATEDDATE = AttributesEnum.CREATED_DATE.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int MODIFIEDDATE = AttributesEnum.MODIFIED_DATE.index();
  public static final int STARTLOCATION = AttributesEnum.START_LOCATION.index();
  public static final int ENDLOCATION = AttributesEnum.END_LOCATION.index();
  public static final int DESCRIPTION = AttributesEnum.DESCRIPTION.index();
  public static final int STATUS = AttributesEnum.STATUS.index();
  public static final int INVENTORYITEM = AttributesEnum.INVENTORY_ITEM.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.inv.Inventory");
  }

  /**
   * Gets the attribute value for InventoryId, using the alias name InventoryId.
   *
   * @return the value of InventoryId
   */
  public DBSequence getInventoryId() {
    return (DBSequence) getAttributeInternal(INVENTORYID);
  }

  /**
   * Sets <code>value</code> as the attribute value for InventoryId.
   *
   * @param value value to set the InventoryId
   */
  public void setInventoryId(DBSequence value) {
    setAttributeInternal(INVENTORYID, value);
  }

  /**
   * Gets the attribute value for InvSerialNum, using the alias name InvSerialNum.
   *
   * @return the value of InvSerialNum
   */
  public String getInvSerialNum() {
    return (String) getAttributeInternal(INVSERIALNUM);
  }

  /**
   * Sets <code>value</code> as the attribute value for InvSerialNum.
   *
   * @param value value to set the InvSerialNum
   */
  public void setInvSerialNum(String value) {
    setAttributeInternal(INVSERIALNUM, value);
  }

  /**
   * Gets the attribute value for InvCutoffDate, using the alias name InvCutoffDate.
   *
   * @return the value of InvCutoffDate
   */
  public Date getInvCutoffDate() {
    return (Date) getAttributeInternal(INVCUTOFFDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for InvCutoffDate.
   *
   * @param value value to set the InvCutoffDate
   */
  public void setInvCutoffDate(Date value) {
    setAttributeInternal(INVCUTOFFDATE, value);
  }

  /**
   * Gets the attribute value for RequestDate, using the alias name RequestDate.
   *
   * @return the value of RequestDate
   */
  public Date getRequestDate() {
    return (Date) getAttributeInternal(REQUESTDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for RequestDate.
   *
   * @param value value to set the RequestDate
   */
  public void setRequestDate(Date value) {
    setAttributeInternal(REQUESTDATE, value);
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
   * Gets the attribute value for StartLocation, using the alias name StartLocation.
   *
   * @return the value of StartLocation
   */
  public String getStartLocation() {
    return (String) getAttributeInternal(STARTLOCATION);
  }

  /**
   * Sets <code>value</code> as the attribute value for StartLocation.
   *
   * @param value value to set the StartLocation
   */
  public void setStartLocation(String value) {
    setAttributeInternal(STARTLOCATION, value);
  }

  /**
   * Gets the attribute value for EndLocation, using the alias name EndLocation.
   *
   * @return the value of EndLocation
   */
  public String getEndLocation() {
    return (String) getAttributeInternal(ENDLOCATION);
  }

  /**
   * Sets <code>value</code> as the attribute value for EndLocation.
   *
   * @param value value to set the EndLocation
   */
  public void setEndLocation(String value) {
    setAttributeInternal(ENDLOCATION, value);
  }

  /**
   * Gets the attribute value for Description, using the alias name Description.
   *
   * @return the value of Description
   */
  public String getDescription() {
    return (String) getAttributeInternal(DESCRIPTION);
  }

  /**
   * Sets <code>value</code> as the attribute value for Description.
   *
   * @param value value to set the Description
   */
  public void setDescription(String value) {
    setAttributeInternal(DESCRIPTION, value);
  }

  /**
   * Gets the attribute value for Status, using the alias name Status.
   *
   * @return the value of Status
   */
  public String getStatus() {
    return (String) getAttributeInternal(STATUS);
  }

  /**
   * Sets <code>value</code> as the attribute value for Status.
   *
   * @param value value to set the Status
   */
  public void setStatus(String value) {
    setAttributeInternal(STATUS, value);
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
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getInventoryItem() {
    return (RowIterator) getAttributeInternal(INVENTORYITEM);
  }

  /**
   * @param inventoryId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence inventoryId) {
    return new Key(new Object[]{inventoryId});
  }
}

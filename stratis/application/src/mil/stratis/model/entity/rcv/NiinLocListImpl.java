package mil.stratis.model.entity.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.domain.Number;
import oracle.jbo.domain.Timestamp;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class NiinLocListImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    WAREHOUSE_ID {
      public Object get(NiinLocListImpl obj) {
        return obj.getWarehouseId();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setWarehouseId((Integer) value);
      }
    },
    WAC_ID {
      public Object get(NiinLocListImpl obj) {
        return obj.getWacId();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setWacId((Number) value);
      }
    },
    LOCATION_ID {
      public Object get(NiinLocListImpl obj) {
        return obj.getLocationId();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setLocationId((Number) value);
      }
    },
    BUILDING {
      public Object get(NiinLocListImpl obj) {
        return obj.getBuilding();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setBuilding((String) value);
      }
    },
    LOCATION_HEADER_BIN_ID {
      public Object get(NiinLocListImpl obj) {
        return obj.getLocationHeaderBinId();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setLocationHeaderBinId((Number) value);
      }
    },
    LOC_CLASSIFICATION_ID {
      public Object get(NiinLocListImpl obj) {
        return obj.getLocClassificationId();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setLocClassificationId((Number) value);
      }
    },
    LOCATION_LABEL {
      public Object get(NiinLocListImpl obj) {
        return obj.getLocationLabel();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setLocationLabel((String) value);
      }
    },
    AISLE {
      public Object get(NiinLocListImpl obj) {
        return obj.getAisle();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setAisle((String) value);
      }
    },
    SIDE {
      public Object get(NiinLocListImpl obj) {
        return obj.getSide();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setSide((String) value);
      }
    },
    BAY {
      public Object get(NiinLocListImpl obj) {
        return obj.getBay();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setBay((String) value);
      }
    },
    LOC_LEVEL {
      public Object get(NiinLocListImpl obj) {
        return obj.getLocLevel();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setLocLevel((String) value);
      }
    },
    SLOT {
      public Object get(NiinLocListImpl obj) {
        return obj.getSlot();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setSlot((String) value);
      }
    },
    NIIN_ID {
      public Object get(NiinLocListImpl obj) {
        return obj.getNiinId();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setNiinId((Integer) value);
      }
    },
    EXPIRATION_DATE {
      public Object get(NiinLocListImpl obj) {
        return obj.getExpirationDate();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setExpirationDate((Timestamp) value);
      }
    },
    AVAILABILITY_FLAG {
      public Object get(NiinLocListImpl obj) {
        return obj.getAvailabilityFlag();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setAvailabilityFlag((String) value);
      }
    },
    NIIN_LOC_ID {
      public Object get(NiinLocListImpl obj) {
        return obj.getNiinLocId();
      }

      public void put(NiinLocListImpl obj, Object value) {
        obj.setNiinLocId((Number) value);
      }
    };
    private static final int FIRST_INDEX = 0;

    public abstract Object get(NiinLocListImpl object);

    public abstract void put(NiinLocListImpl object, Object value);

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

  public static final int WAREHOUSEID = AttributesEnum.WAREHOUSE_ID.index();
  public static final int WACID = AttributesEnum.WAC_ID.index();
  public static final int LOCATIONID = AttributesEnum.LOCATION_ID.index();
  public static final int BUILDING = AttributesEnum.BUILDING.index();
  public static final int LOCATIONHEADERBINID = AttributesEnum.LOCATION_HEADER_BIN_ID.index();
  public static final int LOCCLASSIFICATIONID = AttributesEnum.LOC_CLASSIFICATION_ID.index();
  public static final int LOCATIONLABEL = AttributesEnum.LOCATION_LABEL.index();
  public static final int AISLE = AttributesEnum.AISLE.index();
  public static final int SIDE = AttributesEnum.SIDE.index();
  public static final int BAY = AttributesEnum.BAY.index();
  public static final int LOCLEVEL = AttributesEnum.LOC_LEVEL.index();
  public static final int SLOT = AttributesEnum.SLOT.index();
  public static final int NIINID = AttributesEnum.NIIN_ID.index();
  public static final int EXPIRATIONDATE = AttributesEnum.EXPIRATION_DATE.index();
  public static final int AVAILABILITYFLAG = AttributesEnum.AVAILABILITY_FLAG.index();
  public static final int NIINLOCID = AttributesEnum.NIIN_LOC_ID.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.rcv.NiinLocList");
  }

  /**
   * Gets the attribute value for WarehouseId, using the alias name WarehouseId.
   *
   * @return the value of WarehouseId
   */
  public Integer getWarehouseId() {
    return (Integer) getAttributeInternal(WAREHOUSEID);
  }

  /**
   * Sets <code>value</code> as the attribute value for WarehouseId.
   *
   * @param value value to set the WarehouseId
   */
  public void setWarehouseId(Integer value) {
    setAttributeInternal(WAREHOUSEID, value);
  }

  /**
   * Gets the attribute value for WacId, using the alias name WacId.
   *
   * @return the value of WacId
   */
  public Number getWacId() {
    return (Number) getAttributeInternal(WACID);
  }

  /**
   * Sets <code>value</code> as the attribute value for WacId.
   *
   * @param value value to set the WacId
   */
  public void setWacId(Number value) {
    setAttributeInternal(WACID, value);
  }

  /**
   * Gets the attribute value for LocationId, using the alias name LocationId.
   *
   * @return the value of LocationId
   */
  public oracle.jbo.domain.Number getLocationId() {
    return (oracle.jbo.domain.Number) getAttributeInternal(LOCATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocationId.
   *
   * @param value value to set the LocationId
   */
  public void setLocationId(oracle.jbo.domain.Number value) {
    setAttributeInternal(LOCATIONID, value);
  }

  /**
   * Gets the attribute value for Building, using the alias name Building.
   *
   * @return the value of Building
   */
  public String getBuilding() {
    return (String) getAttributeInternal(BUILDING);
  }

  /**
   * Sets <code>value</code> as the attribute value for Building.
   *
   * @param value value to set the Building
   */
  public void setBuilding(String value) {
    setAttributeInternal(BUILDING, value);
  }

  /**
   * Gets the attribute value for LocationHeaderBinId, using the alias name LocationHeaderBinId.
   *
   * @return the value of LocationHeaderBinId
   */
  public Number getLocationHeaderBinId() {
    return (Number) getAttributeInternal(LOCATIONHEADERBINID);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocationHeaderBinId.
   *
   * @param value value to set the LocationHeaderBinId
   */
  public void setLocationHeaderBinId(Number value) {
    setAttributeInternal(LOCATIONHEADERBINID, value);
  }

  /**
   * Gets the attribute value for LocClassificationId, using the alias name LocClassificationId.
   *
   * @return the value of LocClassificationId
   */
  public Number getLocClassificationId() {
    return (Number) getAttributeInternal(LOCCLASSIFICATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocClassificationId.
   *
   * @param value value to set the LocClassificationId
   */
  public void setLocClassificationId(Number value) {
    setAttributeInternal(LOCCLASSIFICATIONID, value);
  }

  /**
   * Gets the attribute value for LocationLabel, using the alias name LocationLabel.
   *
   * @return the value of LocationLabel
   */
  public String getLocationLabel() {
    return (String) getAttributeInternal(LOCATIONLABEL);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocationLabel.
   *
   * @param value value to set the LocationLabel
   */
  public void setLocationLabel(String value) {
    setAttributeInternal(LOCATIONLABEL, value);
  }

  /**
   * Gets the attribute value for Aisle, using the alias name Aisle.
   *
   * @return the value of Aisle
   */
  public String getAisle() {
    return (String) getAttributeInternal(AISLE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Aisle.
   *
   * @param value value to set the Aisle
   */
  public void setAisle(String value) {
    setAttributeInternal(AISLE, value);
  }

  /**
   * Gets the attribute value for Side, using the alias name Side.
   *
   * @return the value of Side
   */
  public String getSide() {
    return (String) getAttributeInternal(SIDE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Side.
   *
   * @param value value to set the Side
   */
  public void setSide(String value) {
    setAttributeInternal(SIDE, value);
  }

  /**
   * Gets the attribute value for Bay, using the alias name Bay.
   *
   * @return the value of Bay
   */
  public String getBay() {
    return (String) getAttributeInternal(BAY);
  }

  /**
   * Sets <code>value</code> as the attribute value for Bay.
   *
   * @param value value to set the Bay
   */
  public void setBay(String value) {
    setAttributeInternal(BAY, value);
  }

  /**
   * Gets the attribute value for LocLevel, using the alias name LocLevel.
   *
   * @return the value of LocLevel
   */
  public String getLocLevel() {
    return (String) getAttributeInternal(LOCLEVEL);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocLevel.
   *
   * @param value value to set the LocLevel
   */
  public void setLocLevel(String value) {
    setAttributeInternal(LOCLEVEL, value);
  }

  /**
   * Gets the attribute value for Slot, using the alias name Slot.
   *
   * @return the value of Slot
   */
  public String getSlot() {
    return (String) getAttributeInternal(SLOT);
  }

  /**
   * Sets <code>value</code> as the attribute value for Slot.
   *
   * @param value value to set the Slot
   */
  public void setSlot(String value) {
    setAttributeInternal(SLOT, value);
  }

  /**
   * Gets the attribute value for NiinId, using the alias name NiinId.
   *
   * @return the value of NiinId
   */
  public Integer getNiinId() {
    return (Integer) getAttributeInternal(NIINID);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinId.
   *
   * @param value value to set the NiinId
   */
  public void setNiinId(Integer value) {
    setAttributeInternal(NIINID, value);
  }

  /**
   * Gets the attribute value for ExpirationDate, using the alias name ExpirationDate.
   *
   * @return the value of ExpirationDate
   */
  public Timestamp getExpirationDate() {
    return (Timestamp) getAttributeInternal(EXPIRATIONDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ExpirationDate.
   *
   * @param value value to set the ExpirationDate
   */
  public void setExpirationDate(Timestamp value) {
    setAttributeInternal(EXPIRATIONDATE, value);
  }

  /**
   * Gets the attribute value for AvailabilityFlag, using the alias name AvailabilityFlag.
   *
   * @return the value of AvailabilityFlag
   */
  public String getAvailabilityFlag() {
    return (String) getAttributeInternal(AVAILABILITYFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for AvailabilityFlag.
   *
   * @param value value to set the AvailabilityFlag
   */
  public void setAvailabilityFlag(String value) {
    setAttributeInternal(AVAILABILITYFLAG, value);
  }

  /**
   * Gets the attribute value for NiinLocId, using the alias name NiinLocId.
   *
   * @return the value of NiinLocId
   */
  public Number getNiinLocId() {
    return (Number) getAttributeInternal(NIINLOCID);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinLocId.
   *
   * @param value value to set the NiinLocId
   */
  public void setNiinLocId(Number value) {
    setAttributeInternal(NIINLOCID, value);
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
   * @param niinLocId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(Number niinLocId) {
    return new Key(new Object[]{niinLocId});
  }
}

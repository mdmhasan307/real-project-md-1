package mil.stratis.model.entity.loc;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.RowIterator;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Number;
import oracle.jbo.domain.Timestamp;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class LocationImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    LOCATION_ID {
      public Object get(LocationImpl obj) {
        return obj.getLocationId();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setLocationId((DBSequence) value);
      }
    },
    AISLE {
      public Object get(LocationImpl obj) {
        return obj.getAisle();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setAisle((String) value);
      }
    },
    SIDE {
      public Object get(LocationImpl obj) {
        return obj.getSide();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setSide((String) value);
      }
    },
    BAY {
      public Object get(LocationImpl obj) {
        return obj.getBay();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setBay((String) value);
      }
    },
    LOC_LEVEL {
      public Object get(LocationImpl obj) {
        return obj.getLocLevel();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setLocLevel((String) value);
      }
    },
    AVAILABILITY_FLAG {
      public Object get(LocationImpl obj) {
        return obj.getAvailabilityFlag();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setAvailabilityFlag((String) value);
      }
    },
    LOCATION_LABEL {
      public Object get(LocationImpl obj) {
        return obj.getLocationLabel();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setLocationLabel((String) value);
      }
    },
    SLOT {
      public Object get(LocationImpl obj) {
        return obj.getSlot();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setSlot((String) value);
      }
    },
    WAC_ID {
      public Object get(LocationImpl obj) {
        return obj.getWacId();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setWacId((Number) value);
      }
    },
    LOCATION_HEADER_BIN_ID {
      public Object get(LocationImpl obj) {
        return obj.getLocationHeaderBinId();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setLocationHeaderBinId((Number) value);
      }
    },
    MECHANIZED_FLAG {
      public Object get(LocationImpl obj) {
        return obj.getMechanizedFlag();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setMechanizedFlag((String) value);
      }
    },
    LOC_CLASSIFICATION_ID {
      public Object get(LocationImpl obj) {
        return obj.getLocClassificationId();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setLocClassificationId((Number) value);
      }
    },
    DIVIDER_INDEX {
      public Object get(LocationImpl obj) {
        return obj.getDividerIndex();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setDividerIndex((Number) value);
      }
    },
    CUBE {
      public Object get(LocationImpl obj) {
        return obj.getCube();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setCube((Number) value);
      }
    },
    WEIGHT {
      public Object get(LocationImpl obj) {
        return obj.getWeight();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setWeight((Number) value);
      }
    },
    LAST_STOW_DATE {
      public Object get(LocationImpl obj) {
        return obj.getLastStowDate();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setLastStowDate((Timestamp) value);
      }
    },
    CREATED_BY {
      public Object get(LocationImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setCreatedBy((Integer) value);
      }
    },
    CREATED_DATE {
      public Object get(LocationImpl obj) {
        return obj.getCreatedDate();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setCreatedDate((Timestamp) value);
      }
    },
    MODIFIED_BY {
      public Object get(LocationImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setModifiedBy((Integer) value);
      }
    },
    MODIFIED_DATE {
      public Object get(LocationImpl obj) {
        return obj.getModifiedDate();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setModifiedDate((Timestamp) value);
      }
    },
    LOCATION_CLASSIFICATION {
      public Object get(LocationImpl obj) {
        return obj.getLocationClassification();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setLocationClassification((EntityImpl) value);
      }
    },
    NIIN_LOCATION {
      public Object get(LocationImpl obj) {
        return obj.getNiinLocation();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    },
    STOW {
      public Object get(LocationImpl obj) {
        return obj.getStow();
      }

      public void put(LocationImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    };
    private static final int FIRST_INDEX = 0;

    public abstract Object get(LocationImpl object);

    public abstract void put(LocationImpl object, Object value);

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

  public static final int LOCATIONID = AttributesEnum.LOCATION_ID.index();
  public static final int AISLE = AttributesEnum.AISLE.index();
  public static final int SIDE = AttributesEnum.SIDE.index();
  public static final int BAY = AttributesEnum.BAY.index();
  public static final int LOCLEVEL = AttributesEnum.LOC_LEVEL.index();
  public static final int AVAILABILITYFLAG = AttributesEnum.AVAILABILITY_FLAG.index();
  public static final int LOCATIONLABEL = AttributesEnum.LOCATION_LABEL.index();
  public static final int SLOT = AttributesEnum.SLOT.index();
  public static final int WACID = AttributesEnum.WAC_ID.index();
  public static final int LOCATIONHEADERBINID = AttributesEnum.LOCATION_HEADER_BIN_ID.index();
  public static final int MECHANIZEDFLAG = AttributesEnum.MECHANIZED_FLAG.index();
  public static final int LOCCLASSIFICATIONID = AttributesEnum.LOC_CLASSIFICATION_ID.index();
  public static final int DIVIDERINDEX = AttributesEnum.DIVIDER_INDEX.index();
  public static final int CUBE = AttributesEnum.CUBE.index();
  public static final int WEIGHT = AttributesEnum.WEIGHT.index();
  public static final int LASTSTOWDATE = AttributesEnum.LAST_STOW_DATE.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int CREATEDDATE = AttributesEnum.CREATED_DATE.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int MODIFIEDDATE = AttributesEnum.MODIFIED_DATE.index();
  public static final int LOCATIONCLASSIFICATION = AttributesEnum.LOCATION_CLASSIFICATION.index();
  public static final int NIINLOCATION = AttributesEnum.NIIN_LOCATION.index();
  public static final int STOW = AttributesEnum.STOW.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.loc.Location");
  }

  /**
   * Gets the attribute value for LocationId, using the alias name LocationId.
   *
   * @return the value of LocationId
   */
  public DBSequence getLocationId() {
    return (DBSequence) getAttributeInternal(LOCATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for LocationId.
   *
   * @param value value to set the LocationId
   */
  public void setLocationId(DBSequence value) {
    setAttributeInternal(LOCATIONID, value);
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
   * Gets the attribute value for WacId, using the alias name WacId.
   *
   * @return the value of WacId
   */
  public oracle.jbo.domain.Number getWacId() {
    return (oracle.jbo.domain.Number) getAttributeInternal(WACID);
  }

  /**
   * Sets <code>value</code> as the attribute value for WacId.
   *
   * @param value value to set the WacId
   */
  public void setWacId(oracle.jbo.domain.Number value) {
    setAttributeInternal(WACID, value);
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
   * Gets the attribute value for MechanizedFlag, using the alias name MechanizedFlag.
   *
   * @return the value of MechanizedFlag
   */
  public String getMechanizedFlag() {
    return (String) getAttributeInternal(MECHANIZEDFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for MechanizedFlag.
   *
   * @param value value to set the MechanizedFlag
   */
  public void setMechanizedFlag(String value) {
    setAttributeInternal(MECHANIZEDFLAG, value);
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
   * Gets the attribute value for DividerIndex, using the alias name DividerIndex.
   *
   * @return the value of DividerIndex
   */
  public Number getDividerIndex() {
    return (Number) getAttributeInternal(DIVIDERINDEX);
  }

  /**
   * Sets <code>value</code> as the attribute value for DividerIndex.
   *
   * @param value value to set the DividerIndex
   */
  public void setDividerIndex(Number value) {
    setAttributeInternal(DIVIDERINDEX, value);
  }

  /**
   * Gets the attribute value for Cube, using the alias name Cube.
   *
   * @return the value of Cube
   */
  public Number getCube() {
    return (Number) getAttributeInternal(CUBE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Cube.
   *
   * @param value value to set the Cube
   */
  public void setCube(Number value) {
    setAttributeInternal(CUBE, value);
  }

  /**
   * Gets the attribute value for Weight, using the alias name Weight.
   *
   * @return the value of Weight
   */
  public Number getWeight() {
    return (Number) getAttributeInternal(WEIGHT);
  }

  /**
   * Sets <code>value</code> as the attribute value for Weight.
   *
   * @param value value to set the Weight
   */
  public void setWeight(Number value) {
    setAttributeInternal(WEIGHT, value);
  }

  /**
   * Gets the attribute value for LastStowDate, using the alias name LastStowDate.
   *
   * @return the value of LastStowDate
   */
  public Timestamp getLastStowDate() {
    return (Timestamp) getAttributeInternal(LASTSTOWDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for LastStowDate.
   *
   * @param value value to set the LastStowDate
   */
  public void setLastStowDate(Timestamp value) {
    setAttributeInternal(LASTSTOWDATE, value);
  }

  /**
   * Gets the attribute value for CreatedBy, using the alias name CreatedBy.
   *
   * @return the value of CreatedBy
   */
  public Integer getCreatedBy() {
    return (Integer) getAttributeInternal(CREATEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedBy.
   *
   * @param value value to set the CreatedBy
   */
  public void setCreatedBy(Integer value) {
    setAttributeInternal(CREATEDBY, value);
  }

  /**
   * Gets the attribute value for CreatedDate, using the alias name CreatedDate.
   *
   * @return the value of CreatedDate
   */
  public Timestamp getCreatedDate() {
    return (Timestamp) getAttributeInternal(CREATEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedDate.
   *
   * @param value value to set the CreatedDate
   */
  public void setCreatedDate(Timestamp value) {
    setAttributeInternal(CREATEDDATE, value);
  }

  /**
   * Gets the attribute value for ModifiedBy, using the alias name ModifiedBy.
   *
   * @return the value of ModifiedBy
   */
  public Integer getModifiedBy() {
    return (Integer) getAttributeInternal(MODIFIEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedBy.
   *
   * @param value value to set the ModifiedBy
   */
  public void setModifiedBy(Integer value) {
    setAttributeInternal(MODIFIEDBY, value);
  }

  /**
   * Gets the attribute value for ModifiedDate, using the alias name ModifiedDate.
   *
   * @return the value of ModifiedDate
   */
  public Timestamp getModifiedDate() {
    return (Timestamp) getAttributeInternal(MODIFIEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedDate.
   *
   * @param value value to set the ModifiedDate
   */
  public void setModifiedDate(Timestamp value) {
    setAttributeInternal(MODIFIEDDATE, value);
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
   * @return the associated entity oracle.jbo.server.EntityImpl.
   */
  public EntityImpl getLocationClassification() {
    return (EntityImpl) getAttributeInternal(LOCATIONCLASSIFICATION);
  }

  /**
   * Sets <code>value</code> as the associated entity oracle.jbo.server.EntityImpl.
   */
  public void setLocationClassification(EntityImpl value) {
    setAttributeInternal(LOCATIONCLASSIFICATION, value);
  }

  /**
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getNiinLocation() {
    return (RowIterator) getAttributeInternal(NIINLOCATION);
  }

  /**
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getStow() {
    return (RowIterator) getAttributeInternal(STOW);
  }

  /**
   * @param locationId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence locationId) {
    return new Key(new Object[]{locationId});
  }
}

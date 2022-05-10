package mil.stratis.model.entity.ship;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class ShippingManifestImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    SHIPPING_MANIFEST_ID {
      public Object get(ShippingManifestImpl obj) {
        return obj.getShippingManifestId();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setShippingManifestId((DBSequence) value);
      }
    },
    CUSTOMER_ID {
      public Object get(ShippingManifestImpl obj) {
        return obj.getCustomerId();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setCustomerId((Number) value);
      }
    },
    LEAD_TCN {
      public Object get(ShippingManifestImpl obj) {
        return obj.getLeadTcn();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setLeadTcn((String) value);
      }
    },
    MANIFEST {
      public Object get(ShippingManifestImpl obj) {
        return obj.getManifest();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setManifest((String) value);
      }
    },
    FLOOR_LOCATION_ID {
      public Object get(ShippingManifestImpl obj) {
        return obj.getFloorLocationId();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setFloorLocationId((Number) value);
      }
    },
    MANIFEST_DATE {
      public Object get(ShippingManifestImpl obj) {
        return obj.getManifestDate();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setManifestDate((Date) value);
      }
    },
    PICKED_UP_FLAG {
      public Object get(ShippingManifestImpl obj) {
        return obj.getPickedUpFlag();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setPickedUpFlag((String) value);
      }
    },
    DELIVERED_FLAG {
      public Object get(ShippingManifestImpl obj) {
        return obj.getDeliveredFlag();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setDeliveredFlag((String) value);
      }
    },
    PICKED_UP_DATE {
      public Object get(ShippingManifestImpl obj) {
        return obj.getPickedUpDate();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setPickedUpDate((Date) value);
      }
    },
    DELIVERED_DATE {
      public Object get(ShippingManifestImpl obj) {
        return obj.getDeliveredDate();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setDeliveredDate((Date) value);
      }
    },
    CREATED_DATE {
      public Object get(ShippingManifestImpl obj) {
        return obj.getCreatedDate();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setCreatedDate((Date) value);
      }
    },
    CREATED_BY {
      public Object get(ShippingManifestImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setCreatedBy((Number) value);
      }
    },
    MODIFIED_DATE {
      public Object get(ShippingManifestImpl obj) {
        return obj.getModifiedDate();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setModifiedDate((Date) value);
      }
    },
    MODIFIED_BY {
      public Object get(ShippingManifestImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setModifiedBy((Number) value);
      }
    },
    MANIFEST_PRINT_DATE {
      public Object get(ShippingManifestImpl obj) {
        return obj.getManifestPrintDate();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setManifestPrintDate((Date) value);
      }
    },
    DRIVER {
      public Object get(ShippingManifestImpl obj) {
        return obj.getDriver();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setDriver((String) value);
      }
    },
    MODE_OF_SHIPMENT {
      public Object get(ShippingManifestImpl obj) {
        return obj.getModeOfShipment();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setModeOfShipment((String) value);
      }
    },
    EQUIPMENT_NUMBER {
      public Object get(ShippingManifestImpl obj) {
        return obj.getEquipmentNumber();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setEquipmentNumber((Number) value);
      }
    },
    MANIFESTED_BY {
      public Object get(ShippingManifestImpl obj) {
        return obj.getManifestedBy();
      }

      public void put(ShippingManifestImpl obj, Object value) {
        obj.setManifestedBy((Number) value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(ShippingManifestImpl object);

    public abstract void put(ShippingManifestImpl object, Object value);

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

  public static final int SHIPPINGMANIFESTID = AttributesEnum.SHIPPING_MANIFEST_ID.index();
  public static final int CUSTOMERID = AttributesEnum.CUSTOMER_ID.index();
  public static final int LEADTCN = AttributesEnum.LEAD_TCN.index();
  public static final int MANIFEST = AttributesEnum.MANIFEST.index();
  public static final int FLOORLOCATIONID = AttributesEnum.FLOOR_LOCATION_ID.index();
  public static final int MANIFESTDATE = AttributesEnum.MANIFEST_DATE.index();
  public static final int PICKEDUPFLAG = AttributesEnum.PICKED_UP_FLAG.index();
  public static final int DELIVEREDFLAG = AttributesEnum.DELIVERED_FLAG.index();
  public static final int PICKEDUPDATE = AttributesEnum.PICKED_UP_DATE.index();
  public static final int DELIVEREDDATE = AttributesEnum.DELIVERED_DATE.index();
  public static final int CREATEDDATE = AttributesEnum.CREATED_DATE.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int MODIFIEDDATE = AttributesEnum.MODIFIED_DATE.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int MANIFESTPRINTDATE = AttributesEnum.MANIFEST_PRINT_DATE.index();
  public static final int DRIVER = AttributesEnum.DRIVER.index();
  public static final int MODEOFSHIPMENT = AttributesEnum.MODE_OF_SHIPMENT.index();
  public static final int EQUIPMENTNUMBER = AttributesEnum.EQUIPMENT_NUMBER.index();
  public static final int MANIFESTEDBY = AttributesEnum.MANIFESTED_BY.index();

  /**
   * Gets the attribute value for ShippingManifestId, using the alias name ShippingManifestId.
   *
   * @return the value of ShippingManifestId
   */
  public DBSequence getShippingManifestId() {
    return (DBSequence) getAttributeInternal(SHIPPINGMANIFESTID);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShippingManifestId.
   *
   * @param value value to set the ShippingManifestId
   */
  public void setShippingManifestId(DBSequence value) {
    setAttributeInternal(SHIPPINGMANIFESTID, value);
  }

  /**
   * Gets the attribute value for CustomerId, using the alias name CustomerId.
   *
   * @return the value of CustomerId
   */
  public Number getCustomerId() {
    return (Number) getAttributeInternal(CUSTOMERID);
  }

  /**
   * Sets <code>value</code> as the attribute value for CustomerId.
   *
   * @param value value to set the CustomerId
   */
  public void setCustomerId(Number value) {
    setAttributeInternal(CUSTOMERID, value);
  }

  /**
   * Gets the attribute value for LeadTcn, using the alias name LeadTcn.
   *
   * @return the value of LeadTcn
   */
  public String getLeadTcn() {
    return (String) getAttributeInternal(LEADTCN);
  }

  /**
   * Sets <code>value</code> as the attribute value for LeadTcn.
   *
   * @param value value to set the LeadTcn
   */
  public void setLeadTcn(String value) {
    setAttributeInternal(LEADTCN, value);
  }

  /**
   * Gets the attribute value for Manifest, using the alias name Manifest.
   *
   * @return the value of Manifest
   */
  public String getManifest() {
    return (String) getAttributeInternal(MANIFEST);
  }

  /**
   * Sets <code>value</code> as the attribute value for Manifest.
   *
   * @param value value to set the Manifest
   */
  public void setManifest(String value) {
    setAttributeInternal(MANIFEST, value);
  }

  /**
   * Gets the attribute value for FloorLocationId, using the alias name FloorLocationId.
   *
   * @return the value of FloorLocationId
   */
  public Number getFloorLocationId() {
    return (Number) getAttributeInternal(FLOORLOCATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for FloorLocationId.
   *
   * @param value value to set the FloorLocationId
   */
  public void setFloorLocationId(Number value) {
    setAttributeInternal(FLOORLOCATIONID, value);
  }

  /**
   * Gets the attribute value for ManifestDate, using the alias name ManifestDate.
   *
   * @return the value of ManifestDate
   */
  public Date getManifestDate() {
    return (Date) getAttributeInternal(MANIFESTDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ManifestDate.
   *
   * @param value value to set the ManifestDate
   */
  public void setManifestDate(Date value) {
    setAttributeInternal(MANIFESTDATE, value);
  }

  /**
   * Gets the attribute value for PickedUpFlag, using the alias name PickedUpFlag.
   *
   * @return the value of PickedUpFlag
   */
  public String getPickedUpFlag() {
    return (String) getAttributeInternal(PICKEDUPFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for PickedUpFlag.
   *
   * @param value value to set the PickedUpFlag
   */
  public void setPickedUpFlag(String value) {
    setAttributeInternal(PICKEDUPFLAG, value);
  }

  /**
   * Gets the attribute value for DeliveredFlag, using the alias name DeliveredFlag.
   *
   * @return the value of DeliveredFlag
   */
  public String getDeliveredFlag() {
    return (String) getAttributeInternal(DELIVEREDFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for DeliveredFlag.
   *
   * @param value value to set the DeliveredFlag
   */
  public void setDeliveredFlag(String value) {
    setAttributeInternal(DELIVEREDFLAG, value);
  }

  /**
   * Gets the attribute value for PickedUpDate, using the alias name PickedUpDate.
   *
   * @return the value of PickedUpDate
   */
  public Date getPickedUpDate() {
    return (Date) getAttributeInternal(PICKEDUPDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for PickedUpDate.
   *
   * @param value value to set the PickedUpDate
   */
  public void setPickedUpDate(Date value) {
    setAttributeInternal(PICKEDUPDATE, value);
  }

  /**
   * Gets the attribute value for DeliveredDate, using the alias name DeliveredDate.
   *
   * @return the value of DeliveredDate
   */
  public Date getDeliveredDate() {
    return (Date) getAttributeInternal(DELIVEREDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for DeliveredDate.
   *
   * @param value value to set the DeliveredDate
   */
  public void setDeliveredDate(Date value) {
    setAttributeInternal(DELIVEREDDATE, value);
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
   * Gets the attribute value for ManifestPrintDate, using the alias name ManifestPrintDate.
   *
   * @return the value of ManifestPrintDate
   */
  public Date getManifestPrintDate() {
    return (Date) getAttributeInternal(MANIFESTPRINTDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ManifestPrintDate.
   *
   * @param value value to set the ManifestPrintDate
   */
  public void setManifestPrintDate(Date value) {
    setAttributeInternal(MANIFESTPRINTDATE, value);
  }

  /**
   * Gets the attribute value for Driver, using the alias name Driver.
   *
   * @return the value of Driver
   */
  public String getDriver() {
    return (String) getAttributeInternal(DRIVER);
  }

  /**
   * Sets <code>value</code> as the attribute value for Driver.
   *
   * @param value value to set the Driver
   */
  public void setDriver(String value) {
    setAttributeInternal(DRIVER, value);
  }

  /**
   * Gets the attribute value for ModeOfShipment, using the alias name ModeOfShipment.
   *
   * @return the value of ModeOfShipment
   */
  public String getModeOfShipment() {
    return (String) getAttributeInternal(MODEOFSHIPMENT);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModeOfShipment.
   *
   * @param value value to set the ModeOfShipment
   */
  public void setModeOfShipment(String value) {
    setAttributeInternal(MODEOFSHIPMENT, value);
  }

  /**
   * Gets the attribute value for EquipmentNumber, using the alias name EquipmentNumber.
   *
   * @return the value of EquipmentNumber
   */
  public Number getEquipmentNumber() {
    return (Number) getAttributeInternal(EQUIPMENTNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for EquipmentNumber.
   *
   * @param value value to set the EquipmentNumber
   */
  public void setEquipmentNumber(Number value) {
    setAttributeInternal(EQUIPMENTNUMBER, value);
  }

  /**
   * Gets the attribute value for ManifestedBy, using the alias name ManifestedBy.
   *
   * @return the value of ManifestedBy
   */
  public Number getManifestedBy() {
    return (Number) getAttributeInternal(MANIFESTEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for ManifestedBy.
   *
   * @param value value to set the ManifestedBy
   */
  public void setManifestedBy(Number value) {
    setAttributeInternal(MANIFESTEDBY, value);
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
   * @param shippingManifestId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence shippingManifestId) {
    return new Key(new Object[]{shippingManifestId});
  }

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.ship.ShippingManifest");
  }
}

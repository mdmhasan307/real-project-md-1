package mil.stratis.model.entity.site;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.RowIterator;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class CustomerImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    ADDRESS_1 {
      public Object get(CustomerImpl obj) {
        return obj.getAddress1();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setAddress1((String) value);
      }
    },
    CITY {
      public Object get(CustomerImpl obj) {
        return obj.getCity();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setCity((String) value);
      }
    },
    STATE {
      public Object get(CustomerImpl obj) {
        return obj.getState();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setState((String) value);
      }
    },
    ZIP_CODE {
      public Object get(CustomerImpl obj) {
        return obj.getZipCode();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setZipCode((String) value);
      }
    },
    AAC {
      public Object get(CustomerImpl obj) {
        return obj.getAac();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setAac((String) value);
      }
    },
    CUSTOMER_ID {
      public Object get(CustomerImpl obj) {
        return obj.getCustomerId();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setCustomerId((DBSequence) value);
      }
    },
    ADDRESS_2 {
      public Object get(CustomerImpl obj) {
        return obj.getAddress2();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setAddress2((String) value);
      }
    },
    ROUTE_ID {
      public Object get(CustomerImpl obj) {
        return obj.getRouteId();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setRouteId((Number) value);
      }
    },
    RESTRICT_SHIP {
      public Object get(CustomerImpl obj) {
        return obj.getRestrictShip();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setRestrictShip((String) value);
      }
    },
    NAME {
      public Object get(CustomerImpl obj) {
        return obj.getName();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setName((String) value);
      }
    },
    FLOOR_LOCATION_ID {
      public Object get(CustomerImpl obj) {
        return obj.getFloorLocationId();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setFloorLocationId((Number) value);
      }
    },
    SUPPORTED {
      public Object get(CustomerImpl obj) {
        return obj.getSupported();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setSupported((String) value);
      }
    },
    SHIPPING_ROUTE_ID {
      public Object get(CustomerImpl obj) {
        return obj.getShippingRouteId();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setShippingRouteId((Number) value);
      }
    },
    ISSUE {
      public Object get(CustomerImpl obj) {
        return obj.getIssue();
      }

      public void put(CustomerImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(CustomerImpl object);

    public abstract void put(CustomerImpl object, Object value);

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

  public static final int ADDRESS1 = AttributesEnum.ADDRESS_1.index();
  public static final int CITY = AttributesEnum.CITY.index();
  public static final int STATE = AttributesEnum.STATE.index();
  public static final int ZIPCODE = AttributesEnum.ZIP_CODE.index();
  public static final int AAC = AttributesEnum.AAC.index();
  public static final int CUSTOMERID = AttributesEnum.CUSTOMER_ID.index();
  public static final int ADDRESS2 = AttributesEnum.ADDRESS_2.index();
  public static final int ROUTEID = AttributesEnum.ROUTE_ID.index();
  public static final int RESTRICTSHIP = AttributesEnum.RESTRICT_SHIP.index();
  public static final int NAME = AttributesEnum.NAME.index();
  public static final int FLOORLOCATIONID = AttributesEnum.FLOOR_LOCATION_ID.index();
  public static final int SUPPORTED = AttributesEnum.SUPPORTED.index();
  public static final int SHIPPINGROUTEID = AttributesEnum.SHIPPING_ROUTE_ID.index();
  public static final int ISSUE = AttributesEnum.ISSUE.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.site.Customer");
  }

  /**
   * Gets the attribute value for Address1, using the alias name Address1.
   *
   * @return the value of Address1
   */
  public String getAddress1() {
    return (String) getAttributeInternal(ADDRESS1);
  }

  /**
   * Sets <code>value</code> as the attribute value for Address1.
   *
   * @param value value to set the Address1
   */
  public void setAddress1(String value) {
    setAttributeInternal(ADDRESS1, value);
  }

  /**
   * Gets the attribute value for City, using the alias name City.
   *
   * @return the value of City
   */
  public String getCity() {
    return (String) getAttributeInternal(CITY);
  }

  /**
   * Sets <code>value</code> as the attribute value for City.
   *
   * @param value value to set the City
   */
  public void setCity(String value) {
    setAttributeInternal(CITY, value);
  }

  /**
   * Gets the attribute value for State, using the alias name State.
   *
   * @return the value of State
   */
  public String getState() {
    return (String) getAttributeInternal(STATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for State.
   *
   * @param value value to set the State
   */
  public void setState(String value) {
    setAttributeInternal(STATE, value);
  }

  /**
   * Gets the attribute value for ZipCode, using the alias name ZipCode.
   *
   * @return the value of ZipCode
   */
  public String getZipCode() {
    return (String) getAttributeInternal(ZIPCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ZipCode.
   *
   * @param value value to set the ZipCode
   */
  public void setZipCode(String value) {
    setAttributeInternal(ZIPCODE, value);
  }

  /**
   * Gets the attribute value for Aac, using the alias name Aac.
   *
   * @return the value of Aac
   */
  public String getAac() {
    return (String) getAttributeInternal(AAC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Aac.
   *
   * @param value value to set the Aac
   */
  public void setAac(String value) {
    setAttributeInternal(AAC, value);
  }

  /**
   * Gets the attribute value for CustomerId, using the alias name CustomerId.
   *
   * @return the value of CustomerId
   */
  public DBSequence getCustomerId() {
    return (DBSequence) getAttributeInternal(CUSTOMERID);
  }

  /**
   * Sets <code>value</code> as the attribute value for CustomerId.
   *
   * @param value value to set the CustomerId
   */
  public void setCustomerId(DBSequence value) {
    setAttributeInternal(CUSTOMERID, value);
  }

  /**
   * Gets the attribute value for Address2, using the alias name Address2.
   *
   * @return the value of Address2
   */
  public String getAddress2() {
    return (String) getAttributeInternal(ADDRESS2);
  }

  /**
   * Sets <code>value</code> as the attribute value for Address2.
   *
   * @param value value to set the Address2
   */
  public void setAddress2(String value) {
    setAttributeInternal(ADDRESS2, value);
  }

  /**
   * Gets the attribute value for RouteId, using the alias name RouteId.
   *
   * @return the value of RouteId
   */
  public oracle.jbo.domain.Number getRouteId() {
    return (oracle.jbo.domain.Number) getAttributeInternal(ROUTEID);
  }

  /**
   * Sets <code>value</code> as the attribute value for RouteId.
   *
   * @param value value to set the RouteId
   */
  public void setRouteId(oracle.jbo.domain.Number value) {
    setAttributeInternal(ROUTEID, value);
  }

  /**
   * Gets the attribute value for RestrictShip, using the alias name RestrictShip.
   *
   * @return the value of RestrictShip
   */
  public String getRestrictShip() {
    return (String) getAttributeInternal(RESTRICTSHIP);
  }

  /**
   * Sets <code>value</code> as the attribute value for RestrictShip.
   *
   * @param value value to set the RestrictShip
   */
  public void setRestrictShip(String value) {
    setAttributeInternal(RESTRICTSHIP, value);
  }

  /**
   * Gets the attribute value for Name, using the alias name Name.
   *
   * @return the value of Name
   */
  public String getName() {
    return (String) getAttributeInternal(NAME);
  }

  /**
   * Sets <code>value</code> as the attribute value for Name.
   *
   * @param value value to set the Name
   */
  public void setName(String value) {
    setAttributeInternal(NAME, value);
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
   * Gets the attribute value for Supported, using the alias name Supported.
   *
   * @return the value of Supported
   */
  public String getSupported() {
    return (String) getAttributeInternal(SUPPORTED);
  }

  /**
   * Sets <code>value</code> as the attribute value for Supported.
   *
   * @param value value to set the Supported
   */
  public void setSupported(String value) {
    setAttributeInternal(SUPPORTED, value);
  }

  /**
   * Gets the attribute value for ShippingRouteId, using the alias name ShippingRouteId.
   *
   * @return the value of ShippingRouteId
   */
  public Number getShippingRouteId() {
    return (Number) getAttributeInternal(SHIPPINGROUTEID);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShippingRouteId.
   *
   * @param value value to set the ShippingRouteId
   */
  public void setShippingRouteId(Number value) {
    setAttributeInternal(SHIPPINGROUTEID, value);
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
  public RowIterator getIssue() {
    return (RowIterator) getAttributeInternal(ISSUE);
  }

  /**
   * @param customerId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence customerId) {
    return new Key(new Object[]{customerId});
  }
}

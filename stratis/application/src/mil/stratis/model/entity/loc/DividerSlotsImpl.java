package mil.stratis.model.entity.loc;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class DividerSlotsImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    DIVIDER_SLOTS_ID {
      public Object get(DividerSlotsImpl obj) {
        return obj.getDividerSlotsId();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setDividerSlotsId((DBSequence) value);
      }
    },
    DIVIDER_TYPE_ID {
      public Object get(DividerSlotsImpl obj) {
        return obj.getDividerTypeId();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setDividerTypeId((Number) value);
      }
    },
    ROW_NUMBER {
      public Object get(DividerSlotsImpl obj) {
        return obj.getRowNumber();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setRowNumber((Number) value);
      }
    },
    COLUMN_NUMBER {
      public Object get(DividerSlotsImpl obj) {
        return obj.getColumnNumber();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setColumnNumber((Number) value);
      }
    },
    LENGTH {
      public Object get(DividerSlotsImpl obj) {
        return obj.getLength();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setLength((Number) value);
      }
    },
    SELECT_INDEX {
      public Object get(DividerSlotsImpl obj) {
        return obj.getSelectIndex();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setSelectIndex((Number) value);
      }
    },
    WIDTH {
      public Object get(DividerSlotsImpl obj) {
        return obj.getWidth();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setWidth((String) value);
      }
    },
    DISPLAY_POSITION {
      public Object get(DividerSlotsImpl obj) {
        return obj.getDisplayPosition();
      }

      public void put(DividerSlotsImpl obj, Object value) {
        obj.setDisplayPosition((String) value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(DividerSlotsImpl object);

    public abstract void put(DividerSlotsImpl object, Object value);

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

  public static final int DIVIDERSLOTSID = AttributesEnum.DIVIDER_SLOTS_ID.index();
  public static final int DIVIDERTYPEID = AttributesEnum.DIVIDER_TYPE_ID.index();
  public static final int ROWNUMBER = AttributesEnum.ROW_NUMBER.index();
  public static final int COLUMNNUMBER = AttributesEnum.COLUMN_NUMBER.index();
  public static final int LENGTH = AttributesEnum.LENGTH.index();
  public static final int SELECTINDEX = AttributesEnum.SELECT_INDEX.index();
  public static final int WIDTH = AttributesEnum.WIDTH.index();
  public static final int DISPLAYPOSITION = AttributesEnum.DISPLAY_POSITION.index();

  /**
   * Gets the attribute value for DividerSlotsId, using the alias name DividerSlotsId.
   *
   * @return the value of DividerSlotsId
   */
  public DBSequence getDividerSlotsId() {
    return (DBSequence) getAttributeInternal(DIVIDERSLOTSID);
  }

  /**
   * Sets <code>value</code> as the attribute value for DividerSlotsId.
   *
   * @param value value to set the DividerSlotsId
   */
  public void setDividerSlotsId(DBSequence value) {
    setAttributeInternal(DIVIDERSLOTSID, value);
  }

  /**
   * Gets the attribute value for DividerTypeId, using the alias name DividerTypeId.
   *
   * @return the value of DividerTypeId
   */
  public Number getDividerTypeId() {
    return (Number) getAttributeInternal(DIVIDERTYPEID);
  }

  /**
   * Sets <code>value</code> as the attribute value for DividerTypeId.
   *
   * @param value value to set the DividerTypeId
   */
  public void setDividerTypeId(Number value) {
    setAttributeInternal(DIVIDERTYPEID, value);
  }

  /**
   * Gets the attribute value for RowNumber, using the alias name RowNumber.
   *
   * @return the value of RowNumber
   */
  public Number getRowNumber() {
    return (Number) getAttributeInternal(ROWNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for RowNumber.
   *
   * @param value value to set the RowNumber
   */
  public void setRowNumber(Number value) {
    setAttributeInternal(ROWNUMBER, value);
  }

  /**
   * Gets the attribute value for ColumnNumber, using the alias name ColumnNumber.
   *
   * @return the value of ColumnNumber
   */
  public Number getColumnNumber() {
    return (Number) getAttributeInternal(COLUMNNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for ColumnNumber.
   *
   * @param value value to set the ColumnNumber
   */
  public void setColumnNumber(Number value) {
    setAttributeInternal(COLUMNNUMBER, value);
  }

  /**
   * Gets the attribute value for Length, using the alias name Length.
   *
   * @return the value of Length
   */
  public Number getLength() {
    return (Number) getAttributeInternal(LENGTH);
  }

  /**
   * Sets <code>value</code> as the attribute value for Length.
   *
   * @param value value to set the Length
   */
  public void setLength(Number value) {
    setAttributeInternal(LENGTH, value);
  }

  /**
   * Gets the attribute value for SelectIndex, using the alias name SelectIndex.
   *
   * @return the value of SelectIndex
   */
  public Number getSelectIndex() {
    return (Number) getAttributeInternal(SELECTINDEX);
  }

  /**
   * Sets <code>value</code> as the attribute value for SelectIndex.
   *
   * @param value value to set the SelectIndex
   */
  public void setSelectIndex(Number value) {
    setAttributeInternal(SELECTINDEX, value);
  }

  /**
   * Gets the attribute value for Width, using the alias name Width.
   *
   * @return the value of Width
   */
  public String getWidth() {
    return (String) getAttributeInternal(WIDTH);
  }

  /**
   * Sets <code>value</code> as the attribute value for Width.
   *
   * @param value value to set the Width
   */
  public void setWidth(String value) {
    setAttributeInternal(WIDTH, value);
  }

  /**
   * Gets the attribute value for DisplayPosition, using the alias name DisplayPosition.
   *
   * @return the value of DisplayPosition
   */
  public String getDisplayPosition() {
    return (String) getAttributeInternal(DISPLAYPOSITION);
  }

  /**
   * Sets <code>value</code> as the attribute value for DisplayPosition.
   *
   * @param value value to set the DisplayPosition
   */
  public void setDisplayPosition(String value) {
    setAttributeInternal(DISPLAYPOSITION, value);
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
   * @param dividerSlotsId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence dividerSlotsId) {
    return new Key(new Object[]{dividerSlotsId});
  }

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.loc.DividerSlots");
  }
}

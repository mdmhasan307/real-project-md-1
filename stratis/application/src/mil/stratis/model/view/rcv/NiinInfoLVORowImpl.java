package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import mil.stratis.model.entity.site.NiinInfoImpl;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Timestamp;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.ViewRowImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinInfoLVORowImpl extends ViewRowImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    NIIN_ID {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getNiinId();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setNiinId((DBSequence) value);
      }
    },
    NIIN {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getNiin();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setNiin((String) value);
      }
    },
    NOMENCLATURE {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getNomenclature();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setNomenclature((String) value);
      }
    },
    HEIGHT {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getHeight();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setHeight((oracle.jbo.domain.Number) value);
      }
    },
    WEIGHT {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getWeight();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setWeight((oracle.jbo.domain.Number) value);
      }
    },
    LENGTH {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getLength();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setLength((oracle.jbo.domain.Number) value);
      }
    },
    WIDTH {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getWidth();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setWidth((oracle.jbo.domain.Number) value);
      }
    },
    CUBE {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getCube();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setCube((oracle.jbo.domain.Number) value);
      }
    },
    PRICE {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getPrice();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setPrice((oracle.jbo.domain.Number) value);
      }
    },
    ACTIVITY_DATE {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getActivityDate();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setActivityDate((Timestamp) value);
      }
    },
    TAMCN {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getTamcn();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setTamcn((String) value);
      }
    },
    SUPPLY_CLASS {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getSupplyClass();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setSupplyClass((String) value);
      }
    },
    TYPE_OF_MATERIAL {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getTypeOfMaterial();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setTypeOfMaterial((String) value);
      }
    },
    COGNIZANCE_CODE {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getCognizanceCode();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setCognizanceCode((String) value);
      }
    },
    PART_NUMBER {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getPartNumber();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setPartNumber((String) value);
      }
    },
    UI {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getUi();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setUi((String) value);
      }
    },
    CAGE_CODE {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getCageCode();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setCageCode((String) value);
      }
    },
    FSC {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getFsc();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setFsc((String) value);
      }
    },
    SCC {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getScc();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setScc((String) value);
      }
    },
    SHELF_LIFE_CODE {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getShelfLifeCode();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setShelfLifeCode((String) value);
      }
    },
    SERIAL_CONTROL_FLAG {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getSerialControlFlag();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setSerialControlFlag((String) value);
      }
    },
    LOT_CONTROL_FLAG {
      public Object get(NiinInfoLVORowImpl obj) {
        return obj.getLotControlFlag();
      }

      public void put(NiinInfoLVORowImpl obj, Object value) {
        obj.setLotControlFlag((String) value);
      }
    };
    private static AttributesEnum[] vals = null;
    private static int firstIndex = 0;

    public abstract Object get(NiinInfoLVORowImpl object);

    public abstract void put(NiinInfoLVORowImpl object, Object value);

    public int index() {
      return NiinInfoLVORowImpl.AttributesEnum.firstIndex() + ordinal();
    }

    public static int firstIndex() {
      return firstIndex;
    }

    public static int count() {
      return NiinInfoLVORowImpl.AttributesEnum.firstIndex() + NiinInfoLVORowImpl.AttributesEnum.staticValues().length;
    }

    public static AttributesEnum[] staticValues() {
      if (vals == null) {
        vals = NiinInfoLVORowImpl.AttributesEnum.values();
      }
      return vals;
    }
  }

  public static final int NIINID = AttributesEnum.NIIN_ID.index();
  public static final int NIIN = AttributesEnum.NIIN.index();
  public static final int NOMENCLATURE = AttributesEnum.NOMENCLATURE.index();
  public static final int HEIGHT = AttributesEnum.HEIGHT.index();
  public static final int WEIGHT = AttributesEnum.WEIGHT.index();
  public static final int LENGTH = AttributesEnum.LENGTH.index();
  public static final int WIDTH = AttributesEnum.WIDTH.index();
  public static final int CUBE = AttributesEnum.CUBE.index();
  public static final int PRICE = AttributesEnum.PRICE.index();
  public static final int ACTIVITYDATE = AttributesEnum.ACTIVITY_DATE.index();
  public static final int TAMCN = AttributesEnum.TAMCN.index();
  public static final int SUPPLYCLASS = AttributesEnum.SUPPLY_CLASS.index();
  public static final int TYPEOFMATERIAL = AttributesEnum.TYPE_OF_MATERIAL.index();
  public static final int COGNIZANCECODE = AttributesEnum.COGNIZANCE_CODE.index();
  public static final int PARTNUMBER = AttributesEnum.PART_NUMBER.index();
  public static final int UI = AttributesEnum.UI.index();
  public static final int CAGECODE = AttributesEnum.CAGE_CODE.index();
  public static final int FSC = AttributesEnum.FSC.index();
  public static final int SCC = AttributesEnum.SCC.index();
  public static final int SHELFLIFECODE = AttributesEnum.SHELF_LIFE_CODE.index();
  public static final int SERIALCONTROLFLAG = AttributesEnum.SERIAL_CONTROL_FLAG.index();
  public static final int LOTCONTROLFLAG = AttributesEnum.LOT_CONTROL_FLAG.index();

  /**
   * Gets NiinInfo entity object.
   */
  public NiinInfoImpl getNiinInfo() {
    return (NiinInfoImpl) getEntity(0);
  }

  /**
   * Gets the attribute value for NIIN_ID using the alias name NiinId
   */
  public DBSequence getNiinId() {
    return (DBSequence) getAttributeInternal(NIINID);
  }

  /**
   * Sets <code>value</code> as attribute value for NIIN_ID using the alias name NiinId
   */
  public void setNiinId(DBSequence value) {
    setAttributeInternal(NIINID, value);
  }

  /**
   * Gets the attribute value for NIIN using the alias name Niin
   */
  public String getNiin() {
    return (String) getAttributeInternal(NIIN);
  }

  /**
   * Sets <code>value</code> as attribute value for NIIN using the alias name Niin
   */
  public void setNiin(String value) {
    setAttributeInternal(NIIN, value);
  }

  /**
   * Gets the attribute value for NOMENCLATURE using the alias name Nomenclature
   */
  public String getNomenclature() {
    return (String) getAttributeInternal(NOMENCLATURE);
  }

  /**
   * Sets <code>value</code> as attribute value for NOMENCLATURE using the alias name Nomenclature
   */
  public void setNomenclature(String value) {
    setAttributeInternal(NOMENCLATURE, value);
  }

  /**
   * Gets the attribute value for HEIGHT using the alias name Height
   */
  public oracle.jbo.domain.Number getHeight() {
    return (oracle.jbo.domain.Number) getAttributeInternal(HEIGHT);
  }

  /**
   * Sets <code>value</code> as attribute value for HEIGHT using the alias name Height
   */
  public void setHeight(oracle.jbo.domain.Number value) {
    setAttributeInternal(HEIGHT, value);
  }

  /**
   * Gets the attribute value for WEIGHT using the alias name Weight
   */
  public oracle.jbo.domain.Number getWeight() {
    return (oracle.jbo.domain.Number) getAttributeInternal(WEIGHT);
  }

  /**
   * Sets <code>value</code> as attribute value for WEIGHT using the alias name Weight
   */
  public void setWeight(oracle.jbo.domain.Number value) {
    setAttributeInternal(WEIGHT, value);
  }

  /**
   * Gets the attribute value for LENGTH using the alias name Length
   */
  public oracle.jbo.domain.Number getLength() {
    return (oracle.jbo.domain.Number) getAttributeInternal(LENGTH);
  }

  /**
   * Sets <code>value</code> as attribute value for LENGTH using the alias name Length
   */
  public void setLength(oracle.jbo.domain.Number value) {
    setAttributeInternal(LENGTH, value);
  }

  /**
   * Gets the attribute value for WIDTH using the alias name Width
   */
  public oracle.jbo.domain.Number getWidth() {
    return (oracle.jbo.domain.Number) getAttributeInternal(WIDTH);
  }

  /**
   * Sets <code>value</code> as attribute value for WIDTH using the alias name Width
   */
  public void setWidth(oracle.jbo.domain.Number value) {
    setAttributeInternal(WIDTH, value);
  }

  /**
   * Gets the attribute value for CUBE using the alias name Cube
   */
  public oracle.jbo.domain.Number getCube() {
    return (oracle.jbo.domain.Number) getAttributeInternal(CUBE);
  }

  /**
   * Sets <code>value</code> as attribute value for CUBE using the alias name Cube
   */
  public void setCube(oracle.jbo.domain.Number value) {
    setAttributeInternal(CUBE, value);
  }

  /**
   * Gets the attribute value for PRICE using the alias name Price
   */
  public oracle.jbo.domain.Number getPrice() {
    return (oracle.jbo.domain.Number) getAttributeInternal(PRICE);
  }

  /**
   * Sets <code>value</code> as attribute value for PRICE using the alias name Price
   */
  public void setPrice(oracle.jbo.domain.Number value) {
    setAttributeInternal(PRICE, value);
  }

  /**
   * Gets the attribute value for ACTIVITY_DATE using the alias name ActivityDate
   */
  public Timestamp getActivityDate() {
    return (Timestamp) getAttributeInternal(ACTIVITYDATE);
  }

  /**
   * Sets <code>value</code> as attribute value for ACTIVITY_DATE using the alias name ActivityDate
   */
  public void setActivityDate(Timestamp value) {
    setAttributeInternal(ACTIVITYDATE, value);
  }

  /**
   * Gets the attribute value for TAMCN using the alias name Tamcn
   */
  public String getTamcn() {
    return (String) getAttributeInternal(TAMCN);
  }

  /**
   * Sets <code>value</code> as attribute value for TAMCN using the alias name Tamcn
   */
  public void setTamcn(String value) {
    setAttributeInternal(TAMCN, value);
  }

  /**
   * Gets the attribute value for SUPPLY_CLASS using the alias name SupplyClass
   */
  public String getSupplyClass() {
    return (String) getAttributeInternal(SUPPLYCLASS);
  }

  /**
   * Sets <code>value</code> as attribute value for SUPPLY_CLASS using the alias name SupplyClass
   */
  public void setSupplyClass(String value) {
    setAttributeInternal(SUPPLYCLASS, value);
  }

  /**
   * Gets the attribute value for TYPE_OF_MATERIAL using the alias name TypeOfMaterial
   */
  public String getTypeOfMaterial() {
    return (String) getAttributeInternal(TYPEOFMATERIAL);
  }

  /**
   * Sets <code>value</code> as attribute value for TYPE_OF_MATERIAL using the alias name TypeOfMaterial
   */
  public void setTypeOfMaterial(String value) {
    setAttributeInternal(TYPEOFMATERIAL, value);
  }

  /**
   * Gets the attribute value for COGNIZANCE_CODE using the alias name CognizanceCode
   */
  public String getCognizanceCode() {
    return (String) getAttributeInternal(COGNIZANCECODE);
  }

  /**
   * Sets <code>value</code> as attribute value for COGNIZANCE_CODE using the alias name CognizanceCode
   */
  public void setCognizanceCode(String value) {
    setAttributeInternal(COGNIZANCECODE, value);
  }

  /**
   * Gets the attribute value for PART_NUMBER using the alias name PartNumber
   */
  public String getPartNumber() {
    return (String) getAttributeInternal(PARTNUMBER);
  }

  /**
   * Sets <code>value</code> as attribute value for PART_NUMBER using the alias name PartNumber
   */
  public void setPartNumber(String value) {
    setAttributeInternal(PARTNUMBER, value);
  }

  /**
   * Gets the attribute value for UI using the alias name Ui
   */
  public String getUi() {
    return (String) getAttributeInternal(UI);
  }

  /**
   * Sets <code>value</code> as attribute value for UI using the alias name Ui
   */
  public void setUi(String value) {
    setAttributeInternal(UI, value);
  }

  /**
   * Gets the attribute value for CAGE_CODE using the alias name CageCode
   */
  public String getCageCode() {
    return (String) getAttributeInternal(CAGECODE);
  }

  /**
   * Sets <code>value</code> as attribute value for CAGE_CODE using the alias name CageCode
   */
  public void setCageCode(String value) {
    setAttributeInternal(CAGECODE, value);
  }

  /**
   * Gets the attribute value for FSC using the alias name Fsc
   */
  public String getFsc() {
    return (String) getAttributeInternal(FSC);
  }

  /**
   * Sets <code>value</code> as attribute value for FSC using the alias name Fsc
   */
  public void setFsc(String value) {
    setAttributeInternal(FSC, value);
  }

  /**
   * Gets the attribute value for SCC using the alias name Scc
   */
  public String getScc() {
    return (String) getAttributeInternal(SCC);
  }

  /**
   * Sets <code>value</code> as attribute value for SCC using the alias name Scc
   */
  public void setScc(String value) {
    setAttributeInternal(SCC, value);
  }

  /**
   * Gets the attribute value for SHELF_LIFE_CODE using the alias name ShelfLifeCode
   */
  public String getShelfLifeCode() {
    return (String) getAttributeInternal(SHELFLIFECODE);
  }

  /**
   * Sets <code>value</code> as attribute value for SHELF_LIFE_CODE using the alias name ShelfLifeCode
   */
  public void setShelfLifeCode(String value) {
    setAttributeInternal(SHELFLIFECODE, value);
  }

  /**
   * Gets the attribute value for SERIAL_CONTROL_FLAG using the alias name SerialControlFlag
   */
  public String getSerialControlFlag() {
    return (String) getAttributeInternal(SERIALCONTROLFLAG);
  }

  /**
   * Sets <code>value</code> as attribute value for SERIAL_CONTROL_FLAG using the alias name SerialControlFlag
   */
  public void setSerialControlFlag(String value) {
    setAttributeInternal(SERIALCONTROLFLAG, value);
  }

  /**
   * Gets the attribute value for LOT_CONTROL_FLAG using the alias name LotControlFlag
   */
  public String getLotControlFlag() {
    return (String) getAttributeInternal(LOTCONTROLFLAG);
  }

  /**
   * Sets <code>value</code> as attribute value for LOT_CONTROL_FLAG using the alias name LotControlFlag
   */
  public void setLotControlFlag(String value) {
    setAttributeInternal(LOTCONTROLFLAG, value);
  }

  /**
   * getAttrInvokeAccessor: generated method. Do not modify.
   */
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
  protected void setAttrInvokeAccessor(int index, Object value,
                                       AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].put(this, value);
      return;
    }
    super.setAttrInvokeAccessor(index, value, attrDef);
  }
}

package mil.stratis.model.entity.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.Key;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

import java.math.BigDecimal;

@NoArgsConstructor
public class NiinInfoReceiptVImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    NIIN_ID {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getNiinId();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setNiinId((Integer) value);
      }
    },
    FSC {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getFsc();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setFsc((String) value);
      }
    },
    NIIN {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getNiin();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setNiin((String) value);
      }
    },
    SUPPLY_CLASS {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getSupplyClass();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setSupplyClass((String) value);
      }
    },
    TYPE_OF_MATERIAL {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getTypeOfMaterial();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setTypeOfMaterial((String) value);
      }
    },
    SHELF_LIFE_CODE {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getShelfLifeCode();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setShelfLifeCode((String) value);
      }
    },
    SHELF_LIFE_EXTENSION {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getShelfLifeExtension();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setShelfLifeExtension((BigDecimal) value);
      }
    },
    UI {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getUi();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setUi((String) value);
      }
    },
    HEIGHT {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getHeight();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setHeight((Number) value);
      }
    },
    WIDTH {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getWidth();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setWidth((Number) value);
      }
    },
    LENGTH {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getLength();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setLength((Number) value);
      }
    },
    WEIGHT {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getWeight();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setWeight((Number) value);
      }
    },
    CUBE {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getCube();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setCube((Number) value);
      }
    },
    SCC {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getScc();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setScc((String) value);
      }
    },
    RCN {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getRcn();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setRcn((Number) value);
      }
    },
    DOCUMENT_NUMBER {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getDocumentNumber();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setDocumentNumber((String) value);
      }
    },
    SUFFIX {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getSuffix();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setSuffix((String) value);
      }
    },
    PC {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getPc();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setPc((String) value);
      }
    },
    CC {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getCc();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setCc((String) value);
      }
    },
    QUANTITY_INVOICED {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getQuantityInvoiced();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setQuantityInvoiced((Integer) value);
      }
    },
    QUANTITY_BACKORDERED {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getQuantityBackordered();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setQuantityBackordered((Integer) value);
      }
    },
    QUANTITY_RELEASED {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getQuantityReleased();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setQuantityReleased((Integer) value);
      }
    },
    QUANTITY_INDUCTED {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getQuantityInducted();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setQuantityInducted((Integer) value);
      }
    },
    MECH_NON_MECH_FLAG {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getMechNonMechFlag();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setMechNonMechFlag((String) value);
      }
    },
    STATUS {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getStatus();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setStatus((String) value);
      }
    },
    MODIFIED_BY {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setModifiedBy((Integer) value);
      }
    },
    QUANTITY_STOWED {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getQuantityStowed();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setQuantityStowed((Integer) value);
      }
    },
    RSERIAL_NUMBER {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getRserialNumber();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setRserialNumber((String) value);
      }
    },
    QUANTITY_MEASURED {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getQuantityMeasured();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setQuantityMeasured((Integer) value);
      }
    },
    RCUBE {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getRcube();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setRcube((Number) value);
      }
    },
    CODE_VALUE {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getCodeValue();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setCodeValue((String) value);
      }
    },
    SERIAL_CONTROL_FLAG {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getSerialControlFlag();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setSerialControlFlag((String) value);
      }
    },
    LOT_CONTROL_FLAG {
      public Object get(NiinInfoReceiptVImpl obj) {
        return obj.getLotControlFlag();
      }

      public void put(NiinInfoReceiptVImpl obj, Object value) {
        obj.setLotControlFlag((String) value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(NiinInfoReceiptVImpl object);

    public abstract void put(NiinInfoReceiptVImpl object, Object value);

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

  public static final int NIINID = AttributesEnum.NIIN_ID.index();
  public static final int FSC = AttributesEnum.FSC.index();
  public static final int NIIN = AttributesEnum.NIIN.index();
  public static final int SUPPLYCLASS = AttributesEnum.SUPPLY_CLASS.index();
  public static final int TYPEOFMATERIAL = AttributesEnum.TYPE_OF_MATERIAL.index();
  public static final int SHELFLIFECODE = AttributesEnum.SHELF_LIFE_CODE.index();
  public static final int SHELFLIFEEXTENSION = AttributesEnum.SHELF_LIFE_EXTENSION.index();
  public static final int UI = AttributesEnum.UI.index();
  public static final int HEIGHT = AttributesEnum.HEIGHT.index();
  public static final int WIDTH = AttributesEnum.WIDTH.index();
  public static final int LENGTH = AttributesEnum.LENGTH.index();
  public static final int WEIGHT = AttributesEnum.WEIGHT.index();
  public static final int CUBE = AttributesEnum.CUBE.index();
  public static final int SCC = AttributesEnum.SCC.index();
  public static final int RCN = AttributesEnum.RCN.index();
  public static final int DOCUMENTNUMBER = AttributesEnum.DOCUMENT_NUMBER.index();
  public static final int SUFFIX = AttributesEnum.SUFFIX.index();
  public static final int PC = AttributesEnum.PC.index();
  public static final int CC = AttributesEnum.CC.index();
  public static final int QUANTITYINVOICED = AttributesEnum.QUANTITY_INVOICED.index();
  public static final int QUANTITYBACKORDERED = AttributesEnum.QUANTITY_BACKORDERED.index();
  public static final int QUANTITYRELEASED = AttributesEnum.QUANTITY_RELEASED.index();
  public static final int QUANTITYINDUCTED = AttributesEnum.QUANTITY_INDUCTED.index();
  public static final int MECHNONMECHFLAG = AttributesEnum.MECH_NON_MECH_FLAG.index();
  public static final int STATUS = AttributesEnum.STATUS.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int QUANTITYSTOWED = AttributesEnum.QUANTITY_STOWED.index();
  public static final int RSERIALNUMBER = AttributesEnum.RSERIAL_NUMBER.index();
  public static final int QUANTITYMEASURED = AttributesEnum.QUANTITY_MEASURED.index();
  public static final int RCUBE = AttributesEnum.RCUBE.index();
  public static final int CODEVALUE = AttributesEnum.CODE_VALUE.index();
  public static final int SERIALCONTROLFLAG = AttributesEnum.SERIAL_CONTROL_FLAG.index();
  public static final int LOTCONTROLFLAG = AttributesEnum.LOT_CONTROL_FLAG.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.rcv.NiinInfoReceiptV");
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
   * Gets the attribute value for Fsc, using the alias name Fsc.
   *
   * @return the value of Fsc
   */
  public String getFsc() {
    return (String) getAttributeInternal(FSC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Fsc.
   *
   * @param value value to set the Fsc
   */
  public void setFsc(String value) {
    setAttributeInternal(FSC, value);
  }

  /**
   * Gets the attribute value for Niin, using the alias name Niin.
   *
   * @return the value of Niin
   */
  public String getNiin() {
    return (String) getAttributeInternal(NIIN);
  }

  /**
   * Sets <code>value</code> as the attribute value for Niin.
   *
   * @param value value to set the Niin
   */
  public void setNiin(String value) {
    setAttributeInternal(NIIN, value);
  }

  /**
   * Gets the attribute value for SupplyClass, using the alias name SupplyClass.
   *
   * @return the value of SupplyClass
   */
  public String getSupplyClass() {
    return (String) getAttributeInternal(SUPPLYCLASS);
  }

  /**
   * Sets <code>value</code> as the attribute value for SupplyClass.
   *
   * @param value value to set the SupplyClass
   */
  public void setSupplyClass(String value) {
    setAttributeInternal(SUPPLYCLASS, value);
  }

  /**
   * Gets the attribute value for TypeOfMaterial, using the alias name TypeOfMaterial.
   *
   * @return the value of TypeOfMaterial
   */
  public String getTypeOfMaterial() {
    return (String) getAttributeInternal(TYPEOFMATERIAL);
  }

  /**
   * Sets <code>value</code> as the attribute value for TypeOfMaterial.
   *
   * @param value value to set the TypeOfMaterial
   */
  public void setTypeOfMaterial(String value) {
    setAttributeInternal(TYPEOFMATERIAL, value);
  }

  /**
   * Gets the attribute value for ShelfLifeCode, using the alias name ShelfLifeCode.
   *
   * @return the value of ShelfLifeCode
   */
  public String getShelfLifeCode() {
    return (String) getAttributeInternal(SHELFLIFECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShelfLifeCode.
   *
   * @param value value to set the ShelfLifeCode
   */
  public void setShelfLifeCode(String value) {
    setAttributeInternal(SHELFLIFECODE, value);
  }

  /**
   * Gets the attribute value for ShelfLifeExtension, using the alias name ShelfLifeExtension.
   *
   * @return the value of ShelfLifeExtension
   */
  public BigDecimal getShelfLifeExtension() {
    return (BigDecimal) getAttributeInternal(SHELFLIFEEXTENSION);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShelfLifeExtension.
   *
   * @param value value to set the ShelfLifeExtension
   */
  public void setShelfLifeExtension(BigDecimal value) {
    setAttributeInternal(SHELFLIFEEXTENSION, value);
  }

  /**
   * Gets the attribute value for Ui, using the alias name Ui.
   *
   * @return the value of Ui
   */
  public String getUi() {
    return (String) getAttributeInternal(UI);
  }

  /**
   * Sets <code>value</code> as the attribute value for Ui.
   *
   * @param value value to set the Ui
   */
  public void setUi(String value) {
    setAttributeInternal(UI, value);
  }

  /**
   * Gets the attribute value for Height, using the alias name Height.
   *
   * @return the value of Height
   */
  public oracle.jbo.domain.Number getHeight() {
    return (oracle.jbo.domain.Number) getAttributeInternal(HEIGHT);
  }

  /**
   * Sets <code>value</code> as the attribute value for Height.
   *
   * @param value value to set the Height
   */
  public void setHeight(oracle.jbo.domain.Number value) {
    setAttributeInternal(HEIGHT, value);
  }

  /**
   * Gets the attribute value for Width, using the alias name Width.
   *
   * @return the value of Width
   */
  public Number getWidth() {
    return (Number) getAttributeInternal(WIDTH);
  }

  /**
   * Sets <code>value</code> as the attribute value for Width.
   *
   * @param value value to set the Width
   */
  public void setWidth(Number value) {
    setAttributeInternal(WIDTH, value);
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
   * Gets the attribute value for Scc, using the alias name Scc.
   *
   * @return the value of Scc
   */
  public String getScc() {
    return (String) getAttributeInternal(SCC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Scc.
   *
   * @param value value to set the Scc
   */
  public void setScc(String value) {
    setAttributeInternal(SCC, value);
  }

  /**
   * Gets the attribute value for Rcn, using the alias name Rcn.
   *
   * @return the value of Rcn
   */
  public Number getRcn() {
    return (Number) getAttributeInternal(RCN);
  }

  /**
   * Sets <code>value</code> as the attribute value for Rcn.
   *
   * @param value value to set the Rcn
   */
  public void setRcn(Number value) {
    setAttributeInternal(RCN, value);
  }

  /**
   * Gets the attribute value for DocumentNumber, using the alias name DocumentNumber.
   *
   * @return the value of DocumentNumber
   */
  public String getDocumentNumber() {
    return (String) getAttributeInternal(DOCUMENTNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for DocumentNumber.
   *
   * @param value value to set the DocumentNumber
   */
  public void setDocumentNumber(String value) {
    setAttributeInternal(DOCUMENTNUMBER, value);
  }

  /**
   * Gets the attribute value for Suffix, using the alias name Suffix.
   *
   * @return the value of Suffix
   */
  public String getSuffix() {
    return (String) getAttributeInternal(SUFFIX);
  }

  /**
   * Sets <code>value</code> as the attribute value for Suffix.
   *
   * @param value value to set the Suffix
   */
  public void setSuffix(String value) {
    setAttributeInternal(SUFFIX, value);
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
   * Gets the attribute value for QuantityInvoiced, using the alias name QuantityInvoiced.
   *
   * @return the value of QuantityInvoiced
   */
  public Integer getQuantityInvoiced() {
    return (Integer) getAttributeInternal(QUANTITYINVOICED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityInvoiced.
   *
   * @param value value to set the QuantityInvoiced
   */
  public void setQuantityInvoiced(Integer value) {
    setAttributeInternal(QUANTITYINVOICED, value);
  }

  /**
   * Gets the attribute value for QuantityBackordered, using the alias name QuantityBackordered.
   *
   * @return the value of QuantityBackordered
   */
  public Integer getQuantityBackordered() {
    return (Integer) getAttributeInternal(QUANTITYBACKORDERED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityBackordered.
   *
   * @param value value to set the QuantityBackordered
   */
  public void setQuantityBackordered(Integer value) {
    setAttributeInternal(QUANTITYBACKORDERED, value);
  }

  /**
   * Gets the attribute value for QuantityReleased, using the alias name QuantityReleased.
   *
   * @return the value of QuantityReleased
   */
  public Integer getQuantityReleased() {
    return (Integer) getAttributeInternal(QUANTITYRELEASED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityReleased.
   *
   * @param value value to set the QuantityReleased
   */
  public void setQuantityReleased(Integer value) {
    setAttributeInternal(QUANTITYRELEASED, value);
  }

  /**
   * Gets the attribute value for QuantityInducted, using the alias name QuantityInducted.
   *
   * @return the value of QuantityInducted
   */
  public Integer getQuantityInducted() {
    return (Integer) getAttributeInternal(QUANTITYINDUCTED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityInducted.
   *
   * @param value value to set the QuantityInducted
   */
  public void setQuantityInducted(Integer value) {
    setAttributeInternal(QUANTITYINDUCTED, value);
  }

  /**
   * Gets the attribute value for MechNonMechFlag, using the alias name MechNonMechFlag.
   *
   * @return the value of MechNonMechFlag
   */
  public String getMechNonMechFlag() {
    return (String) getAttributeInternal(MECHNONMECHFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for MechNonMechFlag.
   *
   * @param value value to set the MechNonMechFlag
   */
  public void setMechNonMechFlag(String value) {
    setAttributeInternal(MECHNONMECHFLAG, value);
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
   * Gets the attribute value for QuantityStowed, using the alias name QuantityStowed.
   *
   * @return the value of QuantityStowed
   */
  public Integer getQuantityStowed() {
    return (Integer) getAttributeInternal(QUANTITYSTOWED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityStowed.
   *
   * @param value value to set the QuantityStowed
   */
  public void setQuantityStowed(Integer value) {
    setAttributeInternal(QUANTITYSTOWED, value);
  }

  /**
   * Gets the attribute value for RserialNumber, using the alias name RserialNumber.
   *
   * @return the value of RserialNumber
   */
  public String getRserialNumber() {
    return (String) getAttributeInternal(RSERIALNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for RserialNumber.
   *
   * @param value value to set the RserialNumber
   */
  public void setRserialNumber(String value) {
    setAttributeInternal(RSERIALNUMBER, value);
  }

  /**
   * Gets the attribute value for QuantityMeasured, using the alias name QuantityMeasured.
   *
   * @return the value of QuantityMeasured
   */
  public Integer getQuantityMeasured() {
    return (Integer) getAttributeInternal(QUANTITYMEASURED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityMeasured.
   *
   * @param value value to set the QuantityMeasured
   */
  public void setQuantityMeasured(Integer value) {
    setAttributeInternal(QUANTITYMEASURED, value);
  }

  /**
   * Gets the attribute value for Rcube, using the alias name Rcube.
   *
   * @return the value of Rcube
   */
  public Number getRcube() {
    return (Number) getAttributeInternal(RCUBE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Rcube.
   *
   * @param value value to set the Rcube
   */
  public void setRcube(Number value) {
    setAttributeInternal(RCUBE, value);
  }

  /**
   * Gets the attribute value for CodeValue, using the alias name CodeValue.
   *
   * @return the value of CodeValue
   */
  public String getCodeValue() {
    return (String) getAttributeInternal(CODEVALUE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CodeValue.
   *
   * @param value value to set the CodeValue
   */
  public void setCodeValue(String value) {
    setAttributeInternal(CODEVALUE, value);
  }

  /**
   * Gets the attribute value for SerialControlFlag, using the alias name SerialControlFlag.
   *
   * @return the value of SerialControlFlag
   */
  public String getSerialControlFlag() {
    return (String) getAttributeInternal(SERIALCONTROLFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for SerialControlFlag.
   *
   * @param value value to set the SerialControlFlag
   */
  public void setSerialControlFlag(String value) {
    setAttributeInternal(SERIALCONTROLFLAG, value);
  }

  /**
   * Gets the attribute value for LotControlFlag, using the alias name LotControlFlag.
   *
   * @return the value of LotControlFlag
   */
  public String getLotControlFlag() {
    return (String) getAttributeInternal(LOTCONTROLFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for LotControlFlag.
   *
   * @param value value to set the LotControlFlag
   */
  public void setLotControlFlag(String value) {
    setAttributeInternal(LOTCONTROLFLAG, value);
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
   * @param niinId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(Integer niinId) {
    return new Key(new Object[]{niinId});
  }
}

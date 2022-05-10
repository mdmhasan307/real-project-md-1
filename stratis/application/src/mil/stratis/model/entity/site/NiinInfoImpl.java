package mil.stratis.model.entity.site;

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
public class NiinInfoImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    NIIN_ID {
      public Object get(NiinInfoImpl obj) {
        return obj.getNiinId();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setNiinId((DBSequence) value);
      }
    },
    NIIN {
      public Object get(NiinInfoImpl obj) {
        return obj.getNiin();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setNiin((String) value);
      }
    },
    NOMENCLATURE {
      public Object get(NiinInfoImpl obj) {
        return obj.getNomenclature();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setNomenclature((String) value);
      }
    },
    CUBE {
      public Object get(NiinInfoImpl obj) {
        return obj.getCube();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setCube((Number) value);
      }
    },
    PRICE {
      public Object get(NiinInfoImpl obj) {
        return obj.getPrice();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setPrice((Number) value);
      }
    },
    ACTIVITY_DATE {
      public Object get(NiinInfoImpl obj) {
        return obj.getActivityDate();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setActivityDate((Timestamp) value);
      }
    },
    TAMCN {
      public Object get(NiinInfoImpl obj) {
        return obj.getTamcn();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setTamcn((String) value);
      }
    },
    SUPPLY_CLASS {
      public Object get(NiinInfoImpl obj) {
        return obj.getSupplyClass();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setSupplyClass((String) value);
      }
    },
    TYPE_OF_MATERIAL {
      public Object get(NiinInfoImpl obj) {
        return obj.getTypeOfMaterial();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setTypeOfMaterial((String) value);
      }
    },
    COGNIZANCE_CODE {
      public Object get(NiinInfoImpl obj) {
        return obj.getCognizanceCode();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setCognizanceCode((String) value);
      }
    },
    PART_NUMBER {
      public Object get(NiinInfoImpl obj) {
        return obj.getPartNumber();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setPartNumber((String) value);
      }
    },
    UI {
      public Object get(NiinInfoImpl obj) {
        return obj.getUi();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setUi((String) value);
      }
    },
    CAGE_CODE {
      public Object get(NiinInfoImpl obj) {
        return obj.getCageCode();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setCageCode((String) value);
      }
    },
    FSC {
      public Object get(NiinInfoImpl obj) {
        return obj.getFsc();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setFsc((String) value);
      }
    },
    SHELF_LIFE_CODE {
      public Object get(NiinInfoImpl obj) {
        return obj.getShelfLifeCode();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setShelfLifeCode((String) value);
      }
    },
    WEIGHT {
      public Object get(NiinInfoImpl obj) {
        return obj.getWeight();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setWeight((Number) value);
      }
    },
    LENGTH {
      public Object get(NiinInfoImpl obj) {
        return obj.getLength();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setLength((Number) value);
      }
    },
    WIDTH {
      public Object get(NiinInfoImpl obj) {
        return obj.getWidth();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setWidth((Number) value);
      }
    },
    HEIGHT {
      public Object get(NiinInfoImpl obj) {
        return obj.getHeight();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setHeight((Number) value);
      }
    },
    SHELF_LIFE_EXTENSION {
      public Object get(NiinInfoImpl obj) {
        return obj.getShelfLifeExtension();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setShelfLifeExtension((Number) value);
      }
    },
    SCC {
      public Object get(NiinInfoImpl obj) {
        return obj.getScc();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setScc((String) value);
      }
    },
    INVENTORY_THRESHOLD {
      public Object get(NiinInfoImpl obj) {
        return obj.getInventoryThreshold();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setInventoryThreshold((String) value);
      }
    },
    SASSY_BALANCE {
      public Object get(NiinInfoImpl obj) {
        return obj.getSassyBalance();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setSassyBalance((Integer) value);
      }
    },
    RO_THRESHOLD {
      public Object get(NiinInfoImpl obj) {
        return obj.getRoThreshold();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setRoThreshold((Integer) value);
      }
    },
    SMIC {
      public Object get(NiinInfoImpl obj) {
        return obj.getSmic();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setSmic((String) value);
      }
    },
    SERIAL_CONTROL_FLAG {
      public Object get(NiinInfoImpl obj) {
        return obj.getSerialControlFlag();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setSerialControlFlag((String) value);
      }
    },
    LOT_CONTROL_FLAG {
      public Object get(NiinInfoImpl obj) {
        return obj.getLotControlFlag();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setLotControlFlag((String) value);
      }
    },
    NEW_NSN {
      public Object get(NiinInfoImpl obj) {
        return obj.getNewNsn();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setNewNsn((String) value);
      }
    },
    CREATED_BY {
      public Object get(NiinInfoImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setCreatedBy((Number) value);
      }
    },
    MODIFIED_BY {
      public Object get(NiinInfoImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setModifiedBy((Number) value);
      }
    },
    LAST_MHIF_UPDATE_DATE {
      public Object get(NiinInfoImpl obj) {
        return obj.getLastMhifUpdateDate();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setLastMhifUpdateDate((Timestamp) value);
      }
    },
    DEMIL_CODE {
      public Object get(NiinInfoImpl obj) {
        return obj.getDemilCode();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setDemilCode((String) value);
      }
    },
    INVENTORY_ITEM {
      public Object get(NiinInfoImpl obj) {
        return obj.getInventoryItem();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    },
    NIIN_LOCATION {
      public Object get(NiinInfoImpl obj) {
        return obj.getNiinLocation();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    },
    RECEIPT {
      public Object get(NiinInfoImpl obj) {
        return obj.getReceipt();
      }

      public void put(NiinInfoImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(NiinInfoImpl object);

    public abstract void put(NiinInfoImpl object, Object value);

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
  public static final int NIIN = AttributesEnum.NIIN.index();
  public static final int NOMENCLATURE = AttributesEnum.NOMENCLATURE.index();
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
  public static final int SHELFLIFECODE = AttributesEnum.SHELF_LIFE_CODE.index();
  public static final int WEIGHT = AttributesEnum.WEIGHT.index();
  public static final int LENGTH = AttributesEnum.LENGTH.index();
  public static final int WIDTH = AttributesEnum.WIDTH.index();
  public static final int HEIGHT = AttributesEnum.HEIGHT.index();
  public static final int SHELFLIFEEXTENSION = AttributesEnum.SHELF_LIFE_EXTENSION.index();
  public static final int SCC = AttributesEnum.SCC.index();
  public static final int INVENTORYTHRESHOLD = AttributesEnum.INVENTORY_THRESHOLD.index();
  public static final int SASSYBALANCE = AttributesEnum.SASSY_BALANCE.index();
  public static final int ROTHRESHOLD = AttributesEnum.RO_THRESHOLD.index();
  public static final int SMIC = AttributesEnum.SMIC.index();
  public static final int SERIALCONTROLFLAG = AttributesEnum.SERIAL_CONTROL_FLAG.index();
  public static final int LOTCONTROLFLAG = AttributesEnum.LOT_CONTROL_FLAG.index();
  public static final int NEWNSN = AttributesEnum.NEW_NSN.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int LASTMHIFUPDATEDATE = AttributesEnum.LAST_MHIF_UPDATE_DATE.index();
  public static final int DEMILCODE = AttributesEnum.DEMIL_CODE.index();
  public static final int INVENTORYITEM = AttributesEnum.INVENTORY_ITEM.index();
  public static final int NIINLOCATION = AttributesEnum.NIIN_LOCATION.index();
  public static final int RECEIPT = AttributesEnum.RECEIPT.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.site.NiinInfo");
  }

  /**
   * Gets the attribute value for NiinId, using the alias name NiinId.
   *
   * @return the value of NiinId
   */
  public DBSequence getNiinId() {
    return (DBSequence) getAttributeInternal(NIINID);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinId.
   *
   * @param value value to set the NiinId
   */
  public void setNiinId(DBSequence value) {
    setAttributeInternal(NIINID, value);
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
   * Gets the attribute value for Nomenclature, using the alias name Nomenclature.
   *
   * @return the value of Nomenclature
   */
  public String getNomenclature() {
    return (String) getAttributeInternal(NOMENCLATURE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Nomenclature.
   *
   * @param value value to set the Nomenclature
   */
  public void setNomenclature(String value) {
    setAttributeInternal(NOMENCLATURE, value);
  }

  /**
   * Gets the attribute value for Cube, using the alias name Cube.
   *
   * @return the value of Cube
   */
  public oracle.jbo.domain.Number getCube() {
    return (oracle.jbo.domain.Number) getAttributeInternal(CUBE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Cube.
   *
   * @param value value to set the Cube
   */
  public void setCube(oracle.jbo.domain.Number value) {
    setAttributeInternal(CUBE, value);
  }

  /**
   * Gets the attribute value for Price, using the alias name Price.
   *
   * @return the value of Price
   */
  public Number getPrice() {
    return (Number) getAttributeInternal(PRICE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Price.
   *
   * @param value value to set the Price
   */
  public void setPrice(Number value) {
    setAttributeInternal(PRICE, value);
  }

  /**
   * Gets the attribute value for ActivityDate, using the alias name ActivityDate.
   *
   * @return the value of ActivityDate
   */
  public Timestamp getActivityDate() {
    return (Timestamp) getAttributeInternal(ACTIVITYDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ActivityDate.
   *
   * @param value value to set the ActivityDate
   */
  public void setActivityDate(Timestamp value) {
    setAttributeInternal(ACTIVITYDATE, value);
  }

  /**
   * Gets the attribute value for Tamcn, using the alias name Tamcn.
   *
   * @return the value of Tamcn
   */
  public String getTamcn() {
    return (String) getAttributeInternal(TAMCN);
  }

  /**
   * Sets <code>value</code> as the attribute value for Tamcn.
   *
   * @param value value to set the Tamcn
   */
  public void setTamcn(String value) {
    setAttributeInternal(TAMCN, value);
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
   * Gets the attribute value for CognizanceCode, using the alias name CognizanceCode.
   *
   * @return the value of CognizanceCode
   */
  public String getCognizanceCode() {
    return (String) getAttributeInternal(COGNIZANCECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CognizanceCode.
   *
   * @param value value to set the CognizanceCode
   */
  public void setCognizanceCode(String value) {
    setAttributeInternal(COGNIZANCECODE, value);
  }

  /**
   * Gets the attribute value for PartNumber, using the alias name PartNumber.
   *
   * @return the value of PartNumber
   */
  public String getPartNumber() {
    return (String) getAttributeInternal(PARTNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for PartNumber.
   *
   * @param value value to set the PartNumber
   */
  public void setPartNumber(String value) {
    setAttributeInternal(PARTNUMBER, value);
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
   * Gets the attribute value for CageCode, using the alias name CageCode.
   *
   * @return the value of CageCode
   */
  public String getCageCode() {
    return (String) getAttributeInternal(CAGECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CageCode.
   *
   * @param value value to set the CageCode
   */
  public void setCageCode(String value) {
    setAttributeInternal(CAGECODE, value);
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
   * Gets the attribute value for Height, using the alias name Height.
   *
   * @return the value of Height
   */
  public Number getHeight() {
    return (Number) getAttributeInternal(HEIGHT);
  }

  /**
   * Sets <code>value</code> as the attribute value for Height.
   *
   * @param value value to set the Height
   */
  public void setHeight(Number value) {
    setAttributeInternal(HEIGHT, value);
  }

  /**
   * Gets the attribute value for ShelfLifeExtension, using the alias name ShelfLifeExtension.
   *
   * @return the value of ShelfLifeExtension
   */
  public Number getShelfLifeExtension() {
    return (Number) getAttributeInternal(SHELFLIFEEXTENSION);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShelfLifeExtension.
   *
   * @param value value to set the ShelfLifeExtension
   */
  public void setShelfLifeExtension(Number value) {
    setAttributeInternal(SHELFLIFEEXTENSION, value);
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
   * Gets the attribute value for InventoryThreshold, using the alias name InventoryThreshold.
   *
   * @return the value of InventoryThreshold
   */
  public String getInventoryThreshold() {
    return (String) getAttributeInternal(INVENTORYTHRESHOLD);
  }

  /**
   * Sets <code>value</code> as the attribute value for InventoryThreshold.
   *
   * @param value value to set the InventoryThreshold
   */
  public void setInventoryThreshold(String value) {
    setAttributeInternal(INVENTORYTHRESHOLD, value);
  }

  /**
   * Gets the attribute value for SassyBalance, using the alias name SassyBalance.
   *
   * @return the value of SassyBalance
   */
  public Integer getSassyBalance() {
    return (Integer) getAttributeInternal(SASSYBALANCE);
  }

  /**
   * Sets <code>value</code> as the attribute value for SassyBalance.
   *
   * @param value value to set the SassyBalance
   */
  public void setSassyBalance(Integer value) {
    setAttributeInternal(SASSYBALANCE, value);
  }

  /**
   * Gets the attribute value for RoThreshold, using the alias name RoThreshold.
   *
   * @return the value of RoThreshold
   */
  public Integer getRoThreshold() {
    return (Integer) getAttributeInternal(ROTHRESHOLD);
  }

  /**
   * Sets <code>value</code> as the attribute value for RoThreshold.
   *
   * @param value value to set the RoThreshold
   */
  public void setRoThreshold(Integer value) {
    setAttributeInternal(ROTHRESHOLD, value);
  }

  /**
   * Gets the attribute value for Smic, using the alias name Smic.
   *
   * @return the value of Smic
   */
  public String getSmic() {
    return (String) getAttributeInternal(SMIC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Smic.
   *
   * @param value value to set the Smic
   */
  public void setSmic(String value) {
    setAttributeInternal(SMIC, value);
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
   * Gets the attribute value for NewNsn, using the alias name NewNsn.
   *
   * @return the value of NewNsn
   */
  public String getNewNsn() {
    return (String) getAttributeInternal(NEWNSN);
  }

  /**
   * Sets <code>value</code> as the attribute value for NewNsn.
   *
   * @param value value to set the NewNsn
   */
  public void setNewNsn(String value) {
    setAttributeInternal(NEWNSN, value);
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
   * Gets the attribute value for LastMhifUpdateDate, using the alias name LastMhifUpdateDate.
   *
   * @return the value of LastMhifUpdateDate
   */
  public Timestamp getLastMhifUpdateDate() {
    return (Timestamp) getAttributeInternal(LASTMHIFUPDATEDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for LastMhifUpdateDate.
   *
   * @param value value to set the LastMhifUpdateDate
   */
  public void setLastMhifUpdateDate(Timestamp value) {
    setAttributeInternal(LASTMHIFUPDATEDATE, value);
  }

  /**
   * Gets the attribute value for DemilCode, using the alias name DemilCode.
   *
   * @return the value of DemilCode
   */
  public String getDemilCode() {
    return (String) getAttributeInternal(DEMILCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for DemilCode.
   *
   * @param value value to set the DemilCode
   */
  public void setDemilCode(String value) {
    setAttributeInternal(DEMILCODE, value);
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
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getNiinLocation() {
    return (RowIterator) getAttributeInternal(NIINLOCATION);
  }

  /**
   * @return the associated entity oracle.jbo.RowIterator.
   */
  public RowIterator getReceipt() {
    return (RowIterator) getAttributeInternal(RECEIPT);
  }

  /**
   * @param niinId key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence niinId) {
    return new Key(new Object[]{niinId});
  }
}

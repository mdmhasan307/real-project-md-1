package mil.stratis.model.entity.rcv;

import lombok.NoArgsConstructor;
import mil.stratis.model.entity.site.NiinInfoImpl;
import mil.stratis.model.entity.user.UsersImpl;
import mil.stratis.model.entity.wlm.DocumentTypeImpl;
import oracle.jbo.Key;
import oracle.jbo.domain.DBSequence;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class ReceiptImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    RCN {
      public Object get(ReceiptImpl obj) {
        return obj.getRcn();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setRcn((DBSequence) value);
      }
    },
    FRUSTRATE_CODE {
      public Object get(ReceiptImpl obj) {
        return obj.getFrustrateCode();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setFrustrateCode((String) value);
      }
    },
    FRUSTRATE_LOCATION {
      public Object get(ReceiptImpl obj) {
        return obj.getFrustrateLocation();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setFrustrateLocation((String) value);
      }
    },
    QUANTITY_STOWED {
      public Object get(ReceiptImpl obj) {
        return obj.getQuantityStowed();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setQuantityStowed((Number) value);
      }
    },
    QUANTITY_BACKORDERED {
      public Object get(ReceiptImpl obj) {
        return obj.getQuantityBackordered();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setQuantityBackordered((Number) value);
      }
    },
    QUANTITY_RELEASED {
      public Object get(ReceiptImpl obj) {
        return obj.getQuantityReleased();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setQuantityReleased((Number) value);
      }
    },
    QUANTITY_MEASURED {
      public Object get(ReceiptImpl obj) {
        return obj.getQuantityMeasured();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setQuantityMeasured((Number) value);
      }
    },
    QUANTITY_INVOICED {
      public Object get(ReceiptImpl obj) {
        return obj.getQuantityInvoiced();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setQuantityInvoiced((Number) value);
      }
    },
    QUANTITY_INDUCTED {
      public Object get(ReceiptImpl obj) {
        return obj.getQuantityInducted();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setQuantityInducted((Number) value);
      }
    },
    NIIN_ID {
      public Object get(ReceiptImpl obj) {
        return obj.getNiinId();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setNiinId((Number) value);
      }
    },
    CONTRACT_NUMBER {
      public Object get(ReceiptImpl obj) {
        return obj.getContractNumber();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setContractNumber((String) value);
      }
    },
    FUND_CODE {
      public Object get(ReceiptImpl obj) {
        return obj.getFundCode();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setFundCode((String) value);
      }
    },
    SIGNAL_CODE {
      public Object get(ReceiptImpl obj) {
        return obj.getSignalCode();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setSignalCode((String) value);
      }
    },
    DOCUMENT_NUMBER {
      public Object get(ReceiptImpl obj) {
        return obj.getDocumentNumber();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setDocumentNumber((String) value);
      }
    },
    DOCUMENT_ID {
      public Object get(ReceiptImpl obj) {
        return obj.getDocumentId();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setDocumentId((String) value);
      }
    },
    STATUS {
      public Object get(ReceiptImpl obj) {
        return obj.getStatus();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setStatus((String) value);
      }
    },
    CREATED_BY {
      public Object get(ReceiptImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setCreatedBy((Number) value);
      }
    },
    CREATED_DATE {
      public Object get(ReceiptImpl obj) {
        return obj.getCreatedDate();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setCreatedDate((Date) value);
      }
    },
    MODIFIED_BY {
      public Object get(ReceiptImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setModifiedBy((Number) value);
      }
    },
    MODIFIED_DATE {
      public Object get(ReceiptImpl obj) {
        return obj.getModifiedDate();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setModifiedDate((Date) value);
      }
    },
    CONVERSION_FLAG {
      public Object get(ReceiptImpl obj) {
        return obj.getConversionFlag();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setConversionFlag((String) value);
      }
    },
    ROUTING_ID {
      public Object get(ReceiptImpl obj) {
        return obj.getRoutingId();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setRoutingId((String) value);
      }
    },
    WORK_STATION {
      public Object get(ReceiptImpl obj) {
        return obj.getWorkStation();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setWorkStation((String) value);
      }
    },
    RDD {
      public Object get(ReceiptImpl obj) {
        return obj.getRdd();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setRdd((String) value);
      }
    },
    SUPPLEMENTARY_ADDRESS {
      public Object get(ReceiptImpl obj) {
        return obj.getSupplementaryAddress();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setSupplementaryAddress((String) value);
      }
    },
    CONSIGNEE {
      public Object get(ReceiptImpl obj) {
        return obj.getConsignee();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setConsignee((String) value);
      }
    },
    DOD_DIST_CODE {
      public Object get(ReceiptImpl obj) {
        return obj.getDodDistCode();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setDodDistCode((String) value);
      }
    },
    RPD {
      public Object get(ReceiptImpl obj) {
        return obj.getRpd();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setRpd((String) value);
      }
    },
    PARTIAL_SHIPMENT_INDICATOR {
      public Object get(ReceiptImpl obj) {
        return obj.getPartialShipmentIndicator();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setPartialShipmentIndicator((String) value);
      }
    },
    TRACEABILITY_NUMBER {
      public Object get(ReceiptImpl obj) {
        return obj.getTraceabilityNumber();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setTraceabilityNumber((String) value);
      }
    },
    CLASS_COMMODITY_NUMBER {
      public Object get(ReceiptImpl obj) {
        return obj.getClassCommodityNumber();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setClassCommodityNumber((String) value);
      }
    },
    SHIPPED_FROM {
      public Object get(ReceiptImpl obj) {
        return obj.getShippedFrom();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setShippedFrom((String) value);
      }
    },
    CC {
      public Object get(ReceiptImpl obj) {
        return obj.getCc();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setCc((String) value);
      }
    },
    PROJECT_CODE {
      public Object get(ReceiptImpl obj) {
        return obj.getProjectCode();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setProjectCode((String) value);
      }
    },
    PC {
      public Object get(ReceiptImpl obj) {
        return obj.getPc();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setPc((String) value);
      }
    },
    COGNIZANCE_CODE {
      public Object get(ReceiptImpl obj) {
        return obj.getCognizanceCode();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setCognizanceCode((String) value);
      }
    },
    MECH_NON_MECH_FLAG {
      public Object get(ReceiptImpl obj) {
        return obj.getMechNonMechFlag();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setMechNonMechFlag((String) value);
      }
    },
    RATION {
      public Object get(ReceiptImpl obj) {
        return obj.getRation();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setRation((String) value);
      }
    },
    SUFFIX {
      public Object get(ReceiptImpl obj) {
        return obj.getSuffix();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setSuffix((String) value);
      }
    },
    SHELF_LIFE_CODE {
      public Object get(ReceiptImpl obj) {
        return obj.getShelfLifeCode();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setShelfLifeCode((String) value);
      }
    },
    WEIGHT {
      public Object get(ReceiptImpl obj) {
        return obj.getWeight();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setWeight((Number) value);
      }
    },
    LENGTH {
      public Object get(ReceiptImpl obj) {
        return obj.getLength();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setLength((Number) value);
      }
    },
    HEIGHT {
      public Object get(ReceiptImpl obj) {
        return obj.getHeight();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setHeight((Number) value);
      }
    },
    WIDTH {
      public Object get(ReceiptImpl obj) {
        return obj.getWidth();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setWidth((Number) value);
      }
    },
    UI {
      public Object get(ReceiptImpl obj) {
        return obj.getUi();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setUi((String) value);
      }
    },
    PRICE {
      public Object get(ReceiptImpl obj) {
        return obj.getPrice();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setPrice((Number) value);
      }
    },
    FSC {
      public Object get(ReceiptImpl obj) {
        return obj.getFsc();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setFsc((String) value);
      }
    },
    PART_NUMBER {
      public Object get(ReceiptImpl obj) {
        return obj.getPartNumber();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setPartNumber((String) value);
      }
    },
    SERIAL_NUMBER {
      public Object get(ReceiptImpl obj) {
        return obj.getSerialNumber();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setSerialNumber((String) value);
      }
    },
    RI {
      public Object get(ReceiptImpl obj) {
        return obj.getRi();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setRi((String) value);
      }
    },
    CUBE {
      public Object get(ReceiptImpl obj) {
        return obj.getCube();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setCube((Number) value);
      }
    },
    PACKING_CONSOLIDATION_ID {
      public Object get(ReceiptImpl obj) {
        return obj.getPackingConsolidationId();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setPackingConsolidationId((Number) value);
      }
    },
    NIIN {
      public Object get(ReceiptImpl obj) {
        return obj.getNiin();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setNiin((String) value);
      }
    },
    PARTIAL_SHIPMENT {
      public Object get(ReceiptImpl obj) {
        return obj.getPartialShipment();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setPartialShipment((String) value);
      }
    },
    QTY_STOW_LOSS {
      public Object get(ReceiptImpl obj) {
        return obj.getQtyStowloss();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setQtyStowloss((Number) value);
      }
    },
    OVERAGE_FLAG {
      public Object get(ReceiptImpl obj) {
        return obj.getOverageFlag();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setOverageFlag((String) value);
      }
    },
    SHORTAGE_FLAG {
      public Object get(ReceiptImpl obj) {
        return obj.getShortageFlag();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setShortageFlag((String) value);
      }
    },
    NIIN_INFO {
      public Object get(ReceiptImpl obj) {
        return obj.getNiinInfo();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setNiinInfo((NiinInfoImpl) value);
      }
    },
    USERS {
      public Object get(ReceiptImpl obj) {
        return obj.getUsers();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setUsers((UsersImpl) value);
      }
    },
    DOCUMENT_TYPE {
      public Object get(ReceiptImpl obj) {
        return obj.getDocumentType();
      }

      public void put(ReceiptImpl obj, Object value) {
        obj.setDocumentType((DocumentTypeImpl) value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(ReceiptImpl object);

    public abstract void put(ReceiptImpl object, Object value);

    public int index() {
      return ReceiptImpl.AttributesEnum.firstIndex() + ordinal();
    }

    public static int firstIndex() {
      return FIRST_INDEX;
    }

    public static int count() {
      return ReceiptImpl.AttributesEnum.firstIndex() + ReceiptImpl.AttributesEnum.staticValues().length;
    }

    public static AttributesEnum[] staticValues() {
      return AttributesEnum.values();
    }
  }

  public static final int RCN = AttributesEnum.RCN.index();
  public static final int FRUSTRATECODE = AttributesEnum.FRUSTRATE_CODE.index();
  public static final int FRUSTRATELOCATION = AttributesEnum.FRUSTRATE_LOCATION.index();
  public static final int QUANTITYSTOWED = AttributesEnum.QUANTITY_STOWED.index();
  public static final int QUANTITYBACKORDERED = AttributesEnum.QUANTITY_BACKORDERED.index();
  public static final int QUANTITYRELEASED = AttributesEnum.QUANTITY_RELEASED.index();
  public static final int QUANTITYMEASURED = AttributesEnum.QUANTITY_MEASURED.index();
  public static final int QUANTITYINVOICED = AttributesEnum.QUANTITY_INVOICED.index();
  public static final int QUANTITYINDUCTED = AttributesEnum.QUANTITY_INDUCTED.index();
  public static final int NIINID = AttributesEnum.NIIN_ID.index();
  public static final int CONTRACTNUMBER = AttributesEnum.CONTRACT_NUMBER.index();
  public static final int FUNDCODE = AttributesEnum.FUND_CODE.index();
  public static final int SIGNALCODE = AttributesEnum.SIGNAL_CODE.index();
  public static final int DOCUMENTNUMBER = AttributesEnum.DOCUMENT_NUMBER.index();
  public static final int DOCUMENTID = AttributesEnum.DOCUMENT_ID.index();
  public static final int STATUS = AttributesEnum.STATUS.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int CREATEDDATE = AttributesEnum.CREATED_DATE.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int MODIFIEDDATE = AttributesEnum.MODIFIED_DATE.index();
  public static final int CONVERSIONFLAG = AttributesEnum.CONVERSION_FLAG.index();
  public static final int ROUTINGID = AttributesEnum.ROUTING_ID.index();
  public static final int WORKSTATION = AttributesEnum.WORK_STATION.index();
  public static final int RDD = AttributesEnum.RDD.index();
  public static final int SUPPLEMENTARYADDRESS = AttributesEnum.SUPPLEMENTARY_ADDRESS.index();
  public static final int CONSIGNEE = AttributesEnum.CONSIGNEE.index();
  public static final int DODDISTCODE = AttributesEnum.DOD_DIST_CODE.index();
  public static final int RPD = AttributesEnum.RPD.index();
  public static final int PARTIALSHIPMENTINDICATOR = AttributesEnum.PARTIAL_SHIPMENT_INDICATOR.index();
  public static final int TRACEABILITYNUMBER = AttributesEnum.TRACEABILITY_NUMBER.index();
  public static final int CLASSCOMMODITYNUMBER = AttributesEnum.CLASS_COMMODITY_NUMBER.index();
  public static final int SHIPPEDFROM = AttributesEnum.SHIPPED_FROM.index();
  public static final int CC = AttributesEnum.CC.index();
  public static final int PROJECTCODE = AttributesEnum.PROJECT_CODE.index();
  public static final int PC = AttributesEnum.PC.index();
  public static final int COGNIZANCECODE = AttributesEnum.COGNIZANCE_CODE.index();
  public static final int MECHNONMECHFLAG = AttributesEnum.MECH_NON_MECH_FLAG.index();
  public static final int RATION = AttributesEnum.RATION.index();
  public static final int SUFFIX = AttributesEnum.SUFFIX.index();
  public static final int SHELFLIFECODE = AttributesEnum.SHELF_LIFE_CODE.index();
  public static final int WEIGHT = AttributesEnum.WEIGHT.index();
  public static final int LENGTH = AttributesEnum.LENGTH.index();
  public static final int HEIGHT = AttributesEnum.HEIGHT.index();
  public static final int WIDTH = AttributesEnum.WIDTH.index();
  public static final int UI = AttributesEnum.UI.index();
  public static final int PRICE = AttributesEnum.PRICE.index();
  public static final int FSC = AttributesEnum.FSC.index();
  public static final int PARTNUMBER = AttributesEnum.PART_NUMBER.index();
  public static final int SERIALNUMBER = AttributesEnum.SERIAL_NUMBER.index();
  public static final int RI = AttributesEnum.RI.index();
  public static final int CUBE = AttributesEnum.CUBE.index();
  public static final int PACKINGCONSOLIDATIONID = AttributesEnum.PACKING_CONSOLIDATION_ID.index();
  public static final int NIIN = AttributesEnum.NIIN.index();
  public static final int PARTIALSHIPMENT = AttributesEnum.PARTIAL_SHIPMENT.index();
  public static final int QTYSTOWLOSS = AttributesEnum.QTY_STOW_LOSS.index();
  public static final int OVERAGEFLAG = AttributesEnum.OVERAGE_FLAG.index();
  public static final int SHORTAGEFLAG = AttributesEnum.SHORTAGE_FLAG.index();
  public static final int NIININFO = AttributesEnum.NIIN_INFO.index();
  public static final int USERS = AttributesEnum.USERS.index();
  public static final int DOCUMENTTYPE = AttributesEnum.DOCUMENT_TYPE.index();

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.entities.rcv.Receipt");
  }

  /**
   * Gets the attribute value for Rcn, using the alias name Rcn
   */
  public DBSequence getRcn() {
    return (DBSequence) getAttributeInternal(RCN);
  }

  /**
   * Gets the attribute value for FrustrateCode, using the alias name FrustrateCode
   */
  public String getFrustrateCode() {
    return (String) getAttributeInternal(FRUSTRATECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for FrustrateCode
   */
  public void setFrustrateCode(String value) {
    setAttributeInternal(FRUSTRATECODE, value);
  }

  /**
   * Gets the attribute value for FrustrateLocation, using the alias name FrustrateLocation
   */
  public String getFrustrateLocation() {
    return (String) getAttributeInternal(FRUSTRATELOCATION);
  }

  /**
   * Sets <code>value</code> as the attribute value for FrustrateLocation
   */
  public void setFrustrateLocation(String value) {
    setAttributeInternal(FRUSTRATELOCATION, value);
  }

  /**
   * Gets the attribute value for QuantityStowed, using the alias name QuantityStowed
   */
  public Number getQuantityStowed() {
    return (Number) getAttributeInternal(QUANTITYSTOWED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityStowed
   */
  public void setQuantityStowed(Number value) {
    setAttributeInternal(QUANTITYSTOWED, value);
  }

  /**
   * Gets the attribute value for QuantityBackordered, using the alias name QuantityBackordered
   */
  public Number getQuantityBackordered() {
    return (Number) getAttributeInternal(QUANTITYBACKORDERED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityBackordered
   */
  public void setQuantityBackordered(Number value) {
    setAttributeInternal(QUANTITYBACKORDERED, value);
  }

  /**
   * Gets the attribute value for QuantityReleased, using the alias name QuantityReleased
   */
  public Number getQuantityReleased() {
    return (Number) getAttributeInternal(QUANTITYRELEASED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityReleased
   */
  public void setQuantityReleased(Number value) {
    setAttributeInternal(QUANTITYRELEASED, value);
  }

  /**
   * Gets the attribute value for QuantityMeasured, using the alias name QuantityMeasured
   */
  public Number getQuantityMeasured() {
    return (Number) getAttributeInternal(QUANTITYMEASURED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityMeasured
   */
  public void setQuantityMeasured(Number value) {
    setAttributeInternal(QUANTITYMEASURED, value);
  }

  /**
   * Gets the attribute value for QuantityInvoiced, using the alias name QuantityInvoiced
   */
  public Number getQuantityInvoiced() {
    return (Number) getAttributeInternal(QUANTITYINVOICED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityInvoiced
   */
  public void setQuantityInvoiced(Number value) {
    setAttributeInternal(QUANTITYINVOICED, value);
  }

  /**
   * Gets the attribute value for QuantityInducted, using the alias name QuantityInducted
   */
  public Number getQuantityInducted() {
    return (Number) getAttributeInternal(QUANTITYINDUCTED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QuantityInducted
   */
  public void setQuantityInducted(Number value) {
    setAttributeInternal(QUANTITYINDUCTED, value);
  }

  /**
   * Gets the attribute value for NiinId, using the alias name NiinId
   */
  public Number getNiinId() {
    return (Number) getAttributeInternal(NIINID);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinId
   */
  public void setNiinId(Number value) {
    setAttributeInternal(NIINID, value);
  }

  /**
   * Gets the attribute value for ContractNumber, using the alias name ContractNumber
   */
  public String getContractNumber() {
    return (String) getAttributeInternal(CONTRACTNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for ContractNumber
   */
  public void setContractNumber(String value) {
    setAttributeInternal(CONTRACTNUMBER, value);
  }

  /**
   * Gets the attribute value for FundCode, using the alias name FundCode
   */
  public String getFundCode() {
    return (String) getAttributeInternal(FUNDCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for FundCode
   */
  public void setFundCode(String value) {
    setAttributeInternal(FUNDCODE, value);
  }

  /**
   * Gets the attribute value for SignalCode, using the alias name SignalCode
   */
  public String getSignalCode() {
    return (String) getAttributeInternal(SIGNALCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for SignalCode
   */
  public void setSignalCode(String value) {
    setAttributeInternal(SIGNALCODE, value);
  }

  /**
   * Gets the attribute value for DocumentNumber, using the alias name DocumentNumber
   */
  public String getDocumentNumber() {
    return (String) getAttributeInternal(DOCUMENTNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for DocumentNumber
   */
  public void setDocumentNumber(String value) {
    setAttributeInternal(DOCUMENTNUMBER, value);
  }

  /**
   * Gets the attribute value for DocumentId, using the alias name DocumentId
   */
  public String getDocumentId() {
    return (String) getAttributeInternal(DOCUMENTID);
  }

  /**
   * Sets <code>value</code> as the attribute value for DocumentId
   */
  public void setDocumentId(String value) {
    setAttributeInternal(DOCUMENTID, value);
  }

  /**
   * Gets the attribute value for Status, using the alias name Status
   */
  public String getStatus() {
    return (String) getAttributeInternal(STATUS);
  }

  /**
   * Sets <code>value</code> as the attribute value for Status
   */
  public void setStatus(String value) {
    setAttributeInternal(STATUS, value);
  }

  /**
   * Gets the attribute value for CreatedBy, using the alias name CreatedBy
   */
  public Number getCreatedBy() {
    return (Number) getAttributeInternal(CREATEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedBy
   */
  public void setCreatedBy(Number value) {
    setAttributeInternal(CREATEDBY, value);
  }

  /**
   * Gets the attribute value for CreatedDate, using the alias name CreatedDate
   */
  public Date getCreatedDate() {
    return (Date) getAttributeInternal(CREATEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CreatedDate
   */
  public void setCreatedDate(Date value) {
    setAttributeInternal(CREATEDDATE, value);
  }

  /**
   * Gets the attribute value for ModifiedBy, using the alias name ModifiedBy
   */
  public Number getModifiedBy() {
    return (Number) getAttributeInternal(MODIFIEDBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedBy
   */
  public void setModifiedBy(Number value) {
    setAttributeInternal(MODIFIEDBY, value);
  }

  /**
   * Gets the attribute value for ModifiedDate, using the alias name ModifiedDate
   */
  public Date getModifiedDate() {
    return (Date) getAttributeInternal(MODIFIEDDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ModifiedDate
   */
  public void setModifiedDate(Date value) {
    setAttributeInternal(MODIFIEDDATE, value);
  }

  /**
   * Gets the attribute value for ConversionFlag, using the alias name ConversionFlag
   */
  public String getConversionFlag() {
    return (String) getAttributeInternal(CONVERSIONFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for ConversionFlag
   */
  public void setConversionFlag(String value) {
    setAttributeInternal(CONVERSIONFLAG, value);
  }

  /**
   * Gets the attribute value for RoutingId, using the alias name RoutingId
   */
  public String getRoutingId() {
    return (String) getAttributeInternal(ROUTINGID);
  }

  /**
   * Sets <code>value</code> as the attribute value for RoutingId
   */
  public void setRoutingId(String value) {
    setAttributeInternal(ROUTINGID, value);
  }

  /**
   * Gets the attribute value for WorkStation, using the alias name WorkStation
   */
  public String getWorkStation() {
    return (String) getAttributeInternal(WORKSTATION);
  }

  /**
   * Sets <code>value</code> as the attribute value for WorkStation
   */
  public void setWorkStation(String value) {
    setAttributeInternal(WORKSTATION, value);
  }

  /**
   * Gets the attribute value for Rdd, using the alias name Rdd
   */
  public String getRdd() {
    return (String) getAttributeInternal(RDD);
  }

  /**
   * Sets <code>value</code> as the attribute value for Rdd
   */
  public void setRdd(String value) {
    setAttributeInternal(RDD, value);
  }

  /**
   * Gets the attribute value for SupplementaryAddress, using the alias name SupplementaryAddress
   */
  public String getSupplementaryAddress() {
    return (String) getAttributeInternal(SUPPLEMENTARYADDRESS);
  }

  /**
   * Sets <code>value</code> as the attribute value for SupplementaryAddress
   */
  public void setSupplementaryAddress(String value) {
    setAttributeInternal(SUPPLEMENTARYADDRESS, value);
  }

  /**
   * Gets the attribute value for Consignee, using the alias name Consignee
   */
  public String getConsignee() {
    return (String) getAttributeInternal(CONSIGNEE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Consignee
   */
  public void setConsignee(String value) {
    setAttributeInternal(CONSIGNEE, value);
  }

  /**
   * Gets the attribute value for DodDistCode, using the alias name DodDistCode
   */
  public String getDodDistCode() {
    return (String) getAttributeInternal(DODDISTCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for DodDistCode
   */
  public void setDodDistCode(String value) {
    setAttributeInternal(DODDISTCODE, value);
  }

  /**
   * Gets the attribute value for Rpd, using the alias name Rpd
   */
  public String getRpd() {
    return (String) getAttributeInternal(RPD);
  }

  /**
   * Sets <code>value</code> as the attribute value for Rpd
   */
  public void setRpd(String value) {
    setAttributeInternal(RPD, value);
  }

  /**
   * Gets the attribute value for PartialShipmentIndicator, using the alias name PartialShipmentIndicator
   */
  public String getPartialShipmentIndicator() {
    return (String) getAttributeInternal(PARTIALSHIPMENTINDICATOR);
  }

  /**
   * Sets <code>value</code> as the attribute value for PartialShipmentIndicator
   */
  public void setPartialShipmentIndicator(String value) {
    setAttributeInternal(PARTIALSHIPMENTINDICATOR, value);
  }

  /**
   * Gets the attribute value for TraceabilityNumber, using the alias name TraceabilityNumber
   */
  public String getTraceabilityNumber() {
    return (String) getAttributeInternal(TRACEABILITYNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for TraceabilityNumber
   */
  public void setTraceabilityNumber(String value) {
    setAttributeInternal(TRACEABILITYNUMBER, value);
  }

  /**
   * Gets the attribute value for ClassCommodityNumber, using the alias name ClassCommodityNumber
   */
  public String getClassCommodityNumber() {
    return (String) getAttributeInternal(CLASSCOMMODITYNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for ClassCommodityNumber
   */
  public void setClassCommodityNumber(String value) {
    setAttributeInternal(CLASSCOMMODITYNUMBER, value);
  }

  /**
   * Gets the attribute value for ShippedFrom, using the alias name ShippedFrom
   */
  public String getShippedFrom() {
    return (String) getAttributeInternal(SHIPPEDFROM);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShippedFrom
   */
  public void setShippedFrom(String value) {
    setAttributeInternal(SHIPPEDFROM, value);
  }

  /**
   * getAttrInvokeAccessor: generated method. Do not modify.
   */
  @Override
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
  @Override
  protected void setAttrInvokeAccessor(int index, Object value,
                                       AttributeDefImpl attrDef) throws Exception {
    if ((index >= AttributesEnum.firstIndex()) && (index < AttributesEnum.count())) {
      AttributesEnum.staticValues()[index - AttributesEnum.firstIndex()].put(this, value);
      return;
    }
    super.setAttrInvokeAccessor(index, value, attrDef);
  }

  /**
   * Gets the associated entity NiinInfoImpl
   */
  public NiinInfoImpl getNiinInfo() {
    return (NiinInfoImpl) getAttributeInternal(NIININFO);
  }

  /**
   * Sets <code>value</code> as the associated entity NiinInfoImpl
   */
  public void setNiinInfo(NiinInfoImpl value) {
    setAttributeInternal(NIININFO, value);
  }

  /**
   * @return the associated entity mil.stratis.entities.UsersImpl.
   */
  public UsersImpl getUsers() {
    return (UsersImpl) getAttributeInternal(USERS);
  }

  /**
   * Sets <code>value</code> as the associated entity mil.stratis.entities.UsersImpl.
   */
  public void setUsers(UsersImpl value) {
    setAttributeInternal(USERS, value);
  }

  /**
   * @return the associated entity mil.stratis.entities.wlm.DocumentTypeImpl.
   */
  public DocumentTypeImpl getDocumentType() {
    return (DocumentTypeImpl) getAttributeInternal(DOCUMENTTYPE);
  }

  /**
   * Sets <code>value</code> as the associated entity mil.stratis.entities.wlm.DocumentTypeImpl.
   */
  public void setDocumentType(DocumentTypeImpl value) {
    setAttributeInternal(DOCUMENTTYPE, value);
  }

  /**
   * @param rcn key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(DBSequence rcn) {
    return new Key(new Object[]{rcn});
  }

  /**
   * Gets the attribute value for Cc, using the alias name Cc
   */
  public String getCc() {
    return (String) getAttributeInternal(CC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Cc
   */
  public void setCc(String value) {
    setAttributeInternal(CC, value);
  }

  /**
   * Gets the attribute value for ProjectCode, using the alias name ProjectCode
   */
  public String getProjectCode() {
    return (String) getAttributeInternal(PROJECTCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ProjectCode
   */
  public void setProjectCode(String value) {
    setAttributeInternal(PROJECTCODE, value);
  }

  /**
   * Gets the attribute value for Pc, using the alias name Pc
   */
  public String getPc() {
    return (String) getAttributeInternal(PC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Pc
   */
  public void setPc(String value) {
    setAttributeInternal(PC, value);
  }

  /**
   * Gets the attribute value for CognizanceCode, using the alias name CognizanceCode
   */
  public String getCognizanceCode() {
    return (String) getAttributeInternal(COGNIZANCECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for CognizanceCode
   */
  public void setCognizanceCode(String value) {
    setAttributeInternal(COGNIZANCECODE, value);
  }

  /**
   * Gets the attribute value for MechNonMechFlag, using the alias name MechNonMechFlag
   */
  public String getMechNonMechFlag() {
    return (String) getAttributeInternal(MECHNONMECHFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for MechNonMechFlag
   */
  public void setMechNonMechFlag(String value) {
    setAttributeInternal(MECHNONMECHFLAG, value);
  }

  /**
   * Gets the attribute value for Ration, using the alias name Ration
   */
  public String getRation() {
    return (String) getAttributeInternal(RATION);
  }

  /**
   * Sets <code>value</code> as the attribute value for Ration
   */
  public void setRation(String value) {
    setAttributeInternal(RATION, value);
  }

  /**
   * Gets the attribute value for Suffix, using the alias name Suffix
   */
  public String getSuffix() {
    return (String) getAttributeInternal(SUFFIX);
  }

  /**
   * Sets <code>value</code> as the attribute value for Suffix
   */
  public void setSuffix(String value) {
    setAttributeInternal(SUFFIX, value);
  }

  /**
   * Gets the attribute value for ShelfLifeCode, using the alias name ShelfLifeCode
   */
  public String getShelfLifeCode() {
    return (String) getAttributeInternal(SHELFLIFECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShelfLifeCode
   */
  public void setShelfLifeCode(String value) {
    setAttributeInternal(SHELFLIFECODE, value);
  }

  /**
   * Gets the attribute value for Weight, using the alias name Weight
   */
  public Number getWeight() {
    return (Number) getAttributeInternal(WEIGHT);
  }

  /**
   * Sets <code>value</code> as the attribute value for Weight
   */
  public void setWeight(Number value) {
    setAttributeInternal(WEIGHT, value);
  }

  /**
   * Gets the attribute value for Length, using the alias name Length
   */
  public Number getLength() {
    return (Number) getAttributeInternal(LENGTH);
  }

  /**
   * Sets <code>value</code> as the attribute value for Length
   */
  public void setLength(Number value) {
    setAttributeInternal(LENGTH, value);
  }

  /**
   * Gets the attribute value for Height, using the alias name Height
   */
  public Number getHeight() {
    return (Number) getAttributeInternal(HEIGHT);
  }

  /**
   * Sets <code>value</code> as the attribute value for Height
   */
  public void setHeight(Number value) {
    setAttributeInternal(HEIGHT, value);
  }

  /**
   * Gets the attribute value for Width, using the alias name Width
   */
  public Number getWidth() {
    return (Number) getAttributeInternal(WIDTH);
  }

  /**
   * Sets <code>value</code> as the attribute value for Width
   */
  public void setWidth(Number value) {
    setAttributeInternal(WIDTH, value);
  }

  /**
   * Gets the attribute value for Ui, using the alias name Ui
   */
  public String getUi() {
    return (String) getAttributeInternal(UI);
  }

  /**
   * Sets <code>value</code> as the attribute value for Ui
   */
  public void setUi(String value) {
    setAttributeInternal(UI, value);
  }

  /**
   * Gets the attribute value for Price, using the alias name Price
   */
  public Number getPrice() {
    return (Number) getAttributeInternal(PRICE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Price
   */
  public void setPrice(Number value) {
    setAttributeInternal(PRICE, value);
  }

  /**
   * Gets the attribute value for Fsc, using the alias name Fsc
   */
  public String getFsc() {
    return (String) getAttributeInternal(FSC);
  }

  /**
   * Sets <code>value</code> as the attribute value for Fsc
   */
  public void setFsc(String value) {
    setAttributeInternal(FSC, value);
  }

  /**
   * Gets the attribute value for PartNumber, using the alias name PartNumber
   */
  public String getPartNumber() {
    return (String) getAttributeInternal(PARTNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for PartNumber
   */
  public void setPartNumber(String value) {
    setAttributeInternal(PARTNUMBER, value);
  }

  /**
   * Sets <code>value</code> as the attribute value for Rcn
   */
  public void setRcn(DBSequence value) {
    setAttributeInternal(RCN, value);
  }

  /**
   * Gets the attribute value for SerialNumber, using the alias name SerialNumber
   */
  public String getSerialNumber() {
    return (String) getAttributeInternal(SERIALNUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for SerialNumber
   */
  public void setSerialNumber(String value) {
    setAttributeInternal(SERIALNUMBER, value);
  }

  /**
   * Gets the attribute value for Ri, using the alias name Ri
   */
  public String getRi() {
    return (String) getAttributeInternal(RI);
  }

  /**
   * Sets <code>value</code> as the attribute value for Ri
   */
  public void setRi(String value) {
    setAttributeInternal(RI, value);
  }

  /**
   * Gets the attribute value for Cube, using the alias name Cube
   */
  public Number getCube() {
    return (Number) getAttributeInternal(CUBE);
  }

  /**
   * Sets <code>value</code> as the attribute value for Cube
   */
  public void setCube(Number value) {
    setAttributeInternal(CUBE, value);
  }

  /**
   * Gets the attribute value for PackingConsolidationId, using the alias name PackingConsolidationId
   */
  public Number getPackingConsolidationId() {
    return (Number) getAttributeInternal(PACKINGCONSOLIDATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for PackingConsolidationId
   */
  public void setPackingConsolidationId(Number value) {
    setAttributeInternal(PACKINGCONSOLIDATIONID, value);
  }

  /**
   * Gets the attribute value for Niin, using the alias name Niin
   */
  public String getNiin() {
    return (String) getAttributeInternal(NIIN);
  }

  /**
   * Sets <code>value</code> as the attribute value for Niin
   */
  public void setNiin(String value) {
    setAttributeInternal(NIIN, value);
  }

  /**
   * Gets the attribute value for PartialShipment, using the alias name PartialShipment
   */
  public String getPartialShipment() {
    return (String) getAttributeInternal(PARTIALSHIPMENT);
  }

  /**
   * Sets <code>value</code> as the attribute value for PartialShipment
   */
  public void setPartialShipment(String value) {
    setAttributeInternal(PARTIALSHIPMENT, value);
  }

  /**
   * Gets the attribute value for QtyStowloss, using the alias name QtyStowloss
   */
  public Number getQtyStowloss() {
    return (Number) getAttributeInternal(QTYSTOWLOSS);
  }

  /**
   * Sets <code>value</code> as the attribute value for QtyStowloss
   */
  public void setQtyStowloss(Number value) {
    setAttributeInternal(QTYSTOWLOSS, value);
  }

  /**
   * Gets the attribute value for OverageFlag, using the alias name OverageFlag.
   *
   * @return the value of OverageFlag
   */
  public String getOverageFlag() {
    return (String) getAttributeInternal(OVERAGEFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for OverageFlag.
   *
   * @param value value to set the OverageFlag
   */
  public void setOverageFlag(String value) {
    setAttributeInternal(OVERAGEFLAG, value);
  }

  /**
   * Gets the attribute value for ShortageFlag, using the alias name ShortageFlag.
   *
   * @return the value of ShortageFlag
   */
  public String getShortageFlag() {
    return (String) getAttributeInternal(SHORTAGEFLAG);
  }

  /**
   * Sets <code>value</code> as the attribute value for ShortageFlag.
   *
   * @param value value to set the ShortageFlag
   */
  public void setShortageFlag(String value) {
    setAttributeInternal(SHORTAGEFLAG, value);
  }
}
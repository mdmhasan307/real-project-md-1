package mil.stratis.model.entity.wlm;

import lombok.NoArgsConstructor;
import mil.stratis.model.entity.site.CustomerImpl;
import oracle.jbo.Key;
import oracle.jbo.RowIterator;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.AttributeDefImpl;
import oracle.jbo.server.EntityDefImpl;
import oracle.jbo.server.EntityImpl;

@NoArgsConstructor
public class IssueImpl extends EntityImpl {

  /**
   * AttributesEnum: generated enum for identifying attributes and accessors. Do not modify.
   */
  public enum AttributesEnum {
    SCN {
      public Object get(IssueImpl obj) {
        return obj.getScn();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setScn((String) value);
      }
    },
    DOCUMENT_ID {
      public Object get(IssueImpl obj) {
        return obj.getDocumentId();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setDocumentId((String) value);
      }
    },
    DOCUMENT_NUMBER {
      public Object get(IssueImpl obj) {
        return obj.getDocumentNumber();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setDocumentNumber((String) value);
      }
    },
    QTY {
      public Object get(IssueImpl obj) {
        return obj.getQty();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setQty((Number) value);
      }
    },
    ISSUE_TYPE {
      public Object get(IssueImpl obj) {
        return obj.getIssueType();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setIssueType((String) value);
      }
    },
    ISSUE_PRIORITY_DESIGNATOR {
      public Object get(IssueImpl obj) {
        return obj.getIssuePriorityDesignator();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setIssuePriorityDesignator((String) value);
      }
    },
    ISSUE_PRIORITY_GROUP {
      public Object get(IssueImpl obj) {
        return obj.getIssuePriorityGroup();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setIssuePriorityGroup((String) value);
      }
    },
    CUSTOMER_ID {
      public Object get(IssueImpl obj) {
        return obj.getCustomerId();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setCustomerId((Number) value);
      }
    },
    NIIN_ID {
      public Object get(IssueImpl obj) {
        return obj.getNiinId();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setNiinId((Number) value);
      }
    },
    STATUS {
      public Object get(IssueImpl obj) {
        return obj.getStatus();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setStatus((String) value);
      }
    },
    CREATED_BY {
      public Object get(IssueImpl obj) {
        return obj.getCreatedBy();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setCreatedBy((Number) value);
      }
    },
    CREATED_DATE {
      public Object get(IssueImpl obj) {
        return obj.getCreatedDate();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setCreatedDate((Date) value);
      }
    },
    MODIFIED_BY {
      public Object get(IssueImpl obj) {
        return obj.getModifiedBy();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setModifiedBy((Number) value);
      }
    },
    MODIFIED_DATE {
      public Object get(IssueImpl obj) {
        return obj.getModifiedDate();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setModifiedDate((Date) value);
      }
    },
    PACKING_CONSOLIDATION_ID {
      public Object get(IssueImpl obj) {
        return obj.getPackingConsolidationId();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setPackingConsolidationId((Number) value);
      }
    },
    SUFFIX {
      public Object get(IssueImpl obj) {
        return obj.getSuffix();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setSuffix((String) value);
      }
    },
    RCN {
      public Object get(IssueImpl obj) {
        return obj.getRcn();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setRcn((Number) value);
      }
    },
    RDD {
      public Object get(IssueImpl obj) {
        return obj.getRdd();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setRdd((String) value);
      }
    },
    SUPPLEMENTARY_ADDRESS {
      public Object get(IssueImpl obj) {
        return obj.getSupplementaryAddress();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setSupplementaryAddress((String) value);
      }
    },
    FUND_CODE {
      public Object get(IssueImpl obj) {
        return obj.getFundCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setFundCode((String) value);
      }
    },
    MEDIA_AND_STATUS_CODE {
      public Object get(IssueImpl obj) {
        return obj.getMediaAndStatusCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setMediaAndStatusCode((String) value);
      }
    },
    ROUTING_ID_FROM {
      public Object get(IssueImpl obj) {
        return obj.getRoutingIdFrom();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setRoutingIdFrom((String) value);
      }
    },
    SIGNAL_CODE {
      public Object get(IssueImpl obj) {
        return obj.getSignalCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setSignalCode((String) value);
      }
    },
    DISTRIBUTION_CODE {
      public Object get(IssueImpl obj) {
        return obj.getDistributionCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setDistributionCode((String) value);
      }
    },
    PROJECT_CODE {
      public Object get(IssueImpl obj) {
        return obj.getProjectCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setProjectCode((String) value);
      }
    },
    ADVICE_CODE {
      public Object get(IssueImpl obj) {
        return obj.getAdviceCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setAdviceCode((String) value);
      }
    },
    ERO_NUMBER {
      public Object get(IssueImpl obj) {
        return obj.getEroNumber();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setEroNumber((String) value);
      }
    },
    CC {
      public Object get(IssueImpl obj) {
        return obj.getCc();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setCc((String) value);
      }
    },
    DATE_BACK_ORDERED {
      public Object get(IssueImpl obj) {
        return obj.getDateBackOrdered();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setDateBackOrdered((String) value);
      }
    },
    COST_JON {
      public Object get(IssueImpl obj) {
        return obj.getCostJon();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setCostJon((String) value);
      }
    },
    QTY_ISSUED {
      public Object get(IssueImpl obj) {
        return obj.getQtyIssued();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setQtyIssued((Number) value);
      }
    },
    DEMIL_CODE {
      public Object get(IssueImpl obj) {
        return obj.getDemilCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setDemilCode((String) value);
      }
    },
    DISPOSAL_CODE {
      public Object get(IssueImpl obj) {
        return obj.getDisposalCode();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setDisposalCode((String) value);
      }
    },
    RON {
      public Object get(IssueImpl obj) {
        return obj.getRon();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setRon((String) value);
      }
    },
    REL_TO_SHIPPING_BY {
      public Object get(IssueImpl obj) {
        return obj.getRelToShippingBy();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setRelToShippingBy((Number) value);
      }
    },
    REL_TO_SHIPPING_DATE {
      public Object get(IssueImpl obj) {
        return obj.getRelToShippingDate();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setRelToShippingDate((Date) value);
      }
    },
    PICKING {
      public Object get(IssueImpl obj) {
        return obj.getPicking();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setAttributeInternal(index(), value);
      }
    },
    CUSTOMER {
      public Object get(IssueImpl obj) {
        return obj.getCustomer();
      }

      public void put(IssueImpl obj, Object value) {
        obj.setCustomer((CustomerImpl) value);
      }
    };

    private static final int FIRST_INDEX = 0;

    public abstract Object get(IssueImpl object);

    public abstract void put(IssueImpl object, Object value);

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

  public static final int SCN = AttributesEnum.SCN.index();
  public static final int DOCUMENTID = AttributesEnum.DOCUMENT_ID.index();
  public static final int DOCUMENTNUMBER = AttributesEnum.DOCUMENT_NUMBER.index();
  public static final int QTY = AttributesEnum.QTY.index();
  public static final int ISSUETYPE = AttributesEnum.ISSUE_TYPE.index();
  public static final int ISSUEPRIORITYDESIGNATOR = AttributesEnum.ISSUE_PRIORITY_DESIGNATOR.index();
  public static final int ISSUEPRIORITYGROUP = AttributesEnum.ISSUE_PRIORITY_GROUP.index();
  public static final int CUSTOMERID = AttributesEnum.CUSTOMER_ID.index();
  public static final int NIINID = AttributesEnum.NIIN_ID.index();
  public static final int STATUS = AttributesEnum.STATUS.index();
  public static final int CREATEDBY = AttributesEnum.CREATED_BY.index();
  public static final int CREATEDDATE = AttributesEnum.CREATED_DATE.index();
  public static final int MODIFIEDBY = AttributesEnum.MODIFIED_BY.index();
  public static final int MODIFIEDDATE = AttributesEnum.MODIFIED_DATE.index();
  public static final int PACKINGCONSOLIDATIONID = AttributesEnum.PACKING_CONSOLIDATION_ID.index();
  public static final int SUFFIX = AttributesEnum.SUFFIX.index();
  public static final int RCN = AttributesEnum.RCN.index();
  public static final int RDD = AttributesEnum.RDD.index();
  public static final int SUPPLEMENTARYADDRESS = AttributesEnum.SUPPLEMENTARY_ADDRESS.index();
  public static final int FUNDCODE = AttributesEnum.FUND_CODE.index();
  public static final int MEDIAANDSTATUSCODE = AttributesEnum.MEDIA_AND_STATUS_CODE.index();
  public static final int ROUTINGIDFROM = AttributesEnum.ROUTING_ID_FROM.index();
  public static final int SIGNALCODE = AttributesEnum.SIGNAL_CODE.index();
  public static final int DISTRIBUTIONCODE = AttributesEnum.DISTRIBUTION_CODE.index();
  public static final int PROJECTCODE = AttributesEnum.PROJECT_CODE.index();
  public static final int ADVICECODE = AttributesEnum.ADVICE_CODE.index();
  public static final int ERONUMBER = AttributesEnum.ERO_NUMBER.index();
  public static final int CC = AttributesEnum.CC.index();
  public static final int DATEBACKORDERED = AttributesEnum.DATE_BACK_ORDERED.index();
  public static final int COSTJON = AttributesEnum.COST_JON.index();
  public static final int QTYISSUED = AttributesEnum.QTY_ISSUED.index();
  public static final int DEMILCODE = AttributesEnum.DEMIL_CODE.index();
  public static final int DISPOSALCODE = AttributesEnum.DISPOSAL_CODE.index();
  public static final int RON = AttributesEnum.RON.index();
  public static final int RELTOSHIPPINGBY = AttributesEnum.REL_TO_SHIPPING_BY.index();
  public static final int RELTOSHIPPINGDATE = AttributesEnum.REL_TO_SHIPPING_DATE.index();
  public static final int PICKING = AttributesEnum.PICKING.index();
  public static final int CUSTOMER = AttributesEnum.CUSTOMER.index();

  /**
   * Gets the attribute value for Scn, using the alias name Scn.
   *
   * @return the value of Scn
   */
  public String getScn() {
    return (String) getAttributeInternal(SCN);
  }

  /**
   * Sets <code>value</code> as the attribute value for Scn.
   *
   * @param value value to set the Scn
   */
  public void setScn(String value) {
    setAttributeInternal(SCN, value);
  }

  /**
   * Gets the attribute value for DocumentId, using the alias name DocumentId.
   *
   * @return the value of DocumentId
   */
  public String getDocumentId() {
    return (String) getAttributeInternal(DOCUMENTID);
  }

  /**
   * Sets <code>value</code> as the attribute value for DocumentId.
   *
   * @param value value to set the DocumentId
   */
  public void setDocumentId(String value) {
    setAttributeInternal(DOCUMENTID, value);
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
   * Gets the attribute value for Qty, using the alias name Qty.
   *
   * @return the value of Qty
   */
  public Number getQty() {
    return (Number) getAttributeInternal(QTY);
  }

  /**
   * Sets <code>value</code> as the attribute value for Qty.
   *
   * @param value value to set the Qty
   */
  public void setQty(Number value) {
    setAttributeInternal(QTY, value);
  }

  /**
   * Gets the attribute value for IssueType, using the alias name IssueType.
   *
   * @return the value of IssueType
   */
  public String getIssueType() {
    return (String) getAttributeInternal(ISSUETYPE);
  }

  /**
   * Sets <code>value</code> as the attribute value for IssueType.
   *
   * @param value value to set the IssueType
   */
  public void setIssueType(String value) {
    setAttributeInternal(ISSUETYPE, value);
  }

  /**
   * Gets the attribute value for IssuePriorityDesignator, using the alias name IssuePriorityDesignator.
   *
   * @return the value of IssuePriorityDesignator
   */
  public String getIssuePriorityDesignator() {
    return (String) getAttributeInternal(ISSUEPRIORITYDESIGNATOR);
  }

  /**
   * Sets <code>value</code> as the attribute value for IssuePriorityDesignator.
   *
   * @param value value to set the IssuePriorityDesignator
   */
  public void setIssuePriorityDesignator(String value) {
    setAttributeInternal(ISSUEPRIORITYDESIGNATOR, value);
  }

  /**
   * Gets the attribute value for IssuePriorityGroup, using the alias name IssuePriorityGroup.
   *
   * @return the value of IssuePriorityGroup
   */
  public String getIssuePriorityGroup() {
    return (String) getAttributeInternal(ISSUEPRIORITYGROUP);
  }

  /**
   * Sets <code>value</code> as the attribute value for IssuePriorityGroup.
   *
   * @param value value to set the IssuePriorityGroup
   */
  public void setIssuePriorityGroup(String value) {
    setAttributeInternal(ISSUEPRIORITYGROUP, value);
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
   * Gets the attribute value for NiinId, using the alias name NiinId.
   *
   * @return the value of NiinId
   */
  public Number getNiinId() {
    return (Number) getAttributeInternal(NIINID);
  }

  /**
   * Sets <code>value</code> as the attribute value for NiinId.
   *
   * @param value value to set the NiinId
   */
  public void setNiinId(Number value) {
    setAttributeInternal(NIINID, value);
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
   * Gets the attribute value for PackingConsolidationId, using the alias name PackingConsolidationId.
   *
   * @return the value of PackingConsolidationId
   */
  public Number getPackingConsolidationId() {
    return (Number) getAttributeInternal(PACKINGCONSOLIDATIONID);
  }

  /**
   * Sets <code>value</code> as the attribute value for PackingConsolidationId.
   *
   * @param value value to set the PackingConsolidationId
   */
  public void setPackingConsolidationId(Number value) {
    setAttributeInternal(PACKINGCONSOLIDATIONID, value);
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
   * Gets the attribute value for Rdd, using the alias name Rdd.
   *
   * @return the value of Rdd
   */
  public String getRdd() {
    return (String) getAttributeInternal(RDD);
  }

  /**
   * Sets <code>value</code> as the attribute value for Rdd.
   *
   * @param value value to set the Rdd
   */
  public void setRdd(String value) {
    setAttributeInternal(RDD, value);
  }

  /**
   * Gets the attribute value for SupplementaryAddress, using the alias name SupplementaryAddress.
   *
   * @return the value of SupplementaryAddress
   */
  public String getSupplementaryAddress() {
    return (String) getAttributeInternal(SUPPLEMENTARYADDRESS);
  }

  /**
   * Sets <code>value</code> as the attribute value for SupplementaryAddress.
   *
   * @param value value to set the SupplementaryAddress
   */
  public void setSupplementaryAddress(String value) {
    setAttributeInternal(SUPPLEMENTARYADDRESS, value);
  }

  /**
   * Gets the attribute value for FundCode, using the alias name FundCode.
   *
   * @return the value of FundCode
   */
  public String getFundCode() {
    return (String) getAttributeInternal(FUNDCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for FundCode.
   *
   * @param value value to set the FundCode
   */
  public void setFundCode(String value) {
    setAttributeInternal(FUNDCODE, value);
  }

  /**
   * Gets the attribute value for MediaAndStatusCode, using the alias name MediaAndStatusCode.
   *
   * @return the value of MediaAndStatusCode
   */
  public String getMediaAndStatusCode() {
    return (String) getAttributeInternal(MEDIAANDSTATUSCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for MediaAndStatusCode.
   *
   * @param value value to set the MediaAndStatusCode
   */
  public void setMediaAndStatusCode(String value) {
    setAttributeInternal(MEDIAANDSTATUSCODE, value);
  }

  /**
   * Gets the attribute value for RoutingIdFrom, using the alias name RoutingIdFrom.
   *
   * @return the value of RoutingIdFrom
   */
  public String getRoutingIdFrom() {
    return (String) getAttributeInternal(ROUTINGIDFROM);
  }

  /**
   * Sets <code>value</code> as the attribute value for RoutingIdFrom.
   *
   * @param value value to set the RoutingIdFrom
   */
  public void setRoutingIdFrom(String value) {
    setAttributeInternal(ROUTINGIDFROM, value);
  }

  /**
   * Gets the attribute value for SignalCode, using the alias name SignalCode.
   *
   * @return the value of SignalCode
   */
  public String getSignalCode() {
    return (String) getAttributeInternal(SIGNALCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for SignalCode.
   *
   * @param value value to set the SignalCode
   */
  public void setSignalCode(String value) {
    setAttributeInternal(SIGNALCODE, value);
  }

  /**
   * Gets the attribute value for DistributionCode, using the alias name DistributionCode.
   *
   * @return the value of DistributionCode
   */
  public String getDistributionCode() {
    return (String) getAttributeInternal(DISTRIBUTIONCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for DistributionCode.
   *
   * @param value value to set the DistributionCode
   */
  public void setDistributionCode(String value) {
    setAttributeInternal(DISTRIBUTIONCODE, value);
  }

  /**
   * Gets the attribute value for ProjectCode, using the alias name ProjectCode.
   *
   * @return the value of ProjectCode
   */
  public String getProjectCode() {
    return (String) getAttributeInternal(PROJECTCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for ProjectCode.
   *
   * @param value value to set the ProjectCode
   */
  public void setProjectCode(String value) {
    setAttributeInternal(PROJECTCODE, value);
  }

  /**
   * Gets the attribute value for AdviceCode, using the alias name AdviceCode.
   *
   * @return the value of AdviceCode
   */
  public String getAdviceCode() {
    return (String) getAttributeInternal(ADVICECODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for AdviceCode.
   *
   * @param value value to set the AdviceCode
   */
  public void setAdviceCode(String value) {
    setAttributeInternal(ADVICECODE, value);
  }

  /**
   * Gets the attribute value for EroNumber, using the alias name EroNumber.
   *
   * @return the value of EroNumber
   */
  public String getEroNumber() {
    return (String) getAttributeInternal(ERONUMBER);
  }

  /**
   * Sets <code>value</code> as the attribute value for EroNumber.
   *
   * @param value value to set the EroNumber
   */
  public void setEroNumber(String value) {
    setAttributeInternal(ERONUMBER, value);
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
   * Gets the attribute value for DateBackOrdered, using the alias name DateBackOrdered.
   *
   * @return the value of DateBackOrdered
   */
  public String getDateBackOrdered() {
    return (String) getAttributeInternal(DATEBACKORDERED);
  }

  /**
   * Sets <code>value</code> as the attribute value for DateBackOrdered.
   *
   * @param value value to set the DateBackOrdered
   */
  public void setDateBackOrdered(String value) {
    setAttributeInternal(DATEBACKORDERED, value);
  }

  /**
   * Gets the attribute value for CostJon, using the alias name CostJon.
   *
   * @return the value of CostJon
   */
  public String getCostJon() {
    return (String) getAttributeInternal(COSTJON);
  }

  /**
   * Sets <code>value</code> as the attribute value for CostJon.
   *
   * @param value value to set the CostJon
   */
  public void setCostJon(String value) {
    setAttributeInternal(COSTJON, value);
  }

  /**
   * Gets the attribute value for QtyIssued, using the alias name QtyIssued.
   *
   * @return the value of QtyIssued
   */
  public Number getQtyIssued() {
    return (Number) getAttributeInternal(QTYISSUED);
  }

  /**
   * Sets <code>value</code> as the attribute value for QtyIssued.
   *
   * @param value value to set the QtyIssued
   */
  public void setQtyIssued(Number value) {
    setAttributeInternal(QTYISSUED, value);
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
   * Gets the attribute value for DisposalCode, using the alias name DisposalCode.
   *
   * @return the value of DisposalCode
   */
  public String getDisposalCode() {
    return (String) getAttributeInternal(DISPOSALCODE);
  }

  /**
   * Sets <code>value</code> as the attribute value for DisposalCode.
   *
   * @param value value to set the DisposalCode
   */
  public void setDisposalCode(String value) {
    setAttributeInternal(DISPOSALCODE, value);
  }

  /**
   * Gets the attribute value for Ron, using the alias name Ron.
   *
   * @return the value of Ron
   */
  public String getRon() {
    return (String) getAttributeInternal(RON);
  }

  /**
   * Sets <code>value</code> as the attribute value for Ron.
   *
   * @param value value to set the Ron
   */
  public void setRon(String value) {
    setAttributeInternal(RON, value);
  }

  /**
   * Gets the attribute value for RelToShippingBy, using the alias name RelToShippingBy.
   *
   * @return the value of RelToShippingBy
   */
  public Number getRelToShippingBy() {
    return (Number) getAttributeInternal(RELTOSHIPPINGBY);
  }

  /**
   * Sets <code>value</code> as the attribute value for RelToShippingBy.
   *
   * @param value value to set the RelToShippingBy
   */
  public void setRelToShippingBy(Number value) {
    setAttributeInternal(RELTOSHIPPINGBY, value);
  }

  /**
   * Gets the attribute value for RelToShippingDate, using the alias name RelToShippingDate.
   *
   * @return the value of RelToShippingDate
   */
  public Date getRelToShippingDate() {
    return (Date) getAttributeInternal(RELTOSHIPPINGDATE);
  }

  /**
   * Sets <code>value</code> as the attribute value for RelToShippingDate.
   *
   * @param value value to set the RelToShippingDate
   */
  public void setRelToShippingDate(Date value) {
    setAttributeInternal(RELTOSHIPPINGDATE, value);
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
  public RowIterator getPicking() {
    return (RowIterator) getAttributeInternal(PICKING);
  }

  /**
   * @return the associated entity mil.stratis.model.entity.site.CustomerImpl.
   */
  public CustomerImpl getCustomer() {
    return (CustomerImpl) getAttributeInternal(CUSTOMER);
  }

  /**
   * Sets <code>value</code> as the associated entity mil.stratis.model.entity.site.CustomerImpl.
   */
  public void setCustomer(CustomerImpl value) {
    setAttributeInternal(CUSTOMER, value);
  }

  /**
   * @param scn key constituent
   * @return a Key object based on given key constituents.
   */
  public static Key createPrimaryKey(String scn) {
    return new Key(new Object[]{scn});
  }

  /**
   * @return the definition object for this instance class.
   */
  public static synchronized EntityDefImpl getDefinitionObject() {
    return EntityDefImpl.findDefObject("mil.stratis.model.entity.wlm.Issue");
  }
}

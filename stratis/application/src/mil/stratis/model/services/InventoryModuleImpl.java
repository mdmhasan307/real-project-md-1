package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.PadUtil;
import mil.stratis.common.util.RegUtils;
import mil.stratis.model.util.DBUtil;
import mil.stratis.model.view.inv.*;
import mil.stratis.model.view.invsetup.InventoryViewImpl;
import mil.stratis.model.view.whsetup.EquipTableViewImpl;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewObjectImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class InventoryModuleImpl extends ApplicationModuleImpl {

  public static final String NIIN_ID = "NiinId";
  public static final String WAC_ID_STR = "wacIdStr";
  public static final String ORDER_BY_CLAUSE = "  QRSLT.BAY ASC, QRSLT.LOC_LEVEL ASC, QRSLT.SLOT ASC ";
  public static final String ASSIGN_TO_USER_ID = "assignToUserId";
  public static final String LOCATION_ID = "LocationId";
  public static final String QTY_LOT = "QtyLot";
  private String[] niins;
  private String nomenclature;
  private String ui;
  private int qtyNeeded;
  private int dividerIndex;
  private int selectIndex;
  private int surveySelectIndex;
  private int warehouseAc;
  private int locationId;
  private Boolean check;
  private Boolean empty;
  private Boolean surveyEmpty;
  private Integer userId;
  private String docIdent;
  private String riFrom;
  private String mediaStatus;
  private String unitIss;
  private String supAddress;
  private String signal;
  private String fund;
  private String priority = "";
  private String reqDelDate;
  private String advice = "";
  private Double unitWeight;
  private Double unitCube;
  private String uDollars;
  private String uCents;
  private String tDollars;
  private String tCents;
  private String inomenclature;
  private String distribution;
  private String project;
  private Double tWeight;
  private Double tCube;
  private String qty;
  private String siteAac = "";
  private String aac = "";
  private String address1 = "";
  private String address2 = "";
  private String addressName = "";
  private String city = "";
  private String state = "";
  private String zip = "";
  private String cc = "";
  private String ero = "";
  private String pin = "";
  private String equipDescription = "";
  private String equipName = "";
  private String supplyCenter = "";
  private String iCity = "";
  private String iState = "";
  private String iZip = "";
  private String documentNumber = "";
  private String niin = "";
  private String fsc = "";
  private String routingId = "";
  private String unitPrice = "";
  private String partNumber = "";
  private String trueQty = "";
  private String fromAac = "";
  private String serialNumbers = "";

  /**************************************************************
   * Description: Based on the scn provided by the user, this
   * function retrieves the desired information for the 1348 from
   * the Issue and the Niin_info tables.  These values are placed
   * in the setter functions of this application module for further
   * use.
   **************************************************************/
  public void getFormInfo(String scn, String qty) {
    IssueViewObjImpl view;
    NiinInfoView2ObjImpl view2;
    int niinId = 0;
    String uPrice;
    double tPrice;
    String tPrice2;
    int quantity = Integer.parseInt(qty);

    try {
      view = getIssueViewObj1();
      view.applyViewCriteria(null);

      ViewCriteria crit = view.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();
      critRow.setUpperColumns(true);

      //Select everything from the Issue table where the
      //Scn equals the Scn passed in by the user
      critRow.setAttribute("Scn", "=" + "'" + scn + "'");
      crit.addElement(critRow);

      view.applyViewCriteria(crit);
      view.executeQuery();

      Object tempNiinId = null;
      Object tempSup = null;
      Object tempFund = null;
      Object tempMedNStat = null;
      Object tempRiFrom = null;
      Object tempSignal = null;
      Object tempDistribute = null;
      Object tempProjCode = null;
      Object tempDocId = null;
      Object tempRdd = null;
      Object tempAdvice = null;
      Object tempCC = null;
      Object tempEro = null;
      Object tempCustId = null;
      Object tempDocNo = null;
      Object tempPriority = null;
      Object tempIssueType = null;
      Object tempSuffix = null;
      Object issueDemilCode = null;

      Row[] row = view.getAllRowsInRange();
      //If any rows are retured from the query
      if (row.length > 0) {
        //Retrieve the neccessary information on the returned row from the
        //Issue table
        tempNiinId = row[0].getAttribute(NIIN_ID);
        tempSup = row[0].getAttribute("SupplementaryAddress");
        tempFund = row[0].getAttribute("FundCode");
        tempMedNStat = row[0].getAttribute("MediaAndStatusCode");
        tempRiFrom = row[0].getAttribute("RoutingIdFrom");
        tempSignal = row[0].getAttribute("SignalCode");
        tempDistribute = row[0].getAttribute("DistributionCode");
        tempProjCode = row[0].getAttribute("ProjectCode");
        tempDocId = row[0].getAttribute("DocumentId");
        tempRdd = row[0].getAttribute("Rdd");
        tempAdvice = row[0].getAttribute("AdviceCode");
        tempCC = row[0].getAttribute("Cc");
        tempEro = row[0].getAttribute("EroNumber");
        tempCustId = row[0].getAttribute("CustomerId");
        tempDocNo = row[0].getAttribute("DocumentNumber");
        tempPriority = row[0].getAttribute("IssuePriorityDesignator");
        tempIssueType = row[0].getAttribute("IssueType");
        tempSuffix = row[0].getAttribute("Suffix");
        issueDemilCode = row[0].getAttribute("DemilCode");
      }

      String tempNiinId2;

      //If the temporary value for Priority is not null
      if (tempPriority != null)
        //Set this module's setter method with the string
        //value of the Priority code
        this.setPriority(tempPriority.toString());

      //If the temporary value for NiinId is not null
      if (tempNiinId != null) {
        //Get the String value of the NiinId
        tempNiinId2 = tempNiinId.toString();
        //Get the int value of the NiinId
        niinId = Integer.parseInt(tempNiinId2);
      }

      //If the temporary value for the Document Number is not null
      if (tempDocNo != null) {
        //Set this module's setter method with the string value
        //of the Document number
        val tempSuffixTrim = tempSuffix != null ? tempSuffix.toString().trim() : null;
        val tempDocNoTrim = tempDocNo.toString().trim();
        if (tempSuffix != null && !tempSuffixTrim.equals(""))
          this.setDocumentNumber(tempDocNoTrim + tempSuffixTrim);
        else
          this.setDocumentNumber(tempDocNoTrim);
      }
      //If the temporary value for the advice code is not null
      if (tempAdvice != null)
        //Set this module's setter method with the string value
        //of the advice code
        this.setAdvice(tempAdvice.toString());

      //If the temporary value for the condition code is not null
      if (tempCC != null)
        //Set this module's setter method with the string value
        //of the condition code
        this.setCc(tempCC.toString());

      //If the temporary value for the Ero number is not null
      if (tempEro != null)
        //Set this module's setter method with the string value
        //of the ero number
        this.setEro(tempEro.toString());

      //If the temporary value for the customer id is not null
      if (tempCustId != null) {
        //Set this module's setter method with the string value
        //of the customer id

        this.getAddressInfo(Integer.parseInt(tempCustId.toString()));
      }
      this.getAddressOfSiteAac();

      //If the temporary value for the RDD is not null
      if (tempRdd != null)
        //Set this module's setter method with the string value
        //of the Required Delivery Date
        this.setReqDelDate(tempRdd.toString());
      else
        //Set this module's RDD to a blank
        this.setReqDelDate("");

      //If the temporary value for the DocId is not null
      if (tempDocId != null)
        //Set this module's setter method with the string value
        //of the Document Id
        this.setDocIdent(tempDocId.toString());
      else
        //Set this module's Doc Id to a blank
        this.setDocIdent("");

      //If the temporary value for the Supplementary address is not null
      if (tempSup != null)
        //Set this module's setter method with the string value
        //of the Supplementary address
        this.setSupAddress(tempSup.toString());
      else
        //Set this module's Supplementary address to a blank
        this.setSupAddress("");

      //If the temporary value for the Fund code is not null
      if (tempFund != null)
        //Set this module's setter method with the string value
        //of the Fund code
        this.setFund(tempFund.toString());
      else
        //Set this module's Fund code to a blank
        this.setFund("");

      //If the temporary value for the Media & Status code is not null
      if (tempMedNStat != null)
        //Set this module's setter method with the string value
        //of the Media Status code
        this.setMediaStatus(tempMedNStat.toString());
      else
        //Set this module's Media & Status code to a blank
        this.setMediaStatus("");

      //If the temporary value for the Ri From number is not null
      if (tempRiFrom != null)
        //Set this module's setter method with the int value
        //of the Ri From Number
        this.setRiFrom(tempRiFrom.toString());

      //If the temporary value for the signal code is not null
      if (tempSignal != null)
        //Set this module's setter method with the String value
        //of the signal code
        this.setSignal(tempSignal.toString());
      else
        //Set this module's signal code to a blank
        this.setSignal("");

      //If the temporary value for the distribution code is not null
      if (tempDistribute != null)
        //Set this module's setter method with the String value
        //of the distribution code
        this.setDistribution(tempDistribute.toString());
      else
        //Set this module's distribution code to a blank
        this.setDistribution("");

      //If the temporary value for the project code is not null
      if (tempProjCode != null)
        //Set this module's setter method with the String value
        //of the project code
        this.setProject(tempProjCode.toString());
      else
        //Set this module's project code to a blank
        this.setProject("");

      //If the value of NiinId is not zero
      if (niinId != 0) {

        view2 = getNiinInfoView2Obj1();
        view2.applyViewCriteria(null);

        ViewCriteria crit2 = view2.createViewCriteria();
        ViewCriteriaRow critRow2 = crit2.createViewCriteriaRow();
        critRow2.setUpperColumns(true);

        //Select everything from the NiinInfo table
        //where the Niin Id is equal to the Niin Id retreived
        //earlier in this function from the issue table
        critRow2.setAttribute(NIIN_ID, "=" + niinId);
        crit2.addElement(critRow2);

        //Apply the criteria
        view2.applyViewCriteria(crit2);
        //Execute the query
        view2.executeQuery();

        Row[] row2 = view2.getAllRowsInRange();

        //If any rows are retured from the query
        if (row2.length > 0) {
          //Retrieve the neccessary information on the returned row from the
          //Niin Info table
          Object tempUi = row2[0].getAttribute("Ui");
          Object tempCube = row2[0].getAttribute("Cube");
          Object tempPrice = row2[0].getAttribute("Price");
          Object tempWeight = row2[0].getAttribute("Weight");
          Object tempNomen = row2[0].getAttribute("Nomenclature");
          Object tempNiin = row2[0].getAttribute("Niin");
          Object tempFsc = row2[0].getAttribute("Fsc");
          Object tempPartNum = row2[0].getAttribute("PartNumber");
          Object tempDemilCode = row2[0].getAttribute("DemilCode");

          int index;
          double price;

          //If the temporary value for the Part Number is not null
          if (tempPartNum != null)
            //Set this module's setter method with the String value
            //of the Part Number
            this.setPartNumber(tempPartNum.toString());

          //If the temporary value for the Niin is not null
          if (tempNiin != null)
            //Set this module's setter method with the String value
            //of the Niin value
            this.setNiin(tempNiin.toString());

          //If the temporary value for the Fsc is not null
          if (tempFsc != null)
            //Set this module's setter method with the String value
            //of the Fsc value
            this.setFsc(tempFsc.toString());

          //If the temporary value for the Ui code is not null
          if (tempUi != null)
            //Set this module's setter method with the String value
            //of the distribution code
            this.setUi(tempUi.toString());

          //If the temporary value for the Cube value is not null
          if (tempCube != null) {
            //Set this module's setter method with the String value
            //of the Cube value
            this.setUnitCube(Double.parseDouble(tempCube.toString()));
            //Multiply the cube value by the quantity, and set the
            //this module's total cube value.
            BigDecimal big = BigDecimal.valueOf(this.getUnitCube());
            BigDecimal ttotalCube = big.multiply(new BigDecimal(quantity));
            BigDecimal totalCube = ttotalCube.setScale(2, RoundingMode.HALF_EVEN);
            this.settCube(totalCube.doubleValue());
          }

          //If the temporary value for the price is not null
          if (tempPrice != null) {

            //Get the String value of the price
            uPrice = tempPrice.toString();
            this.setUnitPrice(uPrice);
            //Get the Double value of the price
            price = Double.parseDouble(uPrice);
            //Get the index of the period in the price
            index = uPrice.indexOf('.');

            //If there are no numbers in front of the period
            if (index == 0) {
              //Set this application module's UnitDollars value
              //to 0
              this.setuDollars(PadUtil.padItZeros("0", 5));
              //Get the value of the digits behind the period, and
              //set this application module's Unit Cents value
              this.setuCents(PadUtil.padItTrailingZeros(uPrice.substring(index + 1), 2));
            }

            //If there is no period
            else if (index == -1) {
              //Set this application module's UnitDollars value
              //to the value of unit price
              this.setuDollars(PadUtil.padItZeros(uPrice, 5));
              //Set this application module's Unit Cents value
              // to 00
              this.setuCents("00");
            }
            //If there is a period, with digits in front and behind
            //it
            else {
              //Get the value of the digits in front of the period
              //and set this application module's UnitDollars
              //value.
              this.setuDollars(PadUtil.padItZeros(uPrice.substring(0, index), 5));
              //Get the value of the digits behind the period
              //and set this application module's UnitCents
              //value
              this.setuCents(PadUtil.padItTrailingZeros(uPrice.substring(index + 1), 2));
            }

            //Multiply the price by the quantity to get the
            //total price
            BigDecimal big = BigDecimal.valueOf(price);
            BigDecimal ttotalPrice = big.multiply(new BigDecimal(quantity));
            BigDecimal totalPrice = ttotalPrice.setScale(2, RoundingMode.HALF_EVEN);

            tPrice = totalPrice.doubleValue();

            //Get the string value of the total price
            tPrice2 = String.valueOf(tPrice);
            //Get the index value of the period
            index = tPrice2.indexOf('.');

            //If there are no digits before the period
            if (index == 0) {
              //Set this application module's Total Dollars value
              //to 0
              this.settDollars(PadUtil.padItZeros("", 5));
              //Get the value of the digits behind the period, and
              //set this application module's Total Cents value
              this.settCents(PadUtil.padItTrailingZeros(tPrice2.substring(index + 1), 2));
            }
            //If there is no period
            else if (index == -1) {
              //Set this application module's Total Dollars value
              //to the value of total price
              this.settDollars(PadUtil.padItZeros(tPrice2, 5));
              //Set this application module's Total Cents value
              // to 00
              this.settCents("00");
            }
            //If there is a period, with digits in front and behind
            //it
            else {
              //Get the value of the digits in front of the period
              //and set this application module's Total Dollars
              //value.
              this.settDollars(PadUtil.padItZeros(tPrice2.substring(0, index), 5));
              //Get the value of the digits behind the period
              //and set this application module's Total Cents
              //value
              this.settCents(PadUtil.padItTrailingZeros(tPrice2.substring(index + 1), 2));
            }
          }

          //If the temporary value for the weight is not null
          if (tempWeight != null) {
            val tempWeightStr = tempWeight.toString();
            //Set this application module's unit weight to the Double
            //value of the temporary weight value.
            this.setUnitWeight(Double.parseDouble(tempWeightStr));
            //Multiply the Unit Weight value by the quantity, then
            //set this application module's Total Weight quantity to
            //the value of the result.
            BigDecimal big = new BigDecimal(tempWeightStr);
            BigDecimal ttotalWeight = big.multiply(new BigDecimal(quantity));
            BigDecimal totalWeight = ttotalWeight.setScale(2, RoundingMode.HALF_EVEN);
            this.settWeight(totalWeight.doubleValue());
          }
          //If the temporary value for the nomenclature is not null
          if (tempNomen != null)
            //Set this application module's Item Nomenclature value
            //to the String value of the temporary Item Nomenclature
            //value.
            this.setInomenclature(tempNomen.toString());

          if ((getDocIdent().equals("A5J") || getDocIdent().equals("A5A")) && (tempIssueType == null)) {

            if ((issueDemilCode != null) && !issueDemilCode.equals(" ") && !issueDemilCode.equals("")) {
              this.setAdvice(issueDemilCode.toString());
            }
            else if (tempDemilCode != null) {
              this.setAdvice(tempDemilCode.toString());
            }
            else {
              this.setAdvice("");
            }
          }
        }
      }
      //Using the passed in scn, call the function that gets
      //the pin number
      if (tempDocNo != null) getPinInfo(scn, false);
      getSerialInfo(scn, niinId, false);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /***************************************************************
   * Description: Based on the scn provided by the user, this
   * function retrieves the desired information for the 1348 from
   * the Issue and the Niin_info tables.  These values are placed
   * in the setter functions of this application module for further
   * use.
   **************************************************************/
  public void getFormInfoHist(String scn, String docNum, String qty) {
    IssueHistViewObjImpl view;
    NiinInfoView2ObjImpl view2;
    int niinId = 0;
    String uPrice;
    double tPrice;
    String tPrice2;
    int quantity = Integer.parseInt(qty);

    try {
      view = getIssueHistViewObj1();
      view.applyViewCriteria(null);

      ViewCriteria crit = view.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();
      critRow.setUpperColumns(true);

      //Select everything from the Issue table where the
      //Scn equals the Scn passed in by the user
      critRow.setAttribute("Scn", "=" + "'" + scn + "'");

      critRow.setAttribute("DocumentNumber", "=" + "'" + docNum + "'");
      crit.addElement(critRow);

      view.applyViewCriteria(crit);
      view.executeQuery();

      Object tempNiinId = null;
      Object tempSup = null;
      Object tempFund = null;
      Object tempMedNStat = null;
      Object tempRiFrom = null;
      Object tempSignal = null;
      Object tempDistribute = null;
      Object tempProjCode = null;
      Object tempDocId = null;
      Object tempRdd = null;
      Object tempAdvice = null;
      Object tempCC = null;
      Object tempEro = null;
      Object tempCustId = null;
      Object tempDocNo = null;
      Object tempPriority = null;
      Object tempIssueType = null;
      Object tempSuffix = null;

      Row[] row = view.getAllRowsInRange();
      //If any rows are retured from the query
      if (row.length > 0) {
        //Retrieve the neccessary information on the returned row from the
        //Issue table
        tempNiinId = row[0].getAttribute(NIIN_ID);
        tempSup = row[0].getAttribute("SupplementaryAddress");
        tempFund = row[0].getAttribute("FundCode");
        tempMedNStat = row[0].getAttribute("MediaAndStatusCode");
        tempRiFrom = row[0].getAttribute("RoutingIdFrom");
        tempSignal = row[0].getAttribute("SignalCode");
        tempDistribute = row[0].getAttribute("DistributionCode");
        tempProjCode = row[0].getAttribute("ProjectCode");
        tempDocId = row[0].getAttribute("DocumentId");
        tempRdd = row[0].getAttribute("Rdd");
        tempAdvice = row[0].getAttribute("AdviceCode");
        tempCC = row[0].getAttribute("Cc");
        tempEro = row[0].getAttribute("EroNumber");
        tempCustId = row[0].getAttribute("CustomerId");
        tempDocNo = row[0].getAttribute("DocumentNumber");
        tempPriority = row[0].getAttribute("IssuePriorityDesignator");
        tempIssueType = row[0].getAttribute("IssueType");
        tempSuffix = row[0].getAttribute("Suffix");
      }

      String tempNiinId2;

      //If the temporary value for Priority is not null
      if (tempPriority != null)
        //Set this module's setter method with the string
        //value of the Priority code
        this.setPriority(tempPriority.toString());

      //If the temporary value for NiinId is not null
      if (tempNiinId != null) {
        //Get the String value of the NiinId
        tempNiinId2 = tempNiinId.toString();
        //Get the int value of the NiinId
        niinId = Integer.parseInt(tempNiinId2);
      }

      //If the temporary value for the Document Number is not null
      if (tempDocNo != null) {
        //Set this module's setter method with the string value
        //of the Document number
        val tempSuffixTrim = tempSuffix != null ? tempSuffix.toString().trim() : null;
        val tempDocNoTrim = tempDocNo.toString().trim();
        if (tempSuffix != null && !tempSuffixTrim.equals(""))
          this.setDocumentNumber(tempDocNoTrim + tempSuffixTrim);
        else
          this.setDocumentNumber(tempDocNoTrim);
      }
      //If the temporary value for the advice code is not null
      if (tempAdvice != null)
        //Set this module's setter method with the string value
        //of the advice code
        this.setAdvice(tempAdvice.toString());

      //If the temporary value for the condition code is not null
      if (tempCC != null)
        //Set this module's setter method with the string value
        //of the condition code
        this.setCc(tempCC.toString());

      //If the temporary value for the Ero number is not null
      if (tempEro != null)
        //Set this module's setter method with the string value
        //of the ero number
        this.setEro(tempEro.toString());

      //If the temporary value for the customer id is not null
      if (tempCustId != null) {
        //Set this module's setter method with the string value
        //of the customer id

        this.getAddressInfo(Integer.parseInt(tempCustId.toString()));
      }
      this.getAddressOfSiteAac();

      //If the temporary value for the RDD is not null
      if (tempRdd != null)
        //Set this module's setter method with the string value
        //of the Required Delivery Date
        this.setReqDelDate(tempRdd.toString());
      else
        //Set this module's RDD to a blank
        this.setReqDelDate("");

      //If the temporary value for the DocId is not null
      if (tempDocId != null)
        //Set this module's setter method with the string value
        //of the Document Id
        this.setDocIdent(tempDocId.toString());
      else
        //Set this module's Doc Id to a blank
        this.setDocIdent("");

      //If the temporary value for the Supplementary address is not null
      if (tempSup != null)
        //Set this module's setter method with the string value
        //of the Supplementary address
        this.setSupAddress(tempSup.toString());
      else
        //Set this module's Supplementary address to a blank
        this.setSupAddress("");

      //If the temporary value for the Fund code is not null
      if (tempFund != null)
        //Set this module's setter method with the string value
        //of the Fund code
        this.setFund(tempFund.toString());
      else
        //Set this module's Fund code to a blank
        this.setFund("");

      //If the temporary value for the Media & Status code is not null
      if (tempMedNStat != null)
        //Set this module's setter method with the string value
        //of the Media Status code
        this.setMediaStatus(tempMedNStat.toString());
      else
        //Set this module's Media & Status code to a blank
        this.setMediaStatus("");

      //If the temporary value for the Ri From number is not null
      if (tempRiFrom != null)
        //Set this module's setter method with the int value
        //of the Ri From Number
        this.setRiFrom(tempRiFrom.toString());

      //If the temporary value for the signal code is not null
      if (tempSignal != null)
        //Set this module's setter method with the String value
        //of the signal code
        this.setSignal(tempSignal.toString());
      else
        //Set this module's signal code to a blank
        this.setSignal("");

      //If the temporary value for the distribution code is not null
      if (tempDistribute != null)
        //Set this module's setter method with the String value
        //of the distribution code
        this.setDistribution(tempDistribute.toString());
      else
        //Set this module's distribution code to a blank
        this.setDistribution("");

      //If the temporary value for the project code is not null
      if (tempProjCode != null)
        //Set this module's setter method with the String value
        //of the project code
        this.setProject(tempProjCode.toString());
      else
        //Set this module's project code to a blank
        this.setProject("");

      //If the value of NiinId is not zero
      if (niinId != 0) {

        view2 = getNiinInfoView2Obj1();
        view2.applyViewCriteria(null);

        ViewCriteria crit2 = view2.createViewCriteria();
        ViewCriteriaRow critRow2 = crit2.createViewCriteriaRow();
        critRow2.setUpperColumns(true);

        //Select everything from the NiinInfo table
        //where the Niin Id is equal to the Niin Id retreived
        //earlier in this function from the issue table
        critRow2.setAttribute(NIIN_ID, "=" + niinId);
        crit2.addElement(critRow2);

        //Apply the criteria
        view2.applyViewCriteria(crit2);
        //Execute the query
        view2.executeQuery();

        Row[] row2 = view2.getAllRowsInRange();

        //If any rows are retured from the query
        if (row2.length > 0) {
          //Retrieve the neccessary information on the returned row from the
          //Niin Info table
          Object tempUi = row2[0].getAttribute("Ui");
          Object tempCube = row2[0].getAttribute("Cube");
          Object tempPrice = row2[0].getAttribute("Price");
          Object tempWeight = row2[0].getAttribute("Weight");
          Object tempNomen = row2[0].getAttribute("Nomenclature");
          Object tempNiin = row2[0].getAttribute("Niin");
          Object tempFsc = row2[0].getAttribute("Fsc");
          Object tempPartNum = row2[0].getAttribute("PartNumber");
          Object tempDemilCode = row2[0].getAttribute("DemilCode");

          int index;
          double price;

          //If the temporary value for the Part Number is not null
          if (tempPartNum != null)
            //Set this module's setter method with the String value
            //of the Part Number
            this.setPartNumber(tempPartNum.toString());

          //If the temporary value for the Niin is not null
          if (tempNiin != null)
            //Set this module's setter method with the String value
            //of the Niin value
            this.setNiin(tempNiin.toString());

          //If the temporary value for the Fsc is not null
          if (tempFsc != null)
            //Set this module's setter method with the String value
            //of the Fsc value
            this.setFsc(tempFsc.toString());

          //If the temporary value for the Ui code is not null
          if (tempUi != null)
            //Set this module's setter method with the String value
            //of the distribution code
            this.setUi(tempUi.toString());

          //If the temporary value for the Cube value is not null
          if (tempCube != null) {
            //Set this module's setter method with the String value
            //of the Cube value
            this.setUnitCube(Double.parseDouble(tempCube.toString()));
            //Multiply the cube value by the quantity, and set the
            //this module's total cube value.
            BigDecimal big = BigDecimal.valueOf(this.getUnitCube());
            BigDecimal ttotalCube = big.multiply(new BigDecimal(quantity));
            BigDecimal totalCube = ttotalCube.setScale(2, RoundingMode.HALF_EVEN);
            this.settCube(totalCube.doubleValue());
          }

          //If the temporary value for the price is not null
          if (tempPrice != null) {

            //Get the String value of the price
            uPrice = tempPrice.toString();
            this.setUnitPrice(uPrice);
            //Get the Double value of the price
            price = Double.parseDouble(uPrice);
            //Get the index of the period in the price
            index = uPrice.indexOf('.');

            //If there are no numbers in front of the period
            if (index == 0) {
              //Set this application module's UnitDollars value
              //to 0
              this.setuDollars(PadUtil.padItZeros("0", 5));
              //Get the value of the digits behind the period, and
              //set this application module's Unit Cents value
              this.setuCents(PadUtil.padItTrailingZeros(uPrice.substring(index + 1), 2));
            }

            //If there is no period
            else if (index == -1) {
              //Set this application module's UnitDollars value
              //to the value of unit price
              this.setuDollars(PadUtil.padItZeros(uPrice, 5));
              //Set this application module's Unit Cents value
              // to 00
              this.setuCents("00");
            }
            //If there is a period, with digits in front and behind
            //it
            else {
              //Get the value of the digits in front of the period
              //and set this application module's UnitDollars
              //value.
              this.setuDollars(PadUtil.padItZeros(uPrice.substring(0, index), 5));
              //Get the value of the digits behind the period
              //and set this application module's UnitCents
              //value
              this.setuCents(PadUtil.padItTrailingZeros(uPrice.substring(index + 1), 2));
            }

            //Multiply the price by the quantity to get the
            //total price
            BigDecimal big = BigDecimal.valueOf(price);
            BigDecimal ttotalPrice = big.multiply(new BigDecimal(quantity));
            BigDecimal totalPrice = ttotalPrice.setScale(2, RoundingMode.HALF_EVEN);

            tPrice = totalPrice.doubleValue();

            //Get the string value of the total price
            tPrice2 = String.valueOf(tPrice);
            //Get the index value of the period
            index = tPrice2.indexOf('.');
            //If there are no digits before the period
            if (index == 0) {
              //Set this application module's Total Dollars value
              //to 0
              this.settDollars(PadUtil.padItZeros("", 5));
              //Get the value of the digits behind the period, and
              //set this application module's Total Cents value
              this.settCents(PadUtil.padItTrailingZeros(tPrice2.substring(index + 1), 2));
            }
            //If there is no period
            else if (index == -1) {
              //Set this application module's Total Dollars value
              //to the value of total price
              this.settDollars(PadUtil.padItZeros(tPrice2, 5));
              //Set this application module's Total Cents value
              // to 00
              this.settCents("00");
            }
            //If there is a period, with digits in front and behind
            //it
            else {
              //Get the value of the digits in front of the period
              //and set this application module's Total Dollars
              //value.
              this.settDollars(PadUtil.padItZeros(tPrice2.substring(0, index), 5));
              //Get the value of the digits behind the period
              //and set this application module's Total Cents
              //value
              this.settCents(PadUtil.padItTrailingZeros(tPrice2.substring(index + 1), 2));
            }
          }

          //If the temporary value for the weight is not null
          if (tempWeight != null) {
            val tempWeightStr = tempWeight.toString();
            //Set this application module's unit weight to the Double
            //value of the temporary weight value.
            this.setUnitWeight(Double.parseDouble(tempWeightStr));
            //Multiply the Unit Weight value by the quantity, then
            //set this application module's Total Weight quantity to
            //the value of the result.
            BigDecimal big = new BigDecimal(tempWeightStr);
            BigDecimal ttotalWeight = big.multiply(new BigDecimal(quantity));
            BigDecimal totalWeight = ttotalWeight.setScale(2, RoundingMode.HALF_EVEN);
            this.settWeight(totalWeight.doubleValue());
          }
          //If the temporary value for the nomenclature is not null
          if (tempNomen != null)
            //Set this application module's Item Nomenclature value
            //to the String value of the temporary Item Nomenclature
            //value.
            this.setInomenclature(tempNomen.toString());

          if ((getDocIdent().equals("A5J") || getDocIdent().equals("A5A")) && (tempIssueType == null)) {

            if (tempDemilCode != null) {
              this.setAdvice(tempDemilCode.toString());
            }
            else {
              this.setAdvice("");
            }
          }
        }
      }
      //Using the passed in scn, call the function that gets
      //the pin number
      getPinInfo(scn, true);
      getSerialInfo(scn, niinId, true);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /****************************************************************
   * Description: Based on the customer id obtained from the
   * getFormInfo function, this function gathers the corresponding
   * address information for the 1348 from the Customer table.  The
   * values are then placed in this application module's setter functions for
   * further use. This address information will appear in the ship to
   * section of the 1348.
   ****************************************************************/
  public void getAddressInfo(int custId) {

    CustomerViewObjImpl view;
    try {
      log.debug("InventoryModule.getAddressInfo of customer_id  {}", custId);
      view = getCustomerViewObj1();
      view.applyViewCriteria(null);

      ViewCriteria crit = view.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();
      critRow.setUpperColumns(true);
      //Select everything from the Customer table where the
      //customer id equals the custId value passed in by the user
      critRow.setAttribute("CustomerId", "=" + custId);
      crit.addElement(critRow);

      //Apply the criteria
      view.applyViewCriteria(crit);
      //Execute the query
      view.executeQuery();

      Row[] row = view.getAllRowsInRange();
      //If any rows are returned by the Query
      if (row.length > 0) {
        //Retrieve the neccessary information from the first
        //row of the returned query
        Object tempAac = row[0].getAttribute("Aac");
        Object tempAdd1 = row[0].getAttribute("Address1");
        Object tempAdd2 = row[0].getAttribute("Address2");
        Object tempState = row[0].getAttribute("State");
        Object tempCity = row[0].getAttribute("City");
        Object tempZip = row[0].getAttribute("ZipCode");
        Object tempName = row[0].getAttribute("Name");

        //If the temporary value of the Aac is not null
        if (tempAac != null) {
          //Set this application module's Aac value
          //to the String value of the temporary aac value
          setAac(tempAac.toString());
          //Call the function to get the address information using
          //the Aac value
        }

        //If the temporary value of Address1 is not null
        if (tempAdd1 != null)
          //Set this application module's Address1 value to
          //the temporary Address1 value
          setAddress1(tempAdd1.toString());

        //If the temporary value of Address2 is not null
        if (tempAdd2 != null)
          //Set this application module's Address2 value to
          //the temporary Address2 value
          setAddress2(tempAdd2.toString());

        //If the temporary value of State is not null
        if (tempState != null)
          //Set this application module's Address2 value to
          //the temporary Address2 value
          setState(tempState.toString());

        //If the temporary value of Zip is not null
        if (tempZip != null)
          //Set this application module's Zip value to
          //the temporary Zip value
          setZip(tempZip.toString());

        //If the temporary value of City is not null
        if (tempCity != null)
          //Set this application module's City value to
          //the temporary City value
          setCity(tempCity.toString());

        //If the temporary value of Name is not null
        if (tempName != null)
          //Set this application module's Name value to
          //the temporary City value
          setAddressName(tempName.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /****************************************************************
   * Description: This function retrieves the Pin from the picking
   * table based on the scn that the user provides.  This value is
   * also placed in it's corresponding setter method for further use
   ***************************************************************/
  public void getPinInfo(String scn, boolean isHistory) {

    ViewObjectImpl view = (isHistory)
        ? getPickingHistViewObj1()
        : getPickingViewObj1();
    try {
      view.applyViewCriteria(null);

      ViewCriteria crit = view.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();
      critRow.setUpperColumns(true);

      //Select everything from the picking table where the
      //Scn is equal to the Scn provided by the user
      critRow.setAttribute("Scn", "=" + "'" + scn + "'");
      crit.addElement(critRow);

      //Apply the criteria
      view.applyViewCriteria(crit);
      //Execute the query
      view.executeQuery();

      Row[] row = view.getAllRowsInRange();
      //If any rows are returned from the query
      if (row.length > 0) {
        //Retrieve the Pin from the returned row of the query
        Object tempPiin = row[0].getAttribute("Pin");

        //If the temporary Pin value is not null
        if (tempPiin != null)
          //Set this application modules's Pin value
          //using the String representation of the temporary
          //Pin value
          setPin(tempPiin.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /****************************************************************
   * Description: This function retrieves the serial numbers from the picking_serial_num_track
   * table based on the scn that the user provides.  This value is
   * also placed in it's corresponding setter method for further use
   ***************************************************************/
  public void getSerialInfo(String scn, int niinId, boolean isHistory) {

    String sql;
    StringBuilder serials = new StringBuilder();

    setSerialNumbers("");

    try {
      if (isHistory) {
        sql = "select serial_track.serial_number from pick_serial_lot_num_hist p left join (select serial_number, serial_lot_num_track_id, niin_id from serial_lot_num_track UNION select serial_number, serial_lot_num_track_id, niin_id from serial_lot_num_track_hist) serial_track on p.serial_lot_num_track_id = serial_track.serial_lot_num_track_id where scn = ? and serial_track.niin_id = ? order by serial_track.serial_number";
      }
      else {
        sql = "select serial_track.serial_number from pick_serial_lot_num p left join (select serial_number, serial_lot_num_track_id, niin_id from serial_lot_num_track UNION select serial_number, serial_lot_num_track_id, niin_id from serial_lot_num_track_hist) serial_track on p.serial_lot_num_track_id = serial_track.serial_lot_num_track_id where scn = ? and serial_track.niin_id = ? order by serial_track.serial_number";
      }
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(sql, 0)) {
        stR.setString(1, scn);
        stR.setInt(2, niinId);
        try (ResultSet rs = stR.executeQuery()) {
          while (rs.next()) {
            if (rs.getString(1) != null) {
              if (serials.length() == 0) {
                serials = new StringBuilder(rs.getString(1));
              }
              else {
                serials.append(", ").append(rs.getString(1));
              }
            }
          }
        }
      }
      setSerialNumbers(serials.toString());
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**************************************************************
   * Created By: Evan Hardy
   * Created: October 2007
   * Description: This function retrieves the workstation
   * information specifically for the 1348.
   **************************************************************/
  public void getWorkstationInfo(Integer workstationId) {
    EquipTableViewImpl view;
    try {
      view = getEquipTableView1();
      view.applyViewCriteria(null);

      ViewCriteria crit = view.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();
      critRow.setUpperColumns(true);

      //Select everything from the Equip table where the
      //Equipment Number is equal to the workstation id
      //passed in by the user
      critRow.setAttribute("EquipmentNumber", "=" + workstationId);
      crit.addElement(critRow);

      //Apply the criteria
      view.applyViewCriteria(crit);
      //Execute the query
      view.executeQuery();

      Row[] row = view.getAllRowsInRange();
      //If any rows are returned from the query
      if (row.length > 0) {
        //Retrieve the Name and Description values from the
        //returned query
        Object tempName = row[0].getAttribute("Name");
        Object tempDescription = row[0].getAttribute("Description");

        //If the temporary value of Name is not null
        if (tempName != null)
          //Set this application module's Equipment name value
          //using the temporary Name value
          this.setEquipName(tempName.toString());

        //If the temporary value of Description is not null
        if (tempDescription != null)
          //Set this application module's Description value
          //using the temporary Description value
          this.setEquipDescription(tempDescription.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /****************************************************************
   * Created By: Veronica Berryman
   * Created: June 2008
   * Description: This function retrieves the address information from
   * the Customer table based on the aac provided by the user.  This
   * information will appear in the "ship to" section of the 1348
   ****************************************************************/
  public void getAddressByAac(String aac) {
    CustomerViewObjImpl view;

    try {
      view = getCustomerViewObj1();
      view.applyViewCriteria(null);

      ViewCriteria crit = view.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();
      critRow.setUpperColumns(true);
      //Select everything from the Customer table where the
      //customer id equals the custId value passed in by the user
      critRow.setAttribute("Aac", "='" + aac + "'");
      crit.addElement(critRow);

      //Apply the criteria
      view.applyViewCriteria(crit);
      //Execute the query
      view.executeQuery();

      Row[] row = view.getAllRowsInRange();
      //If any rows are returned by the Query
      if (row.length > 0) {
        //Retrieve the neccessary information from the first
        //row of the returned query
        Object tempAdd1 = row[0].getAttribute("Address1");
        Object tempAdd2 = row[0].getAttribute("Address2");
        Object tempState = row[0].getAttribute("State");
        Object tempCity = row[0].getAttribute("City");
        Object tempZip = row[0].getAttribute("ZipCode");

        //If the temporary value of Address1 is not null
        if (tempAdd1 != null)
          //Set this application module's Address1 value to
          //the temporary Address1 value
          setAddress1(tempAdd1.toString());

        //If the temporary value of Address2 is not null
        if (tempAdd2 != null)
          //Set this application module's Address2 value to
          //the temporary Address2 value
          setAddress2(tempAdd2.toString());

        //If the temporary value of State is not null
        if (tempState != null)
          //Set this application module's Address2 value to
          //the temporary Address2 value
          setState(tempState.toString());

        //If the temporary value of Zip is not null
        if (tempZip != null)
          //Set this application module's Zip value to
          //the temporary Zip value
          setZip(tempZip.toString());

        //If the temporary value of City is not null
        if (tempCity != null)
          //Set this application module's City value to
          //the temporary City value
          setCity(tempCity.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void getAddressOfSiteAac() {

    try {
      SiteInfoViewObjImpl view = getSiteInfoViewObj1();
      view.applyViewCriteria(null);
      view.executeQuery();

      Row[] row = view.getAllRowsInRange();
      //If the query returns any rows
      if (row.length > 0) {
        //Retrieve the City, State, ZipCode, and SupplyCenterName
        //attributes from the returned query
        Object tempSupcenter = row[0].getAttribute("SupplyCenterName");
        Object tempCity = row[0].getAttribute("City");
        Object tempState = row[0].getAttribute("State");
        Object tempZip = row[0].getAttribute("ZipCode");
        Object tempRId = row[0].getAttribute("RoutingId");
        Object tempAac = row[0].getAttribute("Aac");

        //If the temporary value of the Aac is not null
        if (tempAac != null) {
          val aac = tempAac.toString();
          this.setSiteAac(aac);
          this.setFromAac(aac);
        }

        //If the temporary value of the Route Id is not null
        if (tempRId != null)
          //Set this application module's Route Id value
          //using the temporary value of Route Id
          this.setRoutingId(tempRId.toString());
        //If the temporary value of Supply Center is not null
        if (tempSupcenter != null)
          //Set this application module's Supply Center value
          //using the temporary value of Supply Center
          this.setSupplyCenter(tempSupcenter.toString());
        //If the temporary value of City is not null
        if (tempCity != null)
          //Set this application module's City value
          //using the temporary value of City
          this.setiCity(tempCity.toString());
        //If the temporary value of State is not null
        if (tempState != null)
          //Set this application module's State value
          //using the temporary value of State
          this.setiState(tempState.toString());
        //If the temporary value of Zip is not null
        if (tempZip != null)
          //Set this application module's Zip value
          //using the temporary value of Zip
          this.setiZip(tempZip.toString());
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int checkInvSize(boolean flag) {
    int retVal = 0;
    try {
      InvPendingViewObjImpl view = this.getInvPendingViewObj1();
      int index = view.getCurrentRowIndex();

      if (!flag)
        index += 1;

      if (index != -1) {
        if (index == view.getRowCount())
          retVal = 1;
        else if (index == view.getRowCount() - 1)
          retVal = 2;
        else
          retVal = 3;
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return retVal;
  }

  /**
   * Container's getter for InvPendingViewObj1.
   */
  public InvPendingViewObjImpl getInvPendingViewObj1() {
    return (InvPendingViewObjImpl) findViewObject("InvPendingViewObj1");
  }

  /**
   * Container's getter for NiinInfoViewObj1.
   */
  public NiinInfoViewObjImpl getNiinInfoViewObj1() {
    return (NiinInfoViewObjImpl) findViewObject("NiinInfoViewObj1");
  }

  /**
   * Container's getter for NiinGroupViewObj1.
   */
  public NiinGroupViewObjImpl getNiinGroupViewObj1() {
    return (NiinGroupViewObjImpl) findViewObject("NiinGroupViewObj1");
  }

  /**
   * Container's getter for NiinLocationViewObj1.
   */
  public NiinLocationViewObjImpl getNiinLocationViewObj1() {
    return (NiinLocationViewObjImpl) findViewObject("NiinLocationViewObj1");
  }

  /**
   * Container's getter for InvItemViewObj1.
   */
  public InvItemViewObjImpl getInvItemViewObj1() {
    return (InvItemViewObjImpl) findViewObject("InvItemViewObj1");
  }

  /**
   * Container's getter for NiinLookupViewObj1.
   */
  public NiinLookupViewObjImpl getNiinLookupViewObj1() {
    return (NiinLookupViewObjImpl) findViewObject("NiinLookupViewObj1");
  }

  /**
   * Container's getter for LocPendingViewObj1.
   */
  public LocPendingViewObjImpl getLocPendingViewObj1() {
    return (LocPendingViewObjImpl) findViewObject("LocPendingViewObj1");
  }

  /**
   * Container's getter for EquipTableView1.
   */
  public EquipTableViewImpl getEquipTableView1() {
    return (EquipTableViewImpl) findViewObject("EquipTableView1");
  }

  /**
   * Container's getter for InventorySetupModule1.
   */
  public ApplicationModuleImpl getInventorySetupModule1() {
    return (ApplicationModuleImpl) findApplicationModule("InventorySetupModule1");
  }

  /**
   * Container's getter for InventoryView1.
   */
  public InventoryViewImpl getInventoryView1() {
    return (InventoryViewImpl) findViewObject("InventoryView1");
  }

  /**
   * Container's getter for PickingViewObj1.
   */
  public PickingViewObjImpl getPickingViewObj1() {
    return (PickingViewObjImpl) findViewObject("PickingViewObj1");
  }

  /**
   * Container's getter for NiinInfoView2Obj1.
   */
  public NiinInfoView2ObjImpl getNiinInfoView2Obj1() {
    return (NiinInfoView2ObjImpl) findViewObject("NiinInfoView2Obj1");
  }

  /**
   * Container's getter for IssueViewObj1.
   */
  public IssueViewObjImpl getIssueViewObj1() {
    return (IssueViewObjImpl) findViewObject("IssueViewObj1");
  }

  /**
   * Container's getter for CustomerViewObj1.
   */
  public CustomerViewObjImpl getCustomerViewObj1() {
    return (CustomerViewObjImpl) findViewObject("CustomerViewObj1");
  }

  /**
   * Container's getter for SiteInfoViewObj1.
   */
  public SiteInfoViewObjImpl getSiteInfoViewObj1() {
    return (SiteInfoViewObjImpl) findViewObject("SiteInfoViewObj1");
  }

  /**
   * Container's getter for LocClassificationViewObj1.
   */
  public LocClassificationViewObjImpl getLocClassificationViewObj1() {
    return (LocClassificationViewObjImpl) findViewObject("LocClassificationViewObj1");
  }

  public void setSurveySelectIndex(int surveySelectIndex) {
    this.surveySelectIndex = surveySelectIndex;
  }

  public int getSurveySelectIndex() {
    return surveySelectIndex;
  }

  public void setEmpty(Boolean empty) {
    this.empty = empty;
  }

  public Boolean getEmpty() {
    return empty;
  }

  public void setSurveyEmpty(Boolean surveyEmpty) {
    this.surveyEmpty = surveyEmpty;
  }

  public Boolean getSurveyEmpty() {
    return surveyEmpty;
  }

  public void setDividerIndex(int dividerIndex) {
    this.dividerIndex = dividerIndex;
  }

  public int getDividerIndex() {
    return dividerIndex;
  }

  public void setSelectIndex(int selectIndex) {
    this.selectIndex = selectIndex;
  }

  public int getSelectIndex() {
    return selectIndex;
  }

  public void setCheck(Boolean check) {
    this.check = check;
  }

  public Boolean getCheck() {
    return check;
  }

  public void setNiins(String[] niins) {
    this.niins = niins;
  }

  public String[] getNiins() {
    return niins;
  }

  public void setQtyNeeded(int qty) {
    this.qtyNeeded = qty;
  }

  public int getQtyNeeded() {
    return qtyNeeded;
  }

  public void setWarehouseAc(int warehouseAc) {
    this.warehouseAc = warehouseAc;
  }

  public int getWarehouseAc() {
    return warehouseAc;
  }

  public void setNomenclature(String nomenclature) {
    this.nomenclature = nomenclature;
  }

  public String getNomenclature() {
    return nomenclature;
  }

  public void setUi(String ui) {
    this.ui = ui;
  }

  public String getUi() {
    return ui;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setLocationId(int locationId) {
    this.locationId = locationId;
  }

  public int getLocationId() {
    return locationId;
  }

  public void setDocIdent(String docIdent) {
    this.docIdent = docIdent;
  }

  public String getDocIdent() {
    return docIdent;
  }

  public void setRiFrom(String riFrom) {
    this.riFrom = riFrom;
  }

  public String getRiFrom() {
    return riFrom;
  }

  public void setMediaStatus(String mediaStatus) {
    this.mediaStatus = mediaStatus;
  }

  public String getMediaStatus() {
    return mediaStatus;
  }

  public void setUnitIss(String unitIss) {
    this.unitIss = unitIss;
  }

  public String getUnitIss() {
    return unitIss;
  }

  public void setSupAddress(String supAddress) {
    this.supAddress = supAddress;
  }

  public String getSupAddress() {
    return supAddress;
  }

  public void setSignal(String signal) {
    this.signal = signal;
    if (!this.signal.equals("A") && !this.signal.equals("B") && !this.signal.equals("C")) {
      this.address1 = "";
      this.address2 = "";
    }
  }

  public String getSignal() {
    return signal;
  }

  public void setFund(String fund) {
    this.fund = fund;
  }

  public String getFund() {
    return fund;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getPriority() {
    return priority;
  }

  public void setReqDelDate(String reqDelDate) {
    this.reqDelDate = reqDelDate;
  }

  public String getReqDelDate() {
    return reqDelDate;
  }

  public void setAdvice(String advice) {
    this.advice = advice;
  }

  public String getAdvice() {
    return advice;
  }

  public void setUnitWeight(Double unitWeight) {
    this.unitWeight = unitWeight;
  }

  public Double getUnitWeight() {
    return (unitWeight == null) ? 0.0 : unitWeight;
  }

  public void setUnitCube(Double unitCube) {
    this.unitCube = unitCube;
  }

  public Double getUnitCube() {
    return (unitCube == null) ? 0.0 : unitCube;
  }

  public void setuDollars(String uDollars) {
    this.uDollars = uDollars;
  }

  public String getuDollars() {
    return uDollars;
  }

  public void setuCents(String uCents) {
    this.uCents = uCents;
  }

  public String getuCents() {
    return uCents;
  }

  public void settDollars(String tDollars) {
    this.tDollars = tDollars;
  }

  public String gettDollars() {
    return tDollars;
  }

  public void settCents(String tCents) {
    this.tCents = tCents;
  }

  public String gettCents() {
    return tCents;
  }

  public void setInomenclature(String inomenclature) {
    this.inomenclature = inomenclature;
  }

  public String getInomenclature() {
    return inomenclature;
  }

  public void setDistribution(String distribution) {
    this.distribution = distribution;
  }

  public String getDistribution() {
    return distribution;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getProject() {
    return project;
  }

  public void settWeight(Double tWeight) {
    this.tWeight = tWeight;
  }

  public Double gettWeight() {
    return (tWeight == null) ? 0.0 : tWeight;
  }

  public void settCube(Double tCube) {
    this.tCube = tCube;
  }

  public Double gettCube() {
    return (tCube == null) ? 0.0 : tCube;
  }

  public void setQty(String qty) {
    this.qty = qty;
  }

  public String getQty() {
    return qty;
  }

  public void setSiteAac(String aac) {
    this.siteAac = aac;
  }

  public String getSiteAac() {
    return siteAac;
  }

  public void setAac(String aac) {
    this.aac = aac;
  }

  public String getAac() {
    return aac;
  }

  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  public String getAddress1() {
    return address1;
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  public String getAddress2() {
    return address2;
  }

  public void setAddressName(String name) {
    this.addressName = name;
  }

  public String getAddressName() {
    return addressName;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCity() {
    return city;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public String getZip() {
    return zip;
  }

  public void setCc(String cc) {
    this.cc = cc;
  }

  public String getCc() {
    return cc;
  }

  public void setEro(String ero) {
    this.ero = ero;
  }

  public String getEro() {
    return ero;
  }

  public void setPin(String pin) {
    this.pin = pin;
  }

  public String getPin() {
    return pin;
  }

  public void setSerialNumbers(String serial) {
    this.serialNumbers = serial;
  }

  public String getSerialNumbers() {
    return serialNumbers;
  }

  public void setEquipDescription(String equipDescription) {
    this.equipDescription = equipDescription;
  }

  public String getEquipDescription() {
    return equipDescription;
  }

  public void setEquipName(String equipName) {
    this.equipName = equipName;
  }

  public String getEquipName() {
    return equipName;
  }

  public void setSupplyCenter(String supplyCenter) {
    this.supplyCenter = supplyCenter;
  }

  public String getSupplyCenter() {
    return supplyCenter;
  }

  public void setiCity(String iCity) {
    this.iCity = iCity;
  }

  public String getiCity() {
    return iCity;
  }

  public void setiState(String iState) {
    this.iState = iState;
  }

  public String getiState() {
    return iState;
  }

  public void setiZip(String iZip) {
    this.iZip = iZip;
  }

  public String getiZip() {
    return iZip;
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public String getDocumentNumber() {
    return documentNumber;
  }

  public void setNiin(String niin) {
    this.niin = niin;
  }

  public String getNiin() {
    return niin;
  }

  public void setFsc(String fsc) {
    this.fsc = fsc;
  }

  public String getFsc() {
    return fsc;
  }

  public void setRoutingId(String routingId) {
    this.routingId = routingId;
  }

  public String getRoutingId() {
    return routingId;
  }

  public void setUnitPrice(String unitPrice) {
    this.unitPrice = unitPrice;
  }

  public String getUnitPrice() {
    return unitPrice;
  }

  public void setPartNumber(String partNumber) {
    this.partNumber = partNumber;
  }

  public String getPartNumber() {
    return partNumber;
  }

  public void setTrueQty(String trueQty) {
    this.trueQty = trueQty;
  }

  public String getTrueQty() {
    return trueQty;
  }

  public void setFromAac(String fromAac) {
    this.fromAac = fromAac;
  }

  public String getFromAac() {
    return fromAac;
  }

  /**
   * Container's getter for UniqueNiinViewObj1.
   */
  public UniqueNiinViewObjImpl getUniqueNiinViewObj1() {
    return (UniqueNiinViewObjImpl) findViewObject("UniqueNiinViewObj1");
  }

  /**
   * Container's getter for LocHeaderBinView1.
   */
  public LocHeaderBinViewImpl getLocHeaderBinView1() {
    return (LocHeaderBinViewImpl) findViewObject("LocHeaderBinView1");
  }

  /**
   * Container's getter for ShelfLifeVO1.
   */
  public ShelfLifeVOImpl getShelfLifeVO1() {
    return (ShelfLifeVOImpl) findViewObject("ShelfLifeVO1");
  }

  /**
   * Get the Shelf Life rows.
   */
  public int buildQueueRS(long equipmentNumber, String sortOrder) {

    try {
      int addCount = 0;
      String wacIdStr = "";
      String mechFlag = "";
      this.deleteRowsFromVOForQueue();
      ShelfLifeVOImpl pNQ = getShelfLifeVO1();
      pNQ.setNamedWhereClauseParam(WAC_ID_STR, "-1");
      pNQ.executeQuery();
      ShelfLifeVOImpl pNQA;
      ShelfLifeVOImpl pNQB;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select w.mechanized_flag, w.wac_id from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0)) {
        stR.setLong(1, equipmentNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
          }
        }
      }

      //Build the Stow query
      if (mechFlag.compareTo("V") == 0) {
        pNQ = getShelfLifeVO1();
        pNQ.setRangeSize(-1);
        pNQ.setOrderByClause(" ASSIGN_TO_USER ASC nulls last, BAY, LOC_LEVEL, SLOT ");
        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        // Build where clause for vertical
        pNQ.setWhereClause(" SIDE like 'A' ");
        pNQ.executeQuery();
        pNQ.first();
      }
      else if (mechFlag.compareTo("H") == 0) {

        pNQA = getShelfLifeVOA();
        pNQA.setRangeSize(-1);
        pNQA.setOrderByClause(ORDER_BY_CLAUSE);
        pNQA.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        // Build where clause for Horizantal
        pNQA.setWhereClause(" SIDE like 'A' ");
        pNQA.executeQuery();
        int aCount = pNQA.getRowCount();
        //For Side B
        pNQB = getShelfLifeVOB();
        pNQB.setRangeSize(-1);
        pNQB.setOrderByClause(ORDER_BY_CLAUSE);
        pNQB.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQB.setWhereClause(" SIDE like 'B' ");
        pNQB.executeQuery();
        int bCount = pNQB.getRowCount();
        //Perform the merge of the rows
        int a = 0;
        int b = 0;

        Row ro;
        pNQ.setRangeSize(-1);
        while ((a < aCount) || (b < bCount)) {
          // Add the merge code
          if (a < aCount) {
            ro = pNQA.next();
            if (addCount == 0)
              pNQ.insertRow(ro);
            else
              pNQ.insertRowAtRangeIndex(addCount, ro);
            addCount++;
          }
          if (b < bCount) {
            ro = pNQB.next();
            if (addCount == 0)
              pNQ.insertRow(ro);
            else
              pNQ.insertRowAtRangeIndex(addCount, ro);
            addCount++;
          }
          a++;
          b++;
        }
        pNQA.clearCache();
        pNQB.clearCache();
        pNQ.first();
      }
      else {
        pNQ = getShelfLifeVO1();
        pNQ.setRangeSize(-1);

        pNQ.setOrderByClause(sortOrder);

        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);

        pNQ.executeQuery();
        pNQ.first();
      }
      return pNQ.getRowCount();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 0;
  }

  /**
   * Delete rows.
   */
  public void deleteRowsFromVOForQueue() {
    // Check if a stow is already in the stow list returns true if it exists else false NOT USED
    try {
      ShelfLifeVOImpl pVO = getShelfLifeVO1();
      Row ro = pVO.first();
      if (ro != null)
        pVO.removeCurrentRow();
      while (pVO.hasNext()) {
        pVO.next();
        pVO.removeCurrentRow();
      }
      ShelfLifeVOImpl pVOA = getShelfLifeVOA();
      ro = pVOA.first();
      if (ro != null)
        pVOA.removeCurrentRow();
      while (pVOA.hasNext()) {
        pVOA.next();
        pVOA.removeCurrentRow();
      }
      ShelfLifeVOImpl pVOB = getShelfLifeVOB();
      ro = pVOB.first();
      if (ro != null)
        pVOB.removeCurrentRow();
      while (pVOB.hasNext()) {
        pVOB.next();
        pVOB.removeCurrentRow();
      }
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public String updateExpirationRemarkConfirm(String niinLocIdStr,
                                              String newExpDateStr,
                                              String uid) {
    try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("begin update niin_location set exp_remark = 'N', expiration_date = to_date(?,'YYYY-MM-DD'), modified_by = ? where niin_loc_id = ?; end;", 0)) {
      int niinlocId = Integer.parseInt(niinLocIdStr);
      int systemUserId = Integer.parseInt(uid);

      stR.setString(1, newExpDateStr);
      stR.setInt(2, systemUserId);
      stR.setInt(3, niinlocId);
      stR.executeUpdate();
      this.getDBTransaction().commit();
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  /**
   * Container's getter for ShelfLifeVOA.
   */
  public ShelfLifeVOImpl getShelfLifeVOA() {
    return (ShelfLifeVOImpl) findViewObject("ShelfLifeVOA");
  }

  /**
   * Container's getter for ShelfLifeVOB.
   */
  public ShelfLifeVOImpl getShelfLifeVOB() {
    return (ShelfLifeVOImpl) findViewObject("ShelfLifeVOB");
  }

  /**
   * Container's getter for InvPendingViewObj2.
   */
  public InvPendingViewObjImpl getInvPendingViewObj2() {
    return (InvPendingViewObjImpl) findViewObject("InvPendingViewObj2");
  }

  /**
   * Container's getter for LocPendingViewObj2.
   */
  public LocPendingViewObjImpl getLocPendingViewObj2() {
    return (LocPendingViewObjImpl) findViewObject("LocPendingViewObj2");
  }

  /**
   * Container's getter for InventoryItemVO1.
   */
  public InventoryItemVOImpl getInventoryItemVO1() {
    return (InventoryItemVOImpl) findViewObject("InventoryItemVO1");
  }

  /**
   * Container's getter for InventoryItemVO2.
   */
  public InventoryItemVOImpl getInventoryItemVO2() {
    return (InventoryItemVOImpl) findViewObject("InventoryItemVO2");
  }

  /**
   * Container's getter for InventoryItemVO3.
   */
  public InventoryItemVOImpl getInventoryItemVO3() {
    return (InventoryItemVOImpl) findViewObject("InventoryItemVO3");
  }

  /**
   * Builds a sorted list of INVENTORYPENDING for a WAC so that optimal order is used in processing.
   * Get the InventoryItem rows
   */
  public void buildQueueRSForInventoryItem(long equipmentNumber,
                                           long userId,
                                           boolean assignItemsToUser,
                                           String sortOrder) {

    try {
      int addCount = 0;
      String wacIdStr = "";
      String mechFlag = "";
      String tasksPerTrip = "";
      InventoryItemVOImpl pNQ = getInventoryItemVO1();
      pNQ.setNamedWhereClauseParam(WAC_ID_STR, "-1");
      pNQ.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
      pNQ.executeQuery();
      InventoryItemVOImpl pNQA;
      InventoryItemVOImpl pNQB;

      try (PreparedStatement ps = getDBTransaction().createPreparedStatement(
          "select w.mechanized_flag, w.wac_id, w.tasks_per_trip from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0)) {
        ps.setLong(1, equipmentNumber);

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
            tasksPerTrip = (rs.getString(3) == null) ? "" : rs.getString(3);
          }
        }
      }

      //Build the Inventory query
      if (mechFlag.compareTo("V") == 0) {
        pNQ = getInventoryItemVO1();
        pNQ.setRangeSize(-1);
        pNQ.setOrderByClause(" LOC_LEVEL, BAY, SLOT ");
        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQ.setNamedWhereClauseParam("usrId", userId);
        pNQ.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
        // Build where clause for vertical
        pNQ.setWhereClause(" SIDE like 'A' ");
        pNQ.executeQuery();
        pNQ.first();
      }
      else if (mechFlag.compareTo("H") == 0) {

        pNQA = getInventoryItemVO2();
        pNQA.setRangeSize(-1);
        pNQA.setOrderByClause(ORDER_BY_CLAUSE);
        pNQA.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQA.setNamedWhereClauseParam("usrId", userId);
        pNQA.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
        // Build where clause for Horizontal
        pNQA.setWhereClause(" SIDE like 'A' ");
        pNQA.executeQuery();
        int aCount = pNQA.getRowCount();

        //For Side B
        pNQB = getInventoryItemVO3();
        pNQB.setRangeSize(-1);
        pNQB.setOrderByClause(ORDER_BY_CLAUSE);
        pNQB.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQB.setNamedWhereClauseParam("usrId", userId);
        pNQB.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
        pNQB.setWhereClause(" SIDE like 'B' ");
        pNQB.executeQuery();
        int bCount = pNQB.getRowCount();
        //Perform the merge of the rows
        int a = 0;
        int b = 0;

        Row ro;
        pNQ.setRangeSize(-1);
        while ((a < aCount) || (b < bCount)) {
          // Add the merge code
          if (a < aCount) {
            ro = pNQA.next();
            if (addCount == 0)
              pNQ.insertRow(ro);
            else
              pNQ.insertRowAtRangeIndex(addCount, ro);
            addCount++;
          }
          if (b < bCount) {
            ro = pNQB.next();
            if (addCount == 0)
              pNQ.insertRow(ro);
            else
              pNQ.insertRowAtRangeIndex(addCount, ro);
            addCount++;
          }
          a++;
          b++;
        }
        pNQA.clearCache();
        pNQB.clearCache();
        pNQ.first();
      }
      else {
        pNQ = getInventoryItemVO1();
        pNQ.setRangeSize(-1);
        int fetchSize = (tasksPerTrip.equals("")) ? 12 : Integer.parseInt(tasksPerTrip);
        pNQ.setMaxFetchSize(fetchSize); // MLS2STRAT-382 - set the fetched row based on tasks_per_trip

        pNQ.setOrderByClause(" ASSIGN_TO_USER ASC nulls last, " + sortOrder);

        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQ.setNamedWhereClauseParam("usrId", userId);
        pNQ.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));

        pNQ.executeQuery();
        addCount = pNQ.getRowCount();
        pNQ.first();

        //MLS2STRAT-382 - assign inventory/location survey items to user
        if (assignItemsToUser && addCount > 0) {
          assignInvItemsToUser(addCount, pNQ, String.valueOf(userId));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Used to remove rows from the queue.
   */
  public void deleteRowsFromInventoryItemVOForQueue() {
    // Check if a InventoryItem is already in the  list returns true if it exists else false NOT USED
    try {

      InventoryItemVOImpl pVO = getInventoryItemVO1();
      Row ro = pVO.first();
      if (ro != null)
        pVO.removeCurrentRow();
      while (pVO.hasNext()) {
        pVO.next();
        pVO.removeCurrentRow();
      }
      InventoryItemVOImpl pVOA = getInventoryItemVO2();
      ro = pVOA.first();
      if (ro != null)
        pVOA.removeCurrentRow();
      while (pVOA.hasNext()) {
        pVOA.next();
        pVOA.removeCurrentRow();
      }
      InventoryItemVOImpl pVOB = getInventoryItemVO3();
      ro = pVOB.first();
      if (ro != null)
        pVOB.removeCurrentRow();
      while (pVOB.hasNext()) {
        pVOB.next();
        pVOB.removeCurrentRow();
      }
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void updateInventoryItem(long invtemId, int uId, int invQty,
                                  int niinLocQty, int niinId, String srlOrLotFlag) {
    int cPQty = 0;
    int cNQty = 0;
    if ((invQty - niinLocQty) > 0)
      cPQty = invQty - niinLocQty;
    else
      cNQty = niinLocQty - invQty;

    try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("begin update inventory_item set num_counts = (nvl(num_counts,0) + 1), num_counted = ?, modified_by = ?, cum_pos_adj = ?, cum_neg_adj = ?, niin_loc_qty = ?, completed_by=?, completed_date=sysdate where inventory_item_id = ?; end;", 0)) {

      stR.setInt(1, invQty);
      stR.setInt(2, uId);
      stR.setInt(3, cPQty);
      stR.setInt(4, cNQty);
      stR.setLong(5, niinLocQty);
      stR.setInt(6, uId);
      stR.setLong(7, invtemId);
      stR.executeUpdate();
      this.getDBTransaction().commit();

      this.createSrlOrLotAndInvXref(String.valueOf(invtemId), String.valueOf(niinId));

      InventorySetupModuleImpl inventorySetup = getInventorySetupModuleService();
      inventorySetup.setUserId(uId);
      inventorySetup.verifyInventoryCount(invtemId);
      if ((cPQty > 0 || cNQty > 0) && (srlOrLotFlag.equalsIgnoreCase("Y")) && !inventorySetup.isNIINInventory(invtemId)) {
        inventorySetup.createNIINInventory(niinId, true, false);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Container's getter for LocSurveyVO1.
   */
  public LocSurveyVOImpl getLocSurveyVO1() {
    return (LocSurveyVOImpl) findViewObject("LocSurveyVO1");
  }

  /**
   * Container's getter for LocSurveyVO2.
   */
  public LocSurveyVOImpl getLocSurveyVO2() {
    return (LocSurveyVOImpl) findViewObject("LocSurveyVO2");
  }

  /**
   * Container's getter for LocSurveyVO3.
   */
  public LocSurveyVOImpl getLocSurveyVO3() {
    return (LocSurveyVOImpl) findViewObject("LocSurveyVO3");
  }

  /**
   * Builds a sorted list of LOCSURVEYPENDING for a WAC so that optimal order is used in processing.
   */
  public void buildQueueRSForLocSurvey(long equipmentNumber,
                                       long userId,
                                       boolean assignItemsToUser,
                                       String sortOrder) {

    try {
      int addCount = 0;
      String wacIdStr = "";
      String mechFlag = "";
      String tasksPerTrip = "";
      LocSurveyVOImpl pNQ = getLocSurveyVO1();
      pNQ.setNamedWhereClauseParam(WAC_ID_STR, "-1");
      pNQ.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
      pNQ.executeQuery();
      LocSurveyVOImpl pNQA;
      LocSurveyVOImpl pNQB;
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement(
          "select w.mechanized_flag, w.wac_id, w.tasks_per_trip from wac w, equip e where e.wac_id = w.wac_id and e.equipment_number = ?", 0)) {
        stR.setLong(1, equipmentNumber);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            mechFlag = rs.getString(1);
            wacIdStr = rs.getString(2);
            tasksPerTrip = (rs.getString(3) == null) ? "" : rs.getString(3);
          }
        }
      }

      //Build the Stow query
      if (mechFlag.compareTo("V") == 0) {
        pNQ = getLocSurveyVO1();
        pNQ.setRangeSize(-1);
        pNQ.setOrderByClause(" LOC_LEVEL, BAY, SLOT ");
        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQ.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
        // Build where clause for vertical
        pNQ.setWhereClause(" SIDE like 'A' ");
        pNQ.executeQuery();
        pNQ.first();
      }
      else if (mechFlag.compareTo("H") == 0) {

        pNQA = getLocSurveyVO2();
        pNQA.setRangeSize(-1);
        pNQA.setOrderByClause(ORDER_BY_CLAUSE);
        pNQA.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQA.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
        // Build where clause for Horizantal
        pNQA.setWhereClause(" SIDE like 'A' ");
        pNQA.executeQuery();
        int aCount = pNQA.getRowCount();
        //For Side B
        pNQB = getLocSurveyVO3();
        pNQB.setRangeSize(-1);
        pNQB.setOrderByClause(ORDER_BY_CLAUSE);
        pNQB.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQB.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));
        pNQB.setWhereClause(" SIDE like 'B' ");

        pNQB.executeQuery();
        int bCount = pNQB.getRowCount();
        //Perform the merge of the rows
        int a = 0;
        int b = 0;

        Row ro;
        pNQ.setRangeSize(-1);
        while ((a < aCount) || (b < bCount)) {
          // Add the merge code
          if (a < aCount) {
            ro = pNQA.next();
            if (addCount == 0)
              pNQ.insertRow(ro);
            else
              pNQ.insertRowAtRangeIndex(addCount, ro);
            addCount++;
          }
          if (b < bCount) {
            ro = pNQB.next();
            if (addCount == 0)
              pNQ.insertRow(ro);
            else
              pNQ.insertRowAtRangeIndex(addCount, ro);
            addCount++;
          }
          a++;
          b++;
        }
        pNQA.clearCache();
        pNQB.clearCache();
        pNQ.first();
      }
      else {
        pNQ = getLocSurveyVO1();
        pNQ.setRangeSize(-1);
        int fetchSize = (tasksPerTrip.equals("")) ? 12 : Integer.parseInt(tasksPerTrip);
        pNQ.setMaxFetchSize(fetchSize); // MLS2STRAT-382 - set the fetched row based on tasks_per_trip

        pNQ.setOrderByClause(" ASSIGN_TO_USER ASC nulls last, " + sortOrder);

        pNQ.setNamedWhereClauseParam(WAC_ID_STR, wacIdStr);
        pNQ.setNamedWhereClauseParam(ASSIGN_TO_USER_ID, String.valueOf(userId));

        pNQ.executeQuery();
        addCount = pNQ.getRowCount();
        pNQ.first();

        //MLS2STRAT-382 - assign inventory/location survey items to user
        if (assignItemsToUser && addCount > 0) {
          assignInvItemsToUser(addCount, pNQ, String.valueOf(userId));
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void assignInvItemsToUser(int addCount, ViewObjectImpl pNQ, String userIdStr) throws SQLException {
    //MLS2STRAT-382 - assign inventory/location survey items to user
    if (addCount > 0) {
      int i = 0;
      Row ro = null;
      log.trace("== MLS2STRAT-382 ==  # of rows= {}", addCount);
      StringBuilder invItemIdsList = new StringBuilder();
      while (i < addCount) {
        if (i == 0) {
          ro = pNQ.getCurrentRow();
        }

        if (ro != null) {
          log.trace("== MLS2STRAT-382 ==  invItemId({})= {}", i, ro.getAttribute("InventoryItemId"));

          //Build the PID string list
          if (invItemIdsList.length() > 0) {
            invItemIdsList.append(", ").append(ro.getAttribute("InventoryItemId"));
          }
          else {
            invItemIdsList.append(ro.getAttribute("InventoryItemId"));
          }
        }
        i++;
        ro = pNQ.next();
      }
      pNQ.first();

      // Assign user for fetched rows
      log.trace("== MLS2STRAT-382 ==  invItemIdsList= {}", invItemIdsList);
      log.trace("== MLS2STRAT-382 ==  userid= {}", userIdStr);

      val sql = String.format("begin update inventory_item set  assign_to_user = ?, modified_by = ? where inventory_item_id in (%s); end;", invItemIdsList.toString());
      try (PreparedStatement psAssignItems = getDBTransaction().createPreparedStatement(sql, 0)) {

        psAssignItems.setString(1, userIdStr);
        psAssignItems.setString(2, userIdStr);
        psAssignItems.executeUpdate();
        getDBTransaction().commit();
      }
    }
  }

  public void clearAssignedUserInInventoryItems(Integer userId) {
    try {
      if (userId != null && userId >= 0) {
        //* clear assign_to_user in the inventory_item
        String sqlClear = "update INVENTORY_ITEM set assign_to_user = null where assign_to_user = ?";
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlClear, 0)) {
          pstmt.setInt(1, userId);
          pstmt.executeUpdate();
          getDBTransaction().commit();
        }
      }
    }
    catch (SQLException e) {
      getDBTransaction().rollback();
      log.warn("Failed to clear assigned user inventory items", e);
    }
  }

  public void updateLocSurvey(String niinId, int uId, String locId) {
    try {
      if (niinId == null || !niinId.equalsIgnoreCase("ALL")) {
        NIINLocDetailsVOImpl dvo = getNIINLocDetailsVO1();
        //* patch
        dvo.clearCache();
        dvo.setNamedWhereClauseParam("locIdStr", locId);
        dvo.executeQuery();
        Row nlDRow = dvo.first();
        this.updateInventoryItemLocSurveyForNiin(nlDRow.getAttribute("Niin").toString(), uId, locId);
        while (dvo.hasNext()) {
          nlDRow = dvo.next();
          this.updateInventoryItemLocSurveyForNiin(nlDRow.getAttribute("Niin").toString(), uId, locId);
        }
      }
      else {
        //* patch - if location is empty, don't show failed
        String sqlCount = "select count(*) from niin_location where location_id = " + locId;
        boolean hasNiin = DBUtil.getCountValue(sqlCount, getDBTransaction()) > 0;
        String newStatus = hasNiin ? "LOCSURVEYFAILED" : "LOCSURVEYCOMPLETED";
        try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("begin update inventory_item set num_counts = (nvl(num_counts,0) + 1),  status = ?,  modified_by = ? where " + "inv_type='LOCSURVEY' and location_id = ?; end;", 0)) {
          stR.setString(1, newStatus);
          stR.setInt(2, uId);
          stR.setString(3, locId);
          stR.executeUpdate();
        }
        this.getDBTransaction().commit();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void updateInventoryItemLocSurveyForNiin(String niinFoundStr,
                                                  int uId, String locId) {

    try {
      boolean niinFound = false;
      String nlLocId = "0";
      String niinId = "0";
      try (PreparedStatement ps = getDBTransaction().createPreparedStatement("select nl.niin_loc_id, nl.niin_id from niin_location nl, niin_info nf where nf.niin_id = nl.niin_id and nf.niin = ? and location_id = ?", 0)) {
        ps.setString(1, niinFoundStr);
        ps.setString(2, locId);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            nlLocId = rs.getString(1);
            niinId = rs.getString(2);
          }
        }
      }

      if (!nlLocId.equalsIgnoreCase("0")) {
        NiinLocVOImpl vo = getNiinLocVO1();
        Row nlRow = vo.first();
        if (nlRow.getAttribute("Niin").toString().equalsIgnoreCase(niinFoundStr)) {
          niinFound = true;
        }
        else {
          while ((vo.hasNext()) && (!niinFound)) {
            nlRow = vo.next();
            if (nlRow.getAttribute("Niin").toString().equalsIgnoreCase(niinFoundStr)) {
              niinFound = true;
            }
          }
        }
        if (niinFound) {
          try (PreparedStatement ps = this.getDBTransaction().createPreparedStatement("begin update inventory_item set  status = ?,  modified_by = ? where niin_id = ? and niin_loc_id = ? and inv_type='LOCSURVEY'; end;", 0)) {
            ps.setString(1, "LOCSURVEYCOMPLETED");
            ps.setInt(2, uId);
            ps.setString(3, niinId);
            ps.setString(4, nlLocId);
            ps.executeUpdate();
          }
          this.getDBTransaction().commit();
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Container's getter for NiinLocVO1.
   */
  public NiinLocVOImpl getNiinLocVO1() {
    return (NiinLocVOImpl) findViewObject("NiinLocVO1");
  }

  public void clearNiinLocVOList() {
    try {
      NiinLocVOImpl vo = getNiinLocVO1();
      vo.clearCache();
      vo.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void getNIINLocDetailsVOList() {
    try {
      String locIdStr;
      if (this.getLocSurveyVO1().getCurrentRow() != null && this.getLocSurveyVO1().getCurrentRow().getAttribute(LOCATION_ID) != null) {
        locIdStr = this.getLocSurveyVO1().getCurrentRow().getAttribute(LOCATION_ID).toString();
        NIINLocDetailsVOImpl vo = getNIINLocDetailsVO1();
        vo.setNamedWhereClauseParam("locIdStr", locIdStr);
        vo.executeQuery();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void clearNIINLocDetailsVOList() {
    try {
      NIINLocDetailsVOImpl vo = getNIINLocDetailsVO1();
      vo.clearCache();
      vo.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int addNiinLocVOList(String niinStr, String cLocLabel) {
    try {
      boolean flag = false;
      String niinIdStr = "";
      if (niinStr == null || niinStr.length() < 9)
        return -2; //Invalid Niin
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin_id from niin_info where niin =  ?", 0)) {
        stR.setString(1, niinStr);
        try (ResultSet rs = stR.executeQuery()) {
          if (rs.next()) {
            flag = true;
            niinIdStr = rs.getString(1);
          }
        }
      }

      NiinLocVOImpl vo;
      Row row;
      if (flag) {
        flag = false;
        vo = getNiinLocVO1();
        int cou = 0;
        if (vo != null)
          cou = vo.getRowCount();
        int i = 0;
        while ((i < cou) && (!flag)) {
          if (i == 0)
            row = vo.first();
          else
            row = vo.next();
          if (row != null && row.getAttribute("Niin").toString().equalsIgnoreCase(niinStr))
            flag = true;
          i++;
        }
        if (flag)
          return 0; //Already scanned in
        String actionStr = getActionMessage(niinStr, cLocLabel);
        int fl = 1;

        row = vo.createRow();
        row.setAttribute("Niin", niinStr);
        row.setAttribute(NIIN_ID, niinIdStr);
        row.setAttribute("Comments", actionStr);
        row.setAttribute("Cc", "A");
        if ((actionStr.contains("ADD:")) || (actionStr.contains("RELOCATE:"))) {
          row.setAttribute("NewRow", "Y");
          fl = 2;
        }
        else
          row.setAttribute("NewRow", "N");
        if (!actionStr.equals("NONE: NO ACTION NEEDED.")) {
          //This is a NIIN that does not already exist, so it needs to go in the back to ensure that the NIINs that already exist are presented first to the user, so the CC is consistent
          vo.insertRowAtRangeIndex(vo.getRowCount(), row);
        }
        else {
          vo.insertRow(row);
        }
        return fl;
      }
      else
        return -1; //NIIN not in niin_info
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 1;
  }

  public String getActionMessage(String niinStr, String locLbl) {
    try {
      String msgStr;
      boolean flag = false;
      StringBuilder locList = new StringBuilder();

      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select l.location_label from location l, niin_location nl, niin_info nf " + " where nf.niin =  ? and nf.niin_id = nl.niin_id and nl.location_id = l.location_id ", 0)) {
        stR.setString(1, niinStr);
        try (ResultSet rs = stR.executeQuery()) {

          while ((rs.next()) && (!flag)) {
            if (locLbl.equalsIgnoreCase(rs.getString(1)))
              flag = true;
            locList.append(rs.getString(1)).append(", ");
          }
        }
      }
      if (flag)
        msgStr = "NONE: NO ACTION NEEDED.";
      else if (locList.length() <= 0)
        msgStr = "ADD: STRATIS WILL LET YOU ADD " + niinStr + " TO CURRENT " + locLbl + " LOCATION.";
      else {
        msgStr = "RELOCATE: PLEASE RELOCATE NIIN " + niinStr + " TO " + locList + " LOCATION(S). OR " + "ADD TO CURRENT " + locLbl + " LOCATION.";
      }
      return msgStr;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "";
  }

  public void processLocationSurvey(int uId) {

    try {
      Row lRow = this.getLocSurveyVO1().getCurrentRow();
      int stcount = this.getNiinLocVO1().getRowCount();
      Row nlRow = this.getNiinLocVO1().first();
      int i = 0;
      int nlId = 0;
      //Set all NIIN_LOCATION that matched to LOCSURVEYFAILED and then in next call set each successfull one to LOCSURVEYCOMPLETED
      this.updateLocSurvey("ALL", uId, lRow.getAttribute(LOCATION_ID).toString());
      InventorySetupModuleImpl inventory = getInventorySetupModuleService();
      //Capture the user id
      inventory.setUserId(uId);

      val dateService = mil.usmc.mls2.stratis.core.utility.ContextUtils.getBean(DateService.class);
      while ((i < stcount) && (nlRow != null)) {
        if ((nlRow.getAttribute("Comments").toString().contains("ADD:")) || (nlRow.getAttribute("Comments").toString().contains("RELOCATE:"))) {
          if (nlRow.getAttribute("NewRow").toString().equalsIgnoreCase("Y")) {
            //Insert row in NIIN_LOCATION
            try (PreparedStatement stR = this.getDBTransaction().createPreparedStatement("BEGIN INSERT INTO NIIN_LOCATION (NIIN_ID, CC, QTY, EXPIRATION_DATE, CREATED_BY , LOCATION_ID, LOCKED, CREATED_DATE) " + " VALUES(?,?,?,?,?,?,'N',sysdate); END;", 0)) {

              stR.setString(1, nlRow.getAttribute(NIIN_ID).toString());
              stR.setString(2, nlRow.getAttribute("Cc").toString());
              stR.setInt(3, Integer.parseInt(nlRow.getAttribute("Qty").toString()));
              //date string formatted as ddMMyyyy
              val dateString = "01" + (nlRow.getAttribute("ExpirationDate") == null ? "019999" : nlRow.getAttribute("ExpirationDate").toString());
              val expDate = dateService.convertLocalDateStringToLocalDate(dateString, DateConstants.SITE_DATE_DDMMYYYY_INPUT_FORMATTER_PATTERN);
              stR.setObject(4, expDate);
              stR.setInt(5, uId);
              stR.setString(6, lRow.getAttribute(LOCATION_ID).toString());
              stR.executeUpdate();
            }
            this.getDBTransaction().commit();
            try (PreparedStatement stNLR = getDBTransaction().createPreparedStatement("select unique nl.niin_loc_id from niin_location nl where nl.niin_id = ? and nl.location_id = ?", 0)) {
              stNLR.setString(1, nlRow.getAttribute(NIIN_ID).toString());
              stNLR.setString(2, lRow.getAttribute(LOCATION_ID).toString());
              try (ResultSet rs = stNLR.executeQuery()) {
                if (rs.next()) {
                  nlId = rs.getInt(1);
                }
              }
            }
            this.getWorkLoadManagerService().createErrorQueueRecord("51", "NIIN_LOC_ID", String.valueOf(nlId), String.valueOf(uId), null);
            nlId = 0;
          }
          nlRow.setAttribute("NewRow", "X");
          //Create Inventories for NEW and RELOCATED NIINs
          inventory.createNIINInventory(Integer.parseInt(nlRow.getAttribute(NIIN_ID).toString()), false, true);
        }
        if (getNiinLocVO1().hasNext())
          nlRow = this.getNiinLocVO1().next();
        else
          nlRow = null;
        i++;
      }
      //Set as LOCATIONSURVEYCOMPLETE
      if (stcount > 0) //Case of non empty location
        this.updateLocSurvey("", uId, lRow.getAttribute(LOCATION_ID).toString());
      //Create Inventories for failed loc surveys
      try (PreparedStatement ps = getDBTransaction().createPreparedStatement("select i.niin_id, l.wac_id from inventory_item i, location l where i.status = 'LOCSURVEYFAILED' " + " and i.location_id = l.location_id " + " and i.location_id = ?", 0)) {
        ps.setString(1, lRow.getAttribute(LOCATION_ID).toString());
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            inventory.createNIINInventory(rs.getInt(1), false, true);
          }
        }
      }

      //Finish LOCSurvey (new requirement, close Location Survey)
      InventorySetupModuleImpl inventorySetup = getInventorySetupModuleService();
      inventorySetup.submitInventoryItemRemoveSurvey(lRow.getAttribute("NiinLocId"), lRow.getAttribute("InventoryItemId"), lRow.getAttribute("InventoryId"), uId);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Container's getter for NIINLocDetailsVO1.
   */
  public NIINLocDetailsVOImpl getNIINLocDetailsVO1() {
    return (NIINLocDetailsVOImpl) findViewObject("NIINLocDetailsVO1");
  }

  public WorkLoadManagerImpl getWorkLoadManagerService() {
    try {
      WorkLoadManagerImpl service;
      service = (WorkLoadManagerImpl) getInventorySetupModuleService().getWorkLoadManager1();
      return service;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public InventorySetupModuleImpl getInventorySetupModuleService() {
    try {
      InventorySetupModuleImpl service;
      service = (InventorySetupModuleImpl) getInventorySetupModule1();
      return service;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /**
   * Checks to see if the niin needs serial num or lot num or if it is missing any.
   */
  public String checkNiinNeedsAnySrlOrLotInfo(String niinIdStr, int qty,
                                              String srlFlg, String lotFlg,
                                              String invItemId) {
    try {
      int err = 0;
      String id = "";
      if (!this.isNIINInventory(invItemId))
        return "";
      if (qty == 0)
        return ""; // No pick qty so just ignore
      try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select niin_id, serial_number, lot_con_num from serial_lot_num_track  where niin_id = ? and rownum = 1 ", 0)) {
        stR.setInt(1, Integer.parseInt(niinIdStr));
        try (ResultSet rs = stR.executeQuery()) {

          if (rs.next()) {
            id = rs.getString(1);
            if (rs.getString(2) != null)
              err = 1;
            if ((err <= 0) && (rs.getString(3) != null))
              err = 2;
            if ((err == 1) && (rs.getString(3) != null))
              err = 3;
          }
        }
      }

      SerialOrLotNumListInvImpl vo;
      vo = this.getSerialOrLotNumListInv1();
      if ((id.length() <= 0) && (vo == null || vo.getRowCount() <= 0)) {
        if ((srlFlg.equalsIgnoreCase("Y")) && (lotFlg.equalsIgnoreCase("Y")))
          return "STRATIS indicates item(s) needs Serial number(s) and lot con num (s).";
        else if ((srlFlg.equalsIgnoreCase("Y")) && (lotFlg.equalsIgnoreCase("N")))
          return "Serial number entry required.";
        else if ((srlFlg.equalsIgnoreCase("N")) && (lotFlg.equalsIgnoreCase("Y")))
          return "STRATIS indicates item(s) needs lot con num (s) and lot quantity.";
        else
          return "false";
      }
      Row row;
      vo = this.getSerialOrLotNumListInv1();
      int i = 0;
      int qtyList = 0;
      int cou = 0;
      if (vo != null)
        cou = vo.getRowCount();
      while (i < cou) {
        if (i == 0)
          row = vo.first();
        else
          row = vo.next();
        qtyList = qtyList + Integer.parseInt(row.getAttribute(QTY_LOT).toString());
        i++;
      }

      if (qtyList == 0) {
        if (err == 1)
          return "Serial number entry required.";
        if (err == 2)
          return id + " STRATIS indicates item(s) needs lot con num (s) and lot quantity.";
        if (err == 3)
          return id + " STRATIS indicates item(s) needs Serial number(s) and lot con num (s).";
      }
      else if (qtyList < qty) {
        if (err == 1)
          return id + " Please enter serial number(s) of all the items.";
        if (err == 2)
          return id + " Please enter lot con number(s) and lot quantity of all the items.";
        if (err == 3)
          return id + " Please enter Serial and lot con number(s) of all the items.";
      }
      else if (qtyList > qty) {
        if (err == 1)
          return id + " Entered serial number(s) of items are more then the quantity.";
        if (err == 2)
          return id + " Entered lot con number(s) of items are more then the quantity.";
        if (err == 3)
          return id + " Entered Serial and lot con number(s) of items are more then the quantity.";
      }
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Check for Serial or Lot Con requirement failed.";
  }

  /**
   * Creates ref rows for each InvItem.  (INV_SERIAL_LOT_NUM)
   */
  public String createSrlOrLotAndInvXref(String pIdStr, String niinIdStr) {

    try {
      int locId = 1;
      String expDate = "999901";
      try (PreparedStatement ps = getDBTransaction().createPreparedStatement("select nl.location_id, (to_char(nvl(nl.expiration_date,'01-JAN-9999'),'YYYYMM')) from inventory_item i, niin_location nl  where i.inventory_item_id = ? and i.niin_loc_id = nl.niin_loc_id ", 0)) {
        ps.setString(1, pIdStr);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            locId = rs.getInt(1);
            expDate = rs.getString(2);
          }
        }
      }

      SerialOrLotNumListInvImpl vo;
      vo = getSerialOrLotNumListInv1();
      int cou = 0;
      int insCou = 0;
      if (vo != null)
        cou = vo.getRowCount();
      //Check if rows are already created
      try (PreparedStatement ps = getDBTransaction().createPreparedStatement("select count(*) from inv_serial_lot_num where inventory_item_id = ? ", 0)) {
        ps.setInt(1, Integer.parseInt(pIdStr));
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            insCou = rs.getInt(1);
          }
        }
      }

      if (cou <= 0)
        return "";

      if (insCou > 0) {
        //Delete if they are already created
        try (PreparedStatement ps = getDBTransaction().createPreparedStatement("begin delete from inv_serial_lot_num  where inventory_item_id = ? ; end;", 0)) {
          ps.setString(1, pIdStr);
          ps.executeUpdate();
          this.getDBTransaction().commit();
        }
      }
      Row ro = vo.first();

      //recreate from the view object
      while (ro != null) {
        try (PreparedStatement ps = getDBTransaction().createPreparedStatement("begin INSERT INTO inv_serial_lot_num (inventory_item_id, serial_number, lot_con_num, qty, cc, inv_done_flag, timestamp, niin_id, location_id, expiration_date) values (?,?,?,?,?, 'N', sysdate, ?, ?,to_date(nvl(?,'999901'),'YYYYMM')); end;", 0)) {
          ps.setString(1, pIdStr);
          ps.setObject(2, ro.getAttribute("SerialNumber"));
          ps.setObject(3, ro.getAttribute("LotConNum"));
          ps.setObject(4, (ro.getAttribute(QTY_LOT) == null ? "1" : ro.getAttribute(QTY_LOT)));
          ps.setObject(5, (ro.getAttribute("Cc") == null ? "A" : ro.getAttribute("Cc")));
          ps.setObject(6, niinIdStr);
          ps.setInt(7, locId);
          ps.setObject(8, expDate);
          ps.executeUpdate();
        }
        if (vo.hasNext())
          ro = vo.next();
        else
          ro = null;
      }
      getDBTransaction().commit();

      vo.clearCache();
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return "Creation of Serial or Lot Con requirement failed.";
  }

  public String addSerialOrLotNumVOList(String slNum, String lotNum,
                                        String qtyLot, String niinIdStr,
                                        String reqQty, String itemId, String locationId) {
    PreparedStatement stR = null;

    try {
      boolean flag = false;
      int err = 0;
      String id = "";

      if (!RegUtils.isAlphaNumericDashes(slNum)) {
        return "Serial Numbers can contain alphanumeric, underscore, and/or hyphen only.";
      }

      if ((slNum != null && slNum.length() > 0) && ((qtyLot != null && qtyLot.length() > 0) && (Integer.parseInt(qtyLot) > 1)))
        return "For serialized quantity entered must be 1.";

      if ((slNum != null && slNum.length() > 0) && (qtyLot != null && qtyLot.length() > 0)) {
        stR = getDBTransaction().createPreparedStatement("select serial_lot_num_track_id, nvl(serial_number,'') serial_number, nvl(lot_con_num,'') lot_con_num, " + "              nvl(cc,'A'), to_char(expiration_date,'YYYYMM') exp_dt, to_char(sysdate,'YYYYMM') cur_dt, " + "              nvl(issued_flag,'N') from serial_lot_num_track where ((nvl(serial_number,'') =  ?) and (nvl(lot_con_num,'')= ?)) and niin_id = ? ", 0);
        stR.setString(1, slNum);
        stR.setString(2, lotNum);
        stR.setInt(3, Integer.parseInt(niinIdStr));
      }
      else if (slNum != null && slNum.length() > 0) {
        stR = getDBTransaction().createPreparedStatement("select serial_lot_num_track_id, nvl(serial_number,'') serial_number, nvl(lot_con_num,'') lot_con_num, " + "              nvl(cc,'A'), to_char(expiration_date,'YYYYMM') exp_dt, to_char(sysdate,'YYYYMM') cur_dt, " + "              nvl(issued_flag,'N') from serial_lot_num_track where (nvl(serial_number,'') =  ?) and niin_id = ? ", 0);
        stR.setString(1, slNum);
        stR.setInt(2, Integer.parseInt(niinIdStr));
      }
      else if (lotNum != null && lotNum.length() > 0) {
        stR = getDBTransaction().createPreparedStatement("select serial_lot_num_track_id, nvl(serial_number,'') serial_number, nvl(lot_con_num,'') lot_con_num, " + "              nvl(cc,'A'), to_char(expiration_date,'YYYYMM') exp_dt, to_char(sysdate,'YYYYMM') cur_dt, " + "              nvl(issued_flag,'N') from serial_lot_num_track where (nvl(lot_con_num,'')= ?) and niin_id = ? ", 0);
        stR.setString(1, lotNum);
        stR.setInt(2, Integer.parseInt(niinIdStr));
      }
      else {
        log.error("Error in addSerialOrLotNumVOList: unable to generate query, invalid inputs");
        return "Row addition to Serial or Lot Con List failed.";
      }

      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          id = rs.getString(1);
          if (rs.getString(2) != null && rs.getString(7).equalsIgnoreCase("Y"))
            err = 7;
          if ((err <= 0) && (rs.getString(4).equalsIgnoreCase("F")))
            err = 4;
          if ((err <= 0) && (Integer.parseInt(rs.getString(5)) <= Integer.parseInt(rs.getString(6))))
            err = 5;
        }
      }
      if (err == 7)
        return id + " STRATIS indicates item with this serial number is already issued.";

      //Used to get new serial numbers
      String conditionCode = "A";
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select unique nl.cc from niin_location nl, inventory_item m where m.niin_id = nl.niin_id and m.inventory_item_id=? and m.niin_loc_id = nl.niin_loc_id", 0)) {
        pstmt.setObject(1, itemId);
        try (ResultSet rs1 = pstmt.executeQuery()) {
          if (rs1.next()) {
            conditionCode = rs1.getString(1);
          }
        }
      }

      //05-22-2014: Check for a duplicate entry already in the INV_SERIAL_LOT_NUM table
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select serial_number from inv_serial_lot_num where niin_id = ? and serial_number = ? and location_id <> ?", 0)) {
        pstmt.setString(1, niinIdStr);
        pstmt.setString(2, slNum);
        pstmt.setString(3, locationId);
        try (ResultSet rs1 = pstmt.executeQuery()) {
          if (rs1.next()) {
            flag = true;
          }
        }
      }

      if (flag) {
        return "Serial Number already entered in another location";
      }

      SerialOrLotNumListInvImpl vo;
      Row row;
      flag = false;
      vo = this.getSerialOrLotNumListInv1();
      int cou = 0;
      if (vo != null)
        cou = vo.getRowCount();
      int i = 0;
      int qtyListed = Integer.parseInt(qtyLot);

      while ((i < cou) && (!flag)) {
        if (i == 0)
          row = vo.first();
        else
          row = vo.next();
        if (row != null && (row.getAttribute("SerialNumber").toString().equalsIgnoreCase(slNum) && row.getAttribute("LotConNum").toString().equalsIgnoreCase(lotNum))) {
          flag = true;
          qtyListed = qtyListed + Integer.parseInt(row.getAttribute(QTY_LOT).toString());
        }
        i++;
      }
      if (flag)
        return "Srl:" + slNum + "LotNum: " + lotNum + "is already entered."; //Already scanned in
      if (qtyListed > Integer.parseInt(reqQty))
        return "Total quantity added in serial/lot control is more than verified quantity. Please correct.";
      row = vo.createRow();
      row.setAttribute("SerialNumber", slNum);
      row.setAttribute("LotConNum", lotNum);
      row.setAttribute(QTY_LOT, Integer.valueOf(qtyLot));
      row.setAttribute("Cc", conditionCode);
      vo.insertRow(row);
      return "";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    finally {
      try {
        if (stR != null) {
          stR.close();
        }
      }
      catch (Exception e) {
        AdfLogUtility.logException(e);
      }
    }
    return "Row addition to Serial or Lot Con List failed.";
  }

  /**
   * Container's getter for SerialOrLotNumListInv1.
   */
  public SerialOrLotNumListInvImpl getSerialOrLotNumListInv1() {
    return (SerialOrLotNumListInvImpl) findViewObject("SerialOrLotNumListInv1");
  }

  private boolean isNIINInventory(Object inventoryItemID) {
    log.debug("isNIINInventory =  {}", inventoryItemID);
    boolean niinInventory = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select count(*) from inventory i, inventory_item m where i.inventory_id=m.inventory_id" +
            " and inventory_item_id=? and description like '%NIIN%'", 0)) {

      pstmt.setObject(1, inventoryItemID);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0)
            niinInventory = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    return niinInventory;
  }

  /**
   * Container's getter for IssueHistViewObj1.
   */
  public IssueHistViewObjImpl getIssueHistViewObj1() {
    return (IssueHistViewObjImpl) findViewObject("IssueHistViewObj1");
  }

  /**
   * Container's getter for PickingHistViewObj1.
   */
  public PickingHistViewObjImpl getPickingHistViewObj1() {
    return (PickingHistViewObjImpl) findViewObject("PickingHistViewObj1");
  }

  /**
   * Container's getter for Inventory_SetupModule1.
   *
   * @return Inventory_SetupModule1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ApplicationModuleImpl getInventory_SetupModule1() {
    return (ApplicationModuleImpl) findApplicationModule("Inventory_SetupModule1");
  }
}

package mil.stratis.view.print;

import mil.stratis.common.util.Util;
import mil.stratis.model.services.InventoryModuleImpl;
import mil.stratis.view.util.JSFUtils;

public class One348History extends OneThreeFourEightInfo {

  /****************************************************************
   * Description: This function is responsible for generating the script
   * to launch the serial page addendum
   * *************************************************************/
  public String launchSerial() {
    if (serialOverflow) {
      return "window.open('Serial1348Hist.jspx?quantity=" + Util.encodeUTF8(getQuantity()) + "&scn=" + Util.encodeUTF8(theScn) + "');";
    }
    else
      return "";
  }

  public String gatherFormInfo(String scn, String documentNumber, String quantity) {

    String temp;
    Integer temp2;

    String julianDate = getCurrentJulian(4);
    setDocDate(julianDate);

    InventoryModuleImpl Service = getInventoryAMService();

    Service.getFormInfoHist(scn, documentNumber, quantity);

    theScn = scn;

    setDocumentId(Service.getDocIdent());

    setRequisitionFrom(Service.getRiFrom());
    setRoutingIdentifier(Service.getRoutingId());
    setSupplementAddress(Service.getSupAddress());
    setMediaAndStatus(Service.getMediaStatus());
    setDistribution(Service.getDistribution());
    setProjectCode(Service.getProject());
    setPriorityCode(Service.getPriority());
    setAac(Service.getAac());
    setSiteAac(Service.getSiteAac());

    setAdvice(Service.getAdvice());

    setConditionCode(Service.getCc());
    setName(Service.getAddressName());

    setUnitOfIssue(Service.getUi());
    setQuantity(quantity);

    setSignal(Service.getSignal());
    setFundCode(Service.getFund());
    setPriorityCode(Service.getPriority());
    setAdvice(Service.getAdvice());
    setUnitDollars(Service.getuDollars());
    setUnitCents(Service.getuCents());
    setTotalDollars(Service.gettDollars());
    setTotalCents(Service.gettCents());
    setUnitWeight(Service.getUnitWeight().toString());
    setUnitCubed(Service.getUnitCube().toString());
    setTotalWeight(Service.gettWeight().toString());
    setTotalCube(Service.gettCube().toString());

    //5/26/2015: MCF: Fixed issue where nomenclature with apostrophe was creating a gigantic barcode
    String tempNomen = Service.getInomenclature();
    if (tempNomen != null) {
      tempNomen = tempNomen.replaceAll("[^a-zA-Z0-9]", "");
    }

    setItemNomen(tempNomen);
    this.setPin(Service.getPin());
    setSerialNumbers(Service.getSerialNumbers());

    //checks if the serial numbers are over the max (this will print an additional serial number addendum.
    //Serial Numbers are separated by commas
    int count = this.getSerialNumbers().length() - this.getSerialNumbers().replace(",", "").length();
    if (count > MAX_SERIALS) {
      serialOverflow = true;
    }

    temp = (JSFUtils.getManagedBeanValue("userbean.workstationId")).toString();
    temp2 = Integer.parseInt(temp);

    Service.getWorkstationInfo(temp2);
    this.setMarkFor(Service.getAddressName());

    setEquipName(Service.getEquipName());
    setEquipDescription(Service.getEquipDescription());
    this.setInfoCity(Service.getiCity());
    this.setInfoState(Service.getiState());
    this.setInfoZip(Service.getiZip());
    this.setSupplyCenter(Service.getSupplyCenter());
    this.setReqDeldate(Service.getReqDelDate());
    this.setEro(Service.getEro());
    this.setDocumentNumber(Service.getDocumentNumber());
    this.setNiin(Service.getNiin());
    this.setFsc(Service.getFsc());
    this.setUnitPrice(Service.getUnitPrice());
    this.setTrueQty(Service.getTrueQty());
    this.setPin(Service.getPin());

    String theSignal = Util.cleanString(getSignal());
    String theSuppAddr = Util.cleanString(getSupplementAddress());

    if (theSignal.equals("A") || theSignal.equals("B") || theSignal.equals("C") || theSignal.equals("D") || theSignal.equals("W"))
      this.setShipTo(getDocumentNumber().substring(0, 6));
    else {
      if (!Util.isEmpty(theSuppAddr)) this.setShipTo(theSuppAddr);
      else this.setShipTo(getDocumentNumber().substring(0, 6));
    }

    Service.getAddressByAac(getShipTo());
    setAddress1(Service.getAddress1());
    setAddress2(Service.getAddress2());
    this.setState(Service.getState());
    this.setCity(Service.getCity());
    setZip(Service.getZip());

    return "";
  }
}

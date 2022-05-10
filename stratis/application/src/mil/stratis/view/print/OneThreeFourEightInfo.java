package mil.stratis.view.print;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.PadUtil;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.InventoryModuleImpl;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.service.multitenancy.DateService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Calendar;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Slf4j
public class OneThreeFourEightInfo extends BackingHandler {

  private static final char FORMAT_HEADER_GS = 29;
  private static final char FORMAT_HEADER_RS = 30;
  private static final char FORMAT_HEADER_EOT = 4;
  //Maximum number of serial numbers.
  protected static final int MAX_SERIALS = 9;

  String theScn = "";
  String documentId = "";
  String requisitionFrom = "";
  String mediaAndStatus = "";
  String unitOfIssue = "";
  String quantity = "";
  String serviceCode = "";
  String supplementAddress = "";
  String signal = "";
  String fundCode = "";
  String distribution = "";
  String priorityCode = "";
  String reqDeletionDate = "";
  String advice = "";
  String routingIdentifier = "";
  String op = "";
  String conditionCode = "";
  String mgt = "";
  String unitDollars = "";
  String unitCents = "";
  String totalDollars = "";
  String totalCents = "";
  String shipFrom = "";
  String shipTo = "";
  String markFor = "";
  String docDate = "";
  String nmfc = "";
  String frtRate = "";
  String typeCargo = "";
  String ps = "";
  String sl = "";
  String frieghtNomen = "";
  String itemNomen = "";
  String typeCount = "";
  String numCount = "";
  String totalWeight = "";
  String totalCube = "";
  String receivedBy = "";
  String dateReceived = "";
  String up = "";
  String qtyReceived = "";
  String unitWeight = "";
  String unitCubed = "";
  String projectCode = "";
  String mcc = "";
  String ufc = "";
  String siteAac = "";
  String aac = "";
  String address1 = "";
  String address2 = "";
  String name = "";
  String city = "";
  String state = "";
  String zip = "";
  String equipName = "";
  String equipDescription = "";
  String pin = "";
  String supplyCenter = "";
  String infoCity = "";
  String infoState = "";
  String infoZip = "";
  String ero = "";
  String reqDeldate = "";
  String documentNumber = "";
  String niin = "";
  String fsc = "";
  String unitPrice = "";
  String headerCompliance = "[(>";  // escaped value - "%5B)%3E;"
  String partNumber = "";
  String formatHeader = "%";
  String serialNumbers = "";
  boolean serialOverflow = false;
  String trueQty = "";
  String nmfccn = "";
  String serialNumber = "";
  String twoDGenerator = "";

  public OneThreeFourEightInfo() {
    //NO-OP
  }

  public String draw1348Serial() {
    return draw1348Serial(ContextUtils.getBean(DateService.class), OffsetDateTime.now());
  }

  public String draw1348Serial(final DateService dateService, final OffsetDateTime offsetDateTime) {
    StringBuilder sb = new StringBuilder(5000);

    sb.append("<h1>Serial Number Addendum</h1>");
    sb.append("<table cellspacing='5' cellpadding='5' border='0'>");
    sb.append("<font style='font-size:10px;font-weight:bold;'>");
    sb.append("<tr>");
    sb.append("<td>Print Date/Time</td>");
    sb.append("<td>PIN</td>");
    sb.append("<td>Document Number</td>");
    sb.append("<td>NSN</td>");
    sb.append("<td>Quantity</td>");
    sb.append("</tr>");
    sb.append("</font>");
    sb.append("<font style='font-size:10px;'>");
    sb.append("<tr>");
    sb.append("<td>").append(escapeHtml(this.getPrintDate(dateService, offsetDateTime))).append("</td>");
    sb.append("<td>").append(escapeHtml(this.getPin())).append("</td>");
    sb.append("<td>").append(escapeHtml(this.getDocumentNumber())).append("</td>");
    sb.append("<td>").append(escapeHtml(this.getFsc() + this.getNiin())).append("</td>");
    String qty = getQuantity();

    if (qty != null) {
      if (qty.equals("") || qty.equals("null"))
        qty = " ";
    }
    else {
      qty = " ";
    }

    sb.append("<td>").append(escapeHtml(PadUtil.padItZeros(qty, 5))).append("</td>");
    sb.append("</tr>");
    sb.append("</font></table>");
    sb.append("<h2>Serial Numbers</h2>");
    sb.append("<table cellspacing='5' cellpadding='5' border='0'>");
    sb.append("<font style='font-size:10px;'>");

    String[] serials = this.getSerialNumbers().split(",");
    int i = 0;
    for (i = 0; i < serials.length; i++) {
      if (i % 4 == 0) {
        if (i != 0) {
          sb.append("</tr>");
        }
        sb.append("<tr>");
      }
      sb.append("<td>").append(escapeHtml(serials[i])).append("</td>");
    }
    if ((i - 1) % 4 == 0) {
      sb.append("</tr>");
    }
    sb.append("</font></table>");
    return sb.toString();
  }

  public String draw1348() {
    return draw1348(getStratisUrlContextPath(), ContextUtils.getBean(DateService.class), OffsetDateTime.now());
  }

  public String draw1348(final String urlContextPath,
                         final DateService dateService,
                         final OffsetDateTime offsetDateTime) {
    StringBuilder sb = new StringBuilder(5000);

    sb.append("<table cellspacing='0' cellpadding='2' border='0' height='350'>");

    sb.append("<tr>");
    sb.append("<td valign='center' style='font-size:4px;'>1</td>");
    sb.append("<td valign='center' style='font-size:4px;'>2</td>");
    sb.append("<td valign='center' style='font-size:4px;'>3</td>");
    sb.append("<td valign='center' style='font-size:4px;'>4</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7</td>");
    sb.append("<td valign='center' style='font-size:4px;'>&nbsp;</td>");

    sb.append("<td valign='center' style='font-size:4px;'>2<br>3</td>");
    sb.append("<td valign='center' style='font-size:4px;'>2<br>4</td>");
    sb.append("<td valign='center' style='font-size:4px;'>2<br>5</td>");
    sb.append("<td valign='center' style='font-size:4px;'>2<br>6</td>");
    sb.append("<td valign='center' style='font-size:4px;'>2<br>7</td>");
    sb.append("<td valign='center' style='font-size:4px;'>2<br>8</td>");
    sb.append("<td valign='center' style='font-size:4px;'>2<br>9</td>");
    sb.append("<td valign='center' style='font-size:4px;'>&nbsp;</td>");

    sb.append("<td valign='center' style='font-size:4px;'>4<br>5</td>");
    sb.append("<td valign='center' style='font-size:4px;'>4<br>6</td>");
    sb.append("<td valign='center' style='font-size:4px;'>4<br>7</td>");
    sb.append("<td valign='center' style='font-size:4px;'>4<br>8</td>");
    sb.append("<td valign='center' style='font-size:4px;'>4<br>9</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>0</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>1</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>2</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>3</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>4</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>5</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>6</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>7</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>8</td>");
    sb.append("<td valign='center' style='font-size:4px;'>5<br>9</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>0</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>1</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>2</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>3</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>4</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>5</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>6</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>7</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>8</td>");
    sb.append("<td valign='center' style='font-size:4px;'>6<br>9</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>0</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>1</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>2</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>3</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>4</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>5</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>6</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>7</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>8</td>");
    sb.append("<td valign='center' style='font-size:4px;'>7<br>9</td>");
    sb.append("<td valign='center' style='font-size:4px;'>8<br>0</td>");

    sb.append("<td valign='top' colspan='3' style='font-size:4px;'>1. TOTAL PRICE</td>");

    sb.append("<td valign='top' width='150' rowspan='2' colspan='7' style='font-size:4px;'>2. SHIP FROM<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getSiteAac())).append("<br>");
    sb.append(escapeHtml(this.getSupplyCenter())).append("<br>");

    boolean space = false;
    if (!Util.isEmpty(getInfoCity())) {
      sb.append(escapeHtml(this.getInfoCity()));
      space = true;
    }
    if (!Util.isEmpty(getInfoState())) {
      if (space) sb.append(", ");
      sb.append(escapeHtml(this.getInfoState()));
      space = true;
    }
    if (!Util.isEmpty(getInfoZip())) {
      if (space) sb.append("  ");
      sb.append(escapeHtml(this.getInfoZip()));
    }

    sb.append("</font></td>");

    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td rowspan='2' colspan='3' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>DOC<br>DENT</td>");
    sb.append("<td rowspan='2' colspan='3' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>RI<br>FROM</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>M<br>&amp;<br>S</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>&nbsp;</td>");
    sb.append("<td rowspan='2' colspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>UNIT<br>ISS</td>");
    sb.append("<td rowspan='2' colspan='5' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>QUANTITY</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>&nbsp;</td>");

    sb.append("<td rowspan='2' colspan='6' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>SUPPLE-<br>MENTARY<br>ADDRESS</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>S<br>I<br>G</td>");
    sb.append("<td rowspan='2' colspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>F<br>U<br>N<br>D</td>");
    sb.append("<td rowspan='2' colspan='3' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>DIS-<br>TRI-<br>BU-<br>TION</td>");
    sb.append("<td rowspan='2' colspan='3' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>PRO-<br>JECT</td>");
    sb.append("<td rowspan='2' colspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>P<br>R<br>I</td>");
    sb.append("<td rowspan='2' colspan='3' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>REQ'D<br>DEL<br>DATE</td>");
    sb.append("<td rowspan='2' colspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>A<br>D<br>V</td>");
    sb.append("<td rowspan='2' colspan='3' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>RI</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>O<br>/<br>P</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>C<br>O<br>N<br>D</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>M<br>G<br>T</td>");
    sb.append("<td rowspan='2' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 0;font-size:4px;'>M<br>C<br>C</td>");

    sb.append("<td valign='top' colspan='7' height='2' style='font-size:4px;'>");
    sb.append("UNIT PRICE");
    sb.append("</td>");
    sb.append("<td valign='top' colspan='2' rowspan='3' style='font-size:4px;'>");
    sb.append("DOLLARS");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getTotalDollars()));
    sb.append("</font></td>");
    sb.append("<td valign='top' colspan='1' rowspan='3' style='font-size:4px;'>");
    sb.append("CTS");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(PadUtil.padItTrailingZeros(this.getTotalCents(), 2)));
    sb.append("</font></td>");

    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td valign='top' colspan='5' rowspan='2' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 0 1px;font-size:4px;'>");
    sb.append("DOLLARS");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getUnitDollars()));
    sb.append("</font></td>");
    sb.append("<td valign='top' colspan='2' rowspan='2' style='font-size:4px;'>");
    sb.append("CTS");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(PadUtil.padItTrailingZeros(this.getUnitCents(), 2)));
    sb.append("</font></td>");
    sb.append("<td valign='top' colspan='7' rowspan='2' style='font-size:4px;' nowrap='nowrap'>");
    sb.append("4. MARK FOR ");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getAac()));
    sb.append("</font></td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='3' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");

    sb.append(escapeHtml(padString(getDocumentId(), 3)));
    sb.append("</td>");
    sb.append("<td colspan='3' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px;font-weight:bold;' nowrap='nowrap'>");

    val req = padString(getRequisitionFrom(), 3);
    sb.append(escapeHtml(req));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getMediaAndStatus(), 1)));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;'></td>");
    sb.append("<td colspan='2' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    val ui = padString(getUnitOfIssue(), 1);
    sb.append(escapeHtml(ui));
    sb.append("</td>");
    sb.append("<td colspan='5' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    // pad it
    String qty = padString(getQuantity(), 1);
    sb.append(escapeHtml(PadUtil.padItZeros(qty, 5)));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;'></td>");

    sb.append("<td colspan='6' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getSupplementAddress(), 4)));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;' nowrap='nowrap'>");
    sb.append(escapeHtml(padString(getSignal(), 1)));
    sb.append("</td>");
    sb.append("<td colspan='2' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getFundCode(), 1)));
    sb.append("</td>");
    sb.append("<td colspan='3' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getDistribution(), 1)));
    sb.append("</td>");
    sb.append("<td colspan='3' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getProjectCode(), 1)));
    sb.append("</td>");
    sb.append("<td colspan='2' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    // pad it
    String priority = padString(getPriorityCode(), 1);
    if (!" ".equals(priority))
      priority = PadUtil.padItZeros(priority, 2);
    sb.append(escapeHtml(priority));
    sb.append("</td>");
    sb.append("<td colspan='3' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getReqDeldate(), 1)));
    sb.append("</td>");
    sb.append("<td colspan='2' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getAdvice(), 1)));
    sb.append("</td>");
    sb.append("<td colspan='3' style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;' nowrap='nowrap'>");
    sb.append(escapeHtml(this.getRoutingIdentifier()));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;' nowrap='nowrap'>");
    sb.append(escapeHtml(padString(getOp(), 1)));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;' >");
    val cc = padString(getConditionCode(), 1);
    sb.append(escapeHtml(cc));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 0 1px 0;font-size:10px; font-weight:bold;'>");
    sb.append(escapeHtml(padString(getMgt(), 1)));
    sb.append("</td>");
    sb.append("<td style='border-style:solid;border-color:rgb(0,0,0);border-width:0 1px 1px 0;font-size:10px;font-weight:bold;' nowrap='nowrap'>");
    sb.append(escapeHtml(padString(getMcc(), 1)));
    sb.append("</td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='45' rowspan='7' valign='top'>");

    sb.append("<table border='0' style='border-style:none;border-color:rgb(255,255,255);'><tr><td style='border-style:none;border-color:rgb(255,255,255);'>");
    sb.append("<img src='").append(urlContextPath).append("/resources/images/1348/suffix.gif' height='60'>&nbsp;&nbsp;");
    String documentNumberQueryParam = PadUtil.padItTrailingSpaces(this.getDocumentNumber(), 15);
    sb.append("<img src='").append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(Util.encodeUTF8(documentNumberQueryParam)).append("&amp;bt=BAR39&amp;random=1'>");
    sb.append("</td>");
    sb.append("<td rowspan='2' width='20%' style='border-color:rgb(255,255,255);'>");
    sb.append("<b>");
    sb.append("STRATIS");
    sb.append("</b>");
    sb.append("<br>");
    sb.append("<font style='font-size:7px;'>");
    sb.append("PIN: <b>");
    sb.append(escapeHtml(this.getPin()));
    sb.append("</b>");
    sb.append("<br><br>");
    sb.append(escapeHtml(this.getEquipDescription()));
    sb.append("<br>");
    sb.append(escapeHtml(this.getEquipName()));
    sb.append("<br><br>");
    sb.append("PRINT DATE/TIME:");
    sb.append("<br>");
    sb.append(escapeHtml(this.getPrintDate(dateService, offsetDateTime)));

    sb.append("<br></font></td>");
    sb.append("</tr><tr>");
    sb.append("<td style='border-color:rgb(255,255,255);'>");
    sb.append("<img src='").append(urlContextPath).append("/resources/images/1348/national.gif' height='50' width='40'>&nbsp;&nbsp;");
    String barcodeQueryParam = PadUtil.padItTrailingSpaces(this.getFsc() + this.getNiin(), 15);
    sb.append("<img src='").append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(Util.encodeUTF8(barcodeQueryParam)).append("&amp;bt=BAR39&amp;random=2'>");
    sb.append("</td></tr></table>");
    sb.append("</td>");
    sb.append("</tr>");

    //* right side of 1348
    sb.append("<tr>");
    sb.append("<td valign='top' colspan='7' height='15' style='font-size:4px;'>");
    sb.append("5. DOC DATE");
    sb.append("<br>");
    sb.append("<font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getDocDate()));
    sb.append("</font>");
    sb.append("</td>");

    sb.append("<td colspan='4' valign='top' height='15' style='font-size:4px;'>");
    sb.append("6. NMFC");
    sb.append("<br>");
    sb.append("<font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getNmfc()));
    sb.append("</font>");
    sb.append("</td>");

    sb.append("<td colspan='2' valign='top' height='15' style='font-size:4px;'>");
    sb.append("7. FRT RATE");
    sb.append("<br>");
    sb.append("<font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getFrtRate()));
    sb.append("</font>");
    sb.append("</td>");

    sb.append("<td colspan='3' valign='top' height='15' style='font-size:4px;'>");
    sb.append("8. TYPE CARGO");
    sb.append("<br>");
    sb.append("<font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getTypeCargo()));
    sb.append("</font>");
    sb.append("</td>");

    sb.append("<td colspan='1' valign='top' height='15' style='font-size:4px;'>");
    sb.append("9. PS");
    sb.append("<br>");
    sb.append("<font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getPs()));
    sb.append("</font>");
    sb.append("</td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='7' valign='top' height='15' style='font-size:4px;'>");
    sb.append("10. QTY-RECD");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getQtyReceived()));
    sb.append("</font></td>");
    sb.append("<td colspan='1' valign='top' height='15' style='font-size:4px;'>");
    sb.append("11. UP");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getUp()));
    sb.append("</font></td>");
    sb.append("<td colspan='4' height='15' valign='top' style='font-size:4px;'>");
    sb.append("12. UNIT WEIGHT");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getUnitWeight()));
    sb.append("</font></td>");
    sb.append("<td colspan='3' height='15' valign='top' style='font-size:4px;'>");
    sb.append("13. UNIT CUBE");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getUnitCubed()));
    sb.append("</font></td>");
    sb.append("<td colspan='1' height='15' valign='top' style='font-size:4px;'>");
    sb.append("14. UFC");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getUfc()));
    sb.append("</font></td>");
    sb.append("<td valign='top' colspan='1' height='15' style='font-size:4px;' nowrap='nowrap'>");
    sb.append("15. SL");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getSl()));
    sb.append("</font></td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='17' height='15' valign='top' style='font-size:4px;'>");
    sb.append("16. FREIGHTCLASSIFICATION NOMENCLATURE");
    sb.append("<br>");
    sb.append("<font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getFrieghtNomen()));
    sb.append("</font>");
    sb.append("</td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='17' height='15' valign='top' style='font-size:4px;'>");
    sb.append("17. ITEM NOMENCLATURE");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getItemNomen()));
    sb.append("</font></td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='7' height='15' valign='top' style='font-size:4px;'>");
    sb.append("18. TY CONT");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getTypeCount()));
    sb.append("</font></td>");

    sb.append("<td colspan='3' height='15' valign='top' style='font-size:4px;'>");
    sb.append("19. NO CONT");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getNumCount()));
    sb.append("</font></td>");

    sb.append("<td colspan='4' height='15' valign='top' style='font-size:4px;'>");
    sb.append("20. TOTAL WEIGHT");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getTotalWeight()));
    sb.append("</font></td>");

    sb.append("<td colspan='3' height='15' valign='top' style='font-size:4px;'>");
    sb.append("21. TOTAL CUBE");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getTotalCube()));
    sb.append("</font></td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='10' height='15' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 1px 1px;font-size:4px;'>");
    sb.append("22. RECEIVED BY");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getReceivedBy()));
    sb.append("</font></td>");

    sb.append("<td colspan='4' height='15' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 1px 1px;font-size:4px;'>");

    sb.append("23. ERO");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getEro()));
    sb.append("</font></td>");
    sb.append("<td colspan='3' height='15' valign='top' style='border-style:solid;border-color:rgb(0,0,0);border-width:1px 1px 1px 1px;font-size:4px;'>");
    sb.append("24. DATE RECEIVED");
    sb.append("<br><font style='font-size:8px;font-weight:bold;'>");
    sb.append(escapeHtml(this.getDateReceived()));
    sb.append("</font></td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='45' height='40' style='border-color:rgb(255,255,255);'>");
    sb.append("<img src='").append(urlContextPath).append("/resources/images/1348/ric.gif' height='50' width='70'>&nbsp;&nbsp;");
    sb.append(escapeHtml(padString(getFundCode(), 1)));
    String escapedBarcodeQueryParam = PadUtil.padIt(req, 3) + PadUtil.padIt(ui, 2) + PadUtil.padItZeros(qty, 5) + PadUtil.padIt(cc, 1) + PadUtil.padIt("", 2) + PadUtil.padItZeros(this.getUnitPrice().replace(".", ""), 7);
    sb.append("<img src=\"").append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(Util.encodeUTF8(escapedBarcodeQueryParam)).append("&amp;bt=BAR39\"/>");
    sb.append("</td>");

    // added to move ship to down

    sb.append("<td colspan='17' rowspan='2' width='250' valign='top' style='border-color:rgb(255,255,255);'>");
    sb.append("<font style='font-size:4px;'>3.  SHIP TO</font><br>");
    sb.append("<font style='font-weight:bolder;font-family:courier;'>").append(escapeHtml(this.getShipTo())).append("</font><br>");
    sb.append("<font style='font-size:10px;font-family:courier;'>");
    sb.append(escapeHtml(this.getAddress1())).append("<br>");
    sb.append(escapeHtml(this.getAddress2())).append("<br>");

    boolean space2 = false;
    if (!Util.isEmpty(getCity())) {
      sb.append(escapeHtml(this.getCity()));
      space2 = true;
    }
    if (!Util.isEmpty(getState())) {
      if (space2) sb.append(", ");
      sb.append(escapeHtml(this.getState()));
      space2 = true;
    }
    if (!Util.isEmpty(getZip())) {
      if (space2) sb.append("  ");
      sb.append(escapeHtml(this.getZip()));
    }

    if (!this.getSerialNumbers().equals("")) {
      sb.append("<br>" + "Serial Numbers: ");
      if (!serialOverflow) {
        sb.append(escapeHtml(this.getSerialNumbers()));
      }
      else {
        sb.append(" See Serial Number Addendum");
      }
    }

    sb.append("</font>");
    sb.append("</td>");
    sb.append("</tr>");

    sb.append("<tr>");
    sb.append("<td colspan='62' height='90' style='border-color:rgb(255,255,255);font-size:10px'>");
    sb.append("<img src='").append(urlContextPath).append("/resources/images/1348/add.gif' height='55' width='35'>");
    sb.append("&nbsp;&nbsp;");

    String temp22 = this.getHeaderCompliance() + FORMAT_HEADER_RS;
    temp22 = temp22 + "06" + FORMAT_HEADER_GS;  //Record seperator
    temp22 = temp22 + "12S" + PadUtil.padItTrailingSpaces(this.getDocumentNumber(), 15) + FORMAT_HEADER_GS;
    temp22 = temp22 + "N" + PadUtil.padIt(this.getFsc(), 4) + PadUtil.padIt(this.getNiin(), 9) + PadUtil.padIt("", 2) + FORMAT_HEADER_GS;
    temp22 = temp22 + "7Q" + PadUtil.padItLeadingZeros(qty, 5) + this.getUnitOfIssue() + FORMAT_HEADER_GS;
    temp22 = temp22 + "7V" + PadUtil.padIt("", 3) + FORMAT_HEADER_GS;
    temp22 = temp22 + "V" + PadUtil.padIt(this.getRoutingIdentifier(), 3) + FORMAT_HEADER_GS;
    temp22 = temp22 + "2R" + PadUtil.padIt(this.getConditionCode(), 1) + FORMAT_HEADER_GS;
    temp22 = temp22 + "12Q" + getUnitDollars() + "." + PadUtil.padItTrailingZeros(this.getUnitCents(), 2) + "USD" + FORMAT_HEADER_GS;
    temp22 = temp22 + "5P" + PadUtil.padIt(this.getNmfccn(), 6) + FORMAT_HEADER_GS;
    temp22 = temp22 + "25S" + PadUtil.padIt("", 78) + FORMAT_HEADER_GS;
    temp22 = temp22 + "S" + PadUtil.padIt("", 30) + FORMAT_HEADER_GS;
    temp22 = temp22 + "1T" + PadUtil.padIt("", 25) + FORMAT_HEADER_GS;
    temp22 = temp22 + "17V" + PadUtil.padIt("", 5) + FORMAT_HEADER_GS;
    temp22 = temp22 + "1P" + PadUtil.padIt("", 16) + FORMAT_HEADER_GS;
    temp22 = temp22 + "07" + FORMAT_HEADER_GS; //Record seperator
    temp22 = temp22 + "03" + PadUtil.padItTrailingSpaces(this.getProjectCode(), 3) + FORMAT_HEADER_GS;
    temp22 = temp22 + "B6" + PadUtil.padItTrailingSpaces(this.getDistribution(), 3) + FORMAT_HEADER_GS;
    temp22 = temp22 + "27" + PadUtil.padItTrailingSpaces(this.getAac(), 6) + FORMAT_HEADER_GS;
    temp22 = temp22 + "38" + PadUtil.padItTrailingSpaces(this.getItemNomen(), 20) + FORMAT_HEADER_GS;
    temp22 = temp22 + "32" + PadUtil.padItTrailingSpaces(this.getReqDeldate(), 3) + FORMAT_HEADER_GS;
    temp22 = temp22 + "B7" + PadUtil.padItZeros(this.getPriorityCode(), 2) + FORMAT_HEADER_GS;
    temp22 = temp22 + "B8" + PadUtil.padIt(this.getPs(), 1) + FORMAT_HEADER_GS;  //Partial Shipment Indicator
    temp22 = temp22 + "81" + PadUtil.padItTrailingSpaces(this.getSupplementAddress(), 6);
    temp22 = temp22 + FORMAT_HEADER_RS + FORMAT_HEADER_EOT;

    String slotImgSrcUrl = urlContextPath + "/SlotImage?type=BARCODE&bc=" + Util.encodeUTF8(temp22) + "&bt=PDF417&random=4";

    if (JSFUtils.getFacesContext() != null) {
      JSFUtils.setManagedBeanValue("userbean.barcodeText", temp22);
    }
    sb.append("<img src='").append(slotImgSrcUrl).append("'>");

    sb.append("</td>");
    sb.append("</tr>");
    sb.append("</table>");

    return sb.toString();
  }

  private String padString(String val, int numSpaces) {
    if (val != null) {
      if (val.equals("") || val.equals("null"))
        return StringUtils.leftPad(val, numSpaces, " ");
    }
    else {
      String newString = "";
      return StringUtils.leftPad(newString, numSpaces, " ");
    }
    return val;
  }

  /****************************************************************
   * Description: This function is responsible for generating the script
   * to launch the serial page addendum
   * *************************************************************/
  public String launchSerial() {
    if (serialOverflow) {
      return "window.open('Serial1348.jspx?quantity=" + Util.encodeUTF8(getQuantity()) + "&scn=" + Util.encodeUTF8(theScn) + "');";
    }
    else
      return "";
  }

  /****************************************************************
   * Description: This function is responsible for gathering the
   * information for the 1348 form based on the SCN provided by
   * the user.  The information will then retrived from the Issue,
   * picking, site_info, customer, and niin_info tables.
   * *************************************************************/
  public String gatherFormInfo(String scn, String quantity, HttpServletRequest servletRequest) {
    InventoryModuleImpl service = super.getInventoryAMService();
    String temp;
    int temp2;

    String julianDate = getCurrentJulian(4);
    setDocDate(julianDate);

    service.getFormInfo(scn, quantity);
    theScn = scn;
    setDocumentId(service.getDocIdent());

    setRequisitionFrom(service.getRiFrom());
    setRoutingIdentifier(service.getRoutingId());
    setSupplementAddress(service.getSupAddress());
    setMediaAndStatus(service.getMediaStatus());
    setDistribution(service.getDistribution());
    setProjectCode(service.getProject());
    setPriorityCode(service.getPriority());
    setAac(service.getAac());
    setSiteAac(service.getSiteAac());

    setAdvice(service.getAdvice());

    setConditionCode(service.getCc());
    setName(service.getAddressName());

    setUnitOfIssue(service.getUi());
    setQuantity(quantity);

    setSignal(service.getSignal());
    setFundCode(service.getFund());
    setPriorityCode(service.getPriority());
    setAdvice(service.getAdvice());
    setUnitDollars(service.getuDollars());
    setUnitCents(service.getuCents());
    setTotalDollars(service.gettDollars());
    setTotalCents(service.gettCents());
    setUnitWeight(service.getUnitWeight().toString());
    setUnitCubed(service.getUnitCube().toString());
    setTotalWeight(service.gettWeight().toString());
    setTotalCube(service.gettCube().toString());

    //5/26/2015: MCF: Fixed issue where nomenclature with apostrophe was creating a gigantic barcode
    String tempNomen = service.getInomenclature();
    if (tempNomen != null) {
      tempNomen = tempNomen.replaceAll("[^a-zA-Z0-9]", "");
    }
    setItemNomen(tempNomen);

    this.setPin(service.getPin());
    this.setSerialNumbers(service.getSerialNumbers());

    //checks if the serial numbers are over the max (this will print an additional serial number addendum.
    //Serial Numbers are separated by commas
    int count = this.getSerialNumbers().length() - this.getSerialNumbers().replace(",", "").length();
    if (count > MAX_SERIALS) {
      serialOverflow = true;
    }

    temp = (JSFUtils.getManagedBeanValue("userbean.workstationId")).toString();
    temp2 = Integer.parseInt(temp);

    service.getWorkstationInfo(temp2);
    this.setMarkFor(service.getAddressName());

    setEquipName(service.getEquipName());
    setEquipDescription(service.getEquipDescription());
    this.setInfoCity(service.getiCity());
    this.setInfoState(service.getiState());
    this.setInfoZip(service.getiZip());
    this.setSupplyCenter(service.getSupplyCenter());
    this.setReqDeldate(service.getReqDelDate());
    this.setEro(service.getEro());
    this.setDocumentNumber(service.getDocumentNumber());
    this.setNiin(service.getNiin());
    this.setFsc(service.getFsc());
    this.setUnitPrice(service.getUnitPrice());
    this.setTrueQty(service.getTrueQty());
    this.setPin(service.getPin());

    if (this.signal.equals("A") || this.signal.equals("B") || this.signal.equals("C") || this.signal.equals("D") || this.signal.equals("W"))
      this.setShipTo(getDocumentNumber().substring(0, 6));
    else {
      if (!Util.isEmpty(supplementAddress)) this.setShipTo(this.supplementAddress);
      else this.setShipTo(getDocumentNumber().substring(0, 6));
    }

    service.getAddressByAac(getShipTo());
    setAddress1(service.getAddress1());
    setAddress2(service.getAddress2());
    this.setState(service.getState());
    this.setCity(service.getCity());
    setZip(service.getZip());

    return "";
  }

  public void setDocumentId(String documentId) {
    this.documentId = documentId;
  }

  public String getDocumentId() {
    return (documentId == null ? "" : documentId);
  }

  public void setRequisitionFrom(String requisitionFrom) {
    this.requisitionFrom = requisitionFrom;
  }

  public String getRequisitionFrom() {
    return (requisitionFrom == null ? "" : requisitionFrom);
  }

  public void setMediaAndStatus(String mediaAndStatus) {
    this.mediaAndStatus = mediaAndStatus;
  }

  public String getMediaAndStatus() {
    return (mediaAndStatus == null ? "" : mediaAndStatus);
  }

  public void setUnitOfIssue(String unitOfIssue) {
    this.unitOfIssue = unitOfIssue;
  }

  public String getUnitOfIssue() {
    return (unitOfIssue == null ? "" : unitOfIssue);
  }

  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  public String getQuantity() {
    return (quantity == null ? "" : quantity);
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public String getServiceCode() {
    return (serviceCode == null ? "" : serviceCode);
  }

  public void setSupplementAddress(String supplementAddress) {
    this.supplementAddress = supplementAddress;
  }

  public String getSupplementAddress() {
    return (supplementAddress == null ? "" : supplementAddress);
  }

  public void setSignal(String signal) {
    this.signal = signal;
  }

  public String getSignal() {
    return (signal == null ? "" : signal);
  }

  public void setFundCode(String fundCode) {
    this.fundCode = fundCode;
  }

  public String getFundCode() {
    return (fundCode == null ? "" : fundCode);
  }

  public void setDistribution(String distribution) {
    this.distribution = distribution;
  }

  public String getDistribution() {
    return (distribution == null ? "" : distribution);
  }

  public void setPriorityCode(String priorityCode) {
    this.priorityCode = priorityCode;
  }

  public String getPriorityCode() {
    return (priorityCode == null ? "" : priorityCode);
  }

  public void setReqDeletionDate(String reqDeletionDate) {
    this.reqDeletionDate = reqDeletionDate;
  }

  public String getReqDeletionDate() {
    return (reqDeletionDate == null ? "" : reqDeletionDate);
  }

  public void setAdvice(String advice) {
    this.advice = advice;
  }

  public String getAdvice() {
    return (advice == null ? "" : advice);
  }

  public void setRoutingIdentifier(String routingIdentifier) {
    this.routingIdentifier = routingIdentifier;
  }

  public String getRoutingIdentifier() {
    return (routingIdentifier == null ? "" : routingIdentifier);
  }

  public void setOp(String oP) {
    this.op = oP;
  }

  public String getOp() {
    return (op == null ? "" : op);
  }

  public void setConditionCode(String conditionCode) {
    this.conditionCode = conditionCode;
  }

  public String getConditionCode() {
    return (conditionCode == null ? "" : conditionCode);
  }

  public void setMgt(String mgt) {
    this.mgt = mgt;
  }

  public String getMgt() {
    return (mgt == null ? "" : mgt);
  }

  public void setUnitDollars(String unitDollars) {
    this.unitDollars = unitDollars;
  }

  public String getUnitDollars() {
    return (unitDollars == null ? "" : unitDollars);
  }

  public void setUnitCents(String unitCents) {
    this.unitCents = unitCents;
  }

  public String getUnitCents() {
    return (unitCents == null ? "" : unitCents);
  }

  public void setTotalDollars(String totalDollars) {
    this.totalDollars = totalDollars;
  }

  public String getTotalDollars() {
    return (totalDollars == null ? "" : totalDollars);
  }

  public void setTotalCents(String totalCents) {
    this.totalCents = totalCents;
  }

  public String getTotalCents() {
    return (totalCents == null ? "" : totalCents);
  }

  public void setShipFrom(String shipFrom) {
    this.shipFrom = shipFrom;
  }

  public String getShipFrom() {
    return (shipFrom == null ? "" : shipFrom);
  }

  public void setShipTo(String shipTo) {
    this.shipTo = shipTo;
  }

  public String getShipTo() {
    return (shipTo == null ? "" : shipTo);
  }

  public void setMarkFor(String markFor) {
    this.markFor = markFor;
  }

  public String getMarkFor() {
    return (markFor == null ? "" : markFor);
  }

  public void setDocDate(String docDate) {
    this.docDate = docDate;
  }

  public String getDocDate() {
    return (docDate == null ? "" : docDate);
  }

  public void setNmfc(String nmfc) {
    this.nmfc = nmfc;
  }

  public String getNmfc() {
    return (nmfc == null ? "" : nmfc);
  }

  public void setFrtRate(String frtRate) {
    this.frtRate = frtRate;
  }

  public String getFrtRate() {
    return (frtRate == null ? "" : frtRate);
  }

  public void setTypeCargo(String typeCargo) {
    this.typeCargo = typeCargo;
  }

  public String getTypeCargo() {
    return (typeCargo == null ? "" : typeCargo);
  }

  public void setPs(String ps) {
    this.ps = ps;
  }

  public String getPs() {
    return (ps == null ? "" : ps);
  }

  public void setSl(String sl) {
    this.sl = sl;
  }

  public String getSl() {
    return (sl == null ? "" : sl);
  }

  public void setFrieghtNomen(String frieghtNomen) {
    this.frieghtNomen = frieghtNomen;
  }

  public String getFrieghtNomen() {
    return (frieghtNomen == null ? "" : frieghtNomen);
  }

  public void setItemNomen(String itemNomen) {
    this.itemNomen = itemNomen;
  }

  public String getItemNomen() {
    return (itemNomen == null ? "" : itemNomen);
  }

  public void setTypeCount(String typeCount) {
    this.typeCount = typeCount;
  }

  public String getTypeCount() {
    return (typeCount == null ? "" : typeCount);
  }

  public void setNumCount(String numCount) {
    this.numCount = numCount;
  }

  public String getNumCount() {
    return (numCount == null ? "" : numCount);
  }

  public void setTotalWeight(String totalWeight) {
    this.totalWeight = totalWeight;
  }

  public String getTotalWeight() {
    return (totalWeight == null ? "" : totalWeight);
  }

  public void setTotalCube(String totalCube) {
    this.totalCube = totalCube;
  }

  public String getTotalCube() {
    return (totalCube == null ? "" : totalCube);
  }

  public void setSerialNumbers(String serial) {
    this.serialNumbers = serial;
  }

  public String getSerialNumbers() {
    return (serialNumbers == null ? "" : serialNumbers);
  }

  public void setReceivedBy(String receivedBy) {
    this.receivedBy = receivedBy;
  }

  public String getReceivedBy() {
    return (receivedBy == null ? "" : receivedBy);
  }

  public void setDateReceived(String dateReceived) {
    this.dateReceived = dateReceived;
  }

  public String getDateReceived() {
    return (dateReceived == null ? "" : dateReceived);
  }

  public void setUp(String up) {
    this.up = up;
  }

  public String getUp() {
    return (up == null ? "" : up);
  }

  public void setQtyReceived(String qtyReceived) {
    this.qtyReceived = qtyReceived;
  }

  public String getQtyReceived() {
    return (qtyReceived == null ? "" : qtyReceived);
  }

  public void setUnitWeight(String unitWeight) {
    this.unitWeight = unitWeight;
  }

  public String getUnitWeight() {
    return (unitWeight == null ? "" : unitWeight);
  }

  public void setUnitCubed(String unitCubed) {
    this.unitCubed = unitCubed;
  }

  public String getUnitCubed() {
    return (unitCubed == null ? "" : unitCubed);
  }

  public void setProjectCode(String projectCode) {
    this.projectCode = projectCode;
  }

  public String getProjectCode() {
    return (projectCode == null ? "" : projectCode);
  }

  public void setMcc(String mcc) {
    this.mcc = mcc;
  }

  public String getMcc() {
    return (mcc == null ? "" : mcc);
  }

  public void setUfc(String ufc) {
    this.ufc = ufc;
  }

  public String getUfc() {
    return (ufc == null ? "" : ufc);
  }

  public void setAac(String aac) {
    this.aac = aac;
  }

  public String getAac() {
    return (aac == null ? "" : aac);
  }

  public void setSiteAac(String aac) {
    this.siteAac = aac;
  }

  public String getSiteAac() {
    return (siteAac == null ? "" : siteAac);
  }

  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  public String getAddress1() {
    return (address1 == null ? "" : address1);
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
  }

  public String getAddress2() {
    return (address2 == null ? "" : address2);
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return (name == null ? "" : name);
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCity() {
    return (city == null ? "" : city);
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getState() {
    return (state == null ? "" : state);
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public String getZip() {
    return (zip == null ? "" : zip);
  }

  public void setEquipName(String equipName) {
    this.equipName = equipName;
  }

  public String getEquipName() {
    return (equipName == null ? "" : equipName);
  }

  public void setEquipDescription(String equipDescription) {
    this.equipDescription = equipDescription;
  }

  public String getEquipDescription() {
    return (equipDescription == null ? "" : equipDescription);
  }

  public String getPrintDate(final DateService dateService, final OffsetDateTime offsetDateTime) {
    return dateService.shiftAndFormatDate(offsetDateTime, DateConstants.BARCODE_1348_DATE_FORMAT);
  }

  public void setPin(String pin) {
    this.pin = pin;
  }

  public String getPin() {
    return (pin == null ? "" : pin);
  }

  public void setSupplyCenter(String supplyCenter) {
    this.supplyCenter = supplyCenter;
  }

  public String getSupplyCenter() {
    return (supplyCenter == null ? "" : supplyCenter);
  }

  public void setInfoCity(String infoCity) {
    this.infoCity = infoCity;
  }

  public String getInfoCity() {
    return (infoCity == null ? "" : infoCity);
  }

  public void setInfoState(String infoState) {
    this.infoState = infoState;
  }

  public String getInfoState() {
    return (infoState == null ? "" : infoState);
  }

  public void setInfoZip(String infoZip) {
    this.infoZip = infoZip;
  }

  public String getInfoZip() {
    return (infoZip == null ? "" : infoZip);
  }

  public void setEro(String ero) {
    this.ero = ero;
  }

  public String getEro() {
    return (ero == null ? "" : ero);
  }

  public void setReqDeldate(String reqDeldate) {
    this.reqDeldate = reqDeldate;
  }

  public String getReqDeldate() {
    return (reqDeldate == null ? "" : reqDeldate);
  }

  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber;
  }

  public String getDocumentNumber() {
    return (documentNumber == null ? "" : documentNumber);
  }

  public void setNiin(String niin) {
    this.niin = niin;
  }

  public String getNiin() {
    return (niin == null ? "" : niin);
  }

  public void setFsc(String fsc) {
    this.fsc = fsc;
  }

  public String getFsc() {
    return (fsc == null ? "" : fsc);
  }

  public void setUnitPrice(String unitPrice) {
    this.unitPrice = unitPrice;
  }

  public String getUnitPrice() {
    return (unitPrice == null ? "" : unitPrice);
  }

  public String getHeaderCompliance() {
    return (headerCompliance == null ? "" : headerCompliance);
  }

  public void setPartNumber(String partNumber) {
    this.partNumber = partNumber;
  }

  public String getPartNumber() {
    return (partNumber == null ? "" : partNumber);
  }

  public String getFormatHeader() {
    return (formatHeader == null ? "" : formatHeader);
  }

  public void setTrueQty(String trueQty) {
    this.trueQty = trueQty;
  }

  public String getTrueQty() {
    return (trueQty == null ? "" : trueQty);
  }

  public void setNmfccn(String nMFCCN) {
    this.nmfccn = nMFCCN;
  }

  public String getNmfccn() {
    return (nmfccn == null ? "    " : nmfccn);
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getSerialNumber() {
    return (serialNumber == null ? "" : serialNumber);
  }

  public void setTwoDGenerator(String twoDGenerator) {
    this.twoDGenerator = twoDGenerator;
  }

  public String getTwoDGenerator() {
    return (twoDGenerator == null ? "" : twoDGenerator);
  }

  public String getCurrentJulian(int numChars) {
    String retVal = "";
    try {

      Calendar cal = Calendar.getInstance();
      SimpleDateFormat simpleDF = new SimpleDateFormat("yyyyDDD");
      String formattedDate = simpleDF.format(cal.getTime());

      if (numChars == 5) retVal += formattedDate.charAt(2);
      if (numChars == 4 || numChars == 5) retVal += formattedDate.charAt(3);

      if (numChars >= 3 && numChars <= 5) {
        retVal += formattedDate.charAt(4);
        retVal += formattedDate.charAt(5);
        retVal += formattedDate.charAt(6);
        return retVal;
      }
      return "   ";
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    retVal = "   ";
    return retVal;
  }
}

package mil.stratis.view.print;

import mil.stratis.common.util.Util;
import mil.stratis.view.shipping.ShippingHandler;
import mil.stratis.view.util.JSFUtils;

import java.util.Locale;

/**
 * This is a backing bean for Floor Location Label.jspx.
 */
public class LabelBacking extends ShippingHandler {

  /**
   * Constructor.
   */
  public LabelBacking() {
    super();
  }

  /**
   * Configure the URI to use for printing purposes, but extracting
   * the protocol, server name, port and context path out the
   * current URL.
   *
   * @param protocol    - examples, https
   * @param serverName  - the name of the host web server, examples, someserver.domain.com
   * @param port        - the number used to send data across at the network communications level
   * @param contextPath - the path to the web server content
   */
  public void configureURI(final String protocol, final String serverName, final int port,
                           final String contextPath) {
    JSFUtils.setManagedBeanValue("userbean.siteProtocol", protocol);
    JSFUtils.setManagedBeanValue("userbean.siteServerName", serverName);
    JSFUtils.setManagedBeanValue("userbean.siteServerPort", port);
    JSFUtils.setManagedBeanValue("userbean.siteContextPath", contextPath);
  }

  /**
   * Print a floor location label in HTML format.
   *
   * @param floor - 5-digit shipping floor location
   * @param aac   - 6-digit customer Activity Address Code
   * @param area  - shipping routes, examples, Palm
   * @return HTML formatted String
   */
  public String printFloorLocationLabelHTML(final String floor, final String aac, final String area) {
    return printFloorLocationLabelHTML(floor, aac, area, false, getStratisUrlContextPath());
  }

  /**
   * Print a floor location label in HTML format.
   *
   * @param floor          - 5-digit shipping floor location
   * @param aac            - 6-digit customer Activity Address Code
   * @param area           - shipping routes, examples, Palm
   * @param fullyQualified - use to specify path to the rbarcode generator
   * @return HTML formatted String
   */
  public String printFloorLocationLabelHTML(final String floor, final String aac, final String area, final boolean fullyQualified, final String urlContextPath) {
    final String uri = getURI();
    final String colspan2 = "<tr><td colspan='2'>&nbsp;</td></tr>";

    StringBuffer html = new StringBuffer(1380);
    html.append("<html><head><style>");
    html.append("table {font-size:4px}");
    html.append(".floorBarcode {font-size:16px; font-weight:bold}");
    html.append(".floorLine {font-size:16px; font-weight:bold}");
    html.append(".aacBarcode {font-size:16px;font-weight:bold}");
    html.append(".aacLine {font-size:16px;font-weight:bold}");
    html.append(".areaLine {font-size:32px}");
    html.append("</style></head><body onload='javascript:window.print()'>");

    html.append("<table height='100%' width='100%' cellpadding='2' cellspacing='0'>");
    html.append(colspan2);

    html.append("<tr>");
    html.append("<td class='floorLine' align='left'><h1>FLOOR LOCATION</h1></td>");
    html.append("<td class='floorBarcode' align='center' valign='top'><img height='200' width='300' src='");
    if (fullyQualified)
      html.append(uri);
    html.append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(Util.encodeUTF8(floor.toUpperCase(Locale.US))).append("&amp;bt=BAR39'></td>");
    html.append("</tr>");
    html.append(colspan2);

    html.append("<tr>");
    html.append("<td class='aacLine' align='left'><h1>AAC</h1></td>");
    html.append("<td class='aacBarcode' align='center' valign='top'><img height='200' width='300' src='");
    if (fullyQualified)
      html.append(uri);
    html.append(urlContextPath).append("/SlotImage?type=BARCODE&amp;bc=").append(Util.encodeUTF8(aac.toUpperCase(Locale.US))).append("&amp;bt=BAR39&amp;showTitle=true&amp;width=1000&amp;height=500'></td>");
    html.append("</tr>");
    html.append(colspan2);
    html.append(colspan2);

    html.append("<tr>");
    html.append("<td class='areaLine' colspan='2' align='center'><font size='20'>").append(Util.encodeUTF8(area.toUpperCase(Locale.US))).append("</font></td>");
    html.append("</tr>");

    html.append("</table></body></html>");
    return html.toString();
  }
}

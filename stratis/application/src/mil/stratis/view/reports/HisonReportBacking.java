package mil.stratis.view.reports;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.view.admin.AdminBackingHandler;
import mil.stratis.view.reports.xao.GcssHisonReportXAO;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

@Slf4j
@NoArgsConstructor
public class HisonReportBacking extends AdminBackingHandler {

  private transient RichInputText hisonNiin;

  /**
   * This method is called when the hison report is invoked.
   */
  public void submitHison(@SuppressWarnings("all") ActionEvent event) {
    log.debug("HisonReportBacking::submitHison()");

    if (Util.isEmpty(hisonNiin.getValue())) {
      JSFUtils.addFacesErrorMessage("REQUIRED FIELD MISSING", "NIIN or Document Number is required.");
      return;
    }

    String inputValue = Util.trimUpperCaseClean(hisonNiin.getValue());

    // NIINs are always 9 characters and Document numbers are always 14 characters.

    if (inputValue.length() != 9 && inputValue.length() != 14) {

      JSFUtils.addFacesErrorMessage("REQUIRED FIELD INVALID LENGTH", "The NIIN or Document Number entered could not be found. Please enter a valid NIIN or Document Number.");
      return;
    }

    StringBuilder reportBuf = new StringBuilder();
    reportBuf.append("<table border='0' cellpadding='1' cellspacing='0' ");
    reportBuf.append("style='font-size:10px;");
    reportBuf.append("font-family: Courier;border-collapse:collapse;'>");

    GcssHisonReportXAO gcssReport = new GcssHisonReportXAO();

    if ((inputValue.length() == 14)) {
      reportBuf.append(gcssReport.buildDocumentReport(inputValue));
    }
    else {
      reportBuf.append(gcssReport.buildByNiinReport(inputValue));
    }

    reportBuf.append("</table>");
    ADFContext.getCurrent().getSessionScope().put("hisonResults", reportBuf.toString());
    JSFUtils.setManagedBeanValue("userbean.hison", "");
    String url = JSFUtils.getManagedBeanValue("userbean.winPrintURL").toString();
    createPopUpWindows(url);
  }

  public String getHisonReportString() {
    val hison = ADFContext.getCurrent().getSessionScope().get("hisonResults");
    return hison == null ? "" : hison.toString();
  }

  public void setHisonNiin(RichInputText hisonNiin) {
    this.hisonNiin = hisonNiin;
  }

  public RichInputText getHisonNiin() {
    return hisonNiin;
  }

  /**
   * This method creates a pop up window for a given URL
   */
  void createPopUpWindows(String url) {
    FacesContext fctx = FacesContext.getCurrentInstance();
    ExtendedRenderKitService erks = Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
    erks.addScript(fctx, "window.open(\"" + url + "\");");
  }
}

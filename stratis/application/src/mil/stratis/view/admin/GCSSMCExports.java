package mil.stratis.view.admin;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.GCSSMCTransactionsImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.service.gcss.I136NiinService;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.modules.mhif.application.model.ItemMasterProcessResult;
import oracle.adf.view.rich.component.rich.input.RichInputText;
import oracle.adf.view.rich.component.rich.input.RichSelectOneChoice;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

@Slf4j
@NoArgsConstructor
public class GCSSMCExports extends MdssBackingBean {

  private transient RichInputText mhifNiin;

  private String niinValue = "";

  int selectedType = 0;
  private transient RichSelectOneChoice exportType;

  protected enum EXPORT_TYPE {
    MHIF_REQUEST(0),
    MHIF_IMMEDIATE(1);
    private int value;

    EXPORT_TYPE(int v) { value = v; }

    protected int value() { return value; }
  }

  private transient GCSSMCTransactionsImpl gCSSMCService = null;
  
  public void onPageLoad() {
    if (!this.isPostback()) {
      init();
    }
  }

  private void init() {
    niinValue = "";
  }

  public void setMhifNiin(RichInputText mhifNiin) {
    this.mhifNiin = mhifNiin;
  }

  public RichInputText getMhifNiin() {
    return mhifNiin;
  }

  public void setExportType(RichSelectOneChoice exportType) {
    this.exportType = exportType;
  }

  public RichSelectOneChoice getExportType() {
    return exportType;
  }

  public void selectedTypeValueChangeListener(ValueChangeEvent valueChangeEvent) {
    setSelectedType(Integer.parseInt(valueChangeEvent.getNewValue().toString()));
  }

  public void setSelectedType(int value) {
    selectedType = value;
    togglePanelItems(value);
  }

  public int getSelectedType() {
    return selectedType;
  }

  private void togglePanelItems(int value) {
    if (value == EXPORT_TYPE.MHIF_REQUEST.value()) {
      mhifNiin.setDisabled(true);
    }
    else {
      mhifNiin.setDisabled(false);
    }
  }

  public void periodInputValidator(FacesContext facesContext,
                                   UIComponent uiComponent, Object object) {
    if (facesContext != null) {}
    if (uiComponent != null) {}

    try {
      boolean f = false;
      if (object != null) {
        if (!this.isNumericAA(object.toString())) {
          JSFUtils.addFacesErrorMessage("INVALID_INTERVAL", "Please enter valid interval for automatic scheduling.");
          f = true;
        }
        else if ((!f) && (Integer.parseInt(object.toString()) < 1)) {
          JSFUtils.addFacesErrorMessage("INVALID_INTERVAL", "Re-run interval should be at least 1 minutes.");
          f = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private static boolean isNumericAA(String s) {
    return s.matches("[0-9]+");
  }

  public void activateManualExport() {
    try {
      val i136NiinService = ContextUtils.getBean(I136NiinService.class);
      if (!i136NiinService.isWsDisabled()) {
        //I136 - MHIF
        if (selectedType == EXPORT_TYPE.MHIF_REQUEST.value()) {
          val result = i136NiinService.processBatchI136Niins();
          processResultMessage(result);
        }
        else if (selectedType == EXPORT_TYPE.MHIF_IMMEDIATE.value()) {

          // If the component is disable you can not fetch froom it.
          // That is why we are doing this here.
          if (mhifNiin != null) {
            if (mhifNiin.getValue() != null) {
              niinValue = mhifNiin.getValue().toString();
            }
            else {
              niinValue = "";
            }
          }

          if (niinValue.length() != 9) {
            JSFUtils.addFacesErrorMessage("NIIN", "NIIN must be 9 characters in length.");
            return;
          }

          val result = i136NiinService.processI136Niin(niinValue);
          processResultMessage(result);
        }
      }
      else {
        JSFUtils.addFacesErrorMessage("Web services are disabled for this instance.");
      }
    }
    catch (Exception e) {
      log.error("MHIF Request Failed", e);
      JSFUtils.addFacesErrorMessage("MHIF Import failed.");
    }
  }

  private void processResultMessage(ItemMasterProcessResult result) {
    if (result.status().isSuccess()) {
      JSFUtils.addFacesInformationMessage(String.format("MHIF Import result: %s [Total NIINs: %s NIINs Processed: %s NIINs Skipped: %s NIINs Errored: %s]", result.status().getLabel(), result.totalNiins(), result.niinsProcessed(), result.niinsSkipped(), result.niinsErrored()));
    }
    else {
      JSFUtils.addFacesErrorMessage(String.format("MHIF Import Error result: %s [Total NIINs: %s]", result.status().getLabel(), result.totalNiins()));
    }
  }
}

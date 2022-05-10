package mil.stratis.view.session;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.JNDIUtils;
import mil.stratis.jai.ImageUtils;
import mil.stratis.model.services.AppModuleImpl;
import mil.stratis.view.BackingHandler;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ShuttlePageBackingBeanBase is a managed session bean used
 * to provide "state" to those pages which require.  It also is
 * home to functions used primarily for javascript pulldown menus.
 */
@Slf4j
public class ShuttlePageBackingBeanBase extends BackingHandler {

  //* Variables used for javascript pulldown lists

  private List<SelectItem> binFileList = new ArrayList<>();
  private List warehouseWACList = new ArrayList();
  private List warehouseWACListMech = new ArrayList();
  private List warehouseMechListOnly = new ArrayList();

  //* Variables used to maintain state in web pages
  transient Object updateState = 0;
  transient Object screenState = 0;
  boolean disableFlag = false;

  public void setBinFileList(List binFileList) {
    this.binFileList = binFileList;
  }

  /**
   * This function builds the list of all available images for storage bins
   */
  public List getBinFileList() {
    // clear the list before using it
    binFileList.clear();
    String imageDir = "";

    try {
      imageDir = JNDIUtils.getInstance().getProperty("ImagePath");
      imageDir = FacesContext.getCurrentInstance().getExternalContext().getRealPath(imageDir).toString().trim();
      if (imageDir == null) {
        imageDir = "";
      }
    }
    catch (Throwable ex) {
      log.error("Error in {}", ex.getStackTrace()[0].getMethodName(), ex);
    }

    // get the directory list
    File dir = new File(imageDir);
    if (dir != null) {
      if (dir.exists()) {
        String[] children = dir.list();

        if (children != null) {

          // add all the files
          for (int i = 0; i < children.length; i++) {
            // check for the thumb file
            // add it to the list
            String picExtension = ".jpg", picExtensionUp = ".JPG";
            if ((children[i].endsWith(picExtension)) ||
                (children[i].endsWith(picExtensionUp))) {
              binFileList.add(new SelectItem(children[i],
                  children[i]));
            }
          }
        }
        else {
          binFileList.add(new SelectItem("no children found",
              "no children found"));
        }
      }
      else {
        binFileList.add(new SelectItem("no local images",
            "no local images"));
      }
    }
    else {
      // say directory not found
      binFileList.add(new SelectItem("dir not found", "dir not found"));
    }

    // look for error
    if (binFileList.size() < 1) {
      binFileList.add(new SelectItem("no images", "no images"));
    }

    return binFileList;
  }

  /**
   * This function is used by the web to create a html &lt;select&gt; list via javascript.
   * Returns all location classification items.
   *
   * @return String
   */
  public String GetLocationClassificationList() {

    //* build string of javascript syntax
    String rstring = "<script>";

    AppModuleImpl service =
        (AppModuleImpl) getStratisRootService().getAppModule1();

    List idList = new ArrayList(),
        fnList = new ArrayList();

    service.returnAllLocationClassifications(idList, fnList);

    //* fill the return string
    int idListSize = idList.size();
    int fnListSize = fnList.size();

    rstring += "var LocCount=" + idListSize + ";";
    rstring += "idarray = new Array(" + idListSize + ");";
    rstring += "fnarray = new Array(" + fnListSize + ");";

    //* fill the arrays
    for (int i = 0; i < idListSize; i++)
      rstring += "idarray[" + i + "] = \"" + idList.get(i) + "\";";
    for (int i = 0; i < fnListSize; i++)
      rstring += "fnarray[" + i + "] = \"" + fnList.get(i) + "\";";

    rstring += "function findfn(idname)";
    rstring += "{";
    rstring += "var result = \"\";";
    rstring += "for(var i = 0; i < LocCount; i++)";
    rstring += "{";
    rstring += "if(idname == idarray[i])";
    rstring += "{";
    rstring += "result = fnarray[i];";
    rstring += "i = LocCount;";
    rstring += "}";
    rstring += "}";
    rstring += " return result;";
    rstring += "}";

    rstring += "</script>";
    return rstring;
  }

  /**
   * This function is used by the web to create a html &lt;select&gt; list via javascript.
   * Returns all divider type items.
   * NOTE - This could be moved to WarehouseSetupBacking
   *
   * @return String
   */
  public String GetDividerTypeList() {
    //* build string of javascript syntax
    String rstring = "<script>";

    AppModuleImpl Service =
        (AppModuleImpl) getStratisRootService().getAppModule1();

    List idList = new ArrayList(),
        fnList = new ArrayList();

    Service.getDivideTypes(idList, fnList);

    //* fill the return string
    int idListSize = idList.size();

    rstring += "var TypeCount=" + idListSize + ";";
    rstring += "idtarray = new Array(" + idListSize + ");";
    rstring += "fntarray = new Array(" + idListSize + ");";

    //* fill the arrays
    for (int i = 0; i < idListSize; i++) {
      rstring += "idtarray[" + i + "] = \"" + idList.get(i) + "\";";
      rstring += "fntarray[" + i + "] = \"" + fnList.get(i) + "\";";
    }

    rstring += "function findtype(idtname)";
    rstring += "{";
    rstring += "var result = \"\";";
    rstring += "for (var i = 0; i < TypeCount; i++)";
    rstring += "{";
    rstring += "if(idtname == fntarray[i])";
    rstring += "{";
    rstring += "result = idtarray[i];";
    rstring += "i = TypeCount;";
    rstring += "}";
    rstring += "}";
    rstring += " return result;";
    rstring += "}";

    rstring += "</script>";

    return rstring;
  }

  public void setWarehouseWACList(List warehouseWACList) {
    this.warehouseWACList = warehouseWACList;
  }

  /**
   * This function is used by the MECH SETUP web to create an ADF SelectOneChoice.
   * Returns all warehouse wac list items.
   *
   * @return List
   */
  public List getWarehouseWACList() {
    // clear the list before sending it out
    warehouseWACList.clear();

    // call the service to fill the lists
    List IdList = new ArrayList(), FNList = new ArrayList();

    AppModuleImpl service =
        (AppModuleImpl) getStratisRootService().getAppModule1();
    service.returnBuildingWACList(IdList, FNList, false);

    // add all the info
    for (int i = 0; i < IdList.size(); i++) {
      warehouseWACList.add(new SelectItem(IdList.get(i),
          FNList.get(i).toString()));
    }

    return warehouseWACList;
  }

  public void setWarehouseMechListOnly(List warehouseMechListOnly) {
    this.warehouseMechListOnly = warehouseMechListOnly;
  }

  /**
   * This function is used by the MECH SETUP web to create an ADF SelectOneChoice.
   * Returns all warehouse wac list items (mechanized only).
   *
   * @return List
   */
  public List getWarehouseMechListOnly() {
    // clear the list before sending it out
    warehouseMechListOnly.clear();

    // call the service to fill the lists
    List IdList = new ArrayList(), FNList = new ArrayList();

    AppModuleImpl service =
        (AppModuleImpl) getStratisRootService().getAppModule1();
    service.returnBuildingWACList(IdList, FNList, true);
    // add all the info - for horizontal
    for (int i = 0; i < IdList.size(); i++) {
      warehouseMechListOnly.add(new SelectItem(IdList.get(i),
          FNList.get(i).toString()));
    }
    return warehouseMechListOnly;
  }

  public List getWarehouseWACListMech_Horizontal() {
    return getWarehouseWACListMech(true);
  }

  public List getWarehouseWACListMech_Vertical() {
    return getWarehouseWACListMech(false);
  }

  /**
   * This function is used by the NONMECH SETUP web to create an ADF SelectOneChoice.
   * Returns all warehouse wac list items.
   *
   * @return List
   */
  private List getWarehouseWACListMech(boolean isHorizontal) {
    // clear the list before sending it out
    warehouseWACListMech.clear();

    // call the service to fill the lists
    List IdList = new ArrayList(), FNList = new ArrayList();

    AppModuleImpl service =
        (AppModuleImpl) getStratisRootService().getAppModule1();
    service.returnBuildingWACMechList(IdList, FNList, isHorizontal);

    // add all the info
    for (int i = 0; i < IdList.size(); i++) {
      warehouseWACListMech.add(new SelectItem(IdList.get(i),
          FNList.get(i).toString()));
    }
    return warehouseWACListMech;
  }

  /**
   * This function is used by the web to display divider type to user
   * based upon the divider type index given.
   * Returns nothing.  Saves an output image (.png file) into directory.
   */
  public void GetDividerImage(final OutputStream os, int index,
                              int selectindex) {
    List XList = new ArrayList(), YList = new ArrayList(), XLength =
        new ArrayList(), YLength = new ArrayList(), IndexList =
        new ArrayList(), DisplayList = new ArrayList();

    // get the lists of boxes from the service
    AppModuleImpl service =
        (AppModuleImpl) getStratisRootService().getAppModule1();

    service.returnDividerTypeBoxes(XList, YList, XLength, YLength,
        IndexList, index, DisplayList);

    ImageUtils im = new ImageUtils();

    try {
      im.createSlotImage(XList, YList, XLength, YLength, IndexList, os, selectindex, DisplayList);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void setScreenState(Object screenState) {
    this.screenState = screenState;
  }

  public Object getScreenState() {
    return screenState;
  }

  public void updateState(ActionEvent actionEvent) {
    if (actionEvent != null) {
      //NO-OP can't remove parameter due to adf
    }
    setUpdateState(0);
    setDisableFlag(false);
  }

  public void createState(ActionEvent actionEvent) {
    if (actionEvent != null) {
      //NO-OP can't remove parameter due to adf
    }
    setUpdateState(1);
    setDisableFlag(true);
  }

  public void setUpdateState(Object updateState) {
    this.updateState = updateState;
  }

  public Object getUpdateState() {
    return updateState;
  }

  public void setDisableFlag(boolean disableFlag) {
    this.disableFlag = disableFlag;
  }

  public boolean isDisableFlag() {
    return disableFlag;
  }
}


package mil.stratis.view.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.jai.BarcodeCreator;
import mil.stratis.jai.ImageUtils;
import mil.stratis.model.services.AppModuleImpl;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCDataControl;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SlotImage extends HttpServlet {

  private static final SecureRandom generator = new SecureRandom();

  public SlotImage() {
    super();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    String userSiteSelected = (String) request.getSession().getAttribute(StratisConfig.USER_DB_SELECTED);
    String siteName = (String) request.getSession().getAttribute(StratisConfig.USER_SITE_NAME);
    UserSiteSelectionTracker.configureTracker(userSiteSelected, siteName);
    LoginUtils.LoginStatus status = LoginUtils.checkLogedIn(request.getSession());
    boolean loggedIn = LoginUtils.checkAndLogStatus(status);

    if (loggedIn) {
      synchronized (this) {
        if (request.getParameter("type") != null) {
          if (request.getParameter("type").equals("SLOT")) {
            processSlotImage(request, response);
          }
          else if (request.getParameter("type").equals("BARCODE")) {
            processBarcode(request, response);
          }
        }
      }
    }
    else {
      log.warn("User is not logged in!  Will not process slotimage request.");
    }
  }

  public void processBarcode(HttpServletRequest request, HttpServletResponse response) {
    try {
      java.io.OutputStream o = response.getOutputStream();  //Removed here.  Outputstream will be created in backend.

      String barcodeType = request.getParameter("bt");
      String barcodeText = request.getParameter("bc");

      int random = generator.nextInt(100);
      String strRandom = request.getParameter("random");
      if (strRandom != null && !strRandom.equals(""))
        random += Integer.parseInt(strRandom);

      int height = 0;
      String strHeight = request.getParameter("height");
      if (strHeight != null && !strHeight.equals(""))
        height += Integer.parseInt(strHeight);

      int width = 0;
      String strWidth = request.getParameter("width");
      if (strWidth != null && !strWidth.equals(""))
        width += Integer.parseInt(strWidth);

      BarcodeCreator barcodecall = new BarcodeCreator();
      barcodecall.createBarcodeImage(barcodeText, barcodeType, o, random, width, height);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void processSlotImage(HttpServletRequest request, HttpServletResponse response) {
    AppModuleImpl appModuleImplService = null;

    try {
      int divideIndex = 0;
      int selectIndex = 0;
      String diIn = request.getParameter("DI");
      String siIn = request.getParameter("SI");

      // @ZSL add validation
      if (diIn != null && !diIn.equals("") && diIn.matches("[0-9]+")) {
        divideIndex = Integer.parseInt(diIn);
      }

      if (siIn != null && !siIn.equals("") && siIn.matches("[0-9]+")) {
        selectIndex = Integer.parseInt(siIn);
      }

      List<String> xList = new ArrayList<>();
      List<String> yList = new ArrayList<>();
      List<String> xLength = new ArrayList<>();
      List<String> yLength = new ArrayList<>();
      List<String> indexList = new ArrayList<>();
      List<String> displayList = new ArrayList<>();

      //=============== Hyun start ================================

      //pulling amDef and config from bc4j.xcfg

      val binder = BindingContext.getCurrent();
      DCDataControl dc = binder.findDataControl("AppModuleDataControl");
      appModuleImplService = (AppModuleImpl) dc.getDataProvider();

      if (appModuleImplService != null) {
        appModuleImplService.returnDividerTypeBoxes(xList, yList, xLength, yLength, indexList, divideIndex, displayList);
      }
      else {
        log.warn("appModuleImplService is NULL.");
      }

      ImageUtils im = new ImageUtils();

      response.setContentType("image/png; charset=utf8");
      OutputStream os = response.getOutputStream();

      im.createSlotImage(xList, yList, xLength, yLength, indexList, os, selectIndex, displayList);
      // get parameter from request
      os.flush();
      os.close();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }
}

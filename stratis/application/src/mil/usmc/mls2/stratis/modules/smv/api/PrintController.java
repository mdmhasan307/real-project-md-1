package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.processor.PickingProcessor;
import mil.usmc.mls2.stratis.core.service.*;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping(value = MappingConstants.PRINT)
@RequiredArgsConstructor
public class PrintController {
  private final GlobalConstants globalConstants;
  private final StowService stowService;
  private final PickingService pickingService;
  private final PickingProcessor pickingProcessor;
  private final SiteInfoService siteInfoService;
  private final UserService userService;
  private final IssueService issueService;
  private final NiinInfoService niinInfoService;
  private final LocationService locationService;
  private static final String DEFAULT_PAGE = "mobile/print";
  private static final String CONTEXT_PATH = "urlContextPath";

  @GetMapping
  public String show(HttpServletRequest request) {
    request.setAttribute("javascriptPrintingEnabled", globalConstants.isJavascriptPrintingEnabledBySystem());
    return DEFAULT_PAGE;
  }

  @GetMapping(value = "/print-label")
  public String printLabel(HttpServletRequest request) {

    val userinfo = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
    if (userinfo == null) {
      log.error("user info is null for print-label");
      return exceptionPage("Session does not exist", request);
    }
    String page = "";
    try {
      switch (userinfo.getHtmlPrintType()) {
        case STOW_LABEL:
          page = stowLabel(userinfo.getHtmlPrintData(), request);
          break;
        case PICK_LABEL:
          page = pickLabel(userinfo.getHtmlPrintData(), request);
          break;
        case NIIN_LABEL:
          page = nsnLabel(userinfo.getHtmlPrintData(), request);
          break;
        case LOCATION_LABEL:
          page = locationLabel(userinfo.getHtmlPrintData(), request);
          break;
        case NONE:
        default:
          log.error(String.format("User %s accessed print-label with no label type", userinfo.getUsername()));
          page = exceptionPage("No label type selected", request);
          break;
      }

      return page;
    }
    finally {
      //set userprint to false
      userinfo.setUseHTMLPrint(false);
      userinfo.setHtmlPrintData(null);
      userinfo.setHtmlPrintType(LabelType.NONE);
    }
  }

  private String stowLabel(String[] sidList, HttpServletRequest request)  {
    if (sidList == null || sidList.length == 0 || sidList[0].isEmpty()) {
      log.error("Sid is required for the label");
      return exceptionPage("Stow is required for the label", request);
    }

    List<StowLabelData> labels = new ArrayList<>();
    for (String sid: sidList) {
      // retrieve sid
      val criteria = StowSearchCriteria.builder().sid(sid).build();
      val stows = stowService.search(criteria);
      if (stows.isEmpty()) {
        log.error(String.format("No stow found for sid %s", sid));
        return exceptionPage("Stow does not exist", request);
      }

      val city = siteInfoService.getRecord().getCity();
      //add info from sid to
      val stow = stows.get(0);
      val niinInfo = stow.getReceipt().getNiinInfo();
      val receipt = stow.getReceipt();
      var user = "";
      if (stow.getModifiedBy() != null) {
        user = userService.findById(stow.getModifiedBy().toString()).get().getUsername();
      }
      String mid = stow.getLocation().getMechanizedFlag();
      mid = mid == null ? "" : mid;
      if (!mid.equalsIgnoreCase("N")) {
        mid = "MECH " + "(" + mid.trim() + ")";
      } else {
        mid = "NON MECH";
      }

      StowLabelData label = StowLabelData.builder()
          .mechStation(mid)
          .city(city)
          .location(stow.getLocation().getLocationLabel())
          .fscniin(niinInfo.getFsc() + niinInfo.getNiin())
          .ui(niinInfo.getUi())
          .cc(receipt.getCc())
          .docno(receipt.getDocumentNumber())
          .rcn(Integer.toString(receipt.getRcn()))
          .sid(stow.getSid())
          .sidQty(Integer.toString(stow.getQtyToBeStowed()))
          .exp(stow.getExpirationDate().toString())
          .lot(stow.getLotConNum())
          .caseQty(stow.getCaseWeightQty() != null ? Integer.toString(stow.getCaseWeightQty()) : "0")
          .serialNum(stow.getSerialNumber())
          .username(user)
          .datetime(DateTime.now().toDateTime().toString("yyyyMMdd/HHmm"))
          .nomenclature(niinInfo.getNomenclature())
          .wacNumber(stow.getLocation().getWac().getWacNumber()).build();
      labels.add(label);
    }
    request.setAttribute("stowLabels", labels);
    request.setAttribute(CONTEXT_PATH, request.getContextPath());

    return "global/stow-label";
  }

  private String pickLabel(String[] pinList, HttpServletRequest request) {
   try {
      if (pinList == null || pinList.length == 0 || pinList[0].isEmpty()) {
        log.error("Pin is required for the picking label");
        return exceptionPage("Pick is required for the picking label", request);
      }

      List<PickLabelData> labels = new ArrayList<>();
      for (String pin: pinList) {
        // retrieve pick
        val criteria = PickingSearchCriteria.builder().pin(pin).build();
        val picks = pickingService.search(criteria);
        if (picks.isEmpty()) {
          log.error(String.format("No pick found for pin %s", pin));
          return exceptionPage("Pick does not exist", request);
        }

        val city = siteInfoService.getRecord().getCity();

        //add info from sid to
        val pick = picks.get(0);
        val niinInfo = pick.getNiinLocation().getNiinInfo();
        var user = "";
        if (pick.getModifiedBy() != null) {
          user = userService.findById(pick.getModifiedBy().toString()).get().getUsername();
        }
        String mid = pick.getNiinLocation().getLocation().getMechanizedFlag();
        mid = mid == null ? "" : mid;
        if (!mid.equalsIgnoreCase("N")) {
          mid = "MECH " + "(" + mid.trim() + ")";
        }
        else {
          mid = "NON MECH";
        }
        IssueSearchCriteria issueSearchCriteria = IssueSearchCriteria.builder().scn(pick.getScn()).build();
        val issues = issueService.search(issueSearchCriteria);
        if (issues.isEmpty()) {
          log.error(String.format("Issue does not exist for pin %s", pin));
          return exceptionPage("Issue does not exist for pick", request);
        }
        val issue = issues.get(0);

        PickLabelData label = PickLabelData.builder()
            .mechStation(mid)
            .city(city)
            .fscniin(niinInfo.getFsc() + niinInfo.getNiin())
            .cc(pick.getNiinLocation().getCc())
            .docno(issue.getDocumentNumber())
            .packingStation(pickingProcessor.getPackingStationName(pick))
            .scn(pick.getScn())
            .pin(pick.getPin())
            .pinQty(Integer.toString(pick.getQtyPicked()))
            .username(user)
            .datetime(DateTime.now().toDateTime().toString("yyyyMMdd/HHmm"))
            .nomenclature(niinInfo.getNomenclature()).build();
        labels.add(label);
      }
      request.setAttribute("pickLabels", labels);
      request.setAttribute(CONTEXT_PATH, request.getContextPath());
    }
    catch (Exception e) {
      log.error(String.format("Exception for pick label %s", e.getMessage()));
      return exceptionPage("An error occurred while creating label", request);
    }
    return "global/pick-label";
  }

  private String nsnLabel(String[]  niins, HttpServletRequest request) {
    List<BarcodeLabelData> labels = new ArrayList<>();
    for (String niin : niins) {
      String fsc = "";
      if (niin == null || niin.isEmpty()) {
        log.error("NIIN is required for label");
        return exceptionPage("NIIN is required for the label", request);
      }

      if (niin.length() != 13 && niin.length() != 9) {
        log.error(String.format("NIIN %s was the incorrect length", niin));
        return exceptionPage("NIIN must be 13 or 9 characters", request);
      }

      if (niin.length() == 13) {
        fsc = niin.substring(0, 4);
        niin = niin.substring(4);
      }

      val niinInfo = niinInfoService.findByNiin(niin);
      if (!niinInfo.isPresent()) {
        log.error(String.format("Could not find NIIN %s", niin));
        return exceptionPage("NIIN not found", request);
      }

      if (!fsc.isEmpty() && !niinInfo.get().getFsc().equalsIgnoreCase(fsc)) {
        log.error(String.format("FSC %s is not valid for NIIN %s", fsc, niin));
        return exceptionPage("Invalid fsc", request);
      }
      BarcodeLabelData label = BarcodeLabelData.builder()
          .title(fsc.isEmpty() ? "NIIN" : "FSC/NIIN")
          .barcode(fsc + niin).build();
      labels.add(label);
    }
    request.setAttribute("barcodes", labels);
    request.setAttribute(CONTEXT_PATH, request.getContextPath());

    return "global/barcode-label";
  }

  private String locationLabel(String[]  locations, HttpServletRequest request) {
    List<BarcodeLabelData> labels = new ArrayList<>();
    for (String location : locations) {
      if (location == null || location.isEmpty() || location.length() != 9) {
        log.error(String.format("location %s is not valid", location));
        return exceptionPage("A 9 character location is required for the label", request);
      }

      LocationSearchCriteria criteria = LocationSearchCriteria.builder().locationLabel(location).build();
      val locationObj = locationService.search(criteria);
      if (locationObj.isEmpty()) {
        log.error(String.format("Location %s not found", location));
        return exceptionPage("Location not found", request);
      }
      BarcodeLabelData label = BarcodeLabelData.builder()
          .title("LOCATION").barcode(locationObj.get(0).getLocationLabel())
          .build();
      labels.add(label);
    }
    request.setAttribute("barcodes", labels);
    request.setAttribute(CONTEXT_PATH, request.getContextPath());

    return "global/barcode-label";
  }

  private String exceptionPage(String message, HttpServletRequest request) {
    request.setAttribute("errorMessage", message);
    return "global/label-exception";
  }
}

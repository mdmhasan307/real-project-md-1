package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.SpaGetResponse;
import mil.usmc.mls2.stratis.core.domain.model.SpaResponse;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.TEST_PRINT)
@RequiredArgsConstructor
public class TestPrintController {
  //TODO: remove this controller and page

  private static final String DEFAULT_PAGE = "mobile/testprint";

  private final EquipmentService equipmentService;
  private final GlobalConstants globalConstants;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      UserInfo user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());

      //Following string is ZPL (Zebra Print Language) to print a sample label
      user.setupForPrinting(equipmentService.findById(user.getWorkstationId()), "^XA^PRC^LH0,0^FS^MD20^MNW^LH0,0^FS^CWI,510S8_S2.FNT^FS^FO60,160^AIN,31,0^CI0^FR^FDSTRATIS MECH (H)^FS^FO60,192^AIN,31,0^CI0^FR^FDISSUE: CAMP LEJEUNE^FS^FO60,224^AIN,31,0^CI0^FR^FDPACKING STATION: MCPX01PODS^FS^FO60,256^AIN,31,0^CI0^FR^FDFSC/NIIN: 5310009038595^FS^FO60,288^AIN,31,0^CI0^FR^FDCC: A^FS^FO60,320^AIN,31,0^CI0^FR^FDDOC NO: MMC10005292092^FS^FO60,352^AIN,31,0^CI0^FR^FDSCN: W0150000G^FS^FO60,384^AIN,31,0^CI0^FR^FDPIN: W0150000G PIN QTY: 10^FS^FO60,416^AIN,31,0^CI0^FR^FDUSER NAME: JDOE^FS^FO60,448^AIN,31,0^CI0^FR^FDDATE/TIME: 20200529/0339^FS^FO60,480^AIN,31,0^CI0^FR^FDDESC: stratis^FS^FO180,640^B3N,N,200,Y,N^FD^FDW0150000G^FS^PQ1,0,0,N^XZ");

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Test Print");
    }
    catch (Exception e) {
      log.error("Error occurred processing Test Print", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }
}

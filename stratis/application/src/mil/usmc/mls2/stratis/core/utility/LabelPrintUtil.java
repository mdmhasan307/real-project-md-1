package mil.usmc.mls2.stratis.core.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.model.LabelType;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LabelPrintUtil {

  private final GlobalConstants globalConstants;
  private final EquipmentService equipmentService;

  public void printLabel(UserInfo user, String zebraZPL, String[] data, LabelType labelType) {
    if (globalConstants.isAppletPrint()) {
        user.setupForPrinting(equipmentService.findById(user.getWorkstationId()) , zebraZPL);
    }
    else {
      user.setUseHTMLPrint(true);
      user.setHtmlPrintData(data);
      user.setHtmlPrintType(labelType);
    }
  }
}

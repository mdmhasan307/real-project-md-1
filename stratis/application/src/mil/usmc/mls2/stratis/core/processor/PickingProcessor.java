package mil.usmc.mls2.stratis.core.processor;

import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.*;

public interface PickingProcessor {

  ProcessorMessages processPick(Picking pick, PickingDetailInput pickingDetailInput, Issue issue, Integer userId, boolean partialPick);

  void processPickBypass(Picking pick, PickingDetailInput pickingDetailInput, Integer userId);

  void processPickRefuse(Picking pick, PickingDetailInput pickingDetailInput, Integer userId);

  void processForNext(SpaPostResponse spaPostResponse, UserInfo user);

  String getPackingStationName(Picking picking) throws Exception;
}

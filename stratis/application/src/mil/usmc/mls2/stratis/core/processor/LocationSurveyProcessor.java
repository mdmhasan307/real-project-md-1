package mil.usmc.mls2.stratis.core.processor;

import mil.usmc.mls2.stratis.core.domain.model.InventoryItem;
import mil.usmc.mls2.stratis.core.domain.model.SpaPostResponse;

import javax.servlet.http.HttpServletRequest;

public interface LocationSurveyProcessor {

  void failSurvey(Integer locationId, Integer userId);

  SpaPostResponse bypassSurvey(Integer locationId, Integer userId, HttpServletRequest request);

  SpaPostResponse processForNext(InventoryItem inventroyItem, Integer niinId, Integer userId, HttpServletRequest request, SpaPostResponse response);
}

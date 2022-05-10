package mil.usmc.mls2.stratis.core.service;

import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Note that no context-specific properties are supplied to the services
 * These services are client-agnostic, with no knowledge or access to HttpServletRequest, Response, JSFContext, etc.)
 */
public interface EquipmentService {

  void switchWorkstation(UserInfo userInfo, long userRights);

  long getNewUserRights(int userId, int userTypeId);

  Optional<Equipment> findById(Integer id);

  List<Equipment> getAll();

  Long count(EquipmentSearchCriteria criteria);

  List<Equipment> search(EquipmentSearchCriteria criteria);

  Equipment setWorkstation(Integer userId, Integer workstationId);

  void checkAndSetNonMech(String mechflag, Long modifyrights, UserInfo userInfo);
}

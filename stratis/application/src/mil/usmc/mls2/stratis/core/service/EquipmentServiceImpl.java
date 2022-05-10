package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.datatype.user.UserRights;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.event.UserSwitchedWorkstationEvent;
import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.repository.EquipmentRepository;
import mil.usmc.mls2.stratis.core.infrastructure.dao.UserDao;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"Duplicates", "squid:S1192"})
class EquipmentServiceImpl implements EquipmentService {

  private final EquipmentRepository equipmentRepository;
  private final UserDao userDao;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public long getNewUserRights(int userId, int userTypeId) {
    return userDao.getGroupPrivsForUser(userId, userTypeId);
  }

  @Override
  @Transactional
  public void switchWorkstation(UserInfo userInfo, long userRights) {

    try {
      int userId = userInfo.getUserId();
      int workstationId = userInfo.getWorkstationId();

      val newWorkstation = setWorkstation(userId, workstationId);

      userInfo.setComPrintIndex("2");
      userInfo.setComCommandIndex("1");

      userInfo.setWorkstationType(newWorkstation.getDescription());
      userInfo.setWorkstationName(newWorkstation.getName());

      long modifyrights = userRights;

      userInfo.setUserRights(new UserRights(modifyrights, userInfo.getWorkstationType()));

      // check if this is a non mech workstation
      String mechanizedFlag = null;
      if (newWorkstation.getWac() != null) {
        mechanizedFlag = newWorkstation.getWac().getMechanizedFlag();
      }

      // flip the bits over
      modifyrights = ~modifyrights;

      checkAndSetNonMech(mechanizedFlag, modifyrights, userInfo);

      val event = UserSwitchedWorkstationEvent.builder()
          .userInfo(userInfo)
          .build();

      eventPublisher.publishEvent(event);
    }
    catch (Exception e) {
      log.error("Error Switching Workstation", e);
    }
  }

  @Override
  @Transactional
  public Equipment setWorkstation(Integer userId, Integer workstationId) {
    //find the workstation the user is currently switching from and update to unlink the user.
    equipmentRepository.findByCurrentUserId(userId).ifPresent(x -> {
      x.setCurrentUserId(null);
      equipmentRepository.save(x);
    });

    // find the new workstation user is switching to and set the current user
    val newWorkstation = equipmentRepository.findById(workstationId).orElseThrow(() -> new StratisRuntimeException(String.format("No Workstation found matching id %s", workstationId)));
    newWorkstation.setCurrentUserId(userId);
    equipmentRepository.save(newWorkstation);

    //return the new workstation switched to.
    return newWorkstation;
  }

  @Override
  public void checkAndSetNonMech(String mechflag, Long modifyrights, UserInfo userInfo) {
    if (mechflag == null || mechflag.equals("-1")) // no wac
    {

      // drop the stow, pick and inventory rights out
      modifyrights |= UserRights.STRATIS_STOW_RIGHTS;
      modifyrights |= UserRights.STRATIS_PICK_RIGHTS;
      modifyrights |= UserRights.STRATIS_INV_RIGHTS;

      // flip the bits back over
      modifyrights = ~modifyrights;

      // put the rights back in
      userInfo.setUserRights(new UserRights(modifyrights, userInfo.getWorkstationType()));

      // say we have no workstation for this workstation
      userInfo.setNonmech(2);
    }
    else // mech wac
    {
      // say we have a proper wac
      userInfo.setNonmech(1);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Equipment> findById(Integer id) {
    return equipmentRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Equipment> getAll() {
    return equipmentRepository.getAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Long count(EquipmentSearchCriteria criteria) {
    return equipmentRepository.count(criteria);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Equipment> search(EquipmentSearchCriteria criteria) {
    return equipmentRepository.search(criteria);
  }
}

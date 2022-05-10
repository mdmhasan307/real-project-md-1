package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.NiinLocation;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinLocationEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("Duplicates")
public class NiinLocationMapper {

  private static final NiinInfoMapper NIIN_INFO_MAPPER = NiinInfoMapper.INSTANCE;
  private final LocationMapper locationMapper;

  public NiinLocationMapper(@Lazy LocationMapper locationMapper) {
    this.locationMapper = locationMapper;
  }

  public NiinLocation map(NiinLocationEntity input) {
    if (input == null) return null;

    return NiinLocation.builder()
        .caseWeightQty(input.getCaseWeightQty())
        .cc(input.getCc())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .dateOfManufacture(input.getDateOfManufacture())
        .expirationDate(input.getExpirationDate())
        .expRemark(input.getExpRemark())
        .lastInvDate(input.getLastInvDate())
        .location(locationMapper.map(input.getLocation()))
        .locked(input.getLocked())
        .lotConNum(input.getLotConNum())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .niinInfo(NIIN_INFO_MAPPER.entityToModel(input.getNiinInfo()))
        .niinLocationId(input.getNiinLocationId())
        .nsnRemark(input.getNsnRemark())
        .numCounts(input.getNumCounts())
        .numExtents(input.getNumExtents())
        .oldQty(input.getOldQty())
        .oldUi(input.getOldUi())
        .packedDate(input.getPackedDate())
        .pc(input.getPc())
        .projectCode(input.getProjectCode())
        .qty(input.getQty())
        .recallDate(input.getRecallDate())
        .recallFlag(input.getRecallFlag())
        .securityMarkClass(input.getSecurityMarkClass())
        .serialNumber(input.getSerialNumber())
        .underAudit(input.getUnderAudit())
        .build();
  }
}
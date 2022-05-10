package mil.usmc.mls2.stratis.core.domain.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.mapper.NiinInfoMapper;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.NiinInfoEntity;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.NiinInfoEntityRepository;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository.RefSlcEntityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
class NiinInfoRepositoryImpl implements NiinInfoRepository {

  private static final NiinInfoMapper NIIN_INFO_MAPPER = NiinInfoMapper.INSTANCE;

  private final NiinInfoEntityRepository entityRepository;
  private final RefSlcEntityRepository refSlcEntityRepository;

  @Override
  public void save(NiinInfo info) {
    NiinInfoEntity entity;
    if (info.getNiinId() == null) {
      entity = new NiinInfoEntity();
      apply(info, entity);
    }
    else {
      entity = entityRepository.findById(info.getNiinId()).orElseThrow(() -> new IllegalStateException(String.format("Niin not found for ID %s", info.getNiinId())));
      apply(info, entity);
    }

    val result = entityRepository.saveAndFlush(entity);
    NIIN_INFO_MAPPER.entityToModel(result, info); //this maps the updates back into the info object
  }

  @Override
  public Optional<NiinInfo> findById(Integer id) {
    return entityRepository.findById(id).map(NIIN_INFO_MAPPER::entityToModel);
  }

  @Override
  public Optional<NiinInfo> findByNiin(String niin) {
    return entityRepository.findByNiin(niin).map(NIIN_INFO_MAPPER::entityToModel);
  }

  @Override
  public Long count(NiinInfoSearchCriteria criteria) {
    return entityRepository.count(criteria);
  }

  @Override
  public Set<NiinInfo> search(NiinInfoSearchCriteria criteria) {
    val results = entityRepository.search(criteria);
    return NIIN_INFO_MAPPER.entitySetToModelSet(results);
  }

  private void apply(NiinInfo niinInfo, NiinInfoEntity niinInfoEntity) {
    niinInfoEntity.setNiinId(niinInfo.getNiinId());
    niinInfoEntity.setNiin(niinInfo.getNiin());
    niinInfoEntity.setNomenclature(niinInfo.getNomenclature());
    niinInfoEntity.setCube(niinInfo.getCube());
    niinInfoEntity.setPrice(niinInfo.getPrice());
    niinInfoEntity.setActivityDate(niinInfo.getActivityDate());
    niinInfoEntity.setTamcn(niinInfo.getTamcn());
    niinInfoEntity.setSupplyClass(niinInfo.getSupplyClass());
    niinInfoEntity.setTypeOfMaterial(niinInfo.getTypeOfMaterial());
    niinInfoEntity.setCognizanceCode(niinInfo.getCognizanceCode());
    niinInfoEntity.setPartNumber(niinInfo.getPartNumber());
    niinInfoEntity.setUi(niinInfo.getUi());
    niinInfoEntity.setCageCode(niinInfo.getCageCode());
    niinInfoEntity.setFsc(niinInfo.getFsc());
    if (niinInfo.getShelfLifeCode() != null) {
      val refSlcEntity = refSlcEntityRepository.findById(niinInfo.getShelfLifeCode().getRefSlcId()).orElse(null);
      niinInfoEntity.setShelfLifeCode(refSlcEntity);
    }
    else {
      niinInfoEntity.setShelfLifeCode(null);
    }
    niinInfoEntity.setWeight(niinInfo.getWeight());
    niinInfoEntity.setLength(niinInfo.getLength());
    niinInfoEntity.setWidth(niinInfo.getWidth());
    niinInfoEntity.setHeight(niinInfo.getHeight());
    niinInfoEntity.setShelfLifeExtension(niinInfo.getShelfLifeExtension());
    niinInfoEntity.setScc(niinInfo.getScc());
    niinInfoEntity.setInventoryThreshold(niinInfo.getInventoryThreshold());
    niinInfoEntity.setSassyBalance(niinInfo.getSassyBalance());
    niinInfoEntity.setRoThreshold(niinInfo.getRoThreshold());
    niinInfoEntity.setSmic(niinInfo.getSmic());
    niinInfoEntity.setSerialControlFlag(niinInfo.getSerialControlFlag());
    niinInfoEntity.setLotControlFlag(niinInfo.getLotControlFlag());
    niinInfoEntity.setNewNsn(niinInfo.getNewNsn());
    niinInfoEntity.setCreatedBy(niinInfo.getCreatedBy());
    niinInfoEntity.setModifiedBy(niinInfo.getModifiedBy());
    niinInfoEntity.setLastMhifUpdateDate(niinInfo.getLastMhifUpdateDate());
    niinInfoEntity.setDemilCode(niinInfo.getDemilCode());
    niinInfoEntity.setSecurityMarkClass(niinInfo.getSecurityMarkClass());
  }
}
